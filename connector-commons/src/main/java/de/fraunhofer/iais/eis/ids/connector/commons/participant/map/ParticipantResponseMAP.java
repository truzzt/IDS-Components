package de.fraunhofer.iais.eis.ids.connector.commons.participant.map;

import de.fraunhofer.iais.eis.Participant;
import de.fraunhofer.iais.eis.ParticipantResponseMessage;
import de.fraunhofer.iais.eis.ParticipantResponseMessageBuilder;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;
import de.fraunhofer.iais.eis.ids.component.core.TokenRetrievalException;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;

import java.net.URI;
import java.util.Optional;

@Deprecated
public class ParticipantResponseMAP implements MessageAndPayload<ParticipantResponseMessage, Participant> {

    private final ParticipantResponseMessage responseMessage;
    private final Participant participant;

    public ParticipantResponseMAP(Participant participant, URI issuerConnector, String messageModelVersion , URI originalMessage, SecurityTokenProvider securityTokenProvider, URI responseSenderAgent) throws TokenRetrievalException {
        responseMessage = new ParticipantResponseMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._issuerConnector_(issuerConnector)
                ._correlationMessage_(originalMessage)
                ._modelVersion_(messageModelVersion)
                ._senderAgent_(responseSenderAgent)
                ._securityToken_(securityTokenProvider.getSecurityTokenAsDAT())
                .build();
        this.participant  = participant;
    }

    @Override
    public ParticipantResponseMessage getMessage() {
        return responseMessage;
    }

    @Override
    public Optional<Participant> getPayload() {
        return Optional.of(participant);
    }

    @Override
    public SerializedPayload serializePayload() {
        if(participant != null)
        {
            return new SerializedPayload(participant.toRdf().getBytes(), "application/ld+json");
        }
        return SerializedPayload.EMPTY;
    }
}
