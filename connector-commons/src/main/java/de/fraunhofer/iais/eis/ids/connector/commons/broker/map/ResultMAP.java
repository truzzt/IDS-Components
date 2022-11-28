package de.fraunhofer.iais.eis.ids.connector.commons.broker.map;

import de.fraunhofer.iais.eis.ResultMessage;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;

import java.util.Optional;

public class ResultMAP implements MessageAndPayload<ResultMessage, String> {

    private final ResultMessage resultMessage;
    private final String queryResult;

    public ResultMAP(ResultMessage resultMessage, String queryResult) {
        this.resultMessage = resultMessage;
        this.queryResult = queryResult;
    }

    @Override
    public ResultMessage getMessage() {
        return resultMessage;
    }

    @Override
    public Optional<String> getPayload() {
        return Optional.of(queryResult);
    }

    @Override
    public SerializedPayload serializePayload() {
        return new SerializedPayload(queryResult.getBytes(), "text/plain");
    }
}
