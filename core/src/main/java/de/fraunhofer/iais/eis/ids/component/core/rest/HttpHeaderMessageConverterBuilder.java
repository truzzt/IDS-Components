package de.fraunhofer.iais.eis.ids.component.core.rest;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.Token;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;

import javax.xml.datatype.XMLGregorianCalendar;
import java.net.URI;
import java.util.ArrayList;

/**
 * Builder class for HttpHeaderMessageConverter objects
 */
public class HttpHeaderMessageConverterBuilder {
    //PUT, POST, DELETE
    private String method;
    //Address on which operation is to be performed
    private URI target;
    //If method is PUT or POST, this contains the object to be deployed
    private String body;

    private XMLGregorianCalendar issued;

    private URI issuerConnector;

    private URI senderAgent;

    private String modelVersion;

    private DynamicAttributeToken securityToken;

    private ArrayList<URI> recipientAgent;

    private Token authorizationToken;

    private URI transferContract;

    private String contentVersion;

    private URI correlationMessage;

    private RejectionReason rejectionReason;



    public HttpHeaderMessageConverterBuilder method(String method) {
        this.method = method;
        return this;
    }

    public HttpHeaderMessageConverterBuilder target(URI target) {
        this.target = target;
        return this;
    }

    public HttpHeaderMessageConverterBuilder body(String body) {
        this.body = body;
        return this;
    }

    public HttpHeaderMessageConverterBuilder issued(XMLGregorianCalendar issued) {
        this.issued = issued;
        return this;
    }

    public HttpHeaderMessageConverterBuilder issuerConnector(URI issuerConnector) {
        this.issuerConnector = issuerConnector;
        return this;
    }

    public HttpHeaderMessageConverterBuilder senderAgent(URI senderAgent) {
        this.senderAgent = senderAgent;
        return this;
    }

    public HttpHeaderMessageConverterBuilder modelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        return this;
    }

    public HttpHeaderMessageConverterBuilder securityToken(DynamicAttributeToken securityToken) {
        this.securityToken = securityToken;
        return this;
    }

    public HttpHeaderMessageConverterBuilder recipientAgent(ArrayList<URI> recipientAgent) {
        this.recipientAgent = recipientAgent;
        return this;
    }

    public HttpHeaderMessageConverterBuilder authorizationToken(Token authorizationToken) {
        this.authorizationToken = authorizationToken;
        return this;
    }

    public HttpHeaderMessageConverterBuilder transferContract(URI transferContract) {
        this.transferContract = transferContract;
        return this;
    }

    public HttpHeaderMessageConverterBuilder contentVersion(String contentVersion) {
        this.contentVersion = contentVersion;
        return this;
    }

    public HttpHeaderMessageConverterBuilder correlationMessage(URI correlationMessage) {
        this.correlationMessage = correlationMessage;
        return this;
    }

    public HttpHeaderMessageConverterBuilder rejectionReason(RejectionReason rejectionReason) {
        this.rejectionReason = rejectionReason;
        return this;
    }

    //Default Constructor
    public HttpHeaderMessageConverterBuilder(){}

    public HttpHeaderMessageConverter build() throws RejectMessageException {
        HttpHeaderMessageConverter result = new HttpHeaderMessageConverter();
        //Three mandatory fields not relating to headers
        result.method = method;
        result.target = target;
        result.body = body;
        //notNullNotEmpty(method);
        //notNullNotEmpty(target, "target");
        //notNullNotEmpty(body, "body");

        //More mandatory fields extracted from IDS-LDP headers
        result.securityToken = securityToken;
        result.issued = issued;
        result.correlationMessage = correlationMessage;
        result.senderAgent = senderAgent;
        result.modelVersion = modelVersion;
        result.issuerConnector = issuerConnector;
        notNullNotEmpty(securityToken, "securityToken");
        notNullNotEmpty(issued, "issued");
        notNullNotEmpty(senderAgent, "senderAgent");
        notNullNotEmpty(modelVersion, "modelVersion");
        notNullNotEmpty(issuerConnector, "issuerConnector");

        //Other fields are optional
        result.recipientAgent = recipientAgent;
        result.authorizationToken = authorizationToken;
        result.transferContract = transferContract;
        result.contentVersion = contentVersion;

        result.rejectionReason = rejectionReason;

        return result;


    }

    private void notNullNotEmpty(Object o, String fieldName) throws RejectMessageException {
        if(o == null || o.toString().equals(""))
        {
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE, new NullPointerException("Missing mandatory header: " + fieldName));
        }
    }

}
