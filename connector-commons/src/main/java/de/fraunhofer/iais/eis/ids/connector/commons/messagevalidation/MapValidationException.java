package de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation;

public class MapValidationException extends Exception {
    private String message;
    public MapValidationException(String message) { this.message = message; }
    @Override
    public String getMessage() {
        return message;
    }
}
