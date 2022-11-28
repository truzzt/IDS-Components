package de.fraunhofer.iais.eis.ids.component.core.map;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;

import java.net.URI;
import java.util.Optional;

public class DefaultFailureMAP implements MessageAndPayload<RejectionMessage, String> {

    protected RejectionMessage message;
    protected String payload;

    protected DefaultFailureMAP(){}

    public DefaultFailureMAP(InfrastructureComponent infrastructureComponent,
                             URI originalMessage,
                             RejectionReason rejectionReason,
                             String rejectionPayload,
                             DynamicAttributeToken securityToken,
                             URI senderAgent) {
        this.message = new RejectionMessageBuilder()
                ._issuerConnector_(infrastructureComponent.getId())
                ._issued_(CalendarUtil.now())
                ._modelVersion_(infrastructureComponent.getOutboundModelVersion())
                ._correlationMessage_(originalMessage)
                ._rejectionReason_(rejectionReason)
                ._securityToken_(securityToken)
                ._senderAgent_(senderAgent)
                .build();
        this.payload = rejectionPayload;
    }

    public DefaultFailureMAP(URI issuerConnector,
                             String modelVersion,
                             URI originalMessage,
                             RejectionReason rejectionReason,
                             String rejectionPayload,
                             DynamicAttributeToken securityToken,
                             URI senderAgent)
    {
    	this.message = new RejectionMessageBuilder()
                ._issuerConnector_(issuerConnector)
                ._issued_(CalendarUtil.now())
                ._modelVersion_(modelVersion)
                ._correlationMessage_(originalMessage)
                ._rejectionReason_(rejectionReason)
                ._securityToken_(securityToken)
                ._senderAgent_(senderAgent)
                .build();
    	this.payload = rejectionPayload;
    }

    @Override
    public RejectionMessage getMessage() {
        return message;
    }

    @Override
    public Optional<String> getPayload() {
        return Optional.of(payload);
    }

    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(payload.getBytes(), "text/plain");
    }
}
