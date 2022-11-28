package de.fraunhofer.iais.eis.ids.component.interaction.mocking;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;

import static de.fraunhofer.iais.eis.ids.component.interaction.util.TestUtil.loadResourceAsString;

public class MockDispatcher {

    private static final String ONTOLOGY_TTL_RESPONSE;
    private static final String INVALID_ONTOLOGY_TTL_RESPONSE;
    private static final String SHACL_SHAPE_TTL_RESPONSE;
    private static final String INVALID_SHACL_SHAPE_TTL_RESPONSE;

    static {
        try {
            ONTOLOGY_TTL_RESPONSE = loadResourceAsString("customOntology.ttl");
            INVALID_ONTOLOGY_TTL_RESPONSE = loadResourceAsString("invalidOntology.ttl");
            SHACL_SHAPE_TTL_RESPONSE = loadResourceAsString("customShaclShape.ttl");
            INVALID_SHACL_SHAPE_TTL_RESPONSE = loadResourceAsString("invalidShaclShape.ttl");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static Dispatcher dispatcher;

    public static Dispatcher getMockDispatcher() {
        if (dispatcher == null) {
            initDispatcher();
        }
        return dispatcher;
    }

    private static void initDispatcher() {
        dispatcher = new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest recordedRequest) throws InterruptedException {
                switch (Objects.requireNonNull(recordedRequest.getPath())) {
                    case "/ontology.ttl":
                        return getMockResponseWithContent(ONTOLOGY_TTL_RESPONSE);
                    case "/shapes.ttl":
                        return getMockResponseWithContent(SHACL_SHAPE_TTL_RESPONSE);
                    case "/invalid-ontology.ttl":
                        return getMockResponseWithContent(INVALID_ONTOLOGY_TTL_RESPONSE);
                    case "/invalid-shapes.ttl":
                        return getMockResponseWithContent(INVALID_SHACL_SHAPE_TTL_RESPONSE);
                }
                return new MockResponse().setResponseCode(404);
            }
        };
    }

    private static MockResponse getMockResponseWithContent(String content) {
        return new MockResponse()
                .addHeader("Content-Type", "text/turtle; charset=utf-8")
                .setBody(content)
                .setResponseCode(200);
    }

    public static String getTargetOntologyTtlResponse() {
        return ONTOLOGY_TTL_RESPONSE;
    }

    public static String getTargetInvalidOntologyTtlResponse() {
        return INVALID_ONTOLOGY_TTL_RESPONSE;
    }

    public static String getTargetShaclShapeTtlResponse() {
        return SHACL_SHAPE_TTL_RESPONSE;
    }

    public static String getTargetInvalidShaclShapeTtlResponse() {
        return INVALID_SHACL_SHAPE_TTL_RESPONSE;
    }
}
