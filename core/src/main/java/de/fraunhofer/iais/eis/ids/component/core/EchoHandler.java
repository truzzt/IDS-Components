package de.fraunhofer.iais.eis.ids.component.core;

import de.fraunhofer.iais.eis.Message;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class EchoHandler implements MessageHandler<MessageAndPayload<? extends Message, ?>, MessageAndPayload<? extends Message, ?>> {

    @Override
    public MessageAndPayload<? extends Message, ?> handle(MessageAndPayload<? extends Message, ?> messageAndPayload) {
       return new MessageAndPayload<Message, Optional<?>>() {
           @Override
           public Message getMessage() {
               return messageAndPayload.getMessage();
           }

           @Override
           public Optional getPayload() {
               return messageAndPayload.getPayload();
           }

           @Override
           public SerializedPayload serializePayload() {
               return messageAndPayload.serializePayload();
           }
       };
    }

    @Override
    public Collection<Class<? extends Message>> getSupportedMessageTypes() {
        return Collections.singletonList(Message.class);
    }
}
