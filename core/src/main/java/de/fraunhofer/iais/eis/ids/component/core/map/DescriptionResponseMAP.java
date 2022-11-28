package de.fraunhofer.iais.eis.ids.component.core.map;

import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.DescriptionResponseMessage;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;

import java.util.Optional;

public class DescriptionResponseMAP implements MessageAndPayload<DescriptionResponseMessage, String> {

    private DescriptionResponseMessage response;
    private String payload;

    public DescriptionResponseMAP(DescriptionResponseMessage response, String payload) {
        this.response = response;
        this.payload = payload;
    }

    @Override
    public DescriptionResponseMessage getMessage() {
        return response;
    }

    @Override
    public Optional<String> getPayload() {
        return Optional.of(payload);
    }

    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(payload.getBytes(), "application/ld+json");
    }

}
