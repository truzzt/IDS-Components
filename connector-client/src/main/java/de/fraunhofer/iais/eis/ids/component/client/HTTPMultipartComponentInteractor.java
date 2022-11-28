package de.fraunhofer.iais.eis.ids.component.client;

import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;
import de.fraunhofer.iais.eis.ids.component.core.map.NullMAP;
import de.fraunhofer.iais.eis.ids.component.core.RequestType;
import de.fraunhofer.iais.eis.ids.component.interaction.multipart.Multipart;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.eclipse.jetty.http.MultiPartFormInputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.Part;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

public class HTTPMultipartComponentInteractor extends RemoteComponentInteractor {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final URL remoteComponent;

    public HTTPMultipartComponentInteractor(URL remoteComponent) {
        this.remoteComponent = remoteComponent;
    }

    @Override
    public String getSelfDescription() {
        return null;
    }

	@Override
	public MessageAndPayload process(MessageAndPayload request, RequestType requestType) {
		try {
			CloseableHttpResponse response = sendRequest(createTargetUrl(remoteComponent, requestType), request);
			if (response.getStatusLine().getStatusCode() >= 200 && response.getStatusLine().getStatusCode() < 400) {
				return parseResponse(response);
			}
			else {
				logger.warn("Received error response from remote HTTP component.");
				logger.warn("HTTP status code: " + response.getStatusLine().getStatusCode());
				logger.warn("HTTP message: " + response.getStatusLine().getReasonPhrase());
				return new NullMAP();
			}
		}
		catch (IOException e) {
			logger.warn("Error getting or interpreting remote component response", e);
			return new NullMAP();
		}
		catch (RejectMessageException e)
        {
            logger.warn("RejectionMessage received", e);
            return new NullMAP();
        }
	}

    private URL createTargetUrl(URL baseUrl, RequestType... requestTypes) {
        String target = "/data";
        if ((requestTypes.length > 0) && (requestTypes[0] == RequestType.INFRASTRUCTURE)) {
            target = "/infrastructure";
        }
        try {
            return new URL(baseUrl.toString() + target);
        }
        catch (MalformedURLException e) {
            return baseUrl;
        }
    }

    private CloseableHttpResponse sendRequest(URL target, MessageAndPayload messageAndPayload) throws IOException {
        Multipart multipart = new Multipart(messageAndPayload);

		MultipartEntityBuilder builder = MultipartEntityBuilder
				.create()
				.addTextBody("header", multipart.getHeader(), ContentType.parse(multipart.getHeaderContentType()));
		if (messageAndPayload.getPayload().isPresent()) builder.addBinaryBody("payload",
				multipart.getSerializedPayload().getSerialization(),
				ContentType.parse(multipart.getSerializedPayload().getContentType()),
				multipart.getSerializedPayload().getFilename());

		HttpEntity entity = builder.build();

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(target.toString());
        httpPost.setEntity(entity);
        return httpclient.execute(httpPost);
    }

    private MessageAndPayload parseResponse(CloseableHttpResponse response) throws IOException, RejectMessageException {
        MultiPartFormInputStream multiPartFormInputStream = new MultiPartFormInputStream(response.getEntity().getContent(), response.getEntity().getContentType().getValue(), null, null);
        Part header = multiPartFormInputStream.getPart("header");
        Part payload = multiPartFormInputStream.getPart("payload");

        Multipart multipartResponse = new Multipart(IOUtils.toString(header.getInputStream(), Charset.defaultCharset()), header.getContentType());
        if (payload != null) {
            multipartResponse.setSerializedPayload(new SerializedPayload(IOUtils.toByteArray(payload.getInputStream()), payload.getContentType(), payload.getSubmittedFileName()));
        }

        return multipartResponse.toMap();
    }

}
