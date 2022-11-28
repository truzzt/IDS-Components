package de.fraunhofer.iais.eis.ids.component.protocol.http.server;

import de.fraunhofer.iais.eis.*;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;
import de.fraunhofer.iais.eis.ids.component.core.RequestType;
import de.fraunhofer.iais.eis.ids.component.core.rest.HttpHeaderMessageConverter;
import de.fraunhofer.iais.eis.ids.component.core.rest.HttpHeaderMessageConverterBuilder;
import de.fraunhofer.iais.eis.ids.component.interaction.multipart.Multipart;
import de.fraunhofer.iais.eis.ids.component.interaction.multipart.MultipartComponentInteractor;
import de.fraunhofer.iais.eis.ids.component.interaction.rest.RestComponentInteractor;
import de.fraunhofer.iais.eis.ids.jsonld.Serializer;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
public class ComponentController {

    @Autowired
    private ComponentInteractorProvider componentInteractorProvider;

    private final ResponseEntityCreator responseEntityCreator = new ResponseEntityCreator();
    private MultipartComponentInteractor multipartComponentInteractor;
    private RestComponentInteractor restComponentInteractor;
    private boolean isParIS;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @PostConstruct
    private void setUp() {
        multipartComponentInteractor = (MultipartComponentInteractor) componentInteractorProvider.getComponentInteractor();
        restComponentInteractor = new RestComponentInteractor(multipartComponentInteractor.getComponent(), multipartComponentInteractor.getSecurityTokenProvider(), multipartComponentInteractor.getResponseSenderAgent(), multipartComponentInteractor.getPerformShaclValidation());

        try {
            Connector c = new Serializer().deserialize(restComponentInteractor.getSelfDescription(), Connector.class);
            isParIS = c instanceof ParIS;
        }
        catch (IOException e)
        {
            logger.warn("Failed to parse own self description.", e);
            isParIS = false;
        }
    }

    //To keep the "/connectors/**" endpoint, we add this for backward compatibility
    @RequestMapping(path = "/connectors/**", method = {RequestMethod.GET, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}, produces = {"application/ld+json", "text/turtle", "application/n-triples", "application/rdf+xml"})
    public ResponseEntity<String> restCommunicationLegacy(@RequestHeader HttpHeaders headers, HttpServletRequest request)
    {
        if(isParIS)
        {
            return ResponseEntity.notFound().build();
        }
        return restCommunication(headers, request);
    }

    @RequestMapping(path = "/catalog/**", method = {RequestMethod.GET, RequestMethod.HEAD, RequestMethod.OPTIONS, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE}, produces = {"application/ld+json", "text/turtle", "application/n-triples", "application/rdf+xml"})
    public ResponseEntity<String> restCommunication(@RequestHeader HttpHeaders headers, HttpServletRequest request) {

        if(headers.containsKey("requestedelement") || headers.containsKey("ids-requestedelement"))
        {
            return new ResponseEntity<>("requestedElement header provided to request a specific element. Use URI pattern instead", HttpStatus.BAD_REQUEST);
        }

        String value = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

        String endpointUri = multipartComponentInteractor.getComponentUri().toString();
        if(endpointUri.endsWith("/"))
            endpointUri = endpointUri.substring(0, endpointUri.length() - 1);
        endpointUri = endpointUri + value;

        try {
            //Kick off with mandatory fields
            HttpHeaderMessageConverterBuilder httpHeaderMessageConverterBuilder = new HttpHeaderMessageConverterBuilder()
                    .method(request.getMethod())
                    .issued(DatatypeFactory.newInstance().newXMLGregorianCalendar(getFirstOrThrow(headers, "ids-issued")))
                    .issuerConnector(new URI(getFirstOrThrow(headers, "ids-issuerconnector")))
                    .senderAgent(new URI(getFirstOrThrow(headers, "ids-senderagent")))
                    .modelVersion(getFirstOrThrow(headers, "ids-modelversion"))
                    .securityToken(new DynamicAttributeTokenBuilder()._tokenFormat_(TokenFormat.JWT)._tokenValue_(getFirstOrThrow(headers, "ids-securitytoken")).build())
                    .target(new URI(endpointUri));

            //DescriptionRequestMessageBuilder descriptionRequestMessageBuilder = new DescriptionRequestMessageBuilder();

            if (headers.containsKey("ids-recipientagent") && headers.get("ids-recipientagent") != null) {
                //descriptionRequestMessageBuilder._recipientAgent_((ArrayList<? extends URI>) Objects.requireNonNull(headers.get("ids-recipientagent")).stream().map(URI::create).collect(Collectors.toCollection(ArrayList<URI>::new)));
                httpHeaderMessageConverterBuilder.recipientAgent(Objects.requireNonNull(headers.get("ids-recipientagent")).stream().map(URI::create).collect(Collectors.toCollection(ArrayList<URI>::new)));
            }

            if (headers.containsKey("ids-authorizationtoken")) {
                //descriptionRequestMessageBuilder._authorizationToken_(new TokenBuilder()._tokenValue_(Objects.requireNonNull(headers.get("ids-authorizationtoken")).get(0)).build());
                httpHeaderMessageConverterBuilder.authorizationToken(new TokenBuilder()._tokenValue_(Objects.requireNonNull(headers.get("ids-authorizationtoken")).get(0)).build());
            }

            if (headers.containsKey("ids-transfercontract")) {
                //descriptionRequestMessageBuilder._transferContract_(URI.create(Objects.requireNonNull(headers.get("ids-transfercontract")).get(0)));
                httpHeaderMessageConverterBuilder.transferContract(URI.create(Objects.requireNonNull(headers.get("ids-transfercontract")).get(0)));
            }

            if (headers.containsKey("ids-contentversion")) {
                //descriptionRequestMessageBuilder._contentVersion_(Objects.requireNonNull(headers.get("ids-contentversion")).get(0));
                httpHeaderMessageConverterBuilder.contentVersion(Objects.requireNonNull(headers.get("ids-contentversion")).get(0));
            }

            if (headers.containsKey("ids-correlationmessage")) {
                //descriptionRequestMessageBuilder._correlationMessage_(URI.create(Objects.requireNonNull(headers.get("ids-correlationmessage")).get(0)));
                httpHeaderMessageConverterBuilder.correlationMessage(URI.create(Objects.requireNonNull(headers.get("ids-correlationmessage")).get(0)));
            }
            switch (request.getMethod().toLowerCase()) {
                //read access. Use DescriptionRequestMessage semantics
                case "get":
                case "head":
                case "options": {

                    DescriptionRequestMessage m = (DescriptionRequestMessage) httpHeaderMessageConverterBuilder.build().getMessage();

                    if (headers.containsKey("depth")) {
                        //If we don't prepend "ids:", then it is not put into the external properties map.
                        //Checked with SBa - doesn't seem to be problematic for now
                        m.setProperty("ids:depth", headers.get("depth"));
                    }
                    //Content negotiation
                    if(headers.containsKey("accept"))
                    {
                        List<String> acceptHeaders = headers.get("accept");
                        if(acceptHeaders != null) {
                        //Cast Accept headers to string list
                            String acceptValue = String.join(",", acceptHeaders);
                            m.setProperty("ids:accept", acceptValue);
                        }
                    }
                    return processMessageForRest(m.toRdf(), null, request.getMethod());
                }
                //write access (though not delete). Must have some body
                case "put":
                case "post": {
                    httpHeaderMessageConverterBuilder.body(IOUtils.toString(request.getReader()));
                    break;

                }
                //delete access.
                case "delete": {
                    break;
                }
                //Other methods are not allowed. Return appropriate HTTP status
                default: return new ResponseEntity<>("Method not allowed", HttpStatus.METHOD_NOT_ALLOWED);
            }

            HttpHeaderMessageConverter httpHeaderMessageConverter = httpHeaderMessageConverterBuilder.build();

            //getHeader method transforms this to an appropriate message class
            return processMessageForRest(httpHeaderMessageConverter.getMessage().toRdf(), httpHeaderMessageConverter.body, httpHeaderMessageConverter.method);

        } catch (IOException | URISyntaxException | DatatypeConfigurationException | RejectMessageException | IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    private List<String> getOrThrow(HttpHeaders map, String key) throws IOException
    {
        key = key.toLowerCase();
        if(!map.containsKey(key))
        {
            throw new IOException("Missing header: " + key);
        }
        return map.get(key);
    }

    private String getFirstOrThrow(HttpHeaders map, String key) throws IOException
    {
        return getOrThrow(map, key).get(0);
    }


    @RequestMapping(path = "/infrastructure",
            method = RequestMethod.POST,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, "multipart/mixed"})
    public ResponseEntity infrastructureLevelCommunication(
            @RequestPart("header") String header,
            @RequestPart(value = "payload", required = false) byte[] payload,
            //@RequestHeader("X-Real-Ip") String realIp, @RequestHeader("X-Forwarded-For") String forwardedFor
            HttpServletRequest request
        ) throws IOException
    {
        return processMessage(header, payload, RequestType.INFRASTRUCTURE);
    }

    @RequestMapping(path = "/data",
            method = RequestMethod.POST,
            consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, "multipart/mixed"})
    public ResponseEntity dataLevelCommunication(
            @RequestPart("header") String header,
            @RequestPart(value = "payload", required = false) byte[] payload) throws IOException
    {
        return processMessage(header, payload, RequestType.DATA);
    }

    @RequestMapping(path = "/echo",
        method = RequestMethod.POST,
        consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, "multipart/mixed"})
    public ResponseEntity echoLevelCommunication(
            @RequestPart("header") String header,
            @RequestPart(value = "payload", required = false) byte[] payload) throws IOException
    {
        return processMessage(header, payload, RequestType.ECHO);
    }

    @RequestMapping(path="/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getSelfDescription() {
        return multipartComponentInteractor.getSelfDescription();
    }


    private ResponseEntity processMessage(String header, byte[] payload, RequestType requestType) throws IOException {
        Multipart multipart = new Multipart(header, MediaType.APPLICATION_JSON_VALUE);
        multipart.setSerializedPayload(new SerializedPayload(payload));
        Multipart response = multipartComponentInteractor.process(multipart, requestType);

        return responseEntityCreator.fromResponse(response);
    }

    private ResponseEntity<String> processMessageForRest(String header, String body, String method) throws IOException {
        MessageAndPayload<?, ?> response = restComponentInteractor.process(new ImmutablePair<>(header, body), RequestType.INFRASTRUCTURE);

        return responseEntityCreator.fromResponseForRest(response, method);

    }

}
