package de.fraunhofer.iais.eis.ids.component.core;

import de.fraunhofer.iais.eis.Token;

import java.util.Map;

public interface SecurityTokenVerifier {
    @Deprecated
    void verifySecurityToken(Token token) throws TokenVerificationException;

    void verifySecurityToken(Token token, Map<String, Object> additionalAttrs) throws TokenVerificationException;

}
