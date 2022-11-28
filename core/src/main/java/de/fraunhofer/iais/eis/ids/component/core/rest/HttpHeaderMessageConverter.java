package de.fraunhofer.iais.eis.ids.component.core.rest;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class is used to wrap information from incoming write operations via REST (PUT, POST, DELETE)
 */
public class HttpHeaderMessageConverter {
    //PUT, POST, DELETE
    public String method;
    //Address on which operation is to be performed
    public URI target;
    //If method is PUT or POST, this contains the object to be deployed
    public String body;

    public XMLGregorianCalendar issued;

    public URI issuerConnector;

    public URI senderAgent;

    public String modelVersion;

    public DynamicAttributeToken securityToken;

    public ArrayList<URI> recipientAgent;

    public Token authorizationToken;

    public URI transferContract;

    public String contentVersion;

    public URI correlationMessage;

    public RejectionReason rejectionReason;

    HttpHeaderMessageConverter()
    {

    }

    public DescriptionResponseMessage getAsDescriptionResponseMessage()
    {
        return new DescriptionResponseMessageBuilder()
                ._issued_(issued)
                ._issuerConnector_(issuerConnector)
                ._senderAgent_(senderAgent)
                ._securityToken_(securityToken)
                ._recipientAgent_(recipientAgent)
                ._modelVersion_(modelVersion)
                ._authorizationToken_(authorizationToken)
                ._contentVersion_(contentVersion)
                ._transferContract_(transferContract)
                ._correlationMessage_(correlationMessage)
                .build();
    }

    public MessageProcessedNotificationMessage getAsMessageProcessedNotificationMessage()
    {
        return new MessageProcessedNotificationMessageBuilder()
                ._issued_(issued)
                ._issuerConnector_(issuerConnector)
                ._senderAgent_(senderAgent)
                ._securityToken_(securityToken)
                ._recipientAgent_(recipientAgent)
                ._modelVersion_(modelVersion)
                ._authorizationToken_(authorizationToken)
                ._contentVersion_(contentVersion)
                ._transferContract_(transferContract)
                ._correlationMessage_(correlationMessage)
                .build();
    }

    public RejectionMessage getAsRejectionMessage()
    {
        return new RejectionMessageBuilder()
                ._issued_(issued)
                ._issuerConnector_(issuerConnector)
                ._senderAgent_(senderAgent)
                ._securityToken_(securityToken)
                ._recipientAgent_(recipientAgent)
                ._modelVersion_(modelVersion)
                ._authorizationToken_(authorizationToken)
                ._contentVersion_(contentVersion)
                ._transferContract_(transferContract)
                ._correlationMessage_(correlationMessage)
                ._rejectionReason_(rejectionReason)
                .build();
    }


    /**
     * This function attempts to turns the class variables to an IDS Message construct
     * @return IDS Message object representing the received write operation
     */
    public Message getMessage() throws IOException {
        if(method == null)
        {
            throw new RuntimeException("Cannot guess correct message class to return without knowing the HTTP method used.");
        }
        if(target == null)
        {
            throw new RuntimeException("To turn this object into a write operation message, the target field is mandatory.");
        }
        String targetString = target.toString();

        //All read operations are based on DescriptionRequestMessages. The output is reduced though
        if(method.equalsIgnoreCase("GET") || method.equalsIgnoreCase("HEAD") || method.equalsIgnoreCase("OPTIONS"))
        {
            DescriptionRequestMessage message = new DescriptionRequestMessageBuilder()
                    ._issued_(issued)
                    ._issuerConnector_(issuerConnector)
                    ._senderAgent_(senderAgent)
                    ._securityToken_(securityToken)
                    ._recipientAgent_(recipientAgent)
                    ._modelVersion_(modelVersion)
                    ._authorizationToken_(authorizationToken)
                    ._contentVersion_(contentVersion)
                    ._transferContract_(transferContract)
                    ._requestedElement_(target)
                    .build();
            message.setProperty("ids:method", method);
            return message;
        }

        //This regex searches for (negative) numbers, followed either by a slash OR the end of the input
        //http://123.test.de/123/-546/12 would have 3 matches: "123/", "-546/", and "12$" ($ = end of line). The "123.test" does NOT match
        Matcher matcher = Pattern.compile("(-)?\\d\\d*(/|$)").matcher(targetString);
        int numMatches = 0;
        while(matcher.find())
        {
            numMatches++;
        }
        matcher = Pattern.compile(":\\d\\d*").matcher(targetString);
        //If string contains port, remove that from matches
        if(matcher.find())
        {
            numMatches--;
        }

        Logger logger = LoggerFactory.getLogger(getClass());
        logger.info("Num Regex matches: " + numMatches);

        if(method.equalsIgnoreCase("delete"))
        {
            if(numMatches == 1) //This means that the object in question is "at depth one", i.e. we are trying to sign off a connector. A resource would be much deeper down
            {
                return new ConnectorUnavailableMessageBuilder()
                        ._affectedConnector_(target)
                        ._issued_(issued)
                        ._issuerConnector_(issuerConnector)
                        ._senderAgent_(senderAgent)
                        ._securityToken_(securityToken)
                        ._recipientAgent_(recipientAgent)
                        ._modelVersion_(modelVersion)
                        ._authorizationToken_(authorizationToken)
                        ._contentVersion_(contentVersion)
                        ._transferContract_(transferContract)
                        .build();
            }
            else //Not a connector. Interpret this as resource.
            //TODO: If this is the URI of a non-connector and non-resource, this will yield a NOT_FOUND.
            // However, it might still be an existing entity in our triple store. In that case, we need to return a FORBIDDEN or something similar
            {
                return new ResourceUnavailableMessageBuilder()
                        ._affectedResource_(target)
                        ._issued_(issued)
                        ._issuerConnector_(issuerConnector)
                        ._senderAgent_(senderAgent)
                        ._securityToken_(securityToken)
                        ._recipientAgent_(recipientAgent)
                        ._modelVersion_(modelVersion)
                        ._authorizationToken_(authorizationToken)
                        ._contentVersion_(contentVersion)
                        ._transferContract_(transferContract)
                        .build();
            }
        }
        else //PUT or POST
        {
            if(numMatches < 2) //Either at the root endpoint OR at an existing connector
            {

                ConnectorUpdateMessage m = new ConnectorUpdateMessageBuilder()
                        ._affectedConnector_(new Serializer().deserialize(body, Connector.class).getId())
                        ._issued_(issued)
                        ._issuerConnector_(issuerConnector)
                        ._senderAgent_(senderAgent)
                        ._securityToken_(securityToken)
                        ._recipientAgent_(recipientAgent)
                        ._modelVersion_(modelVersion)
                        ._authorizationToken_(authorizationToken)
                        ._contentVersion_(contentVersion)
                        ._transferContract_(transferContract)
                        .build();
                m.setProperty("ids:method", method); //PUT or POST. In case of POST, we need to check whether this connector exists yet
                return m;
            }
            else
            {
                ResourceUpdateMessage m = new ResourceUpdateMessageBuilder()
                        ._affectedResource_(new Serializer().deserialize(body, Resource.class).getId())
                        ._issued_(issued)
                        ._issuerConnector_(issuerConnector)
                        ._senderAgent_(senderAgent)
                        ._securityToken_(securityToken)
                        ._recipientAgent_(recipientAgent)
                        ._modelVersion_(modelVersion)
                        ._authorizationToken_(authorizationToken)
                        ._contentVersion_(contentVersion)
                        ._transferContract_(transferContract)
                        .build();
                m.setProperty("ids:method", method); //PUT or POST. In case of POST, we need to check whether this connector exists yet
                return m;
            }
        }
    }
}
