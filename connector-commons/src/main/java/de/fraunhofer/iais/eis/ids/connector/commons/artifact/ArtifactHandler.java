package de.fraunhofer.iais.eis.ids.connector.commons.artifact;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.MessageHandler;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.TokenRetrievalException;
import de.fraunhofer.iais.eis.ids.component.core.logging.MessageLogger;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;
import de.fraunhofer.iais.eis.ids.connector.commons.artifact.map.ArtifactRequestMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.artifact.map.ArtifactResponseMAP;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

/**
 * Replaced inside EI-Connector with more recent implementation
 */
@Deprecated
public class ArtifactHandler implements MessageHandler<ArtifactRequestMAP, ArtifactResponseMAP> {

    private final InfrastructureComponent infrastructureComponent;
    private final ArtifactFileProvider artifactFileProvider;
    private final SecurityTokenProvider securityTokenProvider;
    private final URI responseSenderUri;

    public ArtifactHandler(InfrastructureComponent infrastructureComponent,
                           ArtifactFileProvider artifactFileProvider,
                           SecurityTokenProvider securityTokenProvider,
                           URI responseSenderUri)
    {
        this.infrastructureComponent = infrastructureComponent;
        this.artifactFileProvider = artifactFileProvider;
        this.securityTokenProvider = securityTokenProvider;
        this.responseSenderUri = responseSenderUri;
    }

    @Override
    public ArtifactResponseMAP handle(ArtifactRequestMAP messageAndPayload) throws RejectMessageException {

        MessageLogger.logMessage(messageAndPayload, false);
        Optional<File> artifact = artifactFileProvider.getArtifact(messageAndPayload.getMessage().getRequestedArtifact());
        try {
            ArtifactResponseMessage artifactResponseMessage = new ArtifactResponseMessageBuilder()
                    ._issuerConnector_(infrastructureComponent.getId())
                    ._issued_(CalendarUtil.now())
                    ._modelVersion_(infrastructureComponent.getOutboundModelVersion())
                    ._correlationMessage_(messageAndPayload.getMessage().getId())
                    ._securityToken_(securityTokenProvider.getSecurityTokenAsDAT())
                    ._senderAgent_(responseSenderUri)
                    .build();

            return artifact.map(file -> new ArtifactResponseMAP(artifactResponseMessage, file))
                    .orElseThrow(() -> new RejectMessageException(RejectionReason.NOT_FOUND, new Exception("The requested file could not be found")));
        }
        catch (TokenRetrievalException e)
        {
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }
    }

    @Override
    public Collection<Class<? extends Message>> getSupportedMessageTypes() {
        return Collections.singletonList(ArtifactRequestMessage.class);
    }

}
