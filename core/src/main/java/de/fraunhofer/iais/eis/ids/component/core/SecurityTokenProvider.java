package de.fraunhofer.iais.eis.ids.component.core;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.TokenFormat;

public abstract class SecurityTokenProvider {

    public abstract String getSecurityToken() throws TokenRetrievalException;
    public DynamicAttributeToken getSecurityTokenAsDAT() throws TokenRetrievalException
    {
        return new DynamicAttributeTokenBuilder()._tokenFormat_(TokenFormat.JWT)._tokenValue_(getSecurityToken()).build();
    }

}
