package de.fraunhofer.iais.eis.ids.component.ecosystemintegration.daps;

import okhttp3.*;
import okio.Buffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This utility class allows the logging of an okhttp3 request.
 * One application of this class can be to log the message exchange with the DAPS in detail
 */
public class LoggingInterceptor implements Interceptor {

    Logger logger = LoggerFactory.getLogger(LoggingInterceptor.class);
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        long t1 = System.nanoTime();
        logger.info("OkHttp: " + String.format("--> Sending request %s", request.url()));

        Buffer requestBuffer = new Buffer();
        if(request.body() != null) {
            request.body().writeTo(requestBuffer);
            logger.info("OkHttp: " + requestBuffer.readUtf8());
        }

        Response response = chain.proceed(request);

        long t2 = System.nanoTime();
        logger.info("OkHttp: " + String.format("<-- Received response for %s in %.1fms%n%s", response.request().url(), (t2 - t1) / 1e6d, response.headers()));

        assert response.body() != null;
        MediaType contentType = response.body().contentType();
        String content = response.body().string();
        logger.info("OkHttp: " + content);

        ResponseBody wrappedBody = ResponseBody.create(contentType, content);
        return response.newBuilder().body(wrappedBody).build();

    }
}