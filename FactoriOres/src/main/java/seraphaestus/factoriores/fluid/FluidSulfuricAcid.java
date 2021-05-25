package seraphaestus.factoriores.fluid;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraftforge.fluids.FluidAttributes;
import seraphaestus.factoriores.FactoriOres;
import seraphaestus.factoriores.Registrar;

public abstract class FluidSulfuricAcid extends FlowingFluid {
	
	@Override
	protected FluidAttributes createAttributes() {
		return FluidAttributes.builder(
                new ResourceLocation(FactoriOres.MOD_ID, "block/sulfuric_acid_still"),
                new ResourceLocation(FactoriOres.MOD_ID, "block/sulfuric_acid_flow"))
                .translationKey("block.factoriores.sulfuric_acid")
                .sound(SoundEvents.ITEM_BUCKET_FILL, SoundEvents.ITEM_BUCKET_EMPTY)
                .build(this);
	}
	
	public Fluid getFlowingFluid() {
		return Registrar.flowingSulfuricAcid.get();
	}

	public Fluid getStillFluid() {
		return Registrar.sulfuricAcid.get();
	}

	public Item getFilledBucket() {
		return Registrar.sulfuricAcidBucket.get();
	}
	
	protected void beforeReplacingBlock(IWorld world, BlockPos pos, BlockState state) {
	}

	public int getSlopeFindDistance(IWorldReader reader) {
		return 4;
	}

	public BlockState getBlockState(FluidState state) {
		return Registrar.blockSulfuricAcid.get().getDefaultState().with(FlowingFluidBlock.LEVEL, Integer.valueOf(getLevelFromState(state)));
	}

	public boolean isEquivalentTo(Fluid fluid) {
		return fluid == Registrar.sulfuricAcid.get() || fluid == Registrar.flowingSulfuricAcid.get();
	}

	public int getLevelDecreasePerBlock(IWorldReader reader) {
		return 1;
	}
	
	//TODO?
	public boolean canDisplace(FluidState state, IBlockReader reader, BlockPos pos, Fluid fluid, Direction direction) {
		return state.getActualHeight(reader, pos) >= 0.44444445F && fluid.isIn(FluidTags.WATER);
	}

	public int getTickRate(IWorldReader reader) {
		return 20;
	}

	protected boolean canSourcesMultiply() {
		return false;
	}

	protected float getExplosionResistance() {
		return 5.0F;
	}

	public static class Flowing extends FluidSulfuricAcid {
		protected void fillStateContainer(StateContainer.Builder<Fluid, FluidState> state) {
			super.fillStateContainer(state);
			state.add(LEVEL_1_8);
		}

		public int getLevel(FluidState state) {
			return state.get(LEVEL_1_8);
		}

		public boolean isSource(FluidState state) {
			return false;
		}
	}

	public static class Source extends FluidSulfuricAcid {
		public int getLevel(FluidState state) {
			return 8;
		}

		public boolean isSource(FluidState state) {
			return true;
		}
	}
}
