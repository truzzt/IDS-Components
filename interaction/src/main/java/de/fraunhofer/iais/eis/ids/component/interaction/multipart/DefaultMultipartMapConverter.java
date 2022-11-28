package de.fraunhofer.iais.eis.ids.component.interaction.multipart;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;
import de.fraunhofer.iais.eis.ids.component.interaction.serialize.HeaderSerializer;
import de.fraunhofer.iais.eis.ids.component.interaction.serialize.HeaderSerializerJsonLd;
import de.fraunhofer.iais.eis.ids.component.interaction.serialize.SerializationException;
import de.fraunhofer.iais.eis.ids.component.interaction.validation.ShaclValidator;
import de.fraunhofer.iais.eis.util.ConstraintViolationException;
import org.apache.jena.shacl.ValidationReport;

import java.io.IOException;

public class DefaultMultipartMapConverter implements MultipartMapConverter {

    private final boolean performShaclValidation;
    private final HeaderSerializer headerSerializer = new HeaderSerializerJsonLd();

    public DefaultMultipartMapConverter(boolean performShaclValidation)
    {
        this.performShaclValidation = performShaclValidation;
    }

    @Override
    public Multipart mapToMultipart(MessageAndPayload messageAndPayload) throws SerializationException {
            String serializedHeader = headerSerializer.serialize(messageAndPayload.getMessage());
            Multipart multipart = new Multipart(serializedHeader, "application/ld+json");

            messageAndPayload.getPayload().ifPresent(payload -> {
                SerializedPayload serializedPayload = messageAndPayload.serializePayload();
                multipart.setSerializedPayload(serializedPayload);
            });

            return multipart;
    }

    @Override
    public MessageAndPayload multipartToMap(Multipart multipart) throws IOException, RejectMessageException {
        if(performShaclValidation) {
            ValidationReport report = ShaclValidator.validateRdf(multipart.getHeader());
            if (!report.conforms()) {
                StringBuilder sb = new StringBuilder();
                sb.append("SHACL validation failed: ");
                report.getEntries().forEach(reportEntry -> sb.append(reportEntry.toString()).append("\n"));
                throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE, new Exception("SHACL validation failed. Report:\n" + sb.toString()));
            }
        }
        Message msg = headerSerializer.deserialize(multipart.getHeader(), Message.class);
        try {
            return MapFactory.getInstance().createMap(msg, multipart.getSerializedPayload());
        }
        catch (ConstraintViolationException e)
        {
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE, e);
        }
    }

}
