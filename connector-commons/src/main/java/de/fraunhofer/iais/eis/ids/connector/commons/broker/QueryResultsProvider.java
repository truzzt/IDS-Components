package de.fraunhofer.iais.eis.ids.connector.commons.broker;

import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;

public interface QueryResultsProvider {

    String getResults(String query) throws RejectMessageException;

}
