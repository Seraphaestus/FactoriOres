package seraphaestus.factoriores;

import net.minecraftforge.eventbus.api.IEventBus;

/*
This class is required to make sure that we don't accidentally try to load any client-side-only classes
  on a dedicated server.
*/

public class ClientModEventRegistrar {
	private final IEventBus eventBus;

    public ClientModEventRegistrar(IEventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void registerClientOnlyEvents() {
        eventBus.register(seraphaestus.factoriores.StartupClient.class);
    }
}
