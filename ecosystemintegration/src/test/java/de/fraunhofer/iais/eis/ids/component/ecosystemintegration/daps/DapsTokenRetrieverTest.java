package de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

public class DapsTokenRetrieverTest {

    private InputStream keyStore;
    private String keyStorePwd, keyStoreAlias;

    @Before
    public void setUp() {
        keyStorePwd = System.getProperty("connectorKeyStorePwd");
        keyStoreAlias = System.getProperty("connectorKeyStoreAlias");
        keyStore = getClass().getClassLoader().getResourceAsStream("isstbroker-keystore.jks");
    }

    @Test //comment out this test if DAPS is down
    @Ignore // TODO activate test again then the IAIS IDS Identity Certificate is accepted by the DAPS again
    public void retrieveTokenFromExternal() throws GeneralSecurityException, IOException {
        //String connectorUUID = "49fa9815-9555-4038-8a9a-4e36de37bf45";
        String dapsUrl = "https://daps.aisec.fraunhofer.de/v2/token";

        DapsTokenRetriever dapsTokenRetriever = new DapsTokenRetriever(keyStore, keyStorePwd, keyStoreAlias);
        dapsTokenRetriever.setSslIgnoreHostName(true);
        dapsTokenRetriever.setSslTrustAllCerts(true);
        String token = dapsTokenRetriever.retrieveToken(dapsUrl);
        Assert.assertFalse(token.isEmpty());
    }

}
