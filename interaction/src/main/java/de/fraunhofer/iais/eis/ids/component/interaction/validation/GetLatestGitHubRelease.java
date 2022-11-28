package de.fraunhofer.iais.eis.ids.component.interaction.validation;

import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.kohsuke.github.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * This class alters the POM file of the interaction module to update the download links of the ontology and the validation shapes to the latest nightly release
 */
public class GetLatestGitHubRelease {

    public static void main(String[] args) throws IOException, XmlPullParserException {

        //Use some OAuth token to increase rate limit.
        GitHub gitHub = new GitHubBuilder().withOAuthToken("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", "iais-eis-ids").build();

        //Select repository
        GHRepository repo = gitHub.getOrganization("International-Data-Spaces-Association").getRepository("InformationModel");

        //List all releases
        List<GHRelease> releases = repo.listReleases().toList();

        //Get latest release
        GHRelease latestRelease = null;
        for(GHRelease release : releases)
        {
            if(latestRelease == null)
            {
                latestRelease = release;
            }
            else
            {
                if(release.getPublished_at().after(latestRelease.getPublished_at()))
                {
                    latestRelease = release;
                }
            }
        }

        assert latestRelease != null;

        //Extract download URLs from release
        String ontologyUrl = null;
        String validationUrl = null;
        for(GHAsset asset : latestRelease.getAssets())
        {
            if(asset.getName().equals("ontology.ttl"))
            {
                ontologyUrl = asset.getBrowserDownloadUrl();
            }
            else if(asset.getName().equals("validation.zip"))
            {
                validationUrl = asset.getBrowserDownloadUrl();
            }
        }

        //Now that we have the URLs, we need to inject them into our POM
        MavenXpp3Reader pomReader = new MavenXpp3Reader();

        //Get root path of this project
        String rootPath = System.getProperty("user.dir");

        //Windows or Linux?
        String separator = "/";
        if(rootPath.contains("\\")) //Windows?
        {
            separator = "\\";
        }

        //Read POM file of interaction module (this module)
        File pomFile = new File(rootPath + separator + "interaction", "/pom.xml");
        Model model = pomReader.read(new FileInputStream(pomFile));

        //Find the download-maven-plugin so that we can modify the configuration of it
        List<Plugin> plugins = model.getBuild().getPlugins();
        for(Plugin plugin : plugins)
        {
            if(plugin.getArtifactId().equals("download-maven-plugin"))
            {
                //Create two executions, one for the ontology and one for the validation shapes

                PluginExecution ontologyExecution = new PluginExecution();
                ontologyExecution.setId("1"); //Some "unique" ID required to tell this execution and the next one apart
                ontologyExecution.setPhase("test-compile"); //Put it into a rather late phase, so that we can update the links prior to the download execution
                //Goal is wget
                ontologyExecution.setGoals(Collections.singletonList("wget"));
                //Child object for configuration
                Xpp3Dom ontologyConfiguration = new Xpp3Dom("configuration");
                //URL and outputFileName children with their values
                Xpp3Dom urlChild = new Xpp3Dom("url");
                urlChild.setValue(ontologyUrl);
                ontologyConfiguration.addChild(urlChild);
                Xpp3Dom outputNameChild = new Xpp3Dom("outputFileName");
                outputNameChild.setValue("ontology.ttl");
                ontologyConfiguration.addChild(outputNameChild);
                Xpp3Dom outputPath = new Xpp3Dom("outputDirectory");

                outputPath.setValue("src/main/resources");
                ontologyConfiguration.addChild(outputPath);

                Xpp3Dom overwrite = new Xpp3Dom("overwrite");
                overwrite.setValue("true");
                ontologyConfiguration.addChild(overwrite);

                Xpp3Dom skipCache = new Xpp3Dom("skipCache");
                skipCache.setValue("true");
                ontologyConfiguration.addChild(skipCache);


                ontologyExecution.setConfiguration(ontologyConfiguration);


                //Same for validation now
                PluginExecution validationExecution = new PluginExecution();
                validationExecution.setId("2");
                validationExecution.setPhase("test-compile");
                validationExecution.setGoals(Collections.singletonList("wget"));
                Xpp3Dom validationConfiguration = new Xpp3Dom("configuration");

                Xpp3Dom urlChild2 = new Xpp3Dom("url");
                Xpp3Dom outputNameChild2 = new Xpp3Dom("outputFileName");
                urlChild2.setValue(validationUrl);
                outputNameChild2.setValue("validation.zip");
                validationConfiguration.addChild(urlChild2);
                validationConfiguration.addChild(outputNameChild2);
                validationConfiguration.addChild(outputPath);
                validationConfiguration.addChild(overwrite);
                validationConfiguration.addChild(skipCache);
                validationExecution.setConfiguration(validationConfiguration);

                //Set executions to the stuff we just created
                plugin.setExecutions(Arrays.asList(ontologyExecution, validationExecution));
            }
        }
        //Write to POM
        MavenXpp3Writer writer = new MavenXpp3Writer();
        writer.write(new FileOutputStream(pomFile), model);
        System.out.println("GitHub links updated!");
    }
}
