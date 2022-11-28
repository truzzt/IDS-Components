package de.fraunhofer.iais.eis.ids.component.core;

import de.fraunhofer.iais.eis.Message;

import java.util.Collection;
import java.util.Collections;

class DummyMessageHandler implements MessageHandler<DummyMAP, DummyMAP> {

    @Override
    public DummyMAP handle(DummyMAP messageAndPayload)  {
        // pass through message
        return new DummyMAP(messageAndPayload.getMessage());
    }

    @Override
    public Collection<Class<? extends Message>> getSupportedMessageTypes() {
        return Collections.singletonList(DummyMessage.class);
    }
}
