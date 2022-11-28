package de.fraunhofer.iais.eis.ids.component.core;

import de.fraunhofer.iais.eis.InfrastructureComponent;

public interface SelfDescriptionProvider {

    InfrastructureComponent getSelfDescription() throws InfomodelFormalException;

}
