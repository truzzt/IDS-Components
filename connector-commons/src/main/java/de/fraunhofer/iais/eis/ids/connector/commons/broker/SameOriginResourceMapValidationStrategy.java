package de.fraunhofer.iais.eis.ids.connector.commons.broker;


import de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation.MapValidationException;
import de.fraunhofer.iais.eis.ids.connector.commons.resource.map.ResourceMAP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SameOriginResourceMapValidationStrategy extends SameOriginMapValidationStrategy<ResourceMAP> {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    //TODO
    @Override
    public void validate(ResourceMAP map) throws MapValidationException {
        logger.warn("SameOriginValidation not yet implemented for Resources");
    }
}
