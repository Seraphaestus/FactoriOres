package seraphaestus.factoriores;

import static com.simibubi.create.foundation.data.ModelGen.customItemModel;
import static seraphaestus.factoriores.FactoriOres.MOD_ID;

import java.util.ArrayList;
import java.util.List;

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
import net.minecraft.block.AbstractBlock.Properties;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BucketItem;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ITag.INamedTag;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import seraphaestus.factoriores.block.BlockBurnerMiner;
import seraphaestus.factoriores.block.BlockCreativeMiner;
import seraphaestus.factoriores.block.BlockElectricalMiner;
import seraphaestus.factoriores.block.BlockMechanicalMiner;
import seraphaestus.factoriores.block.BlockOre;
import seraphaestus.factoriores.block.BlockOreFluid;
import seraphaestus.factoriores.block.BlockSulfur;
import seraphaestus.factoriores.data.OreTemplate;
import seraphaestus.factoriores.fluid.FluidBlockSulfuricAcid;
import seraphaestus.factoriores.fluid.FluidSulfuricAcid;
import seraphaestus.factoriores.item.BlockItemMiner;
import seraphaestus.factoriores.ponder.PonderScenes;
import seraphaestus.factoriores.render.RendererMechanicalMiner;
import seraphaestus.factoriores.tile.InstanceMinerCog;
import seraphaestus.factoriores.tile.TileEntityBurnerMiner;
import seraphaestus.factoriores.tile.TileEntityCreativeMiner;
import seraphaestus.factoriores.tile.TileEntityElectricalMiner;
import seraphaestus.factoriores.tile.TileEntityMechanicalMiner;
import seraphaestus.factoriores.tile.TileEntityOre;
import seraphaestus.factoriores.tile.TileEntityOreFluid;
import seraphaestus.factoriores.worldgen.FeatureConfigOreDeposit;
import seraphaestus.factoriores.worldgen.FeatureOreDeposit;
import seraphaestus.factoriores.worldgen.PlacementConfigOreDeposit;
import seraphaestus.factoriores.worldgen.PlacementConfigOreDeposit.GenDistance;
import seraphaestus.factoriores.worldgen.PlacementOreDeposit;

public class Registrar {
	
	// -------- Registers
	
	public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MOD_ID);
	private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MOD_ID);
	private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(ForgeRegistries.FLUIDS, MOD_ID);
	private static final DeferredRegister<TileEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, MOD_ID);
	public static final NonNullLazyValue<CreateRegistrate> createRegistrate = CreateRegistrate.lazy(FactoriOres.MOD_ID);
	
	// -------- World gen registers
	
	private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MOD_ID);
	private static final DeferredRegister<Placement<?>> DECORATORS = DeferredRegister.create(ForgeRegistries.DECORATORS, MOD_ID);
	
	// -------- Tags
	
	public static final INamedTag<Fluid> tagLixiviant = FluidTags.makeWrapperTag(FactoriOres.MOD_ID + ":lixiviants");
	public static final INamedTag<Item> tagSulfurDust = ItemTags.makeWrapperTag("forge:dusts/sulfur");
	public static final INamedTag<Block> tagSulfurBlock = BlockTags.makeWrapperTag("forge:storage_blocks/sulfur");
	public static final INamedTag<Block> tagStone = BlockTags.makeWrapperTag("forge:stone");
	
	// -------- Fluids
	
    public static RegistryObject<FluidSulfuricAcid.Source> sulfuricAcid;
    public static RegistryObject<FluidSulfuricAcid.Flowing> flowingSulfuricAcid;
	public static RegistryObject<BucketItem> sulfuricAcidBucket;
	public static RegistryObject<Block> blockSulfuricAcid;
	
	// -------- Simple blocks
	
	public static final Block blockGangue = new Block(AbstractBlock.Properties.create(Material.ROCK).requiresTool().hardnessAndResistance(1.5F, 6.0F));
	public static BlockSulfur sulfurBlock;
	
	// -------- Blocks with tile entities
	
	public static List<BlockOre> oreDeposits;
	public static RegistryObject<TileEntityType<TileEntityOre>> tileOreDeposit;
	public static List<BlockOreFluid> fluidDeposits;
	public static RegistryObject<TileEntityType<TileEntityOreFluid>> tileFluidDeposit;
	
	public static final BlockCreativeMiner blockCreativeMiner = new BlockCreativeMiner(Properties.create(Material.IRON));
	public static RegistryObject<TileEntityType<TileEntityCreativeMiner>> tileCreativeMiner;

	public static final BlockBurnerMiner blockBurnerMiner = new BlockBurnerMiner(Properties.create(Material.IRON));
	public static RegistryObject<TileEntityType<TileEntityBurnerMiner>> tileBurnerMiner;

	public static final BlockElectricalMiner blockElectricalMiner = new BlockElectricalMiner(Properties.create(Material.IRON));
	public static RegistryObject<TileEntityType<TileEntityElectricalMiner>> tileElectricalMiner;

	public static final BlockEntry<BlockMechanicalMiner> blockMechanicalMiner = createRegistrate.get()
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
	public static final TileEntityEntry<TileEntityMechanicalMiner> tileMechanicalMiner = createRegistrate.get()
		.itemGroup(() -> StartupCommon.ITEM_GROUP)
		.tileEntity("mechanical_miner_tile", TileEntityMechanicalMiner::new)
			.instance(() -> InstanceMinerCog::new)
			.validBlocks(blockMechanicalMiner)
			.renderer(() -> RendererMechanicalMiner::new)
			.register();
	
	// -------- Worldgen features
	
	public static RegistryObject<Feature<FeatureConfigOreDeposit>> featureOreDeposit;
	public static List<ConfiguredFeature<?, ?>> configuredOreDepositFeatures;
	
	// -------- Other
	
	public static List<OreTemplate> oreTemplates;

	public static void init() {
		BLOCKS.register(FMLJavaModLoadingContext.get().getModEventBus());
		ITEMS.register(FMLJavaModLoadingContext.get().getModEventBus());
		FLUIDS.register(FMLJavaModLoadingContext.get().getModEventBus());
		TILES.register(FMLJavaModLoadingContext.get().getModEventBus());
		FEATURES.register(FMLJavaModLoadingContext.get().getModEventBus());
		DECORATORS.register(FMLJavaModLoadingContext.get().getModEventBus());
		
		registerSimpleBlock("gangue", blockGangue);
		if (ConfigHandler.COMMON.sulfurEnabled.get()) {
			sulfurBlock = new BlockSulfur(AbstractBlock.Properties.create(Material.SAND).hardnessAndResistance(0.5F).sound(SoundType.SAND));
			registerSimpleBlock("sulfur_block", sulfurBlock);
		}

		registerItems();
		registerFluids();
		setupOreTypes();
		registerOreBlocksTilesAndFeatures();
		registerMiners();
	}
	
	private static void registerItems() {
		ITEMS.register("drill_head", () -> new Item((new Item.Properties()).group(StartupCommon.ITEM_GROUP)));
		if (ConfigHandler.COMMON.sulfurEnabled.get()) {
			ITEMS.register("sulfur_dust", () -> new Item((new Item.Properties()).group(StartupCommon.ITEM_GROUP)));
		}
	}
	
	private static void registerFluids() {
		if (ConfigHandler.COMMON.sulfurEnabled.get()) {
			sulfuricAcid = FLUIDS.register("sulfuric_acid", () -> new FluidSulfuricAcid.Source());
		    flowingSulfuricAcid = FLUIDS.register("flowing_sulfuric_acid", () -> new FluidSulfuricAcid.Flowing());
		    sulfuricAcidBucket = ITEMS.register("sulfuric_acid_bucket", () -> new BucketItem(sulfuricAcid, (new Item.Properties()).containerItem(Items.BUCKET).maxStackSize(1).group(StartupCommon.ITEM_GROUP)));	
		    blockSulfuricAcid = BLOCKS.register("sulfuric_acid", () -> new FluidBlockSulfuricAcid(sulfuricAcid, AbstractBlock.Properties.create(Material.WATER).doesNotBlockMovement().hardnessAndResistance(100.0F).noDrops()));
		}
	}
	
	private static void setupOreTypes() {
		oreTemplates = new ArrayList<OreTemplate>();
		
		// Vanilla Ore Drops
		String ironDrop = FactoriOres.CREATE_ACTIVE ? "create:crushed_iron_ore" : "minecraft:iron_nugget";
		String goldDrop = FactoriOres.CREATE_ACTIVE ? "create:crushed_gold_ore" : "minecraft:gold_nugget";
		String sulfurDrop = ConfigHandler.COMMON.sulfurEnabled.get() ? "factoriores:sulfur_dust" : "minecraft:air";
		// Create Ore Drops
		String copperDrop = FactoriOres.CREATE_ACTIVE ? "create:crushed_copper_ore" : 
							(FactoriOres.IE_ACTIVE ? "immersiveengineering:dust_copper" : "minecraft:air");
		String zincDrop = FactoriOres.CREATE_ACTIVE ? "create:crushed_zinc_ore" : "minecraft:air";
		// Immersive Engineering Ore Drops
		String aluminumDrop = FactoriOres.CREATE_ACTIVE ? "create:crushed_aluminum_ore" : 
							(FactoriOres.IE_ACTIVE ? "immersiveengineering:dust_aluminum" : "minecraft:air");
		String leadDrop = FactoriOres.CREATE_ACTIVE ? "create:crushed_lead_ore" : 
							(FactoriOres.IE_ACTIVE ? "immersiveengineering:dust_lead" : "minecraft:air");
		String nickelDrop = FactoriOres.CREATE_ACTIVE ? "create:crushed_nickel_ore" : 
							(FactoriOres.IE_ACTIVE ? "immersiveengineering:dust_nickel" : "minecraft:air");
		String silverDrop = FactoriOres.CREATE_ACTIVE ? "create:crushed_silver_ore" : 
							(FactoriOres.IE_ACTIVE ? "immersiveengineering:dust_silver" : "minecraft:air");
		String uraniumDrop = FactoriOres.CREATE_ACTIVE ? "create:crushed_uranium_ore" : 
							(FactoriOres.IE_ACTIVE ? "immersiveengineering:dust_uranium" : "minecraft:air");
		
		//Vanilla Ores
		oreTemplates.add(new OreTemplate("coal", "minecraft:coal", GenDistance.ALWAYS));
		oreTemplates.add(new OreTemplate("iron", ironDrop, GenDistance.ALWAYS));
		oreTemplates.add(new OreTemplate("gold", goldDrop, GenDistance.MID));
		oreTemplates.add(new OreTemplate("diamond", "minecraft:diamond", GenDistance.DISABLED));
		oreTemplates.add(new OreTemplate("emerald", "minecraft:emerald", GenDistance.DISABLED));
		oreTemplates.add(new OreTemplate("redstone", "minecraft:redstone", GenDistance.MID));
		oreTemplates.add(new OreTemplate("lapis", "minecraft:lapis_lazuli", GenDistance.MID));
		oreTemplates.add(new OreTemplate("quartz", "minecraft:quartz", GenDistance.MID));
		oreTemplates.add(new OreTemplate("stone", "minecraft:stone", GenDistance.DISABLED));
		oreTemplates.add(new OreTemplate("sulfur", sulfurDrop, GenDistance.MID));
		// Create Ores
		oreTemplates.add(new OreTemplate("copper", copperDrop, FactoriOres.IE_ACTIVE || FactoriOres.CREATE_ACTIVE ? GenDistance.ALWAYS : GenDistance.DISABLED));
		oreTemplates.add(new OreTemplate("zinc", zincDrop, FactoriOres.CREATE_ACTIVE ? GenDistance.NEAR : GenDistance.DISABLED));
		// Immersive Engineering Ores
		oreTemplates.add(new OreTemplate("aluminum", aluminumDrop, FactoriOres.IE_ACTIVE ? GenDistance.NEAR : GenDistance.DISABLED));
		oreTemplates.add(new OreTemplate("lead", leadDrop, FactoriOres.IE_ACTIVE ? GenDistance.DISABLED : GenDistance.DISABLED));
		oreTemplates.add(new OreTemplate("nickel", nickelDrop, FactoriOres.IE_ACTIVE ? GenDistance.FAR : GenDistance.DISABLED));
		oreTemplates.add(new OreTemplate("silver", silverDrop, FactoriOres.IE_ACTIVE ? GenDistance.MID : GenDistance.DISABLED));
		oreTemplates.add(new OreTemplate("uranium", uraniumDrop, FactoriOres.IE_ACTIVE ? GenDistance.FAR : GenDistance.DISABLED));
		
		//fluids
		
		String oilDrop = "minecraft:air";
		
		oreTemplates.add(new OreTemplate("water", "minecraft:water", GenDistance.NEAR).setFluid());
		oreTemplates.add(new OreTemplate("lava", "minecraft:lava", GenDistance.MID).setFluid());
		oreTemplates.add(new OreTemplate("oil", oilDrop, GenDistance.FAR).setFluid());
	}

	private static void registerOreBlocksTilesAndFeatures() {
		FeatureOreDeposit oreDeposit = new FeatureOreDeposit(FeatureConfigOreDeposit.CODEC);
		featureOreDeposit = FEATURES.register("ore_deposit", () -> oreDeposit);
		
		oreDeposits = new ArrayList<BlockOre>();
		fluidDeposits = new ArrayList<BlockOreFluid>();
		configuredOreDepositFeatures = new ArrayList<ConfiguredFeature<?, ?>>();
		
		for (OreTemplate ore : oreTemplates) {
			Properties properties = Properties.create(Material.ROCK);
			BlockOre oreBlock;
			if (ore.isFluid) {
				BlockOreFluid fluidOre = new BlockOreFluid(ore.name, properties, ore.amountMin, ore.amountMax);
				fluidDeposits.add(fluidOre);
				oreBlock = fluidOre;
			} else {
				oreBlock = new BlockOre(ore.name, properties, ore.amountMin, ore.amountMax);
				oreDeposits.add(oreBlock);
			}
			
			registerSimpleBlock(oreBlock.getID(), oreBlock);
			registerOreFeature(oreBlock, ore, oreDeposit);
		}
		
		tileOreDeposit = TILES.register("ore_deposit_tile", () -> TileEntityType.Builder.create(TileEntityOre::new, oreDeposits.toArray(new BlockOre[0])).build(null));
		tileFluidDeposit = TILES.register("fluid_deposit_tile", () -> TileEntityType.Builder.create(TileEntityOreFluid::new, fluidDeposits.toArray(new BlockOreFluid[0])).build(null));
	}

	private static void registerMiners() {
		registerMinerBlock("creative_miner", blockCreativeMiner);
		tileCreativeMiner = TILES.register("creative_miner_tile", () -> TileEntityType.Builder.create(TileEntityCreativeMiner::new, blockCreativeMiner).build(null));

		registerMinerBlock("burner_miner", blockBurnerMiner);
		tileBurnerMiner = TILES.register("burner_miner_tile", () -> TileEntityType.Builder.create(TileEntityBurnerMiner::new, blockBurnerMiner).build(null));

		registerMinerBlock("electrical_miner", blockElectricalMiner);
		tileElectricalMiner = TILES.register("electrical_miner_tile", () -> TileEntityType.Builder.create(TileEntityElectricalMiner::new, blockElectricalMiner).build(null));
		
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

	private static void registerOreFeature(BlockOre oreBlock, OreTemplate oreTemplate, FeatureOreDeposit oreDeposit) {
		//Placement configuration
		PlacementOreDeposit placementOreDeposit = new PlacementOreDeposit(PlacementConfigOreDeposit.CODEC);
		
		//Register the placement decorator
		DECORATORS.register(oreBlock.getDecoratorID(), () -> placementOreDeposit);
		
		//Feature configuration
		FeatureConfigOreDeposit featureConfig = new FeatureConfigOreDeposit(oreBlock.getDefaultState(), oreTemplate.patchRadius, oreTemplate.patchDepth, oreTemplate.patchDensity);
		PlacementConfigOreDeposit placementConfig = new PlacementConfigOreDeposit(oreTemplate.genRarity, oreTemplate.genDepth, oreTemplate.genDistance.toString());
		
		final ConfiguredFeature<?, ?> configuredFeatureOreDeposit = oreDeposit
				.configure(featureConfig)
				.decorate(placementOreDeposit.configure(placementConfig));
		
		//Register the feature
		configuredOreDepositFeatures.add(configuredFeatureOreDeposit);
		Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation(FactoriOres.MOD_ID, oreBlock.getID() + "_ore_deposit"), configuredFeatureOreDeposit);
	}
	
	private static void registerSimpleBlock(String id, Block block) {
		BLOCKS.register(id, () -> block);
		
		Item.Properties itemSimpleProperties = new Item.Properties()
				.maxStackSize(64)
				.group(StartupCommon.ITEM_GROUP);
        BlockItem blockItem = new BlockItem(block, itemSimpleProperties);
        
		ITEMS.register(id, () -> blockItem);
	}
	private static void registerMinerBlock(String id, Block block) {
		BLOCKS.register(id, () -> block);
		
		Item.Properties itemSimpleProperties = new Item.Properties()
				.maxStackSize(64)
				.group(StartupCommon.ITEM_GROUP);
        BlockItemMiner blockItem = new BlockItemMiner(block, itemSimpleProperties);
        
		ITEMS.register(id, () -> blockItem);
	}
}
