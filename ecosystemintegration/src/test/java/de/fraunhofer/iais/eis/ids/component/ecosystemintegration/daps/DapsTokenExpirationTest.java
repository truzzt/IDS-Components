package de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps;

import de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps.tokenrenewal.DapsTokenExpirationChecker;
import io.jsonwebtoken.Jwts;
import org.junit.Assert;
import org.junit.Test;

import java.text.ParseException;
import java.util.Date;

public class DapsTokenExpirationTest {

    @Test
    public void expiredTokenYieldsExpired() {
        String expiredToken = generateTestToken(System.currentTimeMillis() - 10000);
        try {
            Assert.assertTrue(new DapsTokenExpirationChecker().needsRenewal(expiredToken));
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void validTokenYieldsValid() {
        String validToken = generateTestToken(System.currentTimeMillis() + 10000);
        try {
            Assert.assertFalse(new DapsTokenExpirationChecker().needsRenewal(validToken));
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            Assert.fail();
        }
    }


    private String generateTestToken(long expirationDate) {
        return Jwts.builder()
                .setIssuer("IDS_components")
                .setSubject("IDS_components")
                .setExpiration(new Date(expirationDate))
                .setAudience("https://api.localhost")
                .setNotBefore(new Date(expirationDate - 10000))
                .compact();
    }
}
