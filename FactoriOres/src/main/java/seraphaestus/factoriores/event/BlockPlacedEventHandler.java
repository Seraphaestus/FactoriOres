package seraphaestus.factoriores.event;

import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BlockPlacedEventHandler {

	@SubscribeEvent
	public static void onBlockPlaced(EntityPlaceEvent event) {
		/*BlockState state = event.getState();
		if (state.getBlock() instanceof BlockMiner || state.getBlock() instanceof BlockMechanicalMiner) {
			event.getWorld().setBlockState(event.getPos().up(), state, 1 | 2);
			event.setCanceled(true);
		}*/
	}
}
