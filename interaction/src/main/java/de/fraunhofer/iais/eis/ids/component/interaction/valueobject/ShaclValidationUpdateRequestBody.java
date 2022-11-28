package de.fraunhofer.iais.eis.ids.component.interaction.valueobject;

public class ShaclValidationUpdateRequestBody {
    private String shaclShapesLocation;
    private String ontologyLocation;

    public ShaclValidationUpdateRequestBody(String shaclShapesLocation, String ontologyLocation) {
        this.shaclShapesLocation = shaclShapesLocation;
        this.ontologyLocation = ontologyLocation;
    }

    public String getShaclShapesLocation() {
        return shaclShapesLocation;
    }

    public void setShaclShapesLocation(String shaclShapesLocation) {
        this.shaclShapesLocation = shaclShapesLocation;
    }

    public String getOntologyLocation() {
        return ontologyLocation;
    }

    public void setOntologyLocation(String ontologyLocation) {
        this.ontologyLocation = ontologyLocation;
    }
}
