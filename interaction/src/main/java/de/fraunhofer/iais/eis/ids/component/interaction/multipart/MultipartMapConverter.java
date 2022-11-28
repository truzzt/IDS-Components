package de.fraunhofer.iais.eis.ids.component.interaction.multipart;

import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.interaction.serialize.SerializationException;

import java.io.IOException;

public interface MultipartMapConverter {

    Multipart mapToMultipart(MessageAndPayload messageAndPayload) throws SerializationException;
    MessageAndPayload multipartToMap(Multipart multipart) throws SerializationException, IOException, RejectMessageException;

}
