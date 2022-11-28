package de.fraunhofer.iais.eis.ids.connector.commons.artifact.map;

import de.fraunhofer.iais.eis.ArtifactResponseMessage;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Optional;

public class ArtifactResponseMAP implements MessageAndPayload<ArtifactResponseMessage, File> {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ArtifactResponseMessage artifactResponseMessage;
    private final File payload;

    public ArtifactResponseMAP(ArtifactResponseMessage artifactResponseMessage, File payload) {
        this.artifactResponseMessage = artifactResponseMessage;
        this.payload = payload;
    }

    @Override
    public ArtifactResponseMessage getMessage() {
        return artifactResponseMessage;
    }

    @Override
    public Optional<File> getPayload() {
        return Optional.of(payload);
    }

    @Override
    public SerializedPayload serializePayload() {
        try {
            return new SerializedPayload(Files.readAllBytes(payload.toPath()), "application/octet-stream", payload.getName());
        }
        catch (IOException e) {
            logger.error("Could not serialize file", e);
            return SerializedPayload.EMPTY;
        }
    }

}
