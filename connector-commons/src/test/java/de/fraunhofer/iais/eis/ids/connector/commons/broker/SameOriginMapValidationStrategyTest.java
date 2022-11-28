package de.fraunhofer.iais.eis.ids.connector.commons.broker;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.TokenRetrievalException;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;
import de.fraunhofer.iais.eis.ids.connector.commons.TestUtil;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.map.InfrastructureComponentMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation.MapValidationException;
import de.fraunhofer.iais.eis.util.PlainLiteral;
import de.fraunhofer.iais.eis.util.Util;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;

import static de.fraunhofer.iais.eis.util.Util.asList;

public class SameOriginMapValidationStrategyTest {
    
    private SameOriginInfrastructureComponentMapValidationStrategy strategy = new SameOriginInfrastructureComponentMapValidationStrategy();

    private SecurityTokenProvider securityTokenProvider = new SecurityTokenProvider() {
        @Override
        public String getSecurityToken() {
            return "test1234";
        }
    };
    URI testUri = TestUtil.dummyUri();
    private final Message connectorAvailable = new ConnectorUpdateMessageBuilder()
            ._issued_(CalendarUtil.now())
            ._modelVersion_("3.0.0")
            ._issuerConnector_(testUri)
            ._affectedConnector_(testUri)
            ._securityToken_(securityTokenProvider.getSecurityTokenAsDAT())
            ._senderAgent_(testUri)
            .build();


    public SameOriginMapValidationStrategyTest() throws TokenRetrievalException {
    }

    @Test(expected = MapValidationException.class )
    public void registration_issuerMismatch() throws MapValidationException, MalformedURLException, URISyntaxException {
        InfrastructureComponent otherConnector = new BaseConnectorBuilder(new URL("http://some.other.co/nnector").toURI())
                ._title_(new ArrayList<>(Collections.singletonList(new PlainLiteral("DWD Open Data Connector"))))
                ._curatorAsUri_(TestUtil.dummyUri())
                ._maintainerAsUri_(TestUtil.dummyUri())
                ._outboundModelVersion_("3.0.0")
                ._inboundModelVersion_(asList("3.0.0"))
                ._resourceCatalog_(Util.asList(new ResourceCatalogBuilder().build()))
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()._accessURL_(URI.create("https://example.org/endpoint")).build())
                .build();

        strategy.validate(new InfrastructureComponentMAP(connectorAvailable, otherConnector));
    }


    @Test
    public void registration_issuerMatch() throws MapValidationException {
        InfrastructureComponent otherConnector = new BaseConnectorBuilder(TestUtil.dummyUri())
                ._title_(new ArrayList<>(Collections.singletonList(new PlainLiteral("DWD Open Data Connector"))))
                ._curatorAsUri_(TestUtil.dummyUri())
                ._maintainerAsUri_(TestUtil.dummyUri())
                ._outboundModelVersion_("3.0.0")
                ._inboundModelVersion_(asList("3.0.0"))
                ._resourceCatalog_(Util.asList(new ResourceCatalogBuilder().build()))
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()._accessURL_(URI.create("https://example.org/endpoint")).build())
                .build();

        strategy.validate(new InfrastructureComponentMAP(connectorAvailable, otherConnector));
    }

}
