package de.fraunhofer.iais.eis.ids.component.core;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.map.DescriptionRequestMAP;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;
import de.fraunhofer.iais.eis.util.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class MessageDispatcherTest {

    private MessageDispatcher dispatcher;

    @Before
    public void setUp() throws MalformedURLException, URISyntaxException {
        InfrastructureComponent dummyInfrastructureComponent = new BaseConnectorBuilder()
                ._maintainerAsUri_(new URL("http://example.org").toURI())
                ._curatorAsUri_(new URL("http://example.org").toURI())
                ._outboundModelVersion_("3.0.0")
                ._inboundModelVersion_(Util.asList("3.0.0"))
                ._resourceCatalog_(Util.asList(new ResourceCatalogBuilder().build()))
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()._accessURL_(URI.create("https://example.org/endpoint")).build())
                .build();
        dispatcher = new MessageDispatcher(dummyInfrastructureComponent);
    }

    @Test
    public void dispatchWithHandlerAvailable() {
        DummyMessageHandler dummyMessageHandler = new DummyMessageHandler();
        dispatcher.addMessageHandler(dummyMessageHandler, RequestType.DATA);

        MessageAndPayload response = dispatcher.dispatch(new DummyMAP(new DummyMessage()), RequestType.DATA);

        Assert.assertNotNull(response);
    }

    @Test(expected = MissingHandlerException.class)
    public void dispatchWithHandlerUnavailable() {
        MessageAndPayload artifactAvailableMAP = new MessageAndPayload<ResourceUpdateMessage, Void>() {

            @Override
            public ResourceUpdateMessage getMessage() {
                try {
                    return new ResourceUpdateMessageBuilder()
                            ._issued_(CalendarUtil.now())
                            ._modelVersion_("3.0.0")
                            ._issuerConnector_(new URL("https://some.connector.com").toURI())
                            ._affectedResource_(new ResourceBuilder(new URI("https://some.connector.com/demoArtifact")).build().getId())
                            ._securityToken_(new DynamicAttributeTokenBuilder()._tokenFormat_(TokenFormat.JWT)._tokenValue_("abcd1234").build())
                            ._senderAgent_(new URI("http://example.org/"))
                            .build();
                } catch (MalformedURLException | URISyntaxException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public Optional<Void> getPayload() {
                return Optional.empty();
            }

            @Override
            public SerializedPayload serializePayload() {
                return SerializedPayload.EMPTY;
            }
        };

        dispatcher.dispatch(artifactAvailableMAP, RequestType.DATA);
    }

    //This test cannot be performed anymore, as the building of messages without a token throws an exception
    /*@Test
    public void addSecurityTokenToResult() {
        String securityToken = "someToken";

        DummyMessageHandler dummyMessageHandler = new DummyMessageHandler();

        dispatcher.addMessageHandler(dummyMessageHandler, RequestType.DATA);
        dispatcher.setSecurityTokenProvider(new SecurityTokenProvider() {
            @Override
            public String getSecurityToken() {
                return securityToken;
            }
        });

        MessageAndPayload result = dispatcher.dispatch(new DummyMAP(new DummyMessage()), RequestType.DATA);

        Assert.assertEquals(securityToken, result.getMessage().getSecurityToken().getTokenValue());
    }*/

    @Test
    public void retrieveSelfDescription() throws MalformedURLException, URISyntaxException {
        DescriptionRequestMessage selfDescriptionRequest = new DescriptionRequestMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._issuerConnector_(new URL("http://example.org/").toURI())
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenFormat_(TokenFormat.JWT)._tokenValue_("abcd1234").build())
                ._modelVersion_("3.0.0")
                ._senderAgent_(new URI("http://example.org/"))
                .build();

        InfrastructureComponent payload = new BaseConnectorBuilder()
                ._maintainerAsUri_(new URL("http://example.org").toURI())
                ._curatorAsUri_(new URL("http://example.org").toURI())
                ._inboundModelVersion_(Util.asList("3.0.0"))
                ._outboundModelVersion_("3.0.0")
                ._resourceCatalog_(Util.asList(new ResourceCatalogBuilder().build()))
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()._accessURL_(URI.create("https://example.org/endpoint")).build())
                .build();

        dispatcher.addMessageHandler(new SelfDescriptionRetrievalHandler(() -> payload, new SecurityTokenProvider() {
            @Override
            public String getSecurityToken() throws TokenRetrievalException {
                return "test1234";
            }
        }, new URI("http://example.org/")), RequestType.DATA);
        MessageAndPayload result = dispatcher.dispatch(new DescriptionRequestMAP(selfDescriptionRequest), RequestType.DATA);

        Assert.assertTrue(result.getMessage() instanceof DescriptionResponseMessage);
        Assert.assertEquals(payload.toRdf(), result.getPayload().get());
    }

}
