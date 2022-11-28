package de.fraunhofer.iais.eis.ids.connector.commons.messagevalidation;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.iais.eis.RejectionReason;
import de.fraunhofer.iais.eis.ids.component.core.MessageAndPayload;
import de.fraunhofer.iais.eis.ids.component.core.MessageHandler;
import de.fraunhofer.iais.eis.ids.component.core.RejectMessageException;

import java.util.ArrayList;
import java.util.List;

public abstract class ValidatingMessageHandler<Incoming extends MessageAndPayload<? extends Message, ?>, Outgoing extends MessageAndPayload<? extends Message, ?>>
    implements MessageHandler<Incoming, Outgoing>
{
    private final List<MapValidationStrategy<Incoming>> mapValidationStrategies = new ArrayList<>();

    @Override
    public final Outgoing handle(Incoming messageAndPayload) throws RejectMessageException {
        try {
            for (MapValidationStrategy<Incoming> strategy : mapValidationStrategies) {
                strategy.validate(messageAndPayload);
            }
        }
        catch (MapValidationException e) {
            throw new RejectMessageException(RejectionReason.MALFORMED_MESSAGE, e);
        }

        return handleValidated(messageAndPayload);
    }

    public abstract Outgoing handleValidated(Incoming messageAndPayload) throws RejectMessageException;

    public void addMapValidationStrategy(MapValidationStrategy<Incoming> mapValidationStrategy) {
        mapValidationStrategies.add(mapValidationStrategy);
    }
}
