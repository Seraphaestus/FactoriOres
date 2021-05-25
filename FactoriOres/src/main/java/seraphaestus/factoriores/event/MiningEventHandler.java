package seraphaestus.factoriores.event;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import seraphaestus.factoriores.block.BlockOre;
import seraphaestus.factoriores.tile.TileEntityOre;

@Mod.EventBusSubscriber
public class MiningEventHandler {
	
	@SubscribeEvent
    public static void onBlockMined(BreakEvent event) {
		if (event.getPlayer().isCreative()) return;
		
		BlockState state = event.getState();
		boolean hardBroken = !event.getPlayer().getHeldItemMainhand().canHarvestBlock(state);

		if (state.getBlock() instanceof BlockOre) {
			TileEntity tileEntity = event.getWorld().getTileEntity(event.getPos());
			if (tileEntity instanceof TileEntityOre) {
				TileEntityOre tileEntityOre = (TileEntityOre)tileEntity;
				
				//drop resource entities,but only on server-side
				if (!event.getWorld().isRemote() && !hardBroken) {
					Block.spawnDrops(state, (World)event.getWorld(), event.getPos());
				}
				
				if (hardBroken) {
					event.getWorld().setBlockState(event.getPos(), Blocks.AIR.getDefaultState(), 1 | 2);
				} else {
					tileEntityOre.decrement(null);
				}
				event.setCanceled(true);
			}
		}
    }
}
