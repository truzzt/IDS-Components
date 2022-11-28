package de.fraunhofer.iais.eis.ids.component.core;

public class TokenVerificationException extends Exception {

    public TokenVerificationException(Throwable cause) {
        super(cause);
    }

    public TokenVerificationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TokenVerificationException(String message) {
        super(message);
    }

}
