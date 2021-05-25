package seraphaestus.factoriores.tile;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

public class TileEntityBase extends TileEntity {

	public TileEntityBase(TileEntityType<?> type) {
		super(type);
	}
	
	// -------- Data (NBT & Packets) methods
	
	@Override
	@Nullable
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT updateTagDescribingTileEntityState = getUpdateTag();
		final int METADATA = 42; // arbitrary.
		return new SUpdateTileEntityPacket(this.pos, METADATA, updateTagDescribingTileEntityState);
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		CompoundNBT updateTagDescribingTileEntityState = pkt.getNbtCompound();
		BlockState blockState = world.getBlockState(pos);
		handleUpdateTag(blockState, updateTagDescribingTileEntityState);
	}

	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT nbtTagCompound = new CompoundNBT();
		write(nbtTagCompound);
		return nbtTagCompound;
	}

	@Override
	public void handleUpdateTag(BlockState blockState, CompoundNBT tag) {
		fromTag(blockState, tag);
	}

}
