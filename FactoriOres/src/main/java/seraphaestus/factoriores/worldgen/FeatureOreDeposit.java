package seraphaestus.factoriores.worldgen;

import java.util.Random;

import com.mojang.serialization.Codec;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.LightType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.Structure;
import seraphaestus.factoriores.Registrar;

public class FeatureOreDeposit extends Feature<FeatureConfigOreDeposit> {
	private static final BlockState AIR = Blocks.CAVE_AIR.getDefaultState();

	public FeatureOreDeposit(Codec<FeatureConfigOreDeposit> codec) {
		super(codec);
	}

	public boolean generate(ISeedReader seedReader, ChunkGenerator chunkGen, Random rnd, BlockPos pos, FeatureConfigOreDeposit config) {
		
		// lower position of feature until it's at ground level
		while (pos.getY() > 5 && seedReader.isAirBlock(pos)) {
			pos = pos.down();
		}

		if (pos.getY() <= 4) {
			return false;
		} else {
			pos = pos.down(4);
			if (seedReader.getStructures(SectionPos.from(pos), Structure.VILLAGE).findAny().isPresent()) {
				return false;
			} else {
				boolean[] isInShape = new boolean[2048];
				int max = rnd.nextInt(4) + 4;	//[4, 8)
				
				//create the feature shape and store it in the boolean[] hash map
				for (int i = 0; i < max; ++i) {
					double d0 = rnd.nextDouble() * 6.0D + 3.0D;	//[3,9)
					double d1 = rnd.nextDouble() * 4.0D + 2.0D;	//[2,6)
					double d2 = rnd.nextDouble() * 6.0D + 3.0D;	//[3,9)
					double d3 = rnd.nextDouble() * (16.0D - d0 - 2.0D) + 1.0D + d0 / 2.0D;	//[2.5, 13.5)
					double d4 = rnd.nextDouble() * (8.0D - d1 - 4.0D) + 2.0D + d1 / 2.0D;	//[3, 5)
					double d5 = rnd.nextDouble() * (16.0D - d2 - 2.0D) + 1.0D + d2 / 2.0D;	//[2.5, 13.5)

					for (int x = 1; x < 15; ++x) {
						for (int z = 1; z < 15; ++z) {
							for (int y = 1; y < 7; ++y) {
								double d6 = ((double) x - d3) / (d0 / 2.0D);
								double d7 = ((double) y - d4) / (d1 / 2.0D);
								double d8 = ((double) z - d5) / (d2 / 2.0D);
								double d9 = d6 * d6 + d7 * d7 + d8 * d8;
								if (d9 < 1.0D) {	//if the coord (d6, d7, d8) is within a circle of radius 1
									isInShape[(x * 16 + z) * 8 + y] = true;
								}
							}
						}
					}
				}
				
				for (int x = 0; x < 16; ++x) {
					for (int z = 0; z < 16; ++z) {
						for (int y = 0; y < 8; ++y) {
							boolean flag = !isInShape[(x * 16 + z) * 8 + y] && (x < 15 && isInShape[((x + 1) * 16 + z) * 8 + y] || x > 0 && isInShape[((x - 1) * 16 + z) * 8 + y] || z < 15 && isInShape[(x * 16 + z + 1) * 8 + y]
									|| z > 0 && isInShape[(x * 16 + (z - 1)) * 8 + y] || y < 7 && isInShape[(x * 16 + z) * 8 + y + 1] || y > 0 && isInShape[(x * 16 + z) * 8 + (y - 1)]);
							if (flag) {
								Material material = seedReader.getBlockState(pos.add(x, y, z)).getMaterial();
								if (y >= 4 && material.isLiquid()) {
									return false;
								}

								if (y < 4 && !material.isSolid() && seedReader.getBlockState(pos.add(x, y, z)) != config.state) {
									return false;
								}
							}
						}
					}
				}
				
				for (int x = 0; x < 16; ++x) {
					for (int z = 0; z < 16; ++z) {
						for (int y = 0; y < 8; ++y) {
							if (isInShape[(x * 16 + z) * 8 + y]) {
								BlockState setState;
								if (y >= 5) {
									setState = AIR;
								} else {
									if (config.patchDensity > 0) {
										if (rnd.nextInt(config.patchDensity) == 0) setState = Registrar.blockGangue.getDefaultState();
										else setState = config.state;
									} else setState = config.state;
								}
								seedReader.setBlockState(pos.add(x, y, z), setState, 2);	//changed 4->5 so the surface is flush with ground level instead of depressed
							}
						}
					}
				}

				for (int x = 0; x < 16; ++x) {
					for (int z = 0; z < 16; ++z) {
						for (int y = 4; y < 8; ++y) {
							if (isInShape[(x * 16 + z) * 8 + y]) {
								BlockPos blockpos = pos.add(x, y - 1, z);
								if (isSoil(seedReader.getBlockState(blockpos).getBlock()) && seedReader.getLightLevel(LightType.SKY, pos.add(x, y, z)) > 0) {
									Biome biome = seedReader.getBiome(blockpos);
									if (biome.getGenerationSettings().getSurfaceConfig().getTop().isIn(Blocks.MYCELIUM)) {
										seedReader.setBlockState(blockpos, Blocks.MYCELIUM.getDefaultState(), 2);
									} else {
										seedReader.setBlockState(blockpos, Blocks.GRASS_BLOCK.getDefaultState(), 2);
									}
								}
							}
						}
					}
				}

				// add depleted ore to the edges
				for (int x = 0; x < 16; ++x) {
					for (int z = 0; z < 16; ++z) {
						for (int y = 0; y < 8; ++y) {
							boolean flag1 = !isInShape[(x * 16 + z) * 8 + y] && (x < 15 && isInShape[((x + 1) * 16 + z) * 8 + y] || x > 0 && isInShape[((x - 1) * 16 + z) * 8 + y] || z < 15 && isInShape[(x * 16 + z + 1) * 8 + y]
									|| z > 0 && isInShape[(x * 16 + (z - 1)) * 8 + y] || y < 7 && isInShape[(x * 16 + z) * 8 + y + 1] || y > 0 && isInShape[(x * 16 + z) * 8 + (y - 1)]);
							if (flag1 && (y < 4 || rnd.nextInt(2) != 0) && seedReader.getBlockState(pos.add(x, y, z)).getMaterial().isSolid()) {
								seedReader.setBlockState(pos.add(x, y, z), Registrar.blockGangue.getDefaultState(), 2);
							}
						}
					}
				}

				return true;
			}
		}
	}
}
