package seraphaestus.factoriores.data.client;

import net.minecraft.data.DataGenerator;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.client.model.generators.ModelFile;
import net.minecraftforge.common.data.ExistingFileHelper;
import seraphaestus.factoriores.FactoriOres;
import seraphaestus.factoriores.Registrar;
import seraphaestus.factoriores.block.BlockOre;

public class OreBlockStateProvider extends BlockStateProvider {
	
	public OreBlockStateProvider(DataGenerator gen, ExistingFileHelper exFileHelper) {
		super(gen, FactoriOres.MOD_ID, exFileHelper);
	}
	
	@Override
	public void registerStatesAndModels() {
		for (BlockOre block : Registrar.oreDeposits) {
			String id = block.getID();
			ModelFile model = models().cubeAll(id, new ResourceLocation("factoriores:block/" + id));
			simpleBlock(block, model);
			simpleBlockItem(block, model);
		}
	}

}
