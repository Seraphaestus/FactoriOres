package seraphaestus.factoriores.data.server;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.LootTableProvider;
import net.minecraft.loot.LootParameterSet;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.LootTableManager;
import net.minecraft.loot.ValidationTracker;
import net.minecraft.util.ResourceLocation;
import seraphaestus.factoriores.FactoriOres;

public class OreLootTableProvider extends LootTableProvider {

	private final List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> lootTables = ImmutableList
			.of(Pair.of(OreBlockLootTables::new, LootParameterSets.BLOCK));

	public OreLootTableProvider(DataGenerator gen) {
		super(gen);
	}

	@Override
	public List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootParameterSet>> getTables() {
		return this.lootTables;
	}

	@Override
	public String getName() {
		return FactoriOres.MOD_ID + ":loot_tables";
	}

	@Override
	protected void validate(Map<ResourceLocation, LootTable> map, ValidationTracker validationtracker) {
		map.forEach((resourceLocation, table) -> {
			LootTableManager.validateLootTable(validationtracker, resourceLocation, table);
		});
	}
}
