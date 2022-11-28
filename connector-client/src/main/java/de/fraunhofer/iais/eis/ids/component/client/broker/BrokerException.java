package de.fraunhofer.iais.eis.ids.component.client.broker;

import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;

public class BrokerException extends Exception {

    private MessageAndPayload response;

    public BrokerException(String message, MessageAndPayload response) {
        super(message);
        this.response = response;
    }

    public MessageAndPayload getResponse() {
        return response;
    }
}
