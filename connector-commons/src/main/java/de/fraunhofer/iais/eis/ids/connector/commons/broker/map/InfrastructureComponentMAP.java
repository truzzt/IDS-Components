package de.fraunhofer.iais.eis.ids.connector.commons.broker.map;

import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;

import java.util.Optional;

public class InfrastructureComponentMAP implements MessageAndPayload<Message, InfrastructureComponent> {

    private final Message message;
    private InfrastructureComponent connectorSelfDescription;

    public InfrastructureComponentMAP(Message message) {
        this.message = message;
    }

    public InfrastructureComponentMAP(Message message, InfrastructureComponent connectorSelfDescription) {
        this(message);
        this.connectorSelfDescription = connectorSelfDescription;
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public Optional<InfrastructureComponent> getPayload() {
        return Optional.ofNullable(connectorSelfDescription);
    }

    @Override
    public SerializedPayload serializePayload() {
        if (connectorSelfDescription != null) {
            return new SerializedPayload(connectorSelfDescription.toRdf().getBytes(), "application/ld+json");
        }
        else return SerializedPayload.EMPTY;
    }
}
