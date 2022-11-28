package de.fraunhofer.iais.eis.ids.component.core;

import de.fraunhofer.iais.eis.DynamicAttributeToken;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.Token;
import de.fraunhofer.iais.eis.util.TypedLiteral;

import javax.validation.constraints.NotNull;
import javax.xml.datatype.XMLGregorianCalendar;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class DummyMessage implements Message {

    private DynamicAttributeToken securityToken;
    private Token authorizationToken;
	private String contentVersion;

    @Override
    public @NotNull URI getId() {
        return null;
    }

    @Override
    public List<TypedLiteral> getLabel() {
        return null;
    }

    @Override
    public List<TypedLiteral> getComment() {
        return null;
    }

    @Override
    public String toRdf() {
        return null;
    }

    @Override
    public Map<String, Object> getProperties() {
        return null;
    }

    @Override
    public void setProperty(String property, Object value) {

    }

    @Override
    public Message deepCopy() {
        return null;
    }

    @Override
    public String getModelVersion() {
        return null;
    }

    @Override
    public void setModelVersion(String s) {

    }

    @Override
    public XMLGregorianCalendar getIssued() {
        return null;
    }

    @Override
    public void setIssued(XMLGregorianCalendar xmlGregorianCalendar) {

    }

    @Override
    public URI getIssuerConnector() {
        return null;
    }

    @Override
    public void setIssuerConnector(URI uri) {

    }

    @Override
    public ArrayList<URI> getRecipientConnector() {
        return null;
    }

    @Override
    public void setRecipientConnector(List<URI> list) {

    }

    @Override
    public ArrayList<URI> getRecipientAgent() {
        return null;
    }

    @Override
    public void setRecipientAgent(List<URI> list) {

    }

    @Override
    public URI getCorrelationMessage() {
        return null;
    }

    @Override
    public void setCorrelationMessage(URI uri) {

    }

    @Override
    public URI getSenderAgent() {
        return null;
    }

    @Override
    public void setSenderAgent(URI uri) {

    }

    @Override
    public DynamicAttributeToken getSecurityToken() {
        return securityToken;
    }

    public void setSecurityToken(DynamicAttributeToken securityToken) {
        this.securityToken = securityToken;
    }

    @Override
    public Token getAuthorizationToken() {
		return authorizationToken;
    }
    
    public void setAuthorizationToken(Token authorizationToken) {
        this.authorizationToken = authorizationToken;
    }

    @Override
    public URI getTransferContract() {
        return null;
    }

    @Override
    public void setTransferContract(URI uri) {

    }

    @Override
	public String getContentVersion() {
		return this.contentVersion;
	}
	
	public void setContentVersion(String contentVersion) {
		this.contentVersion = contentVersion;
	}
}
