package de.fraunhofer.iais.eis.ids.component.interaction.serialize;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;

import java.io.IOException;

public class HeaderSerializerJsonLd implements HeaderSerializer {

    @Override
    public String serialize(Message message) throws SerializationException {
        try {
            return new Serializer().serialize(message);
        }
        catch (IOException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public <T extends Message> T deserialize(String message, Class<T> clazz) throws SerializationException {
        try {
            return new Serializer().deserialize(message, clazz);
        }
        catch (IOException e) {
            throw new SerializationException(e);
        }
    }

}
