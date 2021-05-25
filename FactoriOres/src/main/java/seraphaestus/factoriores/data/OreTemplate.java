package seraphaestus.factoriores.data;

import seraphaestus.factoriores.worldgen.PlacementConfigOreDeposit.GenDistance;

public class OreTemplate {
	public String name;
	public String drop;
	public int amountMin = 10;
	public int amountMax = 50;
	public int patchRadius = 0;
	public int patchDepth = 0;
	public int patchDensity = 16;
	public int genRarity = 40;
	public int genDepth = 8;
	public GenDistance genDistance;
	public boolean isFluid = false;
	
	public OreTemplate(String name, String drop, GenDistance genDistance) {
		this.name = name;
		this.drop = drop;
		this.genDistance = genDistance;
	}
	
	public OreTemplate(String name, String drop, int amountMin, int amountMax, int patchRadius, int patchDepth, int genRarity, int genDepth, GenDistance genDistance) {
		this.name = name;
		this.drop = drop;
		this.amountMin = amountMin;
		this.amountMax = amountMax;
		this.patchRadius = patchRadius;
		this.patchDepth = patchDepth;
		this.genRarity = genRarity;
		this.genDepth = genDepth;
		this.genDistance = genDistance;
	}
	
	public OreTemplate setFluid() {
		this.isFluid = true;
		return this;
	}
}
