package seraphaestus.factoriores.compat;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;

import com.simibubi.create.AllTags.AllBlockTags;
import com.simibubi.create.Create;
import com.simibubi.create.content.AllSections;
import com.simibubi.create.foundation.data.CreateRegistrate;
import com.simibubi.create.foundation.data.SharedProperties;
import com.simibubi.create.foundation.ponder.PonderRegistry;
import com.simibubi.create.foundation.ponder.PonderRegistry.MultiSceneBuilder;
import com.simibubi.create.foundation.ponder.content.PonderTag;
import com.simibubi.create.repack.registrate.util.NonNullLazyValue;
import com.simibubi.create.repack.registrate.util.entry.BlockEntry;
import com.simibubi.create.repack.registrate.util.entry.TileEntityEntry;

import net.minecraft.block.AbstractBlock;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import seraphaestus.factoriores.ConfigHandler;
import seraphaestus.factoriores.FactoriOres;
import seraphaestus.factoriores.StartupCommon;
import seraphaestus.factoriores.block.BlockMechanicalMiner;
import seraphaestus.factoriores.item.BlockItemMiner;
import seraphaestus.factoriores.ponder.PonderScenes;
import seraphaestus.factoriores.render.RendererMechanicalMiner;
import seraphaestus.factoriores.tile.InstanceMinerCog;
import seraphaestus.factoriores.tile.TileEntityMechanicalMiner;

public class CreateRegistrar {
	
	// -------- Register
	
	public static NonNullLazyValue<CreateRegistrate> createRegistrate = CreateRegistrate.lazy(FactoriOres.MOD_ID);
	
	// -------- Blocks with tile entities
	
	public static BlockEntry<BlockMechanicalMiner> blockMechanicalMiner = createRegistrate.get()
			.itemGroup(() -> StartupCommon.ITEM_GROUP)
			.block("mechanical_miner", BlockMechanicalMiner::new)
				.initialProperties(SharedProperties::softMetal)
				.properties(AbstractBlock.Properties::nonOpaque)
				.properties(p -> p.hardnessAndResistance(3.5F, 3.5F))
				.properties(p -> p.requiresTool())
				.tag(AllBlockTags.SAFE_NBT.tag) // Unsure what this tag means (contraption safe?)
				.item(BlockItemMiner::new)
				.transform(customItemModel())
				.register();
	public static TileEntityEntry<TileEntityMechanicalMiner> tileMechanicalMiner = createRegistrate.get()
			.itemGroup(() -> StartupCommon.ITEM_GROUP)
			.tileEntity("mechanical_miner_tile", TileEntityMechanicalMiner::new)
				.instance(() -> InstanceMinerCog::new)
				.validBlocks(blockMechanicalMiner)
				.renderer(() -> RendererMechanicalMiner::new)
				.register();
	
	public static void init() {
		Create.registrate().addToSection(blockMechanicalMiner, AllSections.KINETICS);
	}
	
	@OnlyIn(Dist.CLIENT)
	public static void registerPondering() {
		MultiSceneBuilder msb = PonderRegistry.forComponents(blockMechanicalMiner)
			.addStoryBoard("mechanical_miner", PonderScenes::mechanicalMiner, PonderTag.KINETIC_APPLIANCES);
		if (ConfigHandler.CLIENT.enableLixiviantPonder.get())
			msb.addStoryBoard("mechanical_miner_lixiviant", PonderScenes::mechanicalMinerLixiviant);
		if (ConfigHandler.CLIENT.enableFluidMiningPonder.get())
			msb.addStoryBoard("mechanical_miner_fluid_deposits", PonderScenes::mechanicalMinerFluidDeposits);
		
		PonderRegistry.tags.forTag(PonderTag.KINETIC_APPLIANCES).add(blockMechanicalMiner);
	}

}
