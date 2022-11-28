package de.fraunhofer.iais.eis.ids.component.core.map;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;

import java.util.Optional;

public class NullMAP implements MessageAndPayload<Message, Void> {

    @Override
    public Message getMessage() {
        return null;
    }

    @Override
    public Optional<Void> getPayload() {
        return Optional.empty();
    }

    @Override
    public SerializedPayload serializePayload() {
        return null;
    }
}
