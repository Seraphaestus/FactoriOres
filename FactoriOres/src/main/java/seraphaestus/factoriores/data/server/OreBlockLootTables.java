package seraphaestus.factoriores.data.server;

import net.minecraft.block.Block;
import net.minecraft.data.loot.BlockLootTables;
import net.minecraft.item.Item;
import net.minecraft.loot.ConstantRange;
import net.minecraft.loot.ItemLootEntry;
import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import seraphaestus.factoriores.Registrar;
import seraphaestus.factoriores.block.BlockOre;
import seraphaestus.factoriores.data.OreTemplate;

public class OreBlockLootTables extends BlockLootTables {
	
	@Override
	protected Iterable<Block> getKnownBlocks() {
		return Registrar.BLOCKS.getEntries().stream().filter(b -> b.get() instanceof BlockOre).map(RegistryObject::get)::iterator;
	}

	@Override
	protected void addTables() {
		for (BlockOre blockOre : Registrar.oreDeposits) {
			registerOreBlockLootTable(blockOre);
		}
	}

	private void registerOreBlockLootTable(BlockOre blockIn) {
		for (OreTemplate template : Registrar.oreTemplates) {
			if (template.name == blockIn.name) {
				Item drop = ForgeRegistries.ITEMS.getValue(ResourceLocation.create(template.drop, ':'));
				this.registerLootTable(blockIn, (block) -> { return simpleBlockDrop(drop); });
				return;
			}
		}
	}
	
	protected static LootTable.Builder simpleBlockDrop(IItemProvider item) {
		return LootTable.builder().addLootPool(LootPool.builder().rolls(ConstantRange.of(1)).cast().addEntry(ItemLootEntry.builder(item)));
    }
}
