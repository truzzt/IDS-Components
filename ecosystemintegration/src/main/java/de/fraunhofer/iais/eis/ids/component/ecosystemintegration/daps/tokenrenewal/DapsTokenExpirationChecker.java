package de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps.tokenrenewal;

import com.nimbusds.jwt.JWTParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.util.Date;

public class DapsTokenExpirationChecker implements DapsTokenRenewalChecker {
    private final Logger logger = LoggerFactory.getLogger(DapsTokenExpirationChecker.class);
    @Override
    public boolean needsRenewal(String dapsToken) throws ParseException {
        try {
            Date expirationTime = JWTParser.parse(dapsToken).getJWTClaimsSet().getExpirationTime();
            return expirationTime.before(new Date());
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            throw e;
        }
    }
}
