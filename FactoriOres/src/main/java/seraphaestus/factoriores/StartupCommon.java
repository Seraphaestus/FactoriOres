package seraphaestus.factoriores;

import net.minecraft.block.Blocks;
import net.minecraft.data.DataGenerator;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import seraphaestus.factoriores.command.RegisterCommandsHandler;
import seraphaestus.factoriores.compat.TOPCompat;
import seraphaestus.factoriores.data.client.LangProvider;
import seraphaestus.factoriores.data.client.OreBlockStateProvider;
import seraphaestus.factoriores.data.server.OreLootTableProvider;

public class StartupCommon {

	public static ItemGroup ITEM_GROUP = new ItemGroup(FactoriOres.MOD_ID) {
        @Override
        public ItemStack createIcon() {
            return new ItemStack(Blocks.COAL_ORE);
        }
    };
	
	@SubscribeEvent
	public static void onDataGeneration(GatherDataEvent event) {
		DataGenerator gen = event.getGenerator();

		if (event.includeClient()) {
			gen.addProvider(new LangProvider(gen));
			gen.addProvider(new OreBlockStateProvider(gen, event.getExistingFileHelper()));
		}
		if (event.includeServer()) {
			gen.addProvider(new OreLootTableProvider(gen));
		}
	}

	@SubscribeEvent
	public static void onCommonSetupEvent(FMLCommonSetupEvent event) {
		MinecraftForge.EVENT_BUS.register(RegisterCommandsHandler.class);
	}
	
	@SubscribeEvent
    public static void enqueueIMC(InterModEnqueueEvent event) {
        if (ModList.get().isLoaded("theoneprobe")) {
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", TOPCompat::new);
        }
    }
}
