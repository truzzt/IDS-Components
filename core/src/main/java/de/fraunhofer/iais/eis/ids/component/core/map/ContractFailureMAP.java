package de.fraunhofer.iais.eis.ids.component.core.map;

import de.fraunhofer.iais.eis.ContractRejectionMessageBuilder;
import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;
import de.fraunhofer.iais.eis.util.TypedLiteral;

import java.net.URI;

public class ContractFailureMAP extends DefaultFailureMAP {
    public ContractFailureMAP(InfrastructureComponent infrastructureComponent, URI originalMessage, String rejectionReason, String rejectionPayload, DynamicAttributeToken securityToken, URI senderAgent) {
        this.message = new ContractRejectionMessageBuilder()
                ._issuerConnector_(infrastructureComponent.getId())
                ._issued_(CalendarUtil.now())
                ._modelVersion_(infrastructureComponent.getOutboundModelVersion())
                ._correlationMessage_(originalMessage)
                ._contractRejectionReason_(new TypedLiteral(rejectionReason))
                ._securityToken_(securityToken)
                ._senderAgent_(senderAgent)
                .build();
        this.payload = rejectionPayload;
        //super(infrastructureComponent, originalMessage, rejectionReason, rejectionPayload, securityToken, senderAgent);
    }

    public ContractFailureMAP(URI issuerConnector, String modelVersion, URI originalMessage, String rejectionReason, String rejectionPayload, DynamicAttributeToken securityToken, URI senderAgent) {
        this.message = new ContractRejectionMessageBuilder()
                ._issuerConnector_(issuerConnector)
                ._issued_(CalendarUtil.now())
                ._modelVersion_(modelVersion)
                ._correlationMessage_(originalMessage)
                ._contractRejectionReason_(new TypedLiteral(rejectionReason))
                ._securityToken_(securityToken)
                ._senderAgent_(senderAgent)
                .build();
        this.payload = rejectionPayload;

        //super(issuerConnector, modelVersion, originalMessage, rejectionReason, rejectionPayload, securityToken, senderAgent);
    }
}
