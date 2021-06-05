package seraphaestus.factoriores.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import seraphaestus.factoriores.ConfigHandler;
import seraphaestus.factoriores.Registrar;
import seraphaestus.factoriores.block.BlockElectricalMiner;
import seraphaestus.factoriores.energy.InternalEnergyStorage;

public class TileEntityElectricalMiner extends TileEntityMiner {

	private final InternalEnergyStorage energy;
	private static final int ENERGY_PER_COMPLETED_MINE = 1000;
	private static final int ENERGY_CAPACITY = 16000;
	
	public TileEntityElectricalMiner() {
		super(Registrar.tileElectricalMiner.get());
		energy = new InternalEnergyStorage(ENERGY_CAPACITY, 256, 0);
		energyHandlerLazy = LazyOptional.of(() -> energy);
	}

	@Override
	public void tick() {
		int energyStoredFlag = energy.getEnergyStored();
		
		boolean hasChanged = doMining();
		
		int energyStored = energy.getEnergyStored();
		if (energyStoredFlag != energyStored) {
			hasChanged = true;
			
			int lit = 3;
			if 		(energyStored <= ENERGY_CAPACITY * 0.25) lit = 0;
			else if (energyStored <= ENERGY_CAPACITY * 0.50) lit = 1;
			else if (energyStored <= ENERGY_CAPACITY * 0.75) lit = 2;
			
			if (world.getBlockState(pos).get(BlockElectricalMiner.LIT) != lit) 
				world.setBlockState(pos, world.getBlockState(pos).with(BlockElectricalMiner.LIT, Integer.valueOf(lit)), 3);
		}
		
		if (hasChanged) markDirty();
	}

	@Override
	public boolean calculateIsEnabled() {
		return energy.getEnergyStored() >= getEnergyConsumedPerProgressMade() && super.calculateIsEnabled();
	}

	@Override
	public boolean onProgressMade() {
		energy.consumeEnergy(getEnergyConsumedPerProgressMade());
		return true;
	}
	
	public int getEnergyConsumedPerProgressMade() {
		return ENERGY_PER_COMPLETED_MINE / minerStateData.miningTotalTime;
	}
	
	@Override
	protected int getTotalMiningTime() {
		return ConfigHandler.COMMON.minerSpeedElectrical.get();
	}
	
	@Override
	public int getRange() {
		return ConfigHandler.COMMON.minerRangeElectrical.get();
	}
	
	// -------- Data (NBT & Packets) methods

	@Override
	public CompoundNBT write(CompoundNBT parentNBTTagCompound) {
		super.write(parentNBTTagCompound);
		
		energy.write(parentNBTTagCompound);
		
		return parentNBTTagCompound;
	}

	@Override
	public void fromTag(BlockState blockState, CompoundNBT nbtTagCompound) {
		super.fromTag(blockState, nbtTagCompound);
		
		energy.read(nbtTagCompound);
	}
	
	// -------- Expose for automation
	
	LazyOptional<IEnergyStorage> energyHandlerLazy;
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		if (cap == CapabilityEnergy.ENERGY) {
			return CapabilityEnergy.ENERGY.orEmpty(cap, energyHandlerLazy);
		}
		return super.getCapability(cap, side);
	}
	
	@Override
	public void remove() {
	  super.remove();
	  energyHandlerLazy.invalidate();
	}

}
