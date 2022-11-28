package de.fraunhofer.iais.eis.ids.component.core;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.map.DescriptionRequestMAP;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;
import de.fraunhofer.iais.eis.util.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.xml.datatype.XMLGregorianCalendar;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class EchoHandlerTest {
    private MessageDispatcher dispatcher;

    @Before
    public void setUp() throws MalformedURLException, URISyntaxException {
        InfrastructureComponent infrastructureComponent = new BaseConnectorBuilder()
                ._maintainerAsUri_(new URL("http://example.org").toURI())
                ._curatorAsUri_(new URL("http://example.org").toURI())
                ._outboundModelVersion_("3.0.0")
                ._inboundModelVersion_(Util.asList("3.0.0"))
                ._resourceCatalog_(Util.asList(new ResourceCatalogBuilder().build()))
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()._accessURL_(URI.create("https://example.org/endpoint")).build())
                .build();
        dispatcher = new MessageDispatcher(infrastructureComponent);
        dispatcher.addMessageHandler(new EchoHandler(), RequestType.ECHO); // normally added via DefaultComponent automatically
    }


    @Test
    public void testEchoHandler() throws MalformedURLException, URISyntaxException {
        XMLGregorianCalendar timestamp = CalendarUtil.now();
        DescriptionRequestMessage selfDescriptionRequest = new DescriptionRequestMessageBuilder()
                ._issued_(timestamp)
                ._issuerConnector_(new URL("http://example.org/").toURI())
                ._modelVersion_("3.0.0")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenFormat_(TokenFormat.JWT)._tokenValue_("abcd1234").build())
                ._senderAgent_(new URI("http://example.org/"))
                .build();
        MessageAndPayload response = dispatcher.dispatch(new DescriptionRequestMAP(selfDescriptionRequest), RequestType.ECHO);
        Assert.assertTrue(response.getMessage() instanceof DescriptionRequestMessage);
        Assert.assertEquals(timestamp, response.getMessage().getIssued());
    }
}
