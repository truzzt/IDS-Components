package de.fraunhofer.iais.eis.ids.connector.commons.broker;

import de.fraunhofer.iais.eis.ConnectorUpdateMessage;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.map.InfrastructureComponentMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation.MapValidationException;


import java.net.URI;

@Deprecated
/**
 * Up to date implementation inside broker
 */
public class SameOriginInfrastructureComponentMapValidationStrategy extends SameOriginMapValidationStrategy<InfrastructureComponentMAP> {
    @Override
    public void validate(InfrastructureComponentMAP map) throws MapValidationException {
        Message msg = map.getMessage();

            if (msg instanceof ConnectorUpdateMessage) {
                URI issuer = msg.getIssuerConnector();
                if (!map.getPayload().filter(component -> component.getId().equals(issuer)).isPresent()) {
                    throw new MapValidationException("The issuing connector of this message does not match the connector URI in the payload, violating Same Origin policy.");
                }
            }
    }
}
