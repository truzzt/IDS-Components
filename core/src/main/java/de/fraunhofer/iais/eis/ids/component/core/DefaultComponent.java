package de.fraunhofer.iais.eis.ids.component.core;

import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.Message;

import java.net.URI;
import java.util.Arrays;

public class DefaultComponent implements Component {

    private MessageDispatcher messageDispatcher;
    private SelfDescriptionProvider selfDescriptionProvider;

    public DefaultComponent(SelfDescriptionProvider selfDescriptionProvider, SecurityTokenProvider securityTokenProvider, URI responseSenderAgentUri, boolean addSelfDescriptionHandler)
    {
        this.selfDescriptionProvider = selfDescriptionProvider;
        messageDispatcher = new MessageDispatcher(selfDescriptionProvider.getSelfDescription());
        if(addSelfDescriptionHandler) {
            messageDispatcher.addMessageHandler(new SelfDescriptionRetrievalHandler(selfDescriptionProvider, securityTokenProvider, responseSenderAgentUri), RequestType.INFRASTRUCTURE);
        }
        messageDispatcher.addMessageHandler(new EchoHandler(), RequestType.ECHO);
        messageDispatcher.setFailureResponseSenderAgent(responseSenderAgentUri);
        messageDispatcher.setSecurityTokenProvider(securityTokenProvider);
    }

    public void addSelfDescriptionMessageHandler(SelfDescriptionProvider selfDescriptionProvider, SecurityTokenProvider securityTokenProvider, URI responseSenderAgentUri, boolean addSelfDescriptionHandler)
    {
        this.selfDescriptionProvider = selfDescriptionProvider;
        messageDispatcher = new MessageDispatcher(selfDescriptionProvider.getSelfDescription());
        if(addSelfDescriptionHandler) {
            messageDispatcher.addMessageHandler(new SelfDescriptionRetrievalHandler(selfDescriptionProvider, securityTokenProvider, responseSenderAgentUri), RequestType.INFRASTRUCTURE);
        }
        messageDispatcher.addMessageHandler(new EchoHandler(), RequestType.ECHO);
        messageDispatcher.setFailureResponseSenderAgent(responseSenderAgentUri);

    }

    @Override
    public final InfrastructureComponent getSelfDescription() {
        return selfDescriptionProvider.getSelfDescription();
    }

    @Override
    public MessageAndPayload<? extends Message, ?> process(MessageAndPayload<? extends Message, ?> messageAndPayload, RequestType requestType) {
        return messageDispatcher.dispatch(messageAndPayload, requestType);
    }

    public void addMessageHandler(MessageHandler messageHandler, RequestType requestType, RequestType... furtherRequestTypes) {
        messageDispatcher.addMessageHandler(messageHandler, requestType);
        Arrays.stream(furtherRequestTypes).forEach(rt -> messageDispatcher.addMessageHandler(messageHandler, requestType));
    }

    //We cannot add the security token at this stage. The building of the messages fails with a ConstraintViolationException, if created without security token
    /*public void setSecurityTokenProvider(SecurityTokenProvider securityTokenProvider) {
        messageDispatcher.setSecurityTokenProvider(securityTokenProvider);
    }*/

    public void setSecurityTokenVerifier(SecurityTokenVerifier securityTokenVerifier) {
        messageDispatcher.setSecurityTokenVerifier(securityTokenVerifier);
    }
}
