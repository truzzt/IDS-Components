package de.fraunhofer.iais.eis.ids.component.core;

import java.net.URI;

public class MissingHandlerException extends RuntimeException {

    private URI causingMessageId;

    public MissingHandlerException(URI causingMessageId, String explanation) {
        super(explanation);
        this.causingMessageId = causingMessageId;
    }

    public URI getCausingMessageId() {
        return causingMessageId;
    }
}
