package seraphaestus.factoriores.block;

import java.util.Random;

import com.simibubi.create.content.contraptions.base.KineticBlock;
import com.simibubi.create.content.contraptions.relays.elementary.ICogWheel;
import com.simibubi.create.foundation.block.ITE;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Direction.Axis;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import seraphaestus.factoriores.ConfigHandler;
import seraphaestus.factoriores.Registrar;
import seraphaestus.factoriores.tile.TileEntityMechanicalMiner;

public class BlockMechanicalMiner extends KineticBlock implements ITE<TileEntityMechanicalMiner>, ICogWheel {

	public static final BooleanProperty ENABLED = BlockStateProperties.ENABLED;
	
	public BlockMechanicalMiner(Properties properties) {
		super(properties);
		this.setDefaultState(this.stateContainer.getBaseState().with(ENABLED, false));
	}
	
	// -------- TileEntity stuff
	
	@Override
	public Class<TileEntityMechanicalMiner> getTileEntityClass() {
		return TileEntityMechanicalMiner.class;
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return Registrar.tileMechanicalMiner.create();
	}
	
	// -------- Create Rotation
	
	@Override
	public Axis getRotationAxis(BlockState state) {
		return Axis.Y;
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
			if (tileentity instanceof TileEntityMechanicalMiner) {
				TileEntityMechanicalMiner tileEntityMiner = (TileEntityMechanicalMiner) tileentity;
				tileEntityMiner.dropAllContents(world, blockPos);
			}
			world.updateComparatorOutputLevel(blockPos, this);
			super.onReplaced(state, world, blockPos, newState, isMoving); // call it last, because it removes the TileEntity
		}
	}

	@Override
	public ActionResultType onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand,
			BlockRayTraceResult rayTraceResult) {
		if (world.isRemote)
			return ActionResultType.SUCCESS; // on client side, don't do anything

		ItemStack heldItem = player.getHeldItem(hand);
		if (heldItem.isItemEqual(com.simibubi.create.AllBlocks.COGWHEEL.asStack()) ||
				heldItem.isItemEqual(com.simibubi.create.AllBlocks.LARGE_COGWHEEL.asStack()) ||
				heldItem.isItemEqual(com.simibubi.create.AllBlocks.MECHANICAL_ARM.asStack())) {
			//break from this function so cogwheels can be placed against the miner
			return ActionResultType.PASS;
		}
		TileEntity tileentity = world.getTileEntity(pos);
		if (tileentity instanceof TileEntityMechanicalMiner) {
			TileEntityMechanicalMiner tileEntityMiner = (TileEntityMechanicalMiner) tileentity;
			player.addItemStackToInventory(tileEntityMiner.retrieveItems());
			return ActionResultType.SUCCESS;
		}

		return ActionResultType.PASS;
	}

	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		return world.getBlockState(pos.down().down()).getBlock() instanceof BlockOre;
	}
	
	// -------- Client-side effects
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void animateTick(BlockState state, World world, BlockPos pos, Random rnd) {
		TileEntity tileentity = world.getTileEntity(pos);
		if (tileentity instanceof TileEntityMechanicalMiner) {
			TileEntityMechanicalMiner tileEntityMiner = (TileEntityMechanicalMiner) tileentity;
			if (state.get(ENABLED) && rnd.nextDouble() < ConfigHandler.CLIENT.miningParticleFrequency.get()) {
				BlockParticleData particle = new BlockParticleData(ParticleTypes.BLOCK, tileEntityMiner.getOreState());
				BlockMiner.createMiningParticle(particle, world, pos, rnd);
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public boolean isSideInvisible(BlockState state2, BlockState state1, Direction side) {
		if (side == Direction.DOWN || side == Direction.UP || !state1.isIn(this)) {
			return super.isSideInvisible(state2, state1, side);
		}
		return true;
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
