package de.fraunhofer.iais.eis.ids.component.core;

import de.fraunhofer.iais.eis.Message;

import java.util.Optional;

public interface MessageAndPayload<MessageType extends Message, PayloadType> {

    MessageType getMessage();
    Optional<PayloadType> getPayload();
    SerializedPayload serializePayload();

}
