package de.fraunhofer.iais.eis.ids.component.interaction.serialize;

import de.fraunhofer.iais.eis.Message;

public interface HeaderSerializer {

    String serialize(Message message) throws SerializationException;
    <T extends Message> T deserialize(String message, Class<T> clazz) throws SerializationException;

}
