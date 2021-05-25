package seraphaestus.factoriores.block;

import java.util.Collections;
import java.util.List;

import mcjty.theoneprobe.api.Color;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProgressStyle;
import mcjty.theoneprobe.api.NumberFormat;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BucketItem;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fluids.FluidStack;
import seraphaestus.factoriores.ConfigHandler;
import seraphaestus.factoriores.FactoriOres;
import seraphaestus.factoriores.tile.TileEntityOre;
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
	
	@Override
	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
		TileEntity tileEntity = world.getTileEntity(data.getPos());
		if (tileEntity instanceof TileEntityOreFluid && world instanceof ServerWorld) {
			TileEntityOreFluid teOreFluid = (TileEntityOreFluid)tileEntity;
			int amount = teOreFluid.getAmount();
			int amountMB = amount * ConfigHandler.COMMON.fluidDepositAmount.get();
			int amountMaxMB = amountMax * ConfigHandler.COMMON.fluidDepositAmount.get();
			
			FluidStack fluidStack = (FluidStack)teOreFluid.getDrop(blockState, (ServerWorld)world, this, null, true);
			fluidStack = new FluidStack(fluidStack.getFluid(), amountMB <= 0 ? amountMaxMB : amountMB);
			Color color = new Color(fluidStack.getFluid().getAttributes().getColor(fluidStack));
        	if(fluidStack.getFluid() == Fluids.LAVA) {
    			color = new Color(255, 139, 27);
        	}
        	
        	IFormattableTextComponent text = new StringTextComponent("" + amountMB).append("mB");
        	
        	IFormattableTextComponent prefix = (IFormattableTextComponent) fluidStack.getDisplayName();
        	if (amountMB > 0) prefix = prefix.append(": ");
        	
        	IProgressStyle progressStyle = probeInfo.defaultProgressStyle()
        			.numberFormat(NumberFormat.NONE)
        			.borderlessColor(color, color.darker().darker())
        			.prefix(prefix);
        	if (amountMB > 0) progressStyle = progressStyle.suffix(text);
        	
			probeInfo.tankSimple(amountMaxMB, fluidStack, progressStyle);
			
			if (amount == TileEntityOre.AMOUNT_DUMMY) {
				probeInfo.text(new TranslationTextComponent(FactoriOres.MOD_ID + ".tooltip.ore_block_dummy").formatted(TextFormatting.GRAY));
			} else if (amount == TileEntityOre.AMOUNT_INFINITE) { 
				probeInfo.text(new TranslationTextComponent(FactoriOres.MOD_ID + ".tooltip.ore_block_infinite").formatted(TextFormatting.GRAY));
			}
			
			if (this.requiresLixiviant()) {
				probeInfo.text(new TranslationTextComponent(FactoriOres.MOD_ID + ".tooltip.ore_requires_lixiviant").formatted(TextFormatting.GREEN));
			}
		}
	}
}
