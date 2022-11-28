package de.fraunhofer.iais.eis.ids.component.client.broker;

import de.fraunhofer.iais.eis.BaseConnectorBuilder;
import de.fraunhofer.iais.eis.ResourceCatalogBuilder;
import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.ids.component.client.HTTPMultipartComponentInteractor;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.map.DefaultSuccessMAP;
import de.fraunhofer.iais.eis.util.Util;
import org.junit.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static de.fraunhofer.iais.eis.util.Util.asList;

public class RemoteBrokerTest {

    private RemoteBroker remote;

    @Before
    public void setUp() throws MalformedURLException {
        remote = new RemoteBroker(new HTTPMultipartComponentInteractor(new URL("http://localhost:8080")));
    }

    @Ignore
    @Test
    public void requestResponse() throws IOException, BrokerException, URISyntaxException {
        InfrastructureComponent connector = new BaseConnectorBuilder()
                ._resourceCatalog_(Util.asList(new ResourceCatalogBuilder().build()))
                ._maintainerAsUri_(new URL("http://example.org").toURI())
                ._curatorAsUri_(new URL("http://example.org").toURI())
                ._inboundModelVersion_(asList("3.0.0"))
                ._outboundModelVersion_("3.0.0")
                .build();
        //Update and register are the same from information model version 4.0.0 onwards
        MessageAndPayload response = remote.update(connector, null);
        Assert.assertTrue(response instanceof DefaultSuccessMAP);
    }

}
