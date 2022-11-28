package de.fraunhofer.iais.eis.ids.component.core;

import de.fraunhofer.iais.eis.InfrastructureComponent;
import de.fraunhofer.iais.eis.Message;

public interface Component {

    InfrastructureComponent getSelfDescription();
    MessageAndPayload<? extends Message, ?> process(MessageAndPayload<? extends Message, ?> messageAndPayload, RequestType requestType);

}
