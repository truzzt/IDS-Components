package de.fraunhofer.iais.eis.ids.connector.commons.broker;

import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;

import java.io.IOException;
import java.net.URI;

public interface InfrastructureComponentStatusHandler {

    void unavailable(URI issuerConnector) throws IOException, RejectMessageException;
    //Return (possibly altered) URI of the registered infrastructure component
    URI updated(InfrastructureComponent selfDescription) throws IOException, RejectMessageException;

}
