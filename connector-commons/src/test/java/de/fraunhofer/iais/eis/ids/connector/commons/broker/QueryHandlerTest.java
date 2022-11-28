package de.fraunhofer.iais.eis.ids.connector.commons.broker;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;
import de.fraunhofer.iais.eis.ids.connector.commons.TestUtil;
import de.fraunhofer.iais.eis.ids.connector.commons.broker.map.QueryMAP;
import de.fraunhofer.iais.eis.util.TypedLiteral;
import de.fraunhofer.iais.eis.util.Util;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static de.fraunhofer.iais.eis.util.Util.asList;

public class QueryHandlerTest {

    private final String queryString = "queryString";
    private final InfrastructureComponent broker = new BrokerBuilder(TestUtil.dummyUri())
            ._title_(asList(new TypedLiteral("EIS Broker", "en")))
            ._description_(asList(new TypedLiteral("A semantic impl for demonstration purposes", "en")))
            ._maintainerAsUri_(TestUtil.dummyUri())
            ._curatorAsUri_(TestUtil.dummyUri())
            ._inboundModelVersion_(Util.asList("3.0.0"))
            ._outboundModelVersion_("3.0.0")
            ._resourceCatalog_(Util.asList(new ResourceCatalogBuilder().build()))
            ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
            ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()._accessURL_(URI.create("https://example.org/endpoint")).build())
            .build();

    @Test
    public void query() throws MalformedURLException, RejectMessageException, URISyntaxException {
        QueryMessage brokerQueryMessage = new QueryMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._modelVersion_("3.0.0")
                ._issuerConnector_(new URL("https://some.connector.com").toURI())
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("test1234")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(new URI("http://example.org"))
                .build();

        QueryHandler queryHandler = new QueryHandler(broker, (query) -> {
            Assert.assertEquals(queryString, query);
            return "";
        }, new SecurityTokenProvider() {
            @Override
            public String getSecurityToken() {
                return "test1234";
            }
        }, new URI("http://example.org/"));

        queryHandler.handle(new QueryMAP(brokerQueryMessage, queryString));
    }

}
