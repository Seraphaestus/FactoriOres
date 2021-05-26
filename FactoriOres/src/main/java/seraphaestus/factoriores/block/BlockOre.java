package seraphaestus.factoriores.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import seraphaestus.factoriores.ConfigHandler;
import seraphaestus.factoriores.tile.TileEntityOre;

public class BlockOre extends Block {

	public int amountMin;
	public int amountMax;
	public String name;

	public BlockOre(String name, Properties properties, int amountMin, int amountMax) {
		super(properties.hardnessAndResistance(30.0F, 1.0F).requiresTool());
		this.name = name;
		this.amountMin = (amountMin <= 0 && amountMin != TileEntityOre.AMOUNT_DUMMY) ? TileEntityOre.AMOUNT_INFINITE : amountMin;
		this.amountMax = Math.max(this.amountMin, amountMax);
	}
	
	// -------- Tile Entity methods
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		if (world instanceof World) return new TileEntityOre(((World)world).rand, amountMin, amountMax);
		return new TileEntityOre();
	}
	
	// -------- Events
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		super.onBlockPlacedBy(worldIn, pos, state, placer, stack);
		TileEntity tileentity = worldIn.getTileEntity(pos);
		if (tileentity instanceof TileEntityOre) { // prevent a crash if not the right type, or is null
			TileEntityOre tileEntityOre = (TileEntityOre) tileentity;
			
			Random rnd = worldIn.rand;
			int initAmount;
			if (!(placer instanceof PlayerEntity) || !((PlayerEntity)placer).abilities.isCreativeMode || !((PlayerEntity)placer).isSneaking()) {
				initAmount = rnd.nextInt(amountMax - amountMin) + amountMin;
			} else {
				//if entity is player in creative mode and sneaking
				initAmount = TileEntityOre.AMOUNT_INFINITE;
			}
			
			tileEntityOre.setAmount(initAmount);
		}
	}
	
	// -------- Lixiviant methods
	
	@Override
	public float getPlayerRelativeBlockHardness(BlockState state, PlayerEntity player, IBlockReader reader, BlockPos pos) {
      if (this.requiresLixiviant()) {
    	  return 0.0F;
      } else {
    	  return super.getPlayerRelativeBlockHardness(state, player, reader, pos);
      }
   }
	
	@Override
	public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		if (this.requiresLixiviant()) {
			return Collections.emptyList();
		} else {
			return super.getDrops(state, builder);
		}
	}
	
	public List<ItemStack> getDropsViaMiner(BlockState state, LootContext.Builder builder) {
		return super.getDrops(state, builder);
	}
	
	// ------------------------
	
	public String getID() {
		return name + "_ore";
	}
	public String getOreName() {
		return name.substring(0, 1).toUpperCase() + name.substring(1) + " Deposit";
	}
	public String getDecoratorID() {
		return getID() + "_ore_deposit";
	}
	
	public boolean requiresLixiviant() {
		String registryName = this.getRegistryName().toString();
		return ConfigHandler.COMMON.oresWhichRequireLiquidMining.get().contains(registryName);
	}
}
