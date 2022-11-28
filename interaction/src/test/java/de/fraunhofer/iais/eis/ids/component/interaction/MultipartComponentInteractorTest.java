package de.fraunhofer.iais.eis.ids.component.interaction;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.*;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;
import de.fraunhofer.iais.eis.ids.component.interaction.multipart.Multipart;
import de.fraunhofer.iais.eis.ids.component.interaction.multipart.MultipartComponentInteractor;
import de.fraunhofer.iais.eis.ids.component.interaction.validation.ShaclValidator;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import de.fraunhofer.iais.eis.util.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class MultipartComponentInteractorTest {

    private MultipartComponentInteractor multipartComponentInteractor;

    @Before
    public void setUp() throws URISyntaxException {

        SelfDescriptionProvider selfDescriptionProvider = () -> new BaseConnectorBuilder()
                ._outboundModelVersion_("3.0.0")
                ._inboundModelVersion_(Util.asList("3.0.0"))
                ._curatorAsUri_(dummyUri())
                ._maintainerAsUri_(dummyUri())
                ._resourceCatalog_(Util.asList(new ResourceCatalogBuilder().build()))
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()._accessURL_(URI.create("https://example.org/endpoint")).build())
                .build();

        DefaultComponent component = new DefaultComponent(selfDescriptionProvider, new SecurityTokenProvider() {
            @Override
            public String getSecurityToken() {
                return "test1234";
            }
        }, new URI("http://example.org/"), true);
        multipartComponentInteractor = new MultipartComponentInteractor(component, new SecurityTokenProvider() {
            @Override
            public String getSecurityToken() throws TokenRetrievalException {
                return "test1234";
            }
        }, new URI("http://example.org/"),
                true);
    }

    @Test
    public void processMultipartWithoutPayload() throws IOException, URISyntaxException, RejectMessageException {
        DescriptionRequestMessage request = new DescriptionRequestMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._issuerConnector_(dummyUri())
                ._modelVersion_("3.0.0-SNAPSHOT")
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenValue_("test1234")._tokenFormat_(TokenFormat.JWT).build())
                ._senderAgent_(new URI("http://example.org"))
                .build();
        Serializer serializer = new Serializer();
        Multipart requestMultipart = new Multipart(serializer.serialize(request), "application/ld+json");

        Multipart responseMultipart = multipartComponentInteractor.process(requestMultipart, RequestType.INFRASTRUCTURE);
        MessageAndPayload responseMap = responseMultipart.toMap();

        Assert.assertTrue(responseMap.getMessage() instanceof DescriptionResponseMessage);
        Assert.assertTrue(responseMap.getPayload().isPresent());
        Assert.assertTrue(responseMap.getPayload().get() instanceof String);
    }

    private URI dummyUri() {
        try {
            return new URL("http://example.org/").toURI();
        }
        catch (MalformedURLException | URISyntaxException e) {
            return null;
        }
    }

    @Test
    public void shaclValidationOnRubbishStringTest() {
        boolean ioExceptionThrown = false;
        try {
            ShaclValidator.validateRdf("This is a rubbish string and no JSON-LD");
        }
        catch (IOException e)
        {
            ioExceptionThrown = true;
        }
        Assert.assertTrue(ioExceptionThrown);
    }

}
