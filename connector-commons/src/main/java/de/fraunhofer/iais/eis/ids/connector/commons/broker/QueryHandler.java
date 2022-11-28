package de.fraunhofer.iais.eis.ids.connector.commons.broker;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.MessageHandler;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.TokenRetrievalException;
import de.fraunhofer.iais.eis.ids.component.core.logging.MessageLogger;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.map.QueryMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.map.ResultMAP;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Collection;
import java.util.Collections;

public class QueryHandler implements MessageHandler<QueryMAP, ResultMAP> {

    //private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final InfrastructureComponent infrastructureComponent;
    private final QueryResultsProvider queryResultsProvider;
    private final SecurityTokenProvider securityTokenProvider;
    private final URI responseSenderUri;

    public QueryHandler(InfrastructureComponent infrastructureComponent, QueryResultsProvider queryResultsProvider, SecurityTokenProvider securityTokenProvider, URI responseSenderUri)
    {
        this.infrastructureComponent = infrastructureComponent;
        this.queryResultsProvider = queryResultsProvider;
        this.securityTokenProvider = securityTokenProvider;
        this.responseSenderUri = responseSenderUri;
    }

    @Override
    public ResultMAP handle(QueryMAP messageAndPayload) throws RejectMessageException {

        MessageLogger.logMessage(messageAndPayload, false);
        try {
        ResultMessage result = new ResultMessageBuilder()
                ._issuerConnector_(infrastructureComponent.getId())
                ._issued_(CalendarUtil.now())
                ._modelVersion_(infrastructureComponent.getOutboundModelVersion())
                ._correlationMessage_(messageAndPayload.getMessage().getId())
                ._securityToken_(securityTokenProvider.getSecurityTokenAsDAT())
                ._senderAgent_(responseSenderUri)
                .build();
            if(!messageAndPayload.getPayload().isPresent() || messageAndPayload.getPayload().get().isEmpty())
            {
                throw new RejectMessageException(RejectionReason.BAD_PARAMETERS, new NullPointerException("QueryMessage received without payload. The actual query string needs to be in the payload."));
            }
            return new ResultMAP(result, queryResultsProvider.getResults(messageAndPayload.getPayload().get()));
        }
        //Malformed query?
        catch (TokenRetrievalException e) {
            throw new RejectMessageException(RejectionReason.INTERNAL_RECIPIENT_ERROR, e);
        }
    }

    @Override
    public Collection<Class<? extends Message>> getSupportedMessageTypes() {
        return Collections.singletonList(QueryMessage.class);
    }

}
