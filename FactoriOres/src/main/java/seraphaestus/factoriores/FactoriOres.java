package seraphaestus.factoriores;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import seraphaestus.factoriores.compat.CreateRegistrar;
import seraphaestus.factoriores.event.ClientTooltipHandler;

@Mod("factoriores")
public class FactoriOres {
	
	public static final String MOD_ID = "factoriores";
	public static final Logger LOGGER = LogManager.getLogger();
	
	public static volatile boolean configLoaded = false;
	
	public static boolean IE_ACTIVE = false;
    public static boolean CREATE_ACTIVE = false;
    
    public FactoriOres() {
    	final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
    	final ClientModEventRegistrar clientModEventRegistrar = new ClientModEventRegistrar(modEventBus);
    	
    	IE_ACTIVE = ModList.get().isLoaded("immersiveengineering");
    	CREATE_ACTIVE = ModList.get().isLoaded("create");

    	modEventBus.register(StartupCommon.class);
    	DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> clientModEventRegistrar::registerClientOnlyEvents);
    	
    	ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ConfigHandler.CLIENT_SPEC);
		ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC);
        
		modEventBus.addListener((ModConfig.Loading e) -> ConfigHandler.onConfigLoad());
		modEventBus.addListener((ModConfig.Reloading e) -> ConfigHandler.onConfigLoad());
		
		if (CREATE_ACTIVE) MinecraftForge.EVENT_BUS.register(ClientTooltipHandler.class);
		
        //modEventBus.register(Registrar.class);
        Registrar.init();
        if (CREATE_ACTIVE) CreateRegistrar.init();
    }
}
