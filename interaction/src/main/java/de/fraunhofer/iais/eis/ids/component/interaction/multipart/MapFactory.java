package de.fraunhofer.iais.eis.ids.component.interaction.multipart;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;
import de.fraunhofer.iais.eis.ids.component.core.map.*;
import de.fraunhofer.iais.eis.ids.connector.commons.app.map.AppMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.artifact.map.ArtifactRequestMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.artifact.map.ArtifactResponseMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.map.QueryMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.contract.map.*;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.map.InfrastructureComponentMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.participant.map.ParticipantNotificationMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.participant.map.ParticipantRequestMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.resource.map.ResourceMAP;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.apache.commons.io.FileUtils;
import org.apache.jena.riot.RiotException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class MapFactory {

    private static MapFactory instance;

    private MapFactory() {
    }

    public static MapFactory getInstance() {
        if (instance == null) instance = new MapFactory();
        return instance;
    }

    public MessageAndPayload<?, ?> createMap(Message header, SerializedPayload payload) throws IOException, RejectMessageException {
        // core message types
        if (header instanceof DescriptionRequestMessage) {
            return new DescriptionRequestMAP((DescriptionRequestMessage) header);
        } else if (header instanceof DescriptionResponseMessage) {
            return new DescriptionResponseMAP(
                    (DescriptionResponseMessage) header,
                    new String(payload.getSerialization()));
        } else if (header instanceof MessageProcessedNotificationMessage) {
            return new DefaultSuccessMAP(header.getIssuerConnector(), header.getModelVersion(), header.getCorrelationMessage(), header.getSecurityToken(), header.getSenderAgent());
        } else if (header instanceof RejectionMessage) {
            if (header instanceof ContractRejectionMessage) {
                return new ContractRejectionMAP((ContractRejectionMessage) header);
            }
            return new DefaultFailureMAP(header.getIssuerConnector(),
                    header.getModelVersion(),
                    header.getCorrelationMessage(),
                    ((RejectionMessage) header).getRejectionReason(), new String(payload.getSerialization()), header.getSecurityToken(), header.getSenderAgent());
        }

        // artifact-related message types
        else if (header instanceof ArtifactRequestMessage) {
            return new ArtifactRequestMAP((ArtifactRequestMessage) header);
        } else if (header instanceof ArtifactResponseMessage) {
            File artifactTmp = Files.createTempFile("tmp", payload.getFilename()).toFile();
            FileUtils.writeByteArrayToFile(artifactTmp, payload.getSerialization());

            return new ArtifactResponseMAP((ArtifactResponseMessage) header, artifactTmp);
        }

        // broker-related messages
        else if (header instanceof ConnectorUpdateMessage) {
            return new InfrastructureComponentMAP(header, payloadAsInfrastructureComponent(payload));
        } else if (header instanceof ConnectorUnavailableMessage) {
            return new InfrastructureComponentMAP(header);
        } else if (header instanceof QueryMessage) {
            return new QueryMAP((QueryMessage) header, new String(payload.getSerialization()));
        } else if (header instanceof ResourceUpdateMessage) {
            return new ResourceMAP(header, payloadAsResource(payload));
        } else if (header instanceof ResourceUnavailableMessage) {
            return new ResourceMAP(header);
        }

        // participant-related messages
        else if (header instanceof ParticipantUpdateMessage) {
            return new ParticipantNotificationMAP(header, payloadAsParticipant(payload));
        } else if (header instanceof ParticipantUnavailableMessage) {
            return new ParticipantNotificationMAP(header);
        } else if (header instanceof ParticipantRequestMessage) {
            return new ParticipantRequestMAP((ParticipantRequestMessage) header);
        }

        // contract-related messages
        else if (header instanceof ContractOfferMessage) {
            return new ContractOfferMAP((ContractOfferMessage) header, (ContractOfferImpl) payloadAsContract(payload));
        } else if (header instanceof ContractRequestMessage) {
            return new ContractRequestMAP((ContractRequestMessage) header, (ContractRequest) payloadAsContract(payload));
        } else if (header instanceof ContractAgreementMessage) {
            return new ContractAgreementMAP((ContractAgreementMessage) header, (ContractAgreement) payloadAsContract(payload));
        } else if (header instanceof ContractResponseMessage) {
            return new ContractResponseMAP((ContractResponseMessage) header, (ContractOffer) payloadAsContract(payload));
        }

        //App related messages
        else if(header instanceof AppNotificationMessage)
        {
            if(header instanceof AppAvailableMessage) {
                return new AppMAP(header, (AppResource) payloadAsResource(payload));
            }
            if(header instanceof AppUnavailableMessage) {
                return new AppMAP(header);
            }

        }

        throw new IOException("Could not convert input header to suitable message and payload type. Header: " + header.toRdf());
        //return new NullMAP();
    }

    private InfrastructureComponent payloadAsInfrastructureComponent(SerializedPayload payload) throws IOException, RejectMessageException {
        try {
            String infrastructureComponentSerialization = new String(payload.getSerialization());
            return new Serializer().deserialize(
                    infrastructureComponentSerialization,
                    InfrastructureComponent.class);
        } catch (NullPointerException e) {
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE, new NullPointerException("No payload present (required)"));
        }
        catch (RiotException e) {
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE, new NullPointerException("No valid payload present (required)"));
        }
    }

    private Participant payloadAsParticipant(SerializedPayload payload) throws IOException, RejectMessageException {
        try {
            String participantSerialization = new String(payload.getSerialization());
            return new Serializer().deserialize(
                    participantSerialization,
                    Participant.class);
        } catch (NullPointerException e) {
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE, new NullPointerException("No payload present (required)"));
        }
        catch (RiotException e) {
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE, new NullPointerException("No valid payload present (required)"));
        }
    }

    private Resource payloadAsResource(SerializedPayload payload) throws IOException, RejectMessageException {
        try {
            String resourceSerialization = new String(payload.getSerialization());
            return new Serializer().deserialize(resourceSerialization, Resource.class);
        } catch (NullPointerException e) {
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE, new NullPointerException("No payload present (required)"));
        }
        catch (RiotException e) {
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE, new NullPointerException("No valid payload present (required)"));
        }
    }

    private Contract payloadAsContract(SerializedPayload payload) throws IOException, RejectMessageException {
        try {
            String serialization = new String(payload.getSerialization());
            return new Serializer().deserialize(serialization, Contract.class);
        } catch (NullPointerException e) {
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE, new NullPointerException("No payload present (required)"));
        }
        catch (RiotException e) {
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE, new NullPointerException("No valid payload present (required)"));
        }
    }
}
