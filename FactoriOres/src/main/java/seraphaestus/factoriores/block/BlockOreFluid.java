package seraphaestus.factoriores.block;

import java.util.Collections;
import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import seraphaestus.factoriores.ConfigHandler;
import seraphaestus.factoriores.tile.TileEntityOreFluid;

public class BlockOreFluid extends BlockOre {

	public BlockOreFluid(String name, Properties properties, int amountMin, int amountMax) {
		super(name, properties, amountMin, amountMax);
	}
	
	// -------- Tile Entity methods
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		if (world instanceof World) return new TileEntityOreFluid(((World)world).rand, amountMin, amountMax);
		return new TileEntityOreFluid();
	}
	
	// -------- Events
	
	// -------- Lixiviant methods
	
	@Override
	public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader reader, BlockPos pos) {
      return 0.0F;
   }
	
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		return Collections.emptyList();
	}
	
	@Override
	public List<ItemStack> getDropsViaMiner(BlockState state, LootContext.Builder builder) {
		return Collections.emptyList();
	}
	
	public FluidStack getFluidDropViaMiner(BlockState state, LootContext.Builder builder) {
		List<ItemStack> drops = super.getDrops(state, builder);
		if (drops.isEmpty()) return FluidStack.EMPTY;
		
		ItemStack bucket = drops.get(0);
		if (bucket.getItem() instanceof BucketItem) {
			return new FluidStack(((BucketItem)bucket.getItem()).getFluid(), ConfigHandler.COMMON.fluidDepositAmount.get());
		}
		return FluidStack.EMPTY;
	}
	
	// ------------------------
	
	@Override
	public String getID() {
		return name + "_deposit";
	}
	@Override
	public String getDecoratorID() {
		return getID() + "_fluid_deposit";
	}
	
	@Override
	public boolean requiresLixiviant() {
		return false;
	}
}
