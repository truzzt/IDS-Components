package de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps;

import de.fraunhofer.iais.eis.ids.component.core.TokenVerificationException;
import net.minidev.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

public class ClaimsVerifier implements JWTClaimsVerifier {
    private final Logger logger = LoggerFactory.getLogger(ClaimsVerifier.class);
    //mapping each security profile value with its compatible ones
    //the security profile value depends on the infomodel v3.x.x and v4.x.x. eg.,idsc:BASE_CONNECTOR_SECURITY_PROFILE and idsc:BASE_SECURITY_PROFILE respectively
    private final String[] baseSecProfVals = {"idsc:BASE_CONNECTOR_SECURITY_PROFILE", "idsc:BASE_SECURITY_PROFILE"} ;
    private final String[] trustSecProfVals = {"idsc:BASE_CONNECTOR_SECURITY_PROFILE", "idsc:BASE_SECURITY_PROFILE", "idsc:TRUST_SECURITY_PROFILE", "idsc:TRUSTED_CONNECTOR_SECURITY_PROFILE"};
    private final String[] plusTrustSecProfVals = {"idsc:BASE_CONNECTOR_SECURITY_PROFILE", "idsc:BASE_SECURITY_PROFILE", "idsc:TRUST_SECURITY_PROFILE", "idsc:TRUSTED_CONNECTOR_SECURITY_PROFILE", "idsc:TRUST_PLUS_SECURITY_PROFILE", "idsc:TRUSTED_CONNECTOR_PLUS_SECURITY_PROFILE"};
    private final JSONObject compatibleSecurityProfile = new JSONObject();



    @Override
    public void verify(Map<String, Object> claims, Map<String, Object> additionalAttrs) throws TokenVerificationException {
        logger.info("Verifying token's claims");
        //verify incoming connector's security profile
        if(additionalAttrs != null && additionalAttrs.containsKey("securityProfile")){
            verifySecurityProfile((String) claims.get("securityProfile"), additionalAttrs.get("securityProfile").toString());
        }
        //TODO: Once the values of referringConnector and transportCertsSha256 are properly set in DAT token, need to verify them here
    }

    /**
     * This method is responsible for verifying the security profile of the component as claimed in its IDS message with its DAT token
     * @param registeredSecurityProfile security profile obtained from the DAT token
     * @param givenSecurityProfile security profile obtained from the IDS message's payload
     * */
    private void verifySecurityProfile(String registeredSecurityProfile, String givenSecurityProfile) throws TokenVerificationException {

        //Replace full URIs (if present) by prefixed values. This simplifies the potential number of values these strings can have
        if(registeredSecurityProfile.startsWith("https://w3id.org/idsa/code/"))
        {
            registeredSecurityProfile = registeredSecurityProfile.replace("https://w3id.org/idsa/code/", "idsc:");
        }
        if(givenSecurityProfile.startsWith("https://w3id.org/idsa/code/"))
        {
            givenSecurityProfile = givenSecurityProfile.replace("https://w3id.org/idsa/code/", "idsc:");
        }

        //Initialized yet? If not, add the values to initialize
        if(compatibleSecurityProfile.isEmpty()) {
            compatibleSecurityProfile.put("idsc:BASE_CONNECTOR_SECURITY_PROFILE", baseSecProfVals);
            compatibleSecurityProfile.put("idsc:BASE_SECURITY_PROFILE", baseSecProfVals);
            compatibleSecurityProfile.put("idsc:TRUST_SECURITY_PROFILE", trustSecProfVals);
            compatibleSecurityProfile.put("idsc:TRUSTED_CONNECTOR_SECURITY_PROFILE", trustSecProfVals);
            compatibleSecurityProfile.put("idsc:TRUST_PLUS_SECURITY_PROFILE", plusTrustSecProfVals);
            compatibleSecurityProfile.put("idsc:TRUSTED_CONNECTOR_PLUS_SECURITY_PROFILE", plusTrustSecProfVals);
        }

        String[] vals = (String[]) compatibleSecurityProfile.get(registeredSecurityProfile);
        //if the given value of security profile is not compatible with the token claim's value then reject the message
        if(!Arrays.asList(vals).contains(givenSecurityProfile))
            throw new TokenVerificationException("Security profile violation. Registered security profile at DAPS is:  "+registeredSecurityProfile+ ", but the given security profile is: "+givenSecurityProfile);
    }
}
