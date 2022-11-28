package de.fraunhofer.iais.eis.ids.component.client;

import de.fraunhofer.iais.eis.InfrastructureComponent;

import java.net.URL;

public class RemoteComponentInteractorFactory {

    private static RemoteComponentInteractorFactory instance;

    private RemoteComponentInteractorFactory() {
    }

    public static RemoteComponentInteractorFactory getInstance() {
        if (instance == null) instance = new RemoteComponentInteractorFactory();
        return instance;
    }

    public RemoteComponentInteractor create(URL url) {
        return new HTTPMultipartComponentInteractor(url);
    }

    public RemoteComponentInteractor create(InfrastructureComponent infrastructureComponent) {
        // find out the protocol of the infrastructurecomponent and instantiate it; not implemented so far
        return null;
    }
}
