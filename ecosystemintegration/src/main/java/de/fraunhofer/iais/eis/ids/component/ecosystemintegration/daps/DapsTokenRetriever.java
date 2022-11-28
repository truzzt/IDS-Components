package de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.Util;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.net.ssl.*;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;

public class DapsTokenRetriever {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ObjectMapper mapper = new ObjectMapper();
    private KeyStore keyStore;
    //Subject Key Identifier in Keystore always has the default Object ID
    private static final String SKI_OID = "2.5.29.14";
    //Authority Key Identifier in Keystore always has the default Object ID
    private static final String AKI_OID = "2.5.29.35";

    private InputStream keyStoreStream;
    private String keyStorePassword;
    private String keystoreAliasName;
    private boolean sslIgnoreHostName, sslTrustAllCerts;
    private Key privateKey;
    private X509Certificate certificate;
    private final String grantType;

    //Note: The Orbiter DAPS takes a different grantType than the AISEC DAPS
    public DapsTokenRetriever(Key privateKey, X509Certificate certificate, String grantType)
    {
        this.privateKey = privateKey;
        this.certificate = certificate;
        this.grantType = grantType;
    }

    public DapsTokenRetriever(Key privateKey, X509Certificate certificate)
    {
        this.privateKey = privateKey;
        this.certificate = certificate;
        this.grantType = "client_credentials";
    }

    public DapsTokenRetriever(InputStream keyStoreStream,
                          String keyStorePassword,
                          String keystoreAliasName)
    {
        this.keyStoreStream = keyStoreStream;
        this.keyStorePassword = keyStorePassword;
        this.keystoreAliasName = keystoreAliasName;
        this.grantType = "client_credentials";
    }

    public DapsTokenRetriever(InputStream keyStoreStream,
                              String keyStorePassword,
                              String keystoreAliasName,
                              String grantType)
    {
        this.keyStoreStream = keyStoreStream;
        this.keyStorePassword = keyStorePassword;
        this.keystoreAliasName = keystoreAliasName;
        this.grantType = grantType;
    }
    public void setSslIgnoreHostName(boolean sslIgnoreHostName) {
        this.sslIgnoreHostName = sslIgnoreHostName;
    }

    public void setSslTrustAllCerts(boolean sslTrustAllCerts) {
        this.sslTrustAllCerts = sslTrustAllCerts;
    }

    /**
     * Method to acquire a Dynamic Attribute Token (DAT) from a Dynamic Attribute Provisioning Service (DAPS)
     */
    public String retrieveToken(String dapsUrl) throws GeneralSecurityException, IOException
    {
        Key privKey;
        if(privateKey != null)
        {
            privKey = privateKey;
        }
        else {
            privKey = extractPrivateKeyFromStore();
        }
        String bearerToken = createBearerToken(privKey);
        String accessToken = acquireAccessTokenFromDaps(bearerToken, dapsUrl, grantType);
        //In some cases, the DAPS will simply return an error message, despite correct credentials
        if(accessToken.length() < 200 && accessToken.contains("error")) {
            throw new IOException("Received error response from DAPS: " + accessToken);
        }
        return mapper.readTree(accessToken).at("/access_token").asText();
    }

    private Key extractPrivateKeyFromStore()
            throws GeneralSecurityException, IOException
    {
        if(keyStore == null) {
            keyStore = KeyStore.getInstance("JKS");
            keyStore.load(keyStoreStream, keyStorePassword.toCharArray());
        }
        return keyStore.getKey(keystoreAliasName, keyStorePassword.toCharArray());
    }

    private String createBearerToken(Key privKey) throws KeyStoreException {
        //fetch SKI and AKI
        String ski = getKeyIdentifiers(SKI_OID);
        String aki = getKeyIdentifiers(AKI_OID);
        String uniqueID = ski.concat(":keyid:").concat(aki);

        Date expiryDate = Date.from(Instant.now().plusSeconds(86400));
        JwtBuilder jwtb = Jwts.builder()
                .setIssuer(uniqueID)
                .setSubject(uniqueID)
                .claim("@context", "https://w3id.org/idsa/contexts/context.jsonld")
                .claim("@type", "ids:DatRequestToken")
                .setExpiration(expiryDate)
                .setIssuedAt(Date.from(Instant.now()))
                .setAudience("idsc:IDS_CONNECTORS_ALL")
                .setNotBefore(Date.from(Instant.now().minusMillis(25)));

        return jwtb.signWith(SignatureAlgorithm.RS256, privKey).compact();
    }

    private String getKeyIdentifiers(String oid) throws KeyStoreException {
        //fetch AKI and SKI (source: https://stackoverflow.com/a/31183447)
        SubjectKeyIdentifier subjectKeyIdentifier;
        AuthorityKeyIdentifier authorityKeyIdentifier;
        String keyIdentifier = null;

        X509Certificate cert;
        if(certificate != null)
        {
            cert = certificate;
        }
        else {
            cert = (X509Certificate) keyStore.getCertificate(keystoreAliasName);
        }
        byte[] keyIdentifierOctets = DEROctetString.getInstance(cert.getExtensionValue(oid)).getOctets();
        if(oid.equals(SKI_OID)) {
            subjectKeyIdentifier = SubjectKeyIdentifier.getInstance(keyIdentifierOctets);
            keyIdentifier = new String(Hex.encode(subjectKeyIdentifier.getKeyIdentifier()));
        }
        else if(oid.equals(AKI_OID)) {
            authorityKeyIdentifier = AuthorityKeyIdentifier.getInstance(keyIdentifierOctets);
            keyIdentifier = new String(Hex.encode(authorityKeyIdentifier.getKeyIdentifier()));
        }
        String modifiedKeyIdentifier = keyIdentifier.toUpperCase().replaceAll("..", "$0:");
        return modifiedKeyIdentifier.substring(0, modifiedKeyIdentifier.length()-1);
    }

    private String acquireAccessTokenFromDaps(String bearerToken, String dapsUrl, String grantType) throws IOException, KeyStoreException {
        OkHttpClient client = new OkHttpClient.Builder()
                .sslSocketFactory(getSocketFactory(), getTrustManager())
                .hostnameVerifier(getHostnameVerifier())
                //.addInterceptor(new LoggingInterceptor()) //verbose - only uncomment for debugging
                .build();

        String ski = getKeyIdentifiers(SKI_OID);
        String aki = getKeyIdentifiers(AKI_OID);
        String uniqueID = ski.concat(":keyid:").concat(aki);

        FormBody formBody = new FormBody.Builder()
                .add("grant_type", grantType)
                .add("client_assertion_type", "urn:ietf:params:oauth:client-assertion-type:jwt-bearer")
                .add("client_assertion", bearerToken)
                .add("scope", "idsc:IDS_CONNECTOR_ATTRIBUTES_ALL")
                .add("client_id", uniqueID)
                .build();
        Request request = new Request.Builder().url(dapsUrl).addHeader("Content-Type", "application/x-www-form-urlencoded").post(formBody).build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful() && response.body() != null) {
            return response.body().string();
        }
        else {
            throw new IOException("Unable to retrieve DAPS token (response code: " +response.code()+ ")"+" \n Response: "+response.body().string());
        }
    }

    private SSLSocketFactory getSocketFactory() {
        try {
            if (sslTrustAllCerts) {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                TrustManager[] trustAllCerts = new TrustManager[1];
                trustAllCerts[0] = getTrustManager();
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                return sslContext.getSocketFactory();
            }
        }
        catch (KeyManagementException | NoSuchAlgorithmException e) {
            logger.error("Error creating SSL factory", e);
        }
        return new OkHttpClient().sslSocketFactory();
    }

    private X509TrustManager getTrustManager() {
        if (sslTrustAllCerts) {
            return new TrustAllCertsTrustManager();
        }
        return Util.platformTrustManager();
    }

    private HostnameVerifier getHostnameVerifier() {
        if (sslIgnoreHostName) {
            return (x, y) -> true;
        }
        return new OkHttpClient().hostnameVerifier();
    }

}
