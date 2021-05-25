package seraphaestus.factoriores.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public abstract class BlockTEBase extends Block implements ITileEntityProvider {
	
	// -------- TileEntity stuff
	
	public BlockTEBase(Properties properties) {
		super(properties);
	}

	public boolean eventReceived(BlockState state, World world, BlockPos pos, int int1, int int2) {
		super.eventReceived(state, world, pos, int1, int2);
		TileEntity tileentity = world.getTileEntity(pos);
		return tileentity == null ? false : tileentity.receiveClientEvent(int1, int2);
	}

	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return createNewTileEntity(world);
	}

}
