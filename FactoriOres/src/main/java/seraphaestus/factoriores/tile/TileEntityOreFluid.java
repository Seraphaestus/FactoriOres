package seraphaestus.factoriores.tile;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fluids.FluidStack;
import seraphaestus.factoriores.Registrar;
import seraphaestus.factoriores.block.BlockOre;
import seraphaestus.factoriores.block.BlockOreFluid;

public class TileEntityOreFluid extends TileEntityOre {

	public TileEntityOreFluid(Random rnd, int amountMin, int amountMax) {
		super(Registrar.tileFluidDeposit.get());
		amount = rnd.nextInt(amountMax - amountMin) + amountMin;
	}
	public TileEntityOreFluid() {
		super(Registrar.tileFluidDeposit.get());
	}
	
	// -------- Abstraction methods
	
	@Override
	protected Object emptyStack() {
		return FluidStack.EMPTY;
	}
	
	// ------------------------
	
	public Object getDrop(BlockState state, ServerWorld world, BlockOre oreBlock, @Nullable TileEntity tile, boolean isFromMiner) {
		if (oreBlock instanceof BlockOreFluid) {
			LootContext.Builder builder = (new LootContext.Builder(world)).withRandom(world.rand).withParameter(LootParameters.ORIGIN, Vector3d.ofCenter(pos)).withParameter(LootParameters.TOOL, ItemStack.EMPTY).withNullableParameter(LootParameters.BLOCK_ENTITY, tile);
			return isFromMiner ? ((BlockOreFluid)oreBlock).getFluidDropViaMiner(state, builder) : emptyStack();
		}
		return emptyStack();
	}

}
