package de.fraunhofer.iais.eis.ids.component.interaction.multipart;

import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;
import de.fraunhofer.iais.eis.ids.component.interaction.serialize.SerializationException;

import java.io.IOException;
import java.util.Optional;

public class Multipart {

    private String header, headerContentType;
    private SerializedPayload serializedPayload;
    //Do not perform SHACL validation at this point. Incoming messages will be checked at another place.
    //Performing SHACL validation here would be required if outgoing messages should also be checked
    private MultipartMapConverter multipartMapConverter = new DefaultMultipartMapConverter(false);

    public Multipart(String header, String headerContentType) {
        this.header = header;
        this.headerContentType = headerContentType;
    }

    public Multipart(MessageAndPayload messageAndPayload) throws SerializationException {
        copy(multipartMapConverter.mapToMultipart(messageAndPayload));
    }

    private void copy(Multipart other) {
        this.header = other.header;
        this.headerContentType = other.headerContentType;
        this.serializedPayload = other.serializedPayload;
        this.multipartMapConverter = other.multipartMapConverter;
    }

    public String getHeader() {
        return header;
    }

    public String getHeaderContentType() {
        return headerContentType;
    }

    public SerializedPayload getSerializedPayload() {
        return serializedPayload;
    }

    public void setSerializedPayload(SerializedPayload serializedPayload) {
        this.serializedPayload = serializedPayload;
    }

    public MessageAndPayload toMap() throws IOException, RejectMessageException {
        return multipartMapConverter.multipartToMap(this);
    }

}
