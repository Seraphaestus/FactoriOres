package seraphaestus.factoriores.worldgen;

import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import seraphaestus.factoriores.ConfigHandler;
import seraphaestus.factoriores.Registrar;

@Mod.EventBusSubscriber
public class WorldGenEventHandler {
	
	@SubscribeEvent
	public static void onBiomeLoad(final BiomeLoadingEvent event) {
		if (!ConfigHandler.COMMON.worldgenEnabled.get()) return;
		
		for (ConfiguredFeature<?, ?> configuredFeatureOreDeposit : Registrar.configuredOreDepositFeatures) {
			event.getGeneration().getFeatures(GenerationStage.Decoration.UNDERGROUND_ORES).add(() -> configuredFeatureOreDeposit);
		}
	}
}
