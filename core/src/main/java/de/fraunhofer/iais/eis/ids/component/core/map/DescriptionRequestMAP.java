package de.fraunhofer.iais.eis.ids.component.core.map;

import de.fraunhofer.iais.eis.DescriptionRequestMessage;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;

import java.util.Optional;

public class DescriptionRequestMAP implements MessageAndPayload<DescriptionRequestMessage, Void> {

    private DescriptionRequestMessage descriptionRequest;

    public DescriptionRequestMAP(DescriptionRequestMessage selfDescriptionRequest) {
        this.descriptionRequest = selfDescriptionRequest;
    }

    @Override
    public DescriptionRequestMessage getMessage() {
        return descriptionRequest;
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
