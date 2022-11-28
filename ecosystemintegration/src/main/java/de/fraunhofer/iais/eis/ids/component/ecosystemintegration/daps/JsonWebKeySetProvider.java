package de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

public interface JsonWebKeySetProvider {

    JWKSource getJsonWebKeySet(String jwt) throws JWKSException;

}
