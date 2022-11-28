package de.fraunhofer.iais.eis.ids.connector.commons.identityprovider.map;

import de.fraunhofer.iais.eis.AccessTokenRequestMessage;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;

import java.util.Optional;

public class AccessTokenRequestMAP  implements MessageAndPayload<AccessTokenRequestMessage, Void> {

    private AccessTokenRequestMessage accessTokenRequest;

    public AccessTokenRequestMAP(AccessTokenRequestMessage accessTokenRequest) {
        this.accessTokenRequest = accessTokenRequest;
    }

    @Override
    public AccessTokenRequestMessage getMessage() {
        return accessTokenRequest;
    }

    @Override
    public Optional<Void> getPayload() {
        return Optional.empty();
    }

    @Override
    public SerializedPayload serializePayload() {
        return SerializedPayload.EMPTY;
    }
}
