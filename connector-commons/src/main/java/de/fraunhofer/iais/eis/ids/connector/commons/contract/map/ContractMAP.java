package de.fraunhofer.iais.eis.ids.connector.commons.contract.map;

import de.fraunhofer.iais.eis.Contract;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;

import java.util.Optional;

public abstract class ContractMAP<M extends Message, C extends Contract> implements MessageAndPayload<Message, Contract> {

    M contractMessage;
    C payload;



    @Override
    public Message getMessage() {
        return contractMessage;
    }

    @Override
    public Optional<Contract> getPayload() {
        return Optional.of(payload);
    }

    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(payload.toRdf().getBytes(), "application/ld+json", payload.getId().toString());
    }
}
