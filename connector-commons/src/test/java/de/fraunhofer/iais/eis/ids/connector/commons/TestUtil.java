package de.fraunhofer.iais.eis.ids.connector.commons;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class TestUtil {

    public static URI dummyUri() {
        try {
            return new URL("http://example.org/dummy").toURI();
        }
        catch (MalformedURLException | URISyntaxException e) {
            return null;
        }
    }

}
