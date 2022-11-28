package de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import org.junit.Assert;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;

public class JWKSFromIssuerTest {

    private String dapsToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRlZmF1bHQifQ.eyJzZWN1cml0eV9sZXZlbCI6" +
                                "Nywic2NvcGVzIjpbImlkc19jb25uZWN0b3IiLCJzZWN1cml0eV9sZXZlbCJdLCJhdWQiOiJJRFNfQ29ubmV" +
                                "jdG9yIiwiaXNzIjoiaHR0cHM6Ly9kYXBzLmFpc2VjLmZyYXVuaG9mZXIuZGUiLCJzdWIiOiI0OWZhOTgxNS" +
                                "05NTU1LTQwMzgtOGE5YS00ZTM2ZGUzN2JmNDUiLCJuYmYiOjE1NTU0OTgyNTcsImV4cCI6MTU1NTUwMTg1N" +
                                "30.HqVOMy7S9FjuOCV_bod-P43oMaNl0h45HaIVaozHJywMPS2LJ5khIg_lRriYOZL6dJJhT36CFLnyW5u0" +
                                "f5o6UsITHrc8drLP1jqPH5Sns6B_VpL8GIBUMNTAnN-I4l4KRNbq1duGkFF7QvNctdXY9TO7mgkeDZ-Vvo_" +
                                "XJceutyYFM_hB7FXu9-HtCAVRxrTwzamBH-S0bIu77Ep2eSlwdPYwDH8U8aKTxRFHheJ1scprH5sTRlBTLU" +
                                "Ekb51j6kkql0IJXXQRuF_3zm4gBlCFmyEJOUkpvZ5Pg5WYcj4W4t_M2H32sdTuX85mhxGib-6zhlpG4RKU2" +
                                "e7U7gmpWU5mIQ";

    @Test
    public void jsonWebKeySet_fromTrusted() throws JWKSException, URISyntaxException {
        JWKSFromIssuer jwksFromIssuer = new JWKSFromIssuer(Arrays.asList("daps.aisec.fraunhofer.de"));
        JWKSource wks = jwksFromIssuer.getJsonWebKeySet(dapsToken);

        Assert.assertTrue(wks instanceof RemoteJWKSet);
        Assert.assertEquals("https://daps.aisec.fraunhofer.de/.well-known/jwks.json",
                ((RemoteJWKSet) wks).getJWKSetURL().toString());
    }

    @Test(expected = JWKSException.class)
    public void jsonWebKeySet_fromUntrusted() throws JWKSException {
        JWKSFromIssuer jwksFromIssuer = new JWKSFromIssuer(Collections.emptyList());
        jwksFromIssuer.getJsonWebKeySet(dapsToken);
    }

    @Test(expected = JWKSException.class)
    public void jsonWebKeySet_fromEmptyToken() throws JWKSException, URISyntaxException {
        JWKSFromIssuer jwksFromIssuer = new JWKSFromIssuer(Arrays.asList("daps.aisec.fraunhofer.de"));
        jwksFromIssuer.getJsonWebKeySet("");
    }

}
