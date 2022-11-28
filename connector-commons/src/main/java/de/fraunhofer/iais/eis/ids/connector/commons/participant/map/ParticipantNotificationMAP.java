package de.fraunhofer.iais.eis.ids.connector.commons.participant.map;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Participant;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;

import java.util.Optional;

public class ParticipantNotificationMAP implements MessageAndPayload<Message, Participant> {
    private final Message message;
    private Participant participantSelfDescription;
    public ParticipantNotificationMAP(Message message) {
        this.message = message;
    }

    public ParticipantNotificationMAP(Message message, Participant participantSelfDescription)
    {
        this.message = message;
        this.participantSelfDescription = participantSelfDescription;
    }
    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    //Make this nullable, as unregistering a participant does not contain a participant in the payload
    public Optional<Participant> getPayload() {
        return Optional.ofNullable(participantSelfDescription);
    }

    @Override
    public SerializedPayload serializePayload() {
        if (participantSelfDescription != null) {
            return new SerializedPayload(participantSelfDescription.toRdf().getBytes(), "application/ld+json");
        }
        else return SerializedPayload.EMPTY;
    }
}
