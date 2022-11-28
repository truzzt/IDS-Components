package de.fraunhofer.iais.eis.ids.component.interaction;

import de.fraunhofer.iais.eis.ids.component.core.RequestType;

import java.io.IOException;

public interface ComponentInteractor<REQ, RES, SDC> {

    SDC getSelfDescription();
    RES process(REQ request, RequestType requestType) throws IOException;

}
