package de.fraunhofer.iais.eis.ids.connector.commons.broker.map;

import de.fraunhofer.iais.eis.QueryMessage;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;

import java.util.Optional;

public class QueryMAP implements MessageAndPayload<QueryMessage, String> {

    private final QueryMessage brokerQueryMessage;
    private final String queryString;

    public QueryMAP(QueryMessage brokerQueryMessage, String queryString) {
        this.brokerQueryMessage = brokerQueryMessage;
        this.queryString = queryString;
    }

    @Override
    public QueryMessage getMessage() {
        return brokerQueryMessage;
    }

    @Override
    public Optional<String> getPayload() {
        return Optional.of(queryString);
    }

    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(queryString.getBytes(), "text/plain");
    }
}
