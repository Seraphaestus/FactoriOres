package seraphaestus.factoriores.block;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import seraphaestus.factoriores.Registrar;

public class BlockSulfur extends Block {

	public BlockSulfur(Properties properties) {
		super(properties);
	}
	
	@Override
	public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return 300;
    }
	
	@Override
	public int getFireSpreadSpeed(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return 300;
    }
	
	// triggers when fire consumes the block
	@Override
	public void catchFire(BlockState state, World world, BlockPos pos, @Nullable Direction face, @Nullable LivingEntity igniter) {
		world.setBlockState(pos, Registrar.blockSulfuricAcid.get().getDefaultState(), 2);
	}
	
	public BlockState updatePostPlacement(BlockState state, Direction face, BlockState state2, IWorld world, BlockPos pos, BlockPos pos2) {
		if (state2.getBlock() == Blocks.FIRE) {
			for (Direction direction : Direction.values()) {
				if (world.getBlockState(pos.offset(direction)).getBlock() != Registrar.sulfurBlock) continue;
				BlockPos targetPos = pos.offset(direction).offset(face);
				catchFireFromSameSide(world, targetPos, face);
			}
		}
		return super.updatePostPlacement(state, face, state2, world, pos, pos2);
	}
	
	private static void catchFireFromSameSide(IWorld world, BlockPos targetPos, @Nullable Direction face) {
		if (face == null) return;
		if (world.getBlockState(targetPos).getBlock() == Blocks.AIR) {
			BlockState fireState = Blocks.FIRE.getDefaultState();
			switch (face) {
				case DOWN:
					fireState = fireState.with(FireBlock.UP, true);
					break;
				case NORTH:
					fireState = fireState.with(FireBlock.SOUTH, true);
					break;
				case EAST:
					fireState = fireState.with(FireBlock.WEST, true);
					break;
				case SOUTH:
					fireState = fireState.with(FireBlock.NORTH, true);
					break;
				case WEST:
					fireState = fireState.with(FireBlock.EAST, true);
					break;
				default: return;
			}
			world.setBlockState(targetPos, fireState, 1|2);
		}
	}
}
