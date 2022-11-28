package de.fraunhofer.iais.eis.ids.connector.commons.artifact;

import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.Optional;

public interface ArtifactFileProvider {

    Optional<File> getArtifact(URI artifactId) throws RejectMessageException;

}
