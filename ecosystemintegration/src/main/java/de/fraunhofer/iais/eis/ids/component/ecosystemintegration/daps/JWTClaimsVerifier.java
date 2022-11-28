package de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps;

import de.fraunhofer.iais.eis.ids.component.core.TokenVerificationException;

import java.util.Map;

public interface JWTClaimsVerifier {

    void verify(Map<String, Object> claims, Map<String, Object> additionalAttrs) throws TokenVerificationException;

}
