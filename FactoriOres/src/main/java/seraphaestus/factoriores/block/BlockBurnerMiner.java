package seraphaestus.factoriores.block;

import java.util.Random;
import java.util.function.ToIntFunction;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeHooks;
import seraphaestus.factoriores.ConfigHandler;
import seraphaestus.factoriores.tile.TileEntityBurnerMiner;

public class BlockBurnerMiner extends BlockMiner {

	public static final BooleanProperty LIT = BlockStateProperties.LIT;

	public BlockBurnerMiner(Properties properties) {
		super(properties.luminance(createLightLevelFromBlockState(13)));
		this.setDefaultState(this.stateContainer.getBaseState()
				.with(ENABLED, false)
				.with(LIT, false));
	}
	
	// -------- TileEntity stuff

	@Override
	public TileEntity createNewTileEntity(IBlockReader reader) {
		return new TileEntityBurnerMiner();
	}
	
	// -------- State/Property management
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(ENABLED, LIT);
	}
	
	private static ToIntFunction<BlockState> createLightLevelFromBlockState(int level) {
		return (state) -> {
			return state.get(BlockStateProperties.LIT) ? level : 0;
		};
	}
	
	// -------- Events

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
		
		boolean emptyHandFlag = ConfigHandler.COMMON.requireEmptyHand.get() && !player.getHeldItem(hand).isEmpty();
		
		// on client side, don't do anything
		if (world.isRemote) return emptyHandFlag ? ActionResultType.PASS : ActionResultType.SUCCESS;

		TileEntity tileentity = world.getTileEntity(pos);
		if (tileentity instanceof TileEntityBurnerMiner) {
			TileEntityBurnerMiner tileEntityMiner = (TileEntityBurnerMiner) tileentity;
			ItemStack heldItem = player.getHeldItem(hand);
			if (ForgeHooks.getBurnTime(heldItem) > 0) {
				// add fuel to the miner
				boolean success = tileEntityMiner.addFuel(new ItemStack(heldItem.getItem(), 1));
				if (success && !player.abilities.isCreativeMode) heldItem.shrink(1);
			} else {
				if (emptyHandFlag) return ActionResultType.PASS;
				// retrieve products from the miner
				player.addItemStackToInventory(tileEntityMiner.retrieveItems());
			}
		}

		return ActionResultType.SUCCESS;
	}
	
	// -------- Client-side effects
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(BlockState state, World world, BlockPos pos, Random rnd) {
		if (state.get(LIT)) {
			double d0 = (double) pos.getX() + 0.5D;
			double d1 = (double) pos.getY();
			double d2 = (double) pos.getZ() + 0.5D;
			if (rnd.nextDouble() < 0.1D && !ConfigHandler.COMMON.silentMiners.get()) {
				world.playSound(d0, d1, d2, SoundEvents.BLOCK_FURNACE_FIRE_CRACKLE, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
			}
			
			if (rnd.nextDouble() < ConfigHandler.CLIENT.furnaceParticleFrequency.get()) {
				for (Direction direction : Direction.values()) {
					if (direction == Direction.UP || direction == Direction.DOWN) continue;
					Direction.Axis direction$axis = direction.getAxis();
					//double d3 = 0.52D;
					double d4 = rnd.nextDouble() * 0.6D - 0.3D;
					double d5 = direction$axis == Direction.Axis.X ? (double) direction.getXOffset() * 0.52D : d4;
					double d6 = (rnd.nextDouble() * 6.0D + 2.0D) / 16.0D;
					double d7 = direction$axis == Direction.Axis.Z ? (double) direction.getZOffset() * 0.52D : d4;
					world.addParticle(ParticleTypes.SMOKE, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
					world.addParticle(ParticleTypes.FLAME, d0 + d5, d1 + d6, d2 + d7, 0.0D, 0.0D, 0.0D);
				}
			}
			
		}
		super.animateTick(state, world, pos, rnd);
	}

}
