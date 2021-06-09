package seraphaestus.factoriores.block;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import seraphaestus.factoriores.ConfigHandler;
import seraphaestus.factoriores.tile.TileEntityMechanicalMiner;
import seraphaestus.factoriores.tile.TileEntityMiner;
import seraphaestus.factoriores.util.VecHelper;

public abstract class BlockMiner extends BlockTEBase implements ITileEntityProvider {
	
	public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
	
	public BlockMiner(Properties properties) {
		super(properties.hardnessAndResistance(3.5F, 3.5F).requiresTool().nonOpaque());
		this.setDefaultState(this.stateContainer.getBaseState()
				.with(ENABLED, false));
	}
	
	// -------- State/Property management
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState().with(ENABLED, false);
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(ENABLED);
	}
	
	// -------- Events

	@Override
	public void onReplaced(BlockState state, World world, BlockPos blockPos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			TileEntity tileentity = world.getTileEntity(blockPos);
			if (tileentity instanceof TileEntityMiner) {
				TileEntityMiner tileEntityMiner = (TileEntityMiner) tileentity;
				tileEntityMiner.dropAllContents(world, blockPos);
			}
			world.updateComparatorOutputLevel(blockPos, this);
			super.onReplaced(state, world, blockPos, newState, isMoving); // call it last, because it removes the TileEntity
		}
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult rayTraceResult) {
		
		// cancel if the player's main hand is not empty
		if (!player.getHeldItem(hand).isEmpty()) return ActionResultType.PASS;
		
		if (world.isRemote) return ActionResultType.SUCCESS; // on client side, don't do anything
		
		TileEntity tileentity = world.getTileEntity(pos);
		if (tileentity instanceof TileEntityMiner) {
			TileEntityMiner tileEntityMiner = (TileEntityMiner) tileentity;
			player.addItemStackToInventory(tileEntityMiner.retrieveItems());
		}

		return ActionResultType.SUCCESS;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		if (!ConfigHandler.COMMON.canPlaceAdjacent.get()) {
			if (adjacentToMiners(state, world, pos)) return false;
		}
		
		return world.getBlockState(pos.down().down()).getBlock() instanceof BlockOre;
	}
	
	public static boolean adjacentToMiners(BlockState state, IWorldReader world, BlockPos pos) {
		for (int x = -1; x <= 1; x++) {
			for (int z = -1; z <= 1; z++) {
				if (x == 0 && z == 0) continue;
				TileEntity check = world.getTileEntity(pos.add(x, 0, z));
				if (check == null) continue;
				if (check instanceof TileEntityMiner || check instanceof TileEntityMechanicalMiner) 
					return true;
			}
		}
		return false;
	}
	
	// -------- Client-side effects

	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, World world, BlockPos pos, Random rnd) {
		TileEntity tileentity = world.getTileEntity(pos);
		if (tileentity instanceof TileEntityMiner) {
			TileEntityMiner tileEntityMiner = (TileEntityMiner) tileentity;
			if (state.get(ENABLED) && rnd.nextDouble() < ConfigHandler.CLIENT.miningParticleFrequency.get()) {
				BlockParticleData particle = new BlockParticleData(ParticleTypes.BLOCK, tileEntityMiner.getOreState());
				createMiningParticle(particle, world, pos, rnd);
			}
		}
	}
	
	// Adapted from Create's mechanical mixer particles
	public static void createMiningParticle(IParticleData particle, World world, BlockPos pos, Random rnd) {
		float angle = rnd.nextFloat() * 360;
		Vector3d offset = new Vector3d(0, 0, 0.30f);
		offset = VecHelper.rotate(offset, angle, Axis.Y);
		Vector3d target = offset.mul(1.1f, 1f, 1.1f).add(0, .15f, 0);
		Vector3d center = offset.add(VecHelper.getCenterOf(pos).add(0, -1.4, 0));
		target = VecHelper.offsetRandomly(target.subtract(offset), world.rand, 1 / 128f);
		world.addParticle(particle, center.x, center.y, center.z, target.x, target.y, target.z);
	}
	
	// -------- Redstone integration

	@Override
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(BlockState state, World world, BlockPos pos) {
		return state.get(ENABLED) ? 15 : 0;
	}
}
