package de.fraunhofer.iais.eis.ids.connector.commons.participant.map;

import de.fraunhofer.iais.eis.ParticipantRequestMessage;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;

import java.util.Optional;

@Deprecated
public class ParticipantRequestMAP implements MessageAndPayload<ParticipantRequestMessage, Void> {
        private ParticipantRequestMessage message;
        public ParticipantRequestMAP(ParticipantRequestMessage message) {
            this.message = message;
        }

        @Override
        public ParticipantRequestMessage getMessage() {
            return message;
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
