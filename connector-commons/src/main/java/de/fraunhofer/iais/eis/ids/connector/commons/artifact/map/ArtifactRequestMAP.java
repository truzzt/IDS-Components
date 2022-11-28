package de.fraunhofer.iais.eis.ids.connector.commons.artifact.map;

import de.fraunhofer.iais.eis.ArtifactRequestMessage;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;

import java.util.Optional;

public class ArtifactRequestMAP implements MessageAndPayload<ArtifactRequestMessage, Void> {

    private final ArtifactRequestMessage artifactRequestMessage;

    public ArtifactRequestMAP(ArtifactRequestMessage artifactRequestMessage) {
        this.artifactRequestMessage = artifactRequestMessage;
    }

    @Override
    public ArtifactRequestMessage getMessage() {
        return artifactRequestMessage;
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
