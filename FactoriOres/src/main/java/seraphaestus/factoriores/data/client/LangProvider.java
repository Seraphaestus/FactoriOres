package seraphaestus.factoriores.data.client;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.LanguageProvider;
import seraphaestus.factoriores.FactoriOres;
import seraphaestus.factoriores.Registrar;
import seraphaestus.factoriores.block.BlockOre;

public class LangProvider extends LanguageProvider {
	
	public LangProvider(DataGenerator gen) {
		super(gen, FactoriOres.MOD_ID, "en_us");
	}

	@Override
	protected void addTranslations() {
		add(Registrar.blockGangue, "Depleted Ore");
		for (BlockOre block : Registrar.oreDeposits) {
			add(block, block.getOreName());
		}
		add(Registrar.blockCreativeMiner, "Creative Miner");
		add(Registrar.blockBurnerMiner, "Burner Miner");
		add(Registrar.blockElectricalMiner, "Electrical Miner");
		//add(Registrar.blockMechanicalMiner.get(), "Mechanical Miner");
		
		add("factoriores.tooltip.ore_depletion_eta", "Time Until Ore Depleted:");
	}
}
