package de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps;

import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.util.Base64URL;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Collection;

public class JWKSFromIssuer implements JsonWebKeySetProvider {

    private final Collection<String> trustedIssuers;

    public JWKSFromIssuer(Collection<String> trustedIssuers) {
        this.trustedIssuers = trustedIssuers;
    }

    @Override
    public JWKSource getJsonWebKeySet(String jwt) throws JWKSException {
        String failureMsg;
        try {
            Base64URL[] parts = JOSEObject.split(jwt);
            String payload = parts[1].decodeToString();
            URI issuer;
            try {
                issuer = new URI((String) ((JSONObject) JSONValue.parse(payload)).get("iss"));
            }
            catch (NullPointerException e)
            {
                throw new JWKSException("Could not retrieve issuer from JWT");
            }
            String issuingHost= issuer.getHost();
            int issuingPort = issuer.getPort();

            if (trustedIssuers.contains(issuingHost)) {
                if (issuingPort > 0) {issuingHost = issuingHost + ":" + issuingPort;}
                String dapsKeyEndpoint = issuer.getScheme() + "://" + issuingHost + "/.well-known/jwks.json";
                URL jwksUrl = new URI(dapsKeyEndpoint).toURL();
                return new RemoteJWKSet(jwksUrl);
            }
            else {
                failureMsg = "JWT issuer '" +issuingHost+ "' not found in the list of trusted issuers: "+trustedIssuers;
            }
        }
        catch (ParseException e) {
            failureMsg = "Unable to parse JWT (" +jwt+ ").";
        }
        catch (URISyntaxException e) {
            failureMsg = "Invalid issuer syntax.";
        }
        catch (MalformedURLException e) {
            failureMsg = "Unable to create JWKS lookup URL.";
        }

        throw new JWKSException(failureMsg);

    }
}
