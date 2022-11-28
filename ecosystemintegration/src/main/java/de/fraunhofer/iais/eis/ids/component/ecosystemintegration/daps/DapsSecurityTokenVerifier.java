package de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.RemoteKeySourceException;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import de.fraunhofer.iais.eis.Token;
import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenVerifier;
import de.fraunhofer.iais.eis.ids.component.core.TokenVerificationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;
import java.util.Map;

public class DapsSecurityTokenVerifier implements SecurityTokenVerifier {

    private boolean ignoreJwtExpiration;
    private final JsonWebKeySetProvider jwksProvider;
    private JWTClaimsVerifier jwtClaimsVerifier;
    private JWTClaimsSet claimsSet;
    Logger logger = LoggerFactory.getLogger(DapsSecurityTokenVerifier.class);

    public DapsSecurityTokenVerifier(JsonWebKeySetProvider jwksProvider)
    {
        this.jwksProvider = jwksProvider;
    }

    /**
     * @deprecated do not provide own claims verifier
     */
    @Deprecated
    public DapsSecurityTokenVerifier(JsonWebKeySetProvider jwksProvider, JWTClaimsVerifier jwtClaimsVerifier) {
        this.jwksProvider = jwksProvider;
        this.jwtClaimsVerifier = jwtClaimsVerifier;
    }

    @Override
    public void verifySecurityToken(Token token) throws TokenVerificationException {
        verifySecurityToken(token, null);
    }

    /**
     * This method validates a dynamic attribute token. The checks include verifying time stamps, as well as IDS related attributes.
     * The values of the IDS attributes (such as the security profile, transport certificate SHA, and referring connector)
     * will be checked against the values inside the DAT, if provided.
     * @param token Security Token to be validated
     * @param additionalAttrs Map with additional attributes which should be checked against the actual values in the DAT
     * @throws TokenVerificationException is thrown, if the token is invalid or if it could not be validated (e.g. no connection to DAPS)
     */
    @Override
    public void verifySecurityToken(Token token, Map<String, Object> additionalAttrs) throws TokenVerificationException {
        //check if the token is null or empty
        if (token == null) {
            logger.debug("Received null-token");
            throw new TokenVerificationException("Token must not be null.");
        }

        String tokenValue = token.getTokenValue();
        if (tokenValue.isEmpty()) {
            logger.debug("Received empty-token");
            throw new TokenVerificationException("Token must not be empty.");
        }

        try {
            JWKSource<?> keySource = jwksProvider.getJsonWebKeySet(tokenValue);
            claimsSet = extractClaimsSet(tokenValue, keySource);
            //check for the expiration of the token
            if(!ignoreJwtExpiration && claimsSet.getExpirationTime().before(new Date()))
            {
                throw new TokenVerificationException("The token is outdated.");
            }

            /* Strictly speaking, those do not need to match. We just need to make sure that the issued at is not in the future
            //check if the 'iat' and 'nbf' values are identical (according to IDS communication guide, both values should be identical)
            if(claimsSet.getIssueTime() == null || !claimsSet.getIssueTime().equals(claimsSet.getNotBeforeTime())){
                throw new TokenVerificationException("The token's issued time (iat) and not before time (nbf) are not identical");
            }

             */

            //difference between 'exp' and 'iat'/'nbf' value should be positive. Also, the token should be issued before the current dateTime.
            if(claimsSet.getExpirationTime().before(claimsSet.getIssueTime()) || new Date().before(claimsSet.getIssueTime())) {
                throw new TokenVerificationException("The token's issued time (iat) is invalid");
            }

            if(claimsSet.getNotBeforeTime() == null || new Date().before(claimsSet.getNotBeforeTime()))
            {
                throw new TokenVerificationException("The token's not before time is invalid");
            }
            //further verification of the token against the incoming message request
            jwtClaimsVerifier = new ClaimsVerifier();
            jwtClaimsVerifier.verify(claimsSet.getClaims(), additionalAttrs);
        }
        catch (RemoteKeySourceException e)
        {
            //If this error occurs, it's most probably due to the DAPS having an invalid certificate,
            //which is not trusted by the connector. Logging this as error for debugging
            logger.error("An error occurred while processing the token. Is the DAPS certificate valid?", e);
            throw new TokenVerificationException("An error occurred while verifying your token", e);
        }
        catch (JWKSException e) {
            throw new TokenVerificationException("Error verifying token.", e);
        }
        catch (ParseException | BadJOSEException | JOSEException e) {
            throw new TokenVerificationException("Error processing token.", e);
        }
    }

    //check for the signature of DAT token by retrieving the public key of the DAPS. On successful processing of the token return it's claimset
    private JWTClaimsSet extractClaimsSet(String tokenValue, JWKSource keySource) throws ParseException, JOSEException, BadJOSEException {
        ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
        JWSKeySelector keySelector = new JWSVerificationKeySelector(JWSAlgorithm.RS256, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);
        if (ignoreJwtExpiration) jwtProcessor.setJWTClaimsSetVerifier(null);
        return jwtProcessor.process(tokenValue, null);
    }

    void setIgnoreJwtExpiration(boolean ignoreJwtExpiration) {
        this.ignoreJwtExpiration = ignoreJwtExpiration;
    }

    int getClaimsCount() {
        return claimsSet.getClaims().size();
    }
}
