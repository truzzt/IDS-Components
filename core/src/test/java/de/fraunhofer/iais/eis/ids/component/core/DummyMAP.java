package de.fraunhofer.iais.eis.ids.component.core;


import java.util.Optional;

class DummyMAP implements MessageAndPayload<DummyMessage, String> {

    private DummyMessage dummyMessage;

    DummyMAP(DummyMessage dummyMessage) {
        this.dummyMessage = dummyMessage;
    }

    @Override
    public DummyMessage getMessage() {
        return dummyMessage;
    }

    @Override
    public Optional<String> getPayload() {
        return Optional.empty();
    }

    @Override
    public SerializedPayload serializePayload() {
        return SerializedPayload.EMPTY;
    }
}
