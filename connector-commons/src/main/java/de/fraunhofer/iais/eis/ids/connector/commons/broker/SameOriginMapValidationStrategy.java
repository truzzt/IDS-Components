package de.fraunhofer.iais.eis.ids.connector.commons.broker;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation.MapValidationException;
import de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation.MapValidationStrategy;


public abstract class SameOriginMapValidationStrategy<T extends MessageAndPayload<? extends Message, ?>> implements MapValidationStrategy<T> {
    //private final Logger logger = LoggerFactory.getLogger(this.getClass());

    abstract public void validate(T map) throws MapValidationException;

}
