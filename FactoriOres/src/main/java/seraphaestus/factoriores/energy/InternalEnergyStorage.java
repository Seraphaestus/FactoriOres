package seraphaestus.factoriores.energy;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.energy.EnergyStorage;

public class InternalEnergyStorage extends EnergyStorage {

	public InternalEnergyStorage(int capacity, int maxReceive, int maxExtract) {
		super(capacity, maxReceive, maxExtract);
	}
	
	public CompoundNBT write(CompoundNBT nbt) {
    	nbt.putInt("Energy", energy);
    	return nbt;
    }
    
    public void read(CompoundNBT nbt) {
    	setEnergy(nbt.getInt("Energy"));
    }
    
    public void setEnergy(int energy) {
    	this.energy = energy;
    }
    
    public void consumeEnergy(int amount) {
        energy -= Math.min(energy, amount);
    }

}
