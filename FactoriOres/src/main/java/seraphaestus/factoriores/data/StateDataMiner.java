package seraphaestus.factoriores.data;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.IIntArray;

public class StateDataMiner implements IIntArray {

	// The number of ticks that the current action has been going
	public int miningProgress;
	// The number of ticks required to complete the current action (i.e complete when miningTimeElapsed == miningTimeForCompletion
	public int miningTotalTime;

	// --------- read/write to NBT for permanent storage (on disk, or packet transmission) - used by the TileEntity only

	public void putIntoNBT(CompoundNBT nbtTagCompound) {
		nbtTagCompound.putInt("MiningProgress", miningProgress);
		nbtTagCompound.putInt("MiningTotalTime", miningTotalTime);
	}

	public void readFromNBT(CompoundNBT nbtTagCompound) {
		// Trim the arrays (or pad with 0) to make sure they have the correct number of elements
		miningProgress = nbtTagCompound.getInt("MiningProgress");
		miningTotalTime = nbtTagCompound.getInt("MiningTotalTime");
	}

	// -------- used by vanilla, not intended for mod code
	// * The ints are mapped (internally) as:
	// * 0 = miningProgress
	// * 1 = miningTotalTime

	protected final int MINE_PROGRESS_INDEX = 0;
	protected final int MINE_TOTAL_INDEX = 1;

	@Override
	public int get(int index) {
		validateIndex(index);
		if (index == MINE_PROGRESS_INDEX) {
			return miningProgress;
		} else {
			return miningTotalTime;
		}
	}

	@Override
	public void set(int index, int value) {
		validateIndex(index);
		if (index == MINE_PROGRESS_INDEX) {
			miningProgress = value;
		} else if (index == MINE_TOTAL_INDEX) {
			miningTotalTime = value;
		}
	}

	@Override
	public int size() {
		return 2;
	}

	protected void validateIndex(int index) throws IndexOutOfBoundsException {
		if (index < 0 || index >= size()) {
			throw new IndexOutOfBoundsException("Index out of bounds:" + index);
		}
	}
}
