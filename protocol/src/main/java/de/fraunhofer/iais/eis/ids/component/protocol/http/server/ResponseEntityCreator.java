package de.fraunhofer.iais.eis.ids.component.protocol.http.server;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionMessage;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;
import de.fraunhofer.iais.eis.ids.component.interaction.multipart.Multipart;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.ids.jsonld.SerializerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.MultiValueMap;

import java.io.IOException;
import java.net.URI;
import java.util.stream.Collectors;

import static de.fraunhofer.iais.eis.RejectionReason.*;

class ResponseEntityCreator {
    Logger logger = LoggerFactory.getLogger(ResponseEntityCreator.class);

    private final Serializer serializer = SerializerFactory.getInstance();

    ResponseEntity fromResponseForRest(MessageAndPayload<?, ?> response, String method)
    {
        ResponseEntity.BodyBuilder bodyBuilder = ResponseEntity.ok();

        Message m = response.getMessage();
        if(m instanceof RejectionMessage)
        {
            RejectionReason rejectionReason = ((RejectionMessage) m).getRejectionReason();
            if (NOT_FOUND.equals(rejectionReason)) {
                bodyBuilder = ResponseEntity.status(404);
            } else if (NOT_AUTHORIZED.equals(rejectionReason) || NOT_AUTHENTICATED.equals(rejectionReason)) {
                bodyBuilder = ResponseEntity.status(401);
            } else if (METHOD_NOT_SUPPORTED.equals(rejectionReason) || MESSAGE_TYPE_NOT_SUPPORTED.equals(rejectionReason)) { //Not quite correct
                bodyBuilder = ResponseEntity.status(405);
            } else if (BAD_PARAMETERS.equals(rejectionReason) || MALFORMED_MESSAGE.equals(rejectionReason) || VERSION_NOT_SUPPORTED.equals(rejectionReason)) { //To be checked
                bodyBuilder = ResponseEntity.status(400);
            } else if (TOO_MANY_RESULTS.equals(rejectionReason)) {
                bodyBuilder = ResponseEntity.status(413); //Not 100% semantically correct
            } else if (TEMPORARILY_NOT_AVAILABLE.equals(rejectionReason)) {
                bodyBuilder = ResponseEntity.status(503); //To be checked
            } else if (INTERNAL_RECIPIENT_ERROR.equals(rejectionReason)) {
                bodyBuilder = ResponseEntity.status(500);
            } else {
                bodyBuilder = ResponseEntity.status(500);
                logger.warn("Unknown rejectionReason: " + ((RejectionMessage) m).getRejectionReason().toRdf());
            }
        }
        bodyBuilder.header("ids-securitytoken", m.getSecurityToken().getTokenValue());
        bodyBuilder.header("ids-issued", m.getIssued().toString());
        bodyBuilder.header("ids-issuerconnector", m.getIssuerConnector().toString());
        bodyBuilder.header("ids-senderagent", m.getSenderAgent().toString());
        bodyBuilder.header("ids-modelversion", m.getModelVersion());
        if(m.getAuthorizationToken() != null)
        {
            bodyBuilder.header("ids-authorizationtoken", m.getAuthorizationToken().getTokenValue());
        }
        if(m.getContentVersion() != null && !m.getContentVersion().equals(""))
        {
            bodyBuilder.header("ids-contentversion", m.getContentVersion());
        }
        if(m.getRecipientAgent() != null && !m.getRecipientAgent().isEmpty())
        {
            //Stream list of URIs, mapping them to strings and separating via spaces
            bodyBuilder.header("ids-recipientagent", m.getRecipientAgent().stream().map(URI::toString).collect(Collectors.joining(" ")));
        }
        if(m.getTransferContract() != null)
        {
            bodyBuilder.header("ids-transfercontract", m.getTransferContract().toString());
        }
        if(m.getCorrelationMessage() != null)
        {
            bodyBuilder.header("ids-correlationmessage", m.getCorrelationMessage().toString());
        }

        if(m instanceof RejectionMessage)
        {
            //TODO: Evaluate whether we want to provide the ids-rejectionReason after all. This is probably redundant, seeing that we have HTTP Status codes already
            return bodyBuilder.header("ids-rejectionReason", ((RejectionMessage) m).getRejectionReason().toString()).body(response.getPayload().get());
        }

        if(m.getProperties() != null)
        {
            if(m.getProperties().containsKey("Serialization"))
            {
                String serialization = (String) m.getProperties().get("Serialization");
                switch (serialization) {
                    case "Lang:Turtle" : bodyBuilder.header("Content-Type", "text/turtle"); break;
                    case "Lang:JSON-LD" : bodyBuilder.header("Content-Type", "application/ld+json"); break;
                    case "Lang:RDF/XML" : bodyBuilder.header("Content-Type", "application/rdf+xml"); break;
                    case "Lang:N-Triples" : bodyBuilder.header("Content-Type", "application/n-triples"); break;
                    default: logger.warn("Unknown serialization: " + serialization); bodyBuilder.header("Content-Type", "application/ld+json"); break;
                }
            }
        }
        //For GET, HEAD, and OPTIONS, set Link header to indicate the class of the resource which has been requested
        if(method.equalsIgnoreCase("get") || method.equalsIgnoreCase("head") || method.equalsIgnoreCase("options"))
        {
            if(response.getMessage().getProperties().containsKey("elementType")) {
                bodyBuilder.header("Link", "<" + response.getMessage().getProperties().get("elementType") +">");
            }
        }
        //For all methods, set Allow header to indicate the allowed methods
        switch (method.toLowerCase()) {
            case "get":
                if (response.getPayload().isPresent()) {
                    if (response.getPayload().get() instanceof String) {
                        return bodyBuilder
                                .body(response.getPayload().get());
                    } else {
                        try {
                            return bodyBuilder.body(serializer.serialize(response.getPayload().get()));
                        } catch (IOException e) {
                            //Some internal error occurred - could not serialize own response
                            return ResponseEntity.status(500).build();
                        }
                    }
                }
                return bodyBuilder.build();
            //no body in HEAD
            case "head":
                return bodyBuilder.build();
            case "options": //TODO: Allow headers should ALWAYS be added. Not only if OPTIONS is used
                String typeOfRequestedElement = "";
                if(m.getProperties() != null)
                    typeOfRequestedElement = m.getProperties().getOrDefault("elementType", "").toString();
                String[] connectorTypes = {"Connector", "BaseConnector", "TrustedConnector", "InfrastructureComponent"};
                boolean editable = false;
                for(String s : connectorTypes)
                {
                    if(s.equalsIgnoreCase(typeOfRequestedElement))
                    {
                        editable = true; //TODO: This is not correct yet. Yes, you can DELETE a connector and a resource, but PUT/POST probably is done at root/catalog level
                        break;
                    }
                }
                if(typeOfRequestedElement.endsWith("Resource") || typeOfRequestedElement.endsWith("Catalog"))
                {
                    editable = true;
                }

                if(editable) {
                    //TODO: It might be wise to only tell a client it is able to modify data, if it is really allowed to do so
                    // Here, we say "you can also PUT/POST/DELETE", but then a RejectionMessage might occur, if the client tries to do that (e.g. trying to sign off another connector's resource)
                    bodyBuilder.header("Accept-Post", "application/ld+json", "text/turtle", "application/n-triples", "application/rdf+xml");
                    bodyBuilder.header("Allow", "PUT", "POST", "DELETE");
                }

                return bodyBuilder.header("Allow", "GET", "HEAD", "OPTIONS").build();

            case "put": case "post":
                //Typically, the URI of the registered element is rewritten by the recipient. In that case, we should tell the data provider under which address it can be found
                if(response.getMessage().getProperties().containsKey("Location"))
                {
                    bodyBuilder.header("Location", response.getMessage().getProperties().get("Location").toString());
                }
                return bodyBuilder.build(); //No body
            case "delete":
                return bodyBuilder.build();
            default:
                //Should be impossible, as we do not allow other methods in our endpoint
                throw new RuntimeException("Method " + method + " is not allowed");
        }
    }

    ResponseEntity fromResponse(Multipart response) {
        return ResponseEntity.ok()
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(createBodyParts(response));
    }

    private  MultiValueMap<String, HttpEntity<?>> createBodyParts(Multipart response) {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("header", response.getHeader()).header("Content-Type", response.getHeaderContentType());

        SerializedPayload serializedPayload = response.getSerializedPayload();
        if (serializedPayload != null) {
            builder.part("payload", serializedPayload.getSerialization())
                        .header("Content-Type", serializedPayload.getContentType());
        }

        return builder.build();
    }

}
