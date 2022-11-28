package de.fraunhofer.iais.eis.ids.connector.commons.broker;

import de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation.MapValidationException;
import de.fraunhofer.iais.eis.ids.connector.commons.participant.map.ParticipantNotificationMAP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SameOriginParticipantMapValidationStrategy extends SameOriginMapValidationStrategy<ParticipantNotificationMAP> {
private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //TODO
    @Override
    public void validate(ParticipantNotificationMAP map) throws MapValidationException {
        logger.warn("SameOriginValidation not yet implemented for Participants");
    }
}