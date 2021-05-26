package seraphaestus.factoriores.worldgen;

import java.util.Random;
import java.util.stream.Stream;

import com.mojang.serialization.Codec;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;
import net.minecraft.world.gen.placement.Placement;
import seraphaestus.factoriores.ConfigHandler;
import seraphaestus.factoriores.worldgen.PlacementConfigOreDeposit.GenDistance;

public class PlacementOreDeposit extends Placement<PlacementConfigOreDeposit> {
	
	public PlacementOreDeposit(Codec<PlacementConfigOreDeposit> codec) {
		super(codec);
	}

	@Override
	public Stream<BlockPos> getPositions(WorldDecoratingHelper decorationHelper, Random rnd, PlacementConfigOreDeposit config, BlockPos pos) {
		
		boolean failFlag = false;
		int distanceFromZero = (int)Math.hypot(pos.getX(), pos.getZ());
		
		if (config.rarity <= 0 || config.distance == GenDistance.DISABLED) failFlag = true;
		else if (config.distance == GenDistance.NEAR && distanceFromZero < ConfigHandler.COMMON.genDistanceNear.get()) failFlag = true;
		else if (config.distance == GenDistance.MID && distanceFromZero < ConfigHandler.COMMON.genDistanceMid.get()) failFlag = true;
		else if (config.distance == GenDistance.FAR && distanceFromZero < ConfigHandler.COMMON.genDistanceFar.get()) failFlag = true;
		
		double chanceMod = 1;
		switch (config.distance) {
			case ALWAYS:
				chanceMod = ConfigHandler.COMMON.genChanceModAll.get();
				break;
			case NEAR:
				chanceMod = ConfigHandler.COMMON.genChanceModNear.get();
				break;
			case MID:
				chanceMod = ConfigHandler.COMMON.genChanceModMid.get();
				break;
			case FAR:
				chanceMod = ConfigHandler.COMMON.genChanceModFar.get();
				break;
			default:
				break;
		}
		
		int randomMax = Math.max(1, (int)Math.round(config.rarity / chanceMod));
		if (!failFlag && rnd.nextInt(randomMax) == 0) {
			int x = rnd.nextInt(16) + pos.getX();
			int z = rnd.nextInt(16) + pos.getZ();
			int y = decorationHelper.getMaxY() - rnd.nextInt(config.genDepth);
			return Stream.of(new BlockPos(x, y, z));
		} else {
			return Stream.empty();
		}
	}
}
