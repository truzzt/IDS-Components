package de.fraunhofer.iais.eis.ids.component.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.map.ContractFailureMAP;
import de.fraunhofer.iais.eis.ids.component.core.map.DefaultFailureMAP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.*;
import java.util.Collection;

class MessageDispatcher {

    private final Logger logger = LoggerFactory.getLogger(MessageDispatcher.class);

    private final java.util.Map<RequestType, Collection<MessageHandler<? extends Message, ?>>> allMessageHandlers = new HashMap<>();
    private SecurityTokenProvider securityTokenProvider = new SecurityTokenProvider() {
        @Override
        public String getSecurityToken() {
            return "";
        }
    };
    private SecurityTokenVerifier securityTokenVerifier = new SecurityTokenVerifier() {
        @Override
        public void verifySecurityToken(Token token){
            logger.warn("No security token verifier set. Use setSecurityTokenVerifier method.");
        }

        @Override
        public void verifySecurityToken(Token token, Map<String, Object> additionalAttrs){
            logger.warn("No security token verifier set. Use setSecurityTokenVerifier method.");
        }
    };
    private final InfrastructureComponent infrastructureComponent;
    private URI failureResponseSenderAgent;

    MessageDispatcher(InfrastructureComponent infrastructureComponent) {
        this.infrastructureComponent = infrastructureComponent;
        Arrays.stream(RequestType.values()).forEach(rt -> allMessageHandlers.put(rt, new ArrayList<>()));
    }

    MessageAndPayload<? extends Message, ?> dispatch(MessageAndPayload<? extends Message, ?> messageAndPayload, RequestType requestType) {
        Collection<MessageHandler<? extends  Message, ?>> messageHandlers = allMessageHandlers.get(requestType);
        Optional<? extends MessageHandler<? extends Message, ?>> applicableHandler = messageHandlers.stream()
                .filter(handler -> canHandleMessage(handler, messageAndPayload.getMessage()))
                .findFirst();

        return applicableHandler.map(handler -> handle(handler, messageAndPayload))
        .orElseThrow(() -> new MissingHandlerException(messageAndPayload.getMessage().getId(),
                "No handler for message type '" +messageAndPayload.getMessage().getClass()+ "' available."));
    }

    private boolean canHandleMessage(MessageHandler<? extends Message, ?> handler, Message message) {
        return handler.getSupportedMessageTypes().stream().anyMatch(messageType ->
                messageType.isAssignableFrom(message.getClass()));
    }

    private MessageAndPayload<? extends Message, ?> handle(MessageHandler handler, MessageAndPayload messageAndPayload) {
        MessageAndPayload<? extends Message, ?> result;
        //Generate some invalid token for the case that we are not able to retrieve a valid one
        DynamicAttributeToken ownToken = new DynamicAttributeTokenBuilder()._tokenFormat_(TokenFormat.JWT)._tokenValue_("INVALID").build();
        //additional attributes that needs to be passed to the SecurityTokenVerifier implementation class, in order to verify the token with the message payload. eg., security profile of a connector
        Map<String, Object> additionalAttrs = new HashMap<>();
        try {
            //Attempt to fetch token, possibly remote connection to DAPS required
            ownToken = securityTokenProvider.getSecurityTokenAsDAT();
            if(ownToken.getTokenValue().equals(""))
            {
                logger.warn("An empty token was retrieved from the security token provider. Make sure to have set a proper securityTokenProvider for the Message Dispatcher!");
            }
            try {
                Optional<?> incomingPayload = messageAndPayload.getPayload();
                if(incomingPayload.isPresent()) {
                    if (incomingPayload.get() instanceof InfrastructureComponent) {
                        JsonNode convertPayloadToJson = new ObjectMapper().readTree(((InfrastructureComponent)incomingPayload.get()).toRdf());
                        if (convertPayloadToJson.has("ids:securityProfile"))
                            additionalAttrs.put("securityProfile", convertPayloadToJson.get("ids:securityProfile").get("@id").textValue());
                        else
                            throw new TokenVerificationException("Security profile not set");
                    }
                }
                securityTokenVerifier.verifySecurityToken(messageAndPayload.getMessage().getSecurityToken(), additionalAttrs);
                result = handler.handle(messageAndPayload);
                //addSecurityToken(result.getMessage()); //This does not work anymore. Security tokens need to be set during message class instantiation
            } catch (TokenVerificationException e) {
                result = new DefaultFailureMAP(infrastructureComponent, messageAndPayload.getMessage().getId(), RejectionReason.NOT_AUTHENTICATED, e.getMessage(), ownToken, failureResponseSenderAgent);
            } catch (RejectMessageException e) {
                if (e instanceof ContractRejectMessageException) {
                    result = new ContractFailureMAP(infrastructureComponent, messageAndPayload.getMessage().getId(), ((ContractRejectMessageException) e).getContractRejectionReason().getValue(), e.getRejectionPayload(), ownToken, failureResponseSenderAgent);
                } else {
                    if (e.getRejectionReason().equals(RejectionReason.INTERNAL_RECIPIENT_ERROR)) {
                        logger.error("Internal error occurred.", e);
                    }
                    // TODO attribute writing message details into result MAP and preventing it for productive usages
                    result = new DefaultFailureMAP(infrastructureComponent, messageAndPayload.getMessage().getId(), e.getRejectionReason(), e.getRejectionPayload(), ownToken, failureResponseSenderAgent);
                }
            }
        }
        catch (TokenRetrievalException e)
        {
            e.printStackTrace();
            //Token has dummy values in this case
            result = new DefaultFailureMAP(infrastructureComponent, messageAndPayload.getMessage().getId(), RejectionReason.INTERNAL_RECIPIENT_ERROR, "Failed to retrieve own DAPS token, preventing a valid response." , ownToken, failureResponseSenderAgent);
        }
        //Some other, unknown exception occurred. Catching ALL exceptions here to prevent HTTP 500 status responses
        catch (Exception e)
        {
            e.printStackTrace();
            //Token should be valid. If it weren't, the previous catch clause should have triggered instead
            result = new DefaultFailureMAP(infrastructureComponent, messageAndPayload.getMessage().getId(), RejectionReason.INTERNAL_RECIPIENT_ERROR, "An unknown error has occurred" , ownToken, failureResponseSenderAgent);
        }

        return result;
    }


    void addMessageHandler(MessageHandler messageHandler, RequestType requestType) {
        allMessageHandlers.get(requestType).add(messageHandler);
    }

    void setSecurityTokenProvider(SecurityTokenProvider securityTokenProvider)
    {
        this.securityTokenProvider = securityTokenProvider;
    }

    void setSecurityTokenVerifier(SecurityTokenVerifier securityTokenVerifier) {
        this.securityTokenVerifier = securityTokenVerifier;
    }

    void setFailureResponseSenderAgent(URI responseSenderAgent)
    {
        this.failureResponseSenderAgent = responseSenderAgent;
    }
}
