package seraphaestus.factoriores.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import seraphaestus.factoriores.tile.TileEntityCreativeMiner;

public class BlockCreativeMiner extends BlockMiner {

	public BlockCreativeMiner(Properties properties) {
		super(properties);
	}
	
	// -------- TileEntity stuff

	@Override
	public TileEntity createNewTileEntity(IBlockReader reader) {
		return new TileEntityCreativeMiner();
	}

}
