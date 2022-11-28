package de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps;

import de.fraunhofer.iais.eis.ids.component.core.SecurityTokenProvider;
import de.fraunhofer.iais.eis.ids.component.core.TokenRetrievalException;
import de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps.tokenrenewal.DapsTokenRenewalChecker;
import de.fraunhofer.iais.eis.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.util.List;

public class DapsSecurityTokenProvider extends SecurityTokenProvider {

    private final Logger logger = LoggerFactory.getLogger(DapsSecurityTokenProvider.class);
    private final DapsTokenRetriever dapsTokenRetriever;
    private final String dapsUrl;
    private String securityToken;
    private boolean forceTokenRefresh;
    private final List<DapsTokenRenewalChecker> renewalCheckers;

    public DapsSecurityTokenProvider(PrivateKey privateKey,
                                     X509Certificate certificate,
                                     String dapsUrl,
                                     String grantType,
                                     boolean sslTrustAllCerts,
                                     boolean sslIgnoreHostName,
                                     DapsTokenRenewalChecker... renewalCheckers)
    {
        dapsTokenRetriever = new DapsTokenRetriever(privateKey, certificate, grantType);
        dapsTokenRetriever.setSslTrustAllCerts(sslTrustAllCerts);
        dapsTokenRetriever.setSslIgnoreHostName(sslIgnoreHostName);
        this.dapsUrl = dapsUrl;
        this.renewalCheckers = Util.asList(renewalCheckers);
    }

    public DapsSecurityTokenProvider(PrivateKey privateKey,
                                     X509Certificate certificate,
                                     String dapsUrl,
                                     boolean sslTrustAllCerts,
                                     boolean sslIgnoreHostName,
                                     DapsTokenRenewalChecker... renewalCheckers)
    {
        dapsTokenRetriever = new DapsTokenRetriever(privateKey, certificate);
        dapsTokenRetriever.setSslTrustAllCerts(sslTrustAllCerts);
        dapsTokenRetriever.setSslIgnoreHostName(sslIgnoreHostName);
        this.dapsUrl = dapsUrl;
        this.renewalCheckers = Util.asList(renewalCheckers);
    }

    /**
     * @deprecated The connectorUUID is not required to request a DAT. Use the constructor without that parameter
     */
    @Deprecated
    public DapsSecurityTokenProvider(InputStream keyStore,
                                     String keyStorePassword,
                                     String keystoreAliasName,
                                     String connectorUUID,
                                     String dapsUrl,
                                     boolean sslTrustAllCerts,
                                     boolean sslIgnoreHostName, DapsTokenRenewalChecker... renewalCheckers)
    {
        dapsTokenRetriever = new DapsTokenRetriever(keyStore, keyStorePassword, keystoreAliasName);
        dapsTokenRetriever.setSslTrustAllCerts(sslTrustAllCerts);
        dapsTokenRetriever.setSslIgnoreHostName(sslIgnoreHostName);
        this.dapsUrl = dapsUrl;
        this.renewalCheckers = Util.asList(renewalCheckers);
    }

    public DapsSecurityTokenProvider(InputStream keyStore,
                                     String keyStorePassword,
                                     String keystoreAliasName,
                                     String dapsUrl,
                                     boolean sslTrustAllCerts,
                                     boolean sslIgnoreHostName, DapsTokenRenewalChecker... renewalCheckers)
    {
        dapsTokenRetriever = new DapsTokenRetriever(keyStore, keyStorePassword, keystoreAliasName);
        dapsTokenRetriever.setSslTrustAllCerts(sslTrustAllCerts);
        dapsTokenRetriever.setSslIgnoreHostName(sslIgnoreHostName);
        this.dapsUrl = dapsUrl;
        this.renewalCheckers = Util.asList(renewalCheckers);
    }

    @Override
    public String getSecurityToken() throws TokenRetrievalException {
        try {
            logger.debug("Fetching security token");
            if (securityToken == null || forceTokenRefresh || renewalRequired()) {
                logger.info("Token needs to be fetched anew from DAPS");
                try {
                    securityToken = dapsTokenRetriever.retrieveToken(dapsUrl);
                } catch (Exception e) {
                    throw new TokenRetrievalException("Unable to retrieve DAPS token.", e);
                }
            }
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            throw new TokenRetrievalException("Failed to parse own security token.", e);
        }
        return securityToken;
    }

    private boolean renewalRequired() throws ParseException {
        for(DapsTokenRenewalChecker renewalChecker : renewalCheckers)
        {
            if(renewalChecker.needsRenewal(securityToken)) {
                return true;
            }
        }
        return false;
    }

    public void setForceTokenRefresh(boolean forceTokenRefresh) {
        this.forceTokenRefresh = forceTokenRefresh;
    }
}
