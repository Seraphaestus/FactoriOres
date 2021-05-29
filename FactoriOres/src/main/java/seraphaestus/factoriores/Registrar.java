package seraphaestus.factoriores;

import static seraphaestus.factoriores.FactoriOres.MOD_ID;

import java.util.ArrayList;
import java.util.List;

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
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import seraphaestus.factoriores.block.BlockBurnerMiner;
import seraphaestus.factoriores.block.BlockCreativeMiner;
import seraphaestus.factoriores.block.BlockElectricalMiner;
import seraphaestus.factoriores.block.BlockOre;
import seraphaestus.factoriores.block.BlockOreFluid;
import seraphaestus.factoriores.block.BlockSulfur;
import seraphaestus.factoriores.data.OreTemplate;
import seraphaestus.factoriores.fluid.FluidBlockSulfuricAcid;
import seraphaestus.factoriores.fluid.FluidSulfuricAcid;
import seraphaestus.factoriores.item.BlockItemMiner;
import seraphaestus.factoriores.item.ItemFuel;
import seraphaestus.factoriores.tile.TileEntityBurnerMiner;
import seraphaestus.factoriores.tile.TileEntityCreativeMiner;
import seraphaestus.factoriores.tile.TileEntityElectricalMiner;
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
	
	// -------- World gen registers
	
	private static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, MOD_ID);
	private static final DeferredRegister<Placement<?>> DECORATORS = DeferredRegister.create(ForgeRegistries.DECORATORS, MOD_ID);
	
	// -------- Tags
	
	public static final INamedTag<Fluid> tagLixiviant = FluidTags.makeWrapperTag(FactoriOres.MOD_ID + ":lixiviants");
	public static final INamedTag<Item> tagSulfurDust = ItemTags.makeWrapperTag("forge:dusts/sulfur");
	public static final INamedTag<Block> tagSulfurBlock = BlockTags.makeWrapperTag("forge:storage_blocks/sulfur");
	public static final INamedTag<Block> tagStone = BlockTags.makeWrapperTag("forge:stone");
	public static final INamedTag<Item> tagStoneItem = ItemTags.makeWrapperTag("forge:stone");
	
	// -------- Fluids
	
    public static RegistryObject<FluidSulfuricAcid.Source> sulfuricAcid;
    public static RegistryObject<FluidSulfuricAcid.Flowing> flowingSulfuricAcid;
	public static RegistryObject<BucketItem> sulfuricAcidBucket;
	public static RegistryObject<Block> blockSulfuricAcid;
	
	// -------- Items
	
	public static final Item drillHead = new Item((new Item.Properties()).group(StartupCommon.ITEM_GROUP));
	public static final ItemFuel coalNugget = new ItemFuel(new Item.Properties().maxStackSize(64).group(StartupCommon.ITEM_GROUP));
	
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
	
	// -------- Worldgen features
	
	public static RegistryObject<Feature<FeatureConfigOreDeposit>> featureOreDeposit;
	public static List<ConfiguredFeature<?, ?>> configuredFeaturesDeposits;
	
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
		registerOres();
		registerMiners();
		registerFeatures();
	}
	
	private static void registerItems() {
		ITEMS.register("drill_head", () -> drillHead);
		if (ConfigHandler.COMMON.sulfurEnabled.get()) {
			ITEMS.register("sulfur_dust", () -> new Item((new Item.Properties()).group(StartupCommon.ITEM_GROUP)));
		}
		ITEMS.register("coal_nugget", () -> coalNugget);
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
		
		//Vanilla Ores
		oreTemplates.add(new OreTemplate("coal", "minecraft:coal", GenDistance.NEAR));
		oreTemplates.add(new OreTemplate("iron", "minecraft:iron_nugget", GenDistance.NEAR));
		oreTemplates.add(new OreTemplate("gold", "minecraft:gold_nugget", GenDistance.MID));
		oreTemplates.add(new OreTemplate("diamond", "minecraft:diamond", GenDistance.DISABLED));
		oreTemplates.add(new OreTemplate("emerald", "minecraft:emerald", GenDistance.DISABLED));
		oreTemplates.add(new OreTemplate("redstone", "minecraft:redstone", GenDistance.MID));
		oreTemplates.add(new OreTemplate("lapis", "minecraft:lapis_lazuli", GenDistance.MID));
		oreTemplates.add(new OreTemplate("quartz", "minecraft:quartz", GenDistance.MID));
		oreTemplates.add(new OreTemplate("stone", "minecraft:stone", GenDistance.DISABLED));
		oreTemplates.add(new OreTemplate("sulfur", "factoriores:sulfur_dust", GenDistance.MID));
		// Create Ores
		oreTemplates.add(new OreTemplate("copper", "create:crushed_copper_ore", GenDistance.DISABLED));
		oreTemplates.add(new OreTemplate("zinc", "create:crushed_zinc_ore", GenDistance.DISABLED));
		// Immersive Engineering Ores
		oreTemplates.add(new OreTemplate("aluminum", "immersiveengineering:dust_aluminum", GenDistance.DISABLED));
		oreTemplates.add(new OreTemplate("lead", "immersiveengineering:dust_lead", GenDistance.DISABLED));
		oreTemplates.add(new OreTemplate("nickel", "immersiveengineering:dust_nickel", GenDistance.DISABLED));
		oreTemplates.add(new OreTemplate("silver", "immersiveengineering:dust_silver", GenDistance.DISABLED));
		oreTemplates.add(new OreTemplate("uranium", "immersiveengineering:dust_uranium", GenDistance.DISABLED));
		
		//fluids
		oreTemplates.add(new OreTemplate("water", "minecraft:water_bucket", GenDistance.NEAR).setFluid());
		oreTemplates.add(new OreTemplate("lava", "minecraft:lava_bucket", GenDistance.MID).setFluid());
		oreTemplates.add(new OreTemplate("oil", "immersivepetroleum:oil_bucket", GenDistance.DISABLED).setFluid());
	}

	private static void registerOres() {
		oreDeposits = new ArrayList<BlockOre>();
		fluidDeposits = new ArrayList<BlockOreFluid>();
		
		for (OreTemplate ore : oreTemplates) {
			Properties properties = Properties.create(Material.ROCK);
			BlockOre oreBlock;
			if (ore.isFluid) {
				BlockOreFluid fluidOre = new BlockOreFluid(ore.name, properties, ConfigHandler.COMMON.fluidAmountMin.get(), ConfigHandler.COMMON.fluidAmountMax.get());
				fluidDeposits.add(fluidOre);
				oreBlock = fluidOre;
			} else {
				oreBlock = new BlockOre(ore.name, properties, ConfigHandler.COMMON.oreAmountMin.get(), ConfigHandler.COMMON.oreAmountMax.get());
				oreDeposits.add(oreBlock);
			}
			
			registerSimpleBlock(oreBlock.getID(), oreBlock);
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
	}
	
	private static void registerFeatures() {
		FeatureOreDeposit oreDeposit = new FeatureOreDeposit(FeatureConfigOreDeposit.CODEC);
		featureOreDeposit = FEATURES.register("ore_deposit", () -> oreDeposit);
		
		PlacementOreDeposit placementOreDeposit = new PlacementOreDeposit(PlacementConfigOreDeposit.CODEC);
		DECORATORS.register("ore_deposit", () -> placementOreDeposit);
		
		configuredFeaturesDeposits = new ArrayList<ConfiguredFeature<?, ?>>();
		
		for (OreTemplate template : oreTemplates) {
			boolean breakFlag = false;
			for (BlockOre oreBlock : oreDeposits) {
				if (oreBlock.name == template.name) {
					registerConfiguredFeatureOre(oreDeposit, placementOreDeposit, oreBlock, template);
					breakFlag = true;
					break;
				}
			}
			if (breakFlag) continue;
			for (BlockOre oreBlock : fluidDeposits) {
				if (oreBlock.name == template.name) {
					registerConfiguredFeatureOre(oreDeposit, placementOreDeposit, oreBlock, template);
					break;
				}
			}
		}
	}
	
	private static void registerConfiguredFeatureOre(FeatureOreDeposit feature, PlacementOreDeposit decorator, BlockOre block, OreTemplate template) {
		FeatureConfigOreDeposit featureConfig = new FeatureConfigOreDeposit(block.getDefaultState(), template.patchRadius, template.patchDepth, template.patchDensity);
		PlacementConfigOreDeposit placementConfig = new PlacementConfigOreDeposit(template.genRarity, template.genDepth, template.genDistance.toString());
		
		final ConfiguredFeature<?, ?> configuredFeature = feature
				.configure(featureConfig)
				.decorate(decorator.configure(placementConfig));
		
		//Register the feature
		configuredFeaturesDeposits.add(configuredFeature);
		Registry.register(WorldGenRegistries.CONFIGURED_FEATURE, new ResourceLocation(FactoriOres.MOD_ID, block.name + "_deposit"), configuredFeature);
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
