package de.fraunhofer.iais.eis.ids.component.interaction;

import de.fraunhofer.iais.eis.ids.component.interaction.mocking.MockDispatcher;
import de.fraunhofer.iais.eis.ids.component.interaction.validation.ShaclValidator;
import de.fraunhofer.iais.eis.ids.component.interaction.valueobject.ShaclValidationUpdateRequestBody;
import okhttp3.mockwebserver.MockWebServer;
import org.apache.jena.shacl.ValidationReport;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static de.fraunhofer.iais.eis.ids.component.interaction.util.TestUtil.*;

public class CustomizeShaclValidationTest {

    private MockWebServer mockWebServer;
    private final String exampleUnknownExternalSubclass = "{\"@type\": \"https://w3id.org/idsa/core/ExternalSubclass\"}";

    @Before
    public void initMockServer() throws IOException {
        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(MockDispatcher.getMockDispatcher());
        mockWebServer.start();
    }

    @After
    public void teardown() {
        resetShaclValidatorSingleton();
    }

    // --- Define ShaclValidationUpdateRequestBodies for easy access during testing

    public ShaclValidationUpdateRequestBody getShaclValidationUpdateRequestBodyWithValidLocations() {
        return new ShaclValidationUpdateRequestBody(
                mockWebServer.url("/shapes.ttl").toString(),
                mockWebServer.url("/ontology.ttl").toString());
    }

    public ShaclValidationUpdateRequestBody getShaclValidationUpdateRequestBodyWithInvalidLocations() {
        return new ShaclValidationUpdateRequestBody(
                "this is not a URL",
                "this is not a URL");
    }

    public ShaclValidationUpdateRequestBody getShaclValidationUpdateRequestBodyWith404Shapes() {
        return new ShaclValidationUpdateRequestBody(
                mockWebServer.url("/invalid-link-shacl").toString(),
                mockWebServer.url("/ontology.ttl").toString());
    }

    public ShaclValidationUpdateRequestBody getShaclValidationUpdateRequestBodyWith404Ontology() {
        return new ShaclValidationUpdateRequestBody(
                mockWebServer.url("/shapes.ttl").toString(),
                mockWebServer.url("/invalid-link-ontology").toString());
    }

    public ShaclValidationUpdateRequestBody getShaclValidationUpdateRequestBodyWithInvalidShapes() {
        return new ShaclValidationUpdateRequestBody(
                mockWebServer.url("/invalid-shapes.ttl").toString(),
                mockWebServer.url("/ontology.ttl").toString());
    }

    public ShaclValidationUpdateRequestBody getShaclValidationUpdateRequestBodyWithInvalidOntology() {
        return new ShaclValidationUpdateRequestBody(
                mockWebServer.url("/shapes.ttl").toString(),
                mockWebServer.url("/invalid-ontology.ttl").toString());
    }

    // --- Test mocked server for expected responses

    @Test
    public void serverShouldReturnOntologyOnRequest() throws IOException {
        String response = readContentFromUrl(mockWebServer.url("/ontology.ttl").url());

        Assert.assertEquals(response, MockDispatcher.getTargetOntologyTtlResponse());
    }

    @Test
    public void serverShouldReturnShapesOnRequest() throws IOException {
        String response = readContentFromUrl(mockWebServer.url("/shapes.ttl").url());

        Assert.assertEquals(response, MockDispatcher.getTargetShaclShapeTtlResponse());
    }

    @Test
    public void serverShouldReturnInvalidOntologyOnRequest() throws IOException {
        String response = readContentFromUrl(mockWebServer.url("/invalid-ontology.ttl").url());

        Assert.assertEquals(response, MockDispatcher.getTargetInvalidOntologyTtlResponse());
    }

    @Test
    public void serverShouldReturnInvalidShapesOnRequest() throws IOException {
        String response = readContentFromUrl(mockWebServer.url("/invalid-shapes.ttl").url());

        Assert.assertEquals(response, MockDispatcher.getTargetInvalidShaclShapeTtlResponse());
    }

    // --- Given a ShaclValidationUpdateRequestBody, test if ShaclValidator is reset on invalid input

    @Test
    public void resetValidatorOnInvalidUri() {
        ShaclValidationUpdateRequestBody shaclValidationUpdateRequestBody = getShaclValidationUpdateRequestBodyWithInvalidLocations();
        ShaclValidator.updateShaclValidation(shaclValidationUpdateRequestBody);

        Assert.assertTrue(checkIfShaclValidatorSingletonIsNull());
    }

    @Test
    public void resetValidatorOnUnknownShapesUri() {
        ShaclValidationUpdateRequestBody shaclValidationUpdateRequestBody = getShaclValidationUpdateRequestBodyWith404Shapes();
        ShaclValidator.updateShaclValidation(shaclValidationUpdateRequestBody);

        Assert.assertTrue(checkIfShaclValidatorSingletonIsNull());
    }

    @Test
    public void resetValidatorOnUnknownOntologyUri() {
        ShaclValidationUpdateRequestBody shaclValidationUpdateRequestBody = getShaclValidationUpdateRequestBodyWith404Ontology();
        ShaclValidator.updateShaclValidation(shaclValidationUpdateRequestBody);

        Assert.assertTrue(checkIfShaclValidatorSingletonIsNull());
    }

    @Test
    public void resetValidatorOnInvalidShapeFile() {
        ShaclValidationUpdateRequestBody shaclValidationUpdateRequestBody = getShaclValidationUpdateRequestBodyWithInvalidShapes();
        ShaclValidator.updateShaclValidation(shaclValidationUpdateRequestBody);

        Assert.assertTrue(checkIfShaclValidatorSingletonIsNull());
    }

    @Test
    public void resetValidatorOnInvalidOntologyFile() {
        ShaclValidationUpdateRequestBody shaclValidationUpdateRequestBody = getShaclValidationUpdateRequestBodyWithInvalidOntology();
        ShaclValidator.updateShaclValidation(shaclValidationUpdateRequestBody);

        Assert.assertTrue(checkIfShaclValidatorSingletonIsNull());
    }

    // --- Given a ShaclValidationUpdateRequestBody, test if ShaclValidator uses the new shapes and ontology for validation

    @Test
    public void updatedShapesShouldChangeConformity() throws IOException {
        ValidationReport reportPriorToUpdate = ShaclValidator.validateRdf(exampleUnknownExternalSubclass);
        ShaclValidationUpdateRequestBody shaclValidationUpdateRequestBody = getShaclValidationUpdateRequestBodyWithValidLocations();
        ShaclValidator.updateShaclValidation(shaclValidationUpdateRequestBody);
        ValidationReport reportAfterUpdate = ShaclValidator.validateRdf(exampleUnknownExternalSubclass);

        Assert.assertTrue(reportPriorToUpdate.conforms());
        Assert.assertFalse(reportAfterUpdate.conforms());
    }

}
