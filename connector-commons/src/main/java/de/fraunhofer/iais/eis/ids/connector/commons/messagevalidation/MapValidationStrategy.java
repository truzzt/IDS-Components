package de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation;

import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;

public interface MapValidationStrategy<T extends MessageAndPayload> {

    void validate(T map) throws MapValidationException;

}
