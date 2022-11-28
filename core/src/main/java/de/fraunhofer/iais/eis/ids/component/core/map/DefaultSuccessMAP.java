package de.fraunhofer.iais.eis.ids.component.core.map;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessage;
import de.fraunhofer.iais.eis.MessageProcessedNotificationMessageBuilder;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;

import java.net.URI;
import java.util.Optional;

public class DefaultSuccessMAP implements MessageAndPayload<MessageProcessedNotificationMessage, Void> {

    private final MessageProcessedNotificationMessage message;

    public DefaultSuccessMAP(URI issuerConnector, String messageModelVersion, URI originalMessage, DynamicAttributeToken securityToken, URI senderAgent) {
        message = new MessageProcessedNotificationMessageBuilder()
                ._issuerConnector_(issuerConnector)
                ._issued_(CalendarUtil.now())
                ._modelVersion_(messageModelVersion)
                ._correlationMessage_(originalMessage)
                ._securityToken_(securityToken)
                ._senderAgent_(senderAgent)
                .build();
    }

    public DefaultSuccessMAP(MessageProcessedNotificationMessage message) {
        this.message = message;
    }

    @Override
    public MessageProcessedNotificationMessage getMessage() {
        return message;
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
