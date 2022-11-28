package de.fraunhofer.iais.eis.ids.component.interaction.multipart;

import de.fraunhofer.iais.eis.DynamicAttributeTokenBuilder;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.TokenFormat;
import de.fraunhofer.iais.eis.ids.component.core.*;
import de.fraunhofer.iais.eis.ids.component.core.map.DefaultFailureMAP;
import de.fraunhofer.iais.eis.ids.component.interaction.ComponentInteractor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MultipartComponentInteractor implements ComponentInteractor<Multipart, Multipart, String> {

    public Component getComponent() {
        return component;
    }

    public SecurityTokenProvider getSecurityTokenProvider() {
        return securityTokenProvider;
    }

    public URI getResponseSenderAgent() {
        return responseSenderAgent;
    }

    public boolean getPerformShaclValidation() {
        return performShaclValidation;
    }

    private final Component component;
    private final MultipartMapConverter multipartMapConverter;
    private final SecurityTokenProvider securityTokenProvider;
    private final URI responseSenderAgent;
    private final boolean performShaclValidation;

    @Deprecated
    public MultipartComponentInteractor(Component component, SecurityTokenProvider securityTokenProvider, URI responseSenderAgent)
    {
        this.component = component;
        this.securityTokenProvider = securityTokenProvider;
        this.responseSenderAgent = responseSenderAgent;
        this.performShaclValidation = true;
        multipartMapConverter = new DefaultMultipartMapConverter(true);
    }

    public MultipartComponentInteractor(Component component, SecurityTokenProvider securityTokenProvider, URI responseSenderAgent, boolean performShaclValidation)
    {
        this.component = component;
        this.securityTokenProvider = securityTokenProvider;
        this.responseSenderAgent = responseSenderAgent;
        this.performShaclValidation = performShaclValidation;
        multipartMapConverter = new DefaultMultipartMapConverter(performShaclValidation);
    }


    @Override
    public Multipart process(Multipart request, RequestType requestType) throws IOException {
        MessageAndPayload<?, ?> map;
        //Check if the conversion to a message and payload succeeds
        //For example, this might fail, if the MessageType is not known to the MapFactory, or if the message format is broken
        try {
            map = multipartMapConverter.multipartToMap(request);
        }
        catch (RejectMessageException e)
        {
            try {
                return multipartMapConverter.mapToMultipart(new DefaultFailureMAP(component.getSelfDescription(), new URI("https://INVALID"), e.getRejectionReason(), e.getRejectionPayload(), securityTokenProvider.getSecurityTokenAsDAT(), responseSenderAgent));
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
                    return multipartMapConverter.mapToMultipart(new DefaultFailureMAP(component.getSelfDescription(), new URI("https://INVALID"), RejectionReason.METHOD_NOT_SUPPORTED,"The message could not be parsed, as the message type is not known to this connector. Note that, due to being unable to parse this message, the ID of the correlating message is incorrect.", securityTokenProvider.getSecurityTokenAsDAT(), responseSenderAgent));
                }
                return multipartMapConverter.mapToMultipart(new DefaultFailureMAP(component.getSelfDescription(), new URI("https://INVALID"), RejectionReason.MALFORMED_MESSAGE, "The message could not be parsed due to an error in the message. Note that, due to being unable to parse this message, the ID of the correlating message is incorrect. Error: " + e.getMessage(), securityTokenProvider.getSecurityTokenAsDAT(), responseSenderAgent));
            } catch (URISyntaxException | TokenRetrievalException ex) { ex.printStackTrace(); return null; }
        }
        try {
            try {
                MessageAndPayload<?, ?> response = component.process(map, requestType);
                return multipartMapConverter.mapToMultipart(response);
            } catch (MissingHandlerException e) {
                return multipartMapConverter.mapToMultipart(new DefaultFailureMAP(component.getSelfDescription(), map.getMessage().getId(), RejectionReason.MESSAGE_TYPE_NOT_SUPPORTED, "This connector does not support this message type.", securityTokenProvider.getSecurityTokenAsDAT(), responseSenderAgent));
            }
        }
        catch (TokenRetrievalException e)
        {
            return multipartMapConverter.mapToMultipart(new DefaultFailureMAP(component.getSelfDescription(), map.getMessage().getId(), RejectionReason.INTERNAL_RECIPIENT_ERROR, "Failed to retrieve own security token from DAPS.", new DynamicAttributeTokenBuilder()._tokenFormat_(TokenFormat.JWT)._tokenValue_("INVALID").build(), responseSenderAgent));
        }
        //IOException might for example be thrown if the message type is now known to the MapFactory
        //Note that mapToMultipart can also throw an IOException - however, this should never happen for the DefaultFailureMAP.
        catch (IOException e)
        {
            try {
                return multipartMapConverter.mapToMultipart(new DefaultFailureMAP(component.getSelfDescription(), map.getMessage().getId(), RejectionReason.INTERNAL_RECIPIENT_ERROR, e.getMessage(), securityTokenProvider.getSecurityTokenAsDAT(), responseSenderAgent));
            }
            catch (TokenRetrievalException f)
            {
                return multipartMapConverter.mapToMultipart(new DefaultFailureMAP(component.getSelfDescription(), map.getMessage().getId(), RejectionReason.INTERNAL_RECIPIENT_ERROR, "Failed to retrieve own DAPS token: " + f.getMessage(), new DynamicAttributeTokenBuilder()._tokenValue_("INVALID")._tokenFormat_(TokenFormat.JWT).build(), responseSenderAgent));
            }

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
