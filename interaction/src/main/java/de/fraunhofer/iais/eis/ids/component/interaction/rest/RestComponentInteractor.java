package de.fraunhofer.iais.eis.ids.component.interaction.rest;

import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.component.core.*;
import de.fraunhofer.iais.eis.ids.component.core.map.DefaultFailureMAP;
import de.fraunhofer.iais.eis.ids.component.interaction.ComponentInteractor;
import de.fraunhofer.iais.eis.ids.component.interaction.multipart.MapFactory;
import de.fraunhofer.iais.eis.ids.component.interaction.validation.ShaclValidator;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.jena.shacl.ValidationReport;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class RestComponentInteractor implements ComponentInteractor<ImmutablePair<String, String>, MessageAndPayload<?, ?>, String> {

    private final Component component;
    private final SecurityTokenProvider securityTokenProvider;
    private final URI responseSenderAgent;
    private final boolean performShaclValidation;
    private final String restEndpointPath;



    public String getRestEndpointPath() { return restEndpointPath ; }

    public RestComponentInteractor(Component component, SecurityTokenProvider securityTokenProvider, URI responseSenderAgent, boolean performShaclValidation)
    {
        this.component = component;
        this.securityTokenProvider = securityTokenProvider;
        this.responseSenderAgent = responseSenderAgent;
        this.performShaclValidation = performShaclValidation;
        this.restEndpointPath = "/connectors/";
    }

    public RestComponentInteractor(Component component, SecurityTokenProvider securityTokenProvider, URI responseSenderAgent, boolean performShaclValidation, String restEndpointPath)
    {
        this.component = component;
        this.securityTokenProvider = securityTokenProvider;
        this.responseSenderAgent = responseSenderAgent;
        this.performShaclValidation = performShaclValidation;
        this.restEndpointPath = restEndpointPath;
    }


    @Override
    public MessageAndPayload<?, ?> process(ImmutablePair<String, String> request, RequestType requestType) {
        MessageAndPayload<?, ?> map;
        String header = request.getKey();
        String body = request.getValue();
        if(body == null) body = "";

        //Check if the conversion to a message and payload succeeds
        //For example, this might fail, if the MessageType is not known to the MapFactory, or if the message format is broken
        try {
            Message msg = new Serializer().deserialize(header, Message.class);
            map = MapFactory.getInstance().createMap(msg, new SerializedPayload(body.getBytes()));
            if(performShaclValidation) {
                //TODO: Body validation might be relevant for PUT/POST messages. If changes are made, also apply them to DefaultMultipartMapConverter
                ValidationReport report = ShaclValidator.validateRdf(header);
                if (!report.conforms()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("SHACL validation failed: ");
                    report.getEntries().forEach(reportEntry -> sb.append(reportEntry.toString()).append("\n"));
                    throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE, new Exception("SHACL validation failed. Report:\n" + sb.toString()));
                }
            }
        }
        catch (RejectMessageException e)
        {
            try {
                return new DefaultFailureMAP(component.getSelfDescription(), new URI("https://INVALID"), e.getRejectionReason(), e.getRejectionPayload(), securityTokenProvider.getSecurityTokenAsDAT(), responseSenderAgent);
            } catch (URISyntaxException | TokenRetrievalException ex) {
                ex.printStackTrace();
                return null;
            }
        }
        catch (IOException e)
        { //TODO: We do not yet distinguish if this is a SerializationException or some other IOException
            try {
                if(e.getMessage().contains("Could not convert input header to suitable message and payload type.")) //This message is defined in the MapFactory class
                {
                    return new DefaultFailureMAP(component.getSelfDescription(), new URI("https://INVALID"), RejectionReason.METHOD_NOT_SUPPORTED,"The message could not be parsed, as the message type is not known to this connector. Note that, due to being unable to parse this message, the ID of the correlating message is incorrect.", securityTokenProvider.getSecurityTokenAsDAT(), responseSenderAgent);
                }
                return new DefaultFailureMAP(component.getSelfDescription(), new URI("https://INVALID"), RejectionReason.MALFORMED_MESSAGE, "The message could not be parsed due to an error in the message. Note that, due to being unable to parse this message, the ID of the correlating message is incorrect. Error: " + e.getMessage(), securityTokenProvider.getSecurityTokenAsDAT(), responseSenderAgent);
            } catch (URISyntaxException | TokenRetrievalException ex) { ex.printStackTrace(); return null; }
        }
        try {
            try {
                return component.process(map, requestType);
            } catch (MissingHandlerException e) {
                return new DefaultFailureMAP(component.getSelfDescription(), map.getMessage().getId(), RejectionReason.MESSAGE_TYPE_NOT_SUPPORTED, "This connector does not support this message type.", securityTokenProvider.getSecurityTokenAsDAT(), responseSenderAgent);
            }
        }
        catch (TokenRetrievalException e)
        {
            return new DefaultFailureMAP(component.getSelfDescription(), map.getMessage().getId(), RejectionReason.INTERNAL_RECIPIENT_ERROR, "Failed to retrieve own security token from DAPS.", new DynamicAttributeTokenBuilder()._tokenFormat_(TokenFormat.JWT)._tokenValue_("INVALID").build(), responseSenderAgent);
        }
    }

    @Override
    public String getSelfDescription() {
        return component.getSelfDescription().toRdf();
    }

    public URI getComponentUri() {
        return component.getSelfDescription().getId();
    }

}
