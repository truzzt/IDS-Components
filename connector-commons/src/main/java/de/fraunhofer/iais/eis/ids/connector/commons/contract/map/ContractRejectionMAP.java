package de.fraunhofer.iais.eis.ids.connector.commons.contract.map;

import de.fraunhofer.iais.eis.ContractRejectionMessage;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;

import java.util.Optional;

public class ContractRejectionMAP implements MessageAndPayload<ContractRejectionMessage, Void> {

    private final ContractRejectionMessage contractRejectionMessage;

    public ContractRejectionMAP(ContractRejectionMessage contractRejectionMessage) {
        this.contractRejectionMessage = contractRejectionMessage;
    }

    @Override
    public ContractRejectionMessage getMessage() {
        return contractRejectionMessage;
    }

    @Override
    public Optional<Void> getPayload() {
        return Optional.empty();
    }

    @Override
    public SerializedPayload serializePayload() {
        return SerializedPayload.EMPTY;
    }
}
