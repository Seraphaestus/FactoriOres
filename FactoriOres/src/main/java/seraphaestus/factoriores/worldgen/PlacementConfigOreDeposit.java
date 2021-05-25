package seraphaestus.factoriores.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.world.gen.placement.IPlacementConfig;

public class PlacementConfigOreDeposit implements IPlacementConfig {
	public static final Codec<PlacementConfigOreDeposit> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("rarity").forGetter((config) -> { return config.rarity; }),
			Codec.INT.fieldOf("generation_depth").forGetter((config) -> { return config.genDepth; }),
			Codec.STRING.fieldOf("generation_distance").forGetter((config) -> { return config.distance.toString(); })
		).apply(instance, PlacementConfigOreDeposit::new));
	
	public final int rarity;
	public final int genDepth;
	public final GenDistance distance;
	
	/**
	 * @param rarity  The chance per placement that the deposit patch will generate will be the reciprical of this value (for reference, vanilla water lakes = 4)
	 * @param genDepth The amount of blocks below ground level in which a deposit can generate in
	 * @param genDistance The distance category of the ore deposit, controlling how far from 0,0 it will start generating at
	 */
	public PlacementConfigOreDeposit(int rarity, int genDepth, String genDistance) {
		this.rarity = rarity;
		this.genDepth = genDepth;
		switch (genDistance) {
			case "ALWAYS":
				this.distance = GenDistance.ALWAYS;
				break;
			case "NEAR":
				this.distance = GenDistance.NEAR;
				break;
			case "MID":
				this.distance = GenDistance.MID;
				break;
			case "FAR":
				this.distance = GenDistance.FAR;
				break;
			case "DISABLED":
			default:
				this.distance = GenDistance.DISABLED;
				break;
		}
	}
	
	public enum GenDistance {
	    DISABLED, ALWAYS, NEAR, MID, FAR
	}
}
