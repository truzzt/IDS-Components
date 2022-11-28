package de.fraunhofer.iais.eis.ids.component.interaction.validation;


import de.fraunhofer.iais.eis.ids.component.interaction.valueobject.ShaclValidationUpdateRequestBody;
import org.apache.jena.graph.compose.Union;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RiotException;
import org.apache.jena.shacl.Shapes;
import org.apache.jena.shacl.ValidationReport;
import org.apache.jena.util.FileUtils;
import org.kohsuke.github.*;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Class to validate RDF against the IDS Information Model SHACL shapes
 * Warning: The library handling the GitHub API calls requires Java 11 to work!
 */
public class ShaclValidator {
    private final static org.slf4j.Logger logger = LoggerFactory.getLogger(ShaclValidator.class);
    private Shapes shapes;
    private Model shapesModel;
    private Model ontologyModel;

    private static ShaclValidator validator;

    public static boolean downloadLatestShapes = false;
    public static boolean downloadLatestOntology = false;


    /**
     * Function to get the validator object. Initializes if it does not exist yet. Note that initialization might take a long time and should be done prior to incoming messages
     * @return ShaclValidator object to validate RDF
     * @throws IOException might be thrown if the fetching of the shapes or of the ontology from official repositories fails
     */
    public static ShaclValidator getValidator() throws IOException {
        if(validator == null)
        {
            validator = new ShaclValidator();
        }
        return validator;
    }

    //Allows to initialize before the first message comes in

    /**
     * Function to explicitly initialize the ShaclValidator object. This can be used to avoid long initialization times when a message comes in
     * @throws IOException might be thrown if the fetching of the shapes or of the ontology from official repositories fails
     */
    public static void initialize() throws IOException {
        if(validator == null)
        {
            validator = new ShaclValidator();
        }
    }

    /**
     * Function to validate RDF messages against SHACL shapes.
     * The function is automatically applied to all messages sent and received via this library.
     * The shapes are from the IDS GitHub repository (https://github.com/IndustrialDataSpace/InformationModel/tree/develop/testing) and are delivered with this library.
     * @param messageToValidate Message to be validated alongside the information model against the shape files
     * @return ValidationReport object. Use .conforms to see if the check has passed. org.apache.jena.shacl.lib.ShLib.printReport(report) can be used for printing the report.
     */
    public static ValidationReport validateRdf(String messageToValidate) throws IOException {
        ShaclValidator val = getValidator();
        //Ontology is already loaded. Now we need to parse the message.
        Model messageModel = ModelFactory.createDefaultModel();

        //Read JSON-LD String into a model
        try {
            messageModel.read(new ByteArrayInputStream(messageToValidate.getBytes(StandardCharsets.UTF_8)), null, "JSONLD");
        }
        catch (RiotException e)
        {
            throw new IOException("The message is no valid JSON-LD and could therefore not be checked for information model compliance.", e);
        }

        //Perform the validation
        //The data graph is the information model plus the message, hence let's create a Union graph
        return org.apache.jena.shacl.ShaclValidator.get().validate(val.shapes, new Union(messageModel.getGraph(), val.ontologyModel.getGraph()));
    }

    /**
     * This constructor is called automatically, if no instance exists yet
     * @throws IOException might be thrown if the fetching of the shapes or of the ontology from official repositories fails
     */
    private ShaclValidator() throws IOException {
        logger.info("Initializing SHACL shapes.");

        //Initialize an empty model into which we will be loading the shapes
        shapesModel = ModelFactory.createDefaultModel();

        if(downloadLatestShapes) {
            // Use the latest shapes from github
            logger.info("Getting latest SHACL shapes from GitHub. You can optionally use the shapes included in this library.");

            //Use some OAuth token to increase rate limit.
            GitHub gitHub = new GitHubBuilder().withOAuthToken("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", "iais-eis-ids").build();

            //Select repository
            GHRepository repo = gitHub.getOrganization("International-Data-Spaces-Association").getRepository("InformationModel");

            //Select head of branch develop
            GHRef ref = repo.getRef("heads/develop");

            //Shapes are located in "testing" directory. Make sure to use correct branch by specifying the ref
            List<GHContent> directoryContents = repo.getDirectoryContent("testing", ref.getRef());

            //The list above is not modifiable, so we create our own to which we can add subdirectory content
            ArrayList<GHContent> allContents = new ArrayList<>(directoryContents);
            for (GHContent content : directoryContents) {
                //Is directory? If so, add content to allContents
                if (!content.getName().contains(".")) {
                    allContents.addAll(repo.getDirectoryContent("testing/" + content.getName(), ref.getRef()));
                }
            }

            //Download all files, somewhat in parallel. Afterwards, load them synchronously into shapes model, as we don't know if it's thread safe
            ExecutorService executorService = Executors.newFixedThreadPool(8); //8 threads for parallel downloading
            List<Callable<String>> callableTasks = new ArrayList<>();
            logger.debug("Downloading files from the develop branch of the InformationModel");
            for (GHContent content : allContents) {
                //Skip directories
                if (!content.getName().contains(".")) {
                    continue;
                }
                //Add a task for each file to be downloaded
                callableTasks.add(() -> {
                    //Convert Stream to String
                    InputStream inputStream = new URL("https://raw.githubusercontent.com/International-Data-Spaces-Association/InformationModel/develop/" + content.getPath()).openConnection().getInputStream();
                    ByteArrayOutputStream result = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inputStream.read(buffer)) != -1) {
                        result.write(buffer, 0, length);
                    }
                    return result.toString();
                });
            }
            try {
                //Download all files with some concurrent operations
                List<Future<String>> futures = executorService.invokeAll(callableTasks);
                while (true) {
                    //Check if any job is unfinished
                    boolean allDone = true;
                    for (Future<String> f : futures) {
                        if (!f.isDone()) {
                            allDone = false;
                        }
                    }
                    if (allDone) {
                        break;
                    } else {
                        //Give it some time and try again
                        Thread.sleep(100);
                    }
                }
                logger.debug("Download complete. " + futures.size() + " files downloaded. Loading them into a shape graph...");
                for (Future<String> downloadedFile : futures) {
                    try {
                        //The read function either expects a path to the file or an input stream. So we transform the strings to an input stream
                        shapesModel.read(new ByteArrayInputStream(downloadedFile.get().getBytes()), null, FileUtils.langTurtle);
                    } catch (ExecutionException e) {
                        //Should never happen, as we already waited for all threads to complete
                        e.printStackTrace();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //Make sure to shut down executor service. Otherwise it will hibernate, waiting for further tasks
            executorService.shutdown();
        }
        else {
            // Use the local shapes
            logger.info("Loading SHACL shapes from resources. You can optionally download the latest shapes from GitHub.");
            //Do not download all the shapes, but use some provided in resources
            shapesModel.add(getModelFromZipArchivePath("validation.zip"));
        }

        //All loaded, let's parse!
        shapes = Shapes.parse(shapesModel);
        InputStream ontologyInputStream;

        //Download latest ontology from IDSA website
        ontologyModel = ModelFactory.createDefaultModel();

        if(downloadLatestOntology) {
            logger.info("Downloading ontology from GitHub");
            URL url = new URL("https://international-data-spaces-association.github.io/InformationModel/docs/serializations/ontology.ttl"); //TODO: get ontology link similar to GetLatestGitHubRelease strategy
            ontologyModel = getModelFromUrl(url);
        }
        else {
            logger.info("Loading ontology from resources");
            ontologyModel = getModelFromFilePath("ontology.ttl");
        }
        logger.info("Initialization of SHACL shapes complete.");
    }

    /**
     * Function to replace the SHACL shapes and ontology used by the validator.
     * If no new valid shapes and ontology can be found or downloaded, the validator will be reset to the default state.
     * That means, it needs to initialize again. Otherwise, it will be initialized on the next usage.
     * @param shaclValidationUpdateRequestBody object containing the string representation of the URLs pointing to the turtle files for the new shapes and ontology.
     */
    public static void updateShaclValidation(ShaclValidationUpdateRequestBody shaclValidationUpdateRequestBody) {
        try {
            replaceShaclShapes(shaclValidationUpdateRequestBody);
            replaceOntology(shaclValidationUpdateRequestBody);
        } catch (Exception e) {
            logger.error("Unable to update ShaclValidator. Reason: " + e.getMessage());
            logger.error("Resetting ShaclValidator.");
            validator = null;
        }
    }

    private static void replaceShaclShapes(ShaclValidationUpdateRequestBody shaclValidationUpdateRequestBody) throws IOException {
        URL shaclShapesUrl = stringToUrl(shaclValidationUpdateRequestBody.getShaclShapesLocation());
        logger.info("Downloading new shapes from " + shaclShapesUrl);
        Model newShapes = getModelFromUrl(shaclShapesUrl);
        replaceShapesModel(newShapes);
        logger.info("Successfully replaced shapes in validator with shapes from " + shaclShapesUrl);
    }

    private static void replaceOntology(ShaclValidationUpdateRequestBody shaclValidationUpdateRequestBody) throws IOException {
        URL ontologyUrl = stringToUrl(shaclValidationUpdateRequestBody.getOntologyLocation());
        logger.info("Downloading new ontology from " + ontologyUrl);
        Model newOntology = getModelFromUrl(ontologyUrl);
        replaceOntologyModel(newOntology);
        logger.info("Successfully replaced ontology in validator with ontology from " + ontologyUrl);
    }

    private static void replaceShapesModel(Model newShapes) throws IOException {
        ShaclValidator validator = getValidator();
        validator.shapesModel = newShapes;
        validator.shapes = Shapes.parse(validator.shapesModel);
    }

    private static void replaceOntologyModel(Model newOntology) throws IOException {
        ShaclValidator validator = getValidator();
        validator.ontologyModel = newOntology;
    }

    private static URL stringToUrl(String urlString) throws MalformedURLException {
        return URI.create(urlString).toURL();
    }
    private static Model getModelFromUrl(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        InputStream inputStream = connection.getInputStream();
        return readStreamToModel(inputStream);
    }

    private static Model getModelFromFilePath(String filePath) {
        InputStream inputStream = ShaclValidator.class.getClassLoader().getResourceAsStream(filePath);
        return readStreamToModel(inputStream);
    }

    private static Model getModelFromZipArchivePath(String zipPath) throws IOException {
        InputStream inputStream = ShaclValidator.class.getClassLoader().getResourceAsStream(zipPath);
        //Stream this to some temporary file which will be deleted after program exit
        if (inputStream == null)
            throw new IOException("Failed to retrieve validation.zip from resources. Try setting ShaclValidator.downloadLatestShapes to true");
        File inputStreamToFile = File.createTempFile("validation_zip_file", null);
        inputStreamToFile.deleteOnExit();

        Files.copy(inputStream, inputStreamToFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        ZipFile zipFile = new ZipFile(inputStreamToFile);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();

        Model model = ModelFactory.createDefaultModel();
        while (entries.hasMoreElements()) {
            model.read(zipFile.getInputStream(entries.nextElement()), null, FileUtils.langTurtle);
        }
        logIfModelIsEmpty(model);
        return model;
    }

    private static Model readStreamToModel(InputStream shapesStream) {
        Model model = ModelFactory.createDefaultModel();
        model.read(shapesStream, null, FileUtils.langTurtle);
        logIfModelIsEmpty(model);
        return model;
    }

    private static void logIfModelIsEmpty(Model model) {
        if (model.isEmpty()) {
            logger.info("The new model does not contain any triples. Please check if the provided path is correct or the file is empty.");
        }
    }

}
