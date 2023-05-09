package de.fraunhofer.iais.eis.ids.component.interaction.serialize;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.ids.jsonld.SerializerFactory;

import java.io.IOException;

public class HeaderSerializerJsonLd implements HeaderSerializer {

    private final Serializer serializer = SerializerFactory.getInstance();

    @Override
    public String serialize(Message message) throws SerializationException {
        try {
            return serializer.serialize(message);
        }
        catch (IOException e) {
            throw new SerializationException(e);
        }
    }

    @Override
    public <T extends Message> T deserialize(String message, Class<T> clazz) throws SerializationException {
        try {
            return serializer.deserialize(message, clazz);
        }
        catch (IOException e) {
            throw new SerializationException(e);
        }
    }

}
