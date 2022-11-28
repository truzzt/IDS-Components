package de.fraunhofer.iais.eis.ids.component.core;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.map.DescriptionRequestMAP;
import de.fraunhofer.iais.eis.ids.component.core.map.DescriptionResponseMAP;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

//This class is deprecated, as with the Information Model 3.0.0 release, there are no more SelfDescriptionRequests
//Instead, a general DescriptionRequestHandler should be implemented, which can handle both SelfDescriptions and describing items from its catalog (or index)
@Deprecated
class SelfDescriptionRetrievalHandler implements MessageHandler<DescriptionRequestMAP, DescriptionResponseMAP> {

    private final SelfDescriptionProvider selfDescriptionProvider;
    private final SecurityTokenProvider securityTokenProvider;
    private final URI responseSenderAgentUri;

    public SelfDescriptionRetrievalHandler(SelfDescriptionProvider selfDescriptionProvider, SecurityTokenProvider securityTokenProvider, URI responseSenderAgentUri) {
        this.selfDescriptionProvider = selfDescriptionProvider;
        this.securityTokenProvider = securityTokenProvider;
        this.responseSenderAgentUri = responseSenderAgentUri;
    }

    @Override
    public DescriptionResponseMAP handle(DescriptionRequestMAP messageAndPayload) throws RejectMessageException {
        InfrastructureComponent component = selfDescriptionProvider.getSelfDescription();
        try {
            DescriptionResponseMessage responseMsg = new DescriptionResponseMessageBuilder()
                    ._issuerConnector_(component.getId())
                    ._issued_(CalendarUtil.now())
                    ._modelVersion_(component.getOutboundModelVersion())
                    ._correlationMessage_(messageAndPayload.getMessage().getId())
                    ._securityToken_(securityTokenProvider.getSecurityTokenAsDAT())
                    ._senderAgent_(responseSenderAgentUri)
                    .build();

            return new DescriptionResponseMAP(responseMsg, selfDescriptionProvider.getSelfDescription().toRdf());
        }
        catch (TokenRetrievalException e)
        {
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }
    }

    @Override
    public Collection<Class<? extends Message>> getSupportedMessageTypes() {
        return Collections.singletonList(DescriptionRequestMessage.class);
    }

}
