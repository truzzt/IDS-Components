package de.fraunhofer.iais.eis.ids.connector.commons.app.map;


import de.fraunhofer.iais.eis.AppResource;
import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.SerializedPayload;

import java.util.Optional;

public class AppMAP implements MessageAndPayload<Message, AppResource> {
    private final Message message;
    private AppResource app;
    public AppMAP(Message m)
    {
        this.message = m;
    }

    public AppMAP(Message m, AppResource a)
    {
        this.message = m;
        app = a;
    }

    @Override
    public Message getMessage() {
        return message;
    }

    @Override
    public Optional<AppResource> getPayload() {
        if(app == null)
        {
            return Optional.empty();
        }
        return Optional.of(app);
    }

    @Override
    public SerializedPayload serializePayload() {
        if (app != null) {
            return new SerializedPayload(app.toRdf().getBytes(), "application/ld+json");
        }
        else return SerializedPayload.EMPTY;
    }

}
