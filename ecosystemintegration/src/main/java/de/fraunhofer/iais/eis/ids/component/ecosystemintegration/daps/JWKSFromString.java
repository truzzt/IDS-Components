package de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;

import java.text.ParseException;

public class JWKSFromString implements JsonWebKeySetProvider {

    private final String jwks;

    public JWKSFromString(String jwks) {
        this.jwks = jwks;
    }

    @Override
    public JWKSource getJsonWebKeySet(String jwt) throws JWKSException {
        try {
            JWKSet jwkset = JWKSet.parse(jwks);
            return new ImmutableJWKSet(jwkset);
        }
        catch (ParseException e) {
            throw new JWKSException("Error parsing provided JWKS string");
        }
    }

}
