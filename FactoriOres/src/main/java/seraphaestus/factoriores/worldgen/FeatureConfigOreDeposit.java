package seraphaestus.factoriores.worldgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.world.gen.feature.IFeatureConfig;

public class FeatureConfigOreDeposit implements IFeatureConfig {
	
	public static final Codec<FeatureConfigOreDeposit> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			BlockState.CODEC.fieldOf("state").forGetter((config) -> { return config.state; }),
			Codec.INT.fieldOf("patch_radius").forGetter((config) -> { return config.patchRadius; }),
			Codec.INT.fieldOf("patch_depth").forGetter((config) -> { return config.patchDepth; }),
			Codec.INT.fieldOf("patch_density").forGetter((config) -> { return config.patchDensity; })
		).apply(instance, FeatureConfigOreDeposit::new));
			
	public final BlockState state;
	public final int patchRadius;
	public final int patchDepth;
	public final int patchDensity;
	
	/**
	 * @param  state  The block type the deposit will consist of
	 * @param patchRadius The radius of the ore deposit patch (unimplemented)
	 * @param patchDepth The depth of the ore deposit patch (unimplemented)
	 * @param patchDensity The lower the number, the higher the frequency at which gangue generates instead of an ore block, within the patch. Set <= 0 for 0 frequency
	 */
	public FeatureConfigOreDeposit(BlockState state, int patchRadius, int patchDepth, int patchDensity) {
		this.state = state;
		this.patchRadius = patchRadius;
		this.patchDepth = patchDepth;
		this.patchDensity = patchDensity;
	}
}