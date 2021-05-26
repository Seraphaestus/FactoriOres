package seraphaestus.factoriores;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import seraphaestus.factoriores.compat.CreateRegistrar;
import seraphaestus.factoriores.render.RendererMiner;

public class StartupClient {
	
	private static final ResourceLocation drillHeadRL = new ResourceLocation(FactoriOres.MOD_ID, "block/drill_head");
	
	@SubscribeEvent
	public static void onClientSetupEvent(FMLClientSetupEvent event) {
		if (FactoriOres.CREATE_ACTIVE) {
			RenderTypeLookup.setRenderLayer(CreateRegistrar.blockMechanicalMiner.get(), RenderType.getCutout());
			
			CreateRegistrar.registerPondering();
		}
	}
	
	@SubscribeEvent
	public static void onModelRegister(ModelRegistryEvent evt) {
		ModelLoader.addSpecialModel(drillHeadRL);
		
		ClientRegistry.bindTileEntityRenderer(Registrar.tileCreativeMiner.get(), RendererMiner::new);
		ClientRegistry.bindTileEntityRenderer(Registrar.tileBurnerMiner.get(), RendererMiner::new);
		ClientRegistry.bindTileEntityRenderer(Registrar.tileElectricalMiner.get(), RendererMiner::new);
		
		//PARTIAL_MODEL_TODORendererMechanicalMiner.DRILL_HEAD = new PartialModel(drillHeadRL);
	}
	
	@SubscribeEvent
	public static void onModelBake(ModelBakeEvent event) {
		RendererMiner.DRILL_HEAD = event.getModelRegistry().get(drillHeadRL);
	}
	
}
