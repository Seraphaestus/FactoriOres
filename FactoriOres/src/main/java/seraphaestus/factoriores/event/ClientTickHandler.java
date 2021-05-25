package seraphaestus.factoriores.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class ClientTickHandler {
	
	public static long getTotalElapsedTicksInGame() {
		return totalElapsedTicksInGame;
	}

	@SubscribeEvent
	public static void clientTickEnd(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			if (!Minecraft.getInstance().isGamePaused()) {
				totalElapsedTicksInGame++;
			}
		}
	}

	private static long totalElapsedTicksInGame = 0;
}
