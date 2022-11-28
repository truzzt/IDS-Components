package de.fraunhofer.iais.eis.ids.connector.commons.artifact;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.TokenRetrievalException;
import de.fraunhofer.iais.eis.ids.component.core.util.CalendarUtil;
import de.fraunhofer.iais.eis.ids.connector.commons.artifact.map.ArtifactRequestMAP;
import de.fraunhofer.iais.eis.ids.connector.commons.artifact.map.ArtifactResponseMAP;
import de.fraunhofer.iais.eis.util.Util;

import org.apache.commons.lang3.SystemUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

public class ArtifactHandlerTest {

    private final String connectorId = "http://example.org/connector";
    private final String modelVersion = "2.0.0";


    private static class InvocationCheckArtifactFileProvider implements ArtifactFileProvider {

        Optional<File> artifact;
        boolean artifactAvailable = true;

        @Override
        public Optional<File> getArtifact(URI artifactId) {
            if (!artifactAvailable) return Optional.empty();

            artifact = Optional.of(new File(artifactId));
            return artifact;
        }
    }

    private InfrastructureComponent createDummyComponent() throws MalformedURLException, URISyntaxException {
        return new BaseConnectorBuilder(new URL(connectorId).toURI())
                ._resourceCatalog_(Util.asList(new ResourceCatalogBuilder().build()))
                ._outboundModelVersion_(modelVersion)
                ._inboundModelVersion_(Util.asList(modelVersion))
                ._maintainerAsUri_(new URL("http://example.org/participant").toURI())
                ._curatorAsUri_(new URL("http://example.org/participant").toURI())
                ._securityProfile_(SecurityProfile.BASE_SECURITY_PROFILE)
                ._hasDefaultEndpoint_(new ConnectorEndpointBuilder()._accessURL_(URI.create("https://example.org/endpoint")).build())
                .build();
    }

    @Test
    public void artifactAvailable() throws MalformedURLException, RejectMessageException, URISyntaxException
    {
        URI requestedArtifactId;
        if(SystemUtils.IS_OS_WINDOWS) {
        	requestedArtifactId = new URL("file:/C:/someFile.txt").toURI();
        } else {
        	requestedArtifactId = new URL("file:///someFile.txt").toURI();
        }

        InvocationCheckArtifactFileProvider artifactFileProvider = new InvocationCheckArtifactFileProvider();
        ArtifactHandler artifactHandler = new ArtifactHandler(createDummyComponent(), artifactFileProvider, new SecurityTokenProvider() {
            @Override
            public String getSecurityToken() throws TokenRetrievalException {
                return "test1234";
            }
        }, new URI("http://example.org/"));


        ArtifactResponseMAP response = artifactHandler.handle(new ArtifactRequestMAP(createArtifactRequestMessage(requestedArtifactId)));

        //equivalent to response != null
        //Assert.assertTrue(response.getMessage() instanceof ArtifactResponseMessage);
        Assert.assertTrue(response.getPayload().isPresent());
        Assert.assertEquals(artifactFileProvider.artifact.get().toURI(), requestedArtifactId);
    }

    @Test
    public void artifactUnavailable() throws MalformedURLException, URISyntaxException {
        URI requestedArtifactId = new URL("file:///someFile.txt").toURI();

        InvocationCheckArtifactFileProvider artifactFileProvider = new InvocationCheckArtifactFileProvider();
        artifactFileProvider.artifactAvailable = false;
        ArtifactHandler artifactHandler = new ArtifactHandler(createDummyComponent(), artifactFileProvider, new SecurityTokenProvider() {
            @Override
            public String getSecurityToken() throws TokenRetrievalException {
                return "test1234";
            }
        }, new URI("http://example.org/"));

        try {
            artifactHandler.handle(new ArtifactRequestMAP(createArtifactRequestMessage(requestedArtifactId)));
            Assert.fail();
        }
        catch (RejectMessageException e) {
            Assert.assertEquals(e.getRejectionReason(), RejectionReason.NOT_FOUND);
        }
    }

    private ArtifactRequestMessage createArtifactRequestMessage(URI requestedArtifactId) throws MalformedURLException, URISyntaxException {
        return new ArtifactRequestMessageBuilder()
                ._issued_(CalendarUtil.now())
                ._issuerConnector_(new URL(connectorId).toURI())
                ._modelVersion_(modelVersion)
                ._requestedArtifact_(requestedArtifactId)
                ._securityToken_(new DynamicAttributeTokenBuilder()._tokenFormat_(TokenFormat.JWT)._tokenValue_("test1234").build())
                ._senderAgent_(new URI("http://example.org/"))
                .build();
    }

}
