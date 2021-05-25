package seraphaestus.factoriores.block;

import java.util.function.ToIntFunction;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;
import seraphaestus.factoriores.tile.TileEntityElectricalMiner;

public class BlockElectricalMiner extends BlockMiner {
	
	public static final IntegerProperty LIT = IntegerProperty.create("lit", 0, 3);

	public BlockElectricalMiner(Properties properties) {
		super(properties.luminance(createLightLevelFromBlockState(6)));
		this.setDefaultState(this.stateContainer.getBaseState()
				.with(ENABLED, false)
				.with(LIT, 0));
	}
	
	// -------- TileEntity stuff

	@Override
	public TileEntity createNewTileEntity(IBlockReader reader) {
		return new TileEntityElectricalMiner();
	}
	
	// -------- State/Property management
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(ENABLED, LIT);
	}
	
	private static ToIntFunction<BlockState> createLightLevelFromBlockState(int level) {
		return (state) -> {
			switch (state.get(BlockElectricalMiner.LIT)) {
				case 3: return 6;
				case 2: return 4;
				case 1: return 2;
				default: return 0;
			}
		};
	}

}
