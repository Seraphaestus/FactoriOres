package seraphaestus.factoriores.data;

import net.minecraft.nbt.CompoundNBT;

public class StateDataBurnerMiner extends StateDataMiner {

	// The initial fuel value of the currently burning fuel (in ticks of burn duration)
	public int fuelBurnProgress;
	// The number of burn ticks remaining on the current piece of fuel
	public int fuelBurnTotalTime;

	// --------- read/write to NBT for permanent storage (on disk, or packet transmission) - used by the TileEntity only

	@Override
	public void putIntoNBT(CompoundNBT nbtTagCompound) {
		nbtTagCompound.putInt("MiningProgress", miningProgress);
		nbtTagCompound.putInt("MiningTotalTime", miningTotalTime);
		nbtTagCompound.putInt("FuelBurnProgress", fuelBurnProgress);
		nbtTagCompound.putInt("FuelBurnTotalTime", fuelBurnTotalTime);
	}
	
	@Override
	public void readFromNBT(CompoundNBT nbtTagCompound) {
		// Trim the arrays (or pad with 0) to make sure they have the correct number of elements
		miningProgress = nbtTagCompound.getInt("MiningProgress");
		miningTotalTime = nbtTagCompound.getInt("MiningTotalTime");
		fuelBurnProgress = nbtTagCompound.getInt("FuelBurnProgress");
		fuelBurnTotalTime = nbtTagCompound.getInt("FuelBurnTotalTime");
	}

	// -------- used by vanilla, not intended for mod code
	// * The ints are mapped (internally) as:
	// * 0 = miningProgress
	// * 1 = miningTotalTime
	// * 2 = fuelBurnProgress
	// * 3 = fuelBurnTotalTime

	// protected final int MINE_PROGRESS_INDEX = 0;
	// protected final int MINE_TOTAL_INDEX = 1;
	private final int BURNTIME_INDEX = 2;
	private final int BURNTIME_TOTAL_INDEX = 3;

	@Override
	public int get(int index) {
		validateIndex(index);
		if (index == MINE_PROGRESS_INDEX) {
			return miningProgress;
		} else if (index == MINE_TOTAL_INDEX) {
			return miningTotalTime;
		} else if (index == BURNTIME_INDEX) {
			return fuelBurnProgress;
		} else {
			return fuelBurnTotalTime;
		}
	}

	@Override
	public void set(int index, int value) {
		validateIndex(index);
		if (index == MINE_PROGRESS_INDEX) {
			miningProgress = value;
		} else if (index == MINE_TOTAL_INDEX) {
			miningTotalTime = value;
		} else if (index == BURNTIME_INDEX) {
			fuelBurnProgress = value;
		} else if (index == BURNTIME_TOTAL_INDEX) {
			fuelBurnTotalTime = value;
		}
	}

	@Override
	public int size() {
		return 4;
	}
}
