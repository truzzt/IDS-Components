package de.fraunhofer.iais.eis.ids.component.client.broker;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.client.RemoteComponentInteractor;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.map.DefaultSuccessMAP;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;
import de.fraunhofer.iais.eis.ids.component.core.RequestType;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.map.InfrastructureComponentMAP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RemoteBroker {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private RemoteComponentInteractor remoteComponentInteractor;

    public RemoteBroker(RemoteComponentInteractor remoteComponentInteractor) {
        this.remoteComponentInteractor = remoteComponentInteractor;
    }


    public MessageAndPayload deregister(InfrastructureComponent connector, DynamicAttributeToken daps) throws BrokerException, IOException {
        Message connectorUnavailable = new ConnectorUnavailableMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._issuerConnector_(connector.getId())
                ._modelVersion_(connector.getOutboundModelVersion())
                ._senderAgent_(connector.getMaintainerAsUri())
                ._securityToken_(daps)
                ._affectedConnector_(connector.getId())
                .build();
        return issueRequest(connectorUnavailable, connector, BrokerOperation.UNREGISTER);
    }

    public MessageAndPayload update(InfrastructureComponent connector, DynamicAttributeToken daps) throws BrokerException, IOException {
        Message connectorChange = new ConnectorUpdateMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._issuerConnector_(connector.getId())
                ._modelVersion_(connector.getOutboundModelVersion())
                ._senderAgent_(connector.getMaintainerAsUri())
                ._securityToken_(daps)
                ._affectedConnector_(connector.getId())
                .build();
        return issueRequest(connectorChange, connector, BrokerOperation.UPDATE);
    }

    private MessageAndPayload issueRequest(Message message, InfrastructureComponent source, BrokerOperation operation)
        throws BrokerException, IOException
    {
        MessageAndPayload map = new InfrastructureComponentMAP(message, source);
        MessageAndPayload response = remoteComponentInteractor.process(map, RequestType.INFRASTRUCTURE);
        if (!(response instanceof DefaultSuccessMAP)) {
            throw new BrokerException("Error during remote broker operation " +operation.name(), response);
        }

        logger.info("Success for remote broker operation " +operation.name());
        return response;
    }

    private enum BrokerOperation {
        REGISTER, UNREGISTER, UPDATE
    }

}
