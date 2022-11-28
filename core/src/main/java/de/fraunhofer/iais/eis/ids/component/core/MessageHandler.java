package de.fraunhofer.iais.eis.ids.component.core;

import de.fraunhofer.iais.eis.Message;

import java.util.Collection;

public interface MessageHandler<Incoming extends MessageAndPayload<? extends Message, ?>, Outgoing extends MessageAndPayload<? extends Message, ?>> {

    Outgoing handle(Incoming messageAndPayload) throws RejectMessageException;
    Collection<Class<? extends Message>> getSupportedMessageTypes();

}
