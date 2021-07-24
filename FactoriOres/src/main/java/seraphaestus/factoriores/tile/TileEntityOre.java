package seraphaestus.factoriores.tile;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameters;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import seraphaestus.factoriores.Registrar;
import seraphaestus.factoriores.block.BlockOre;

public class TileEntityOre extends TileEntityBase {

	protected int amount;
	
	public static final int AMOUNT_DUMMY = 0;
	public static final int AMOUNT_INFINITE = -1;

	public TileEntityOre(Random rnd, int amountMin, int amountMax) {
		super(Registrar.tileOreDeposit.get());
		amount = amountMax > amountMin ? rnd.nextInt(amountMax - amountMin) + amountMin : amountMin;
	}
	public TileEntityOre() {
		super(Registrar.tileOreDeposit.get());
	}
	protected TileEntityOre(TileEntityType<?> type) {
		super(type);
	}

	public int getAmount() {
		return amount;
	}
	
	public void setAmount(int amount) {
		this.amount = amount;
		markDirty();
	}
	
	// -------- Abstraction methods
	
	protected Object emptyStack() {
		return ItemStack.EMPTY;
	}
	
	// ------------------------

	public Object decrement(@Nullable TileEntity tile) {
		BlockState state = world.getBlockState(pos);
		if (amount == AMOUNT_DUMMY) return emptyStack();
		if (amount != AMOUNT_INFINITE) {
			--amount;
			markDirty();
			
			if (amount <= 0) {
				world.setBlockState(pos, Registrar.blockGangue.getDefaultState(), 1 | 2);
				this.remove();
			} else {
				world.notifyBlockUpdate(pos, state, state, 2);
			}
		}
		return getDrop(state, world, pos, tile);
	}
	public Object peek(@Nullable TileEntity tile) {
		BlockState state = world.getBlockState(pos);
		return getDrop(state, world, pos, tile);
	}
	
	protected Object getDrop(BlockState state, World world, BlockPos pos, @Nullable TileEntity tile) {
		ServerWorld serverWorld = world.isRemote ? world.getServer().getWorld(world.getRegistryKey()) : (ServerWorld)world;
		boolean isFromMiner = (tile != null) && (tile instanceof TileEntityMiner || tile instanceof TileEntityMechanicalMiner);

		Block block = state.getBlock();
		if (block instanceof BlockOre) {
			return getDrop(state, serverWorld, (BlockOre)block, tile, isFromMiner);
		} else {
			return emptyStack();
		}
	}
	
	protected Object getDrop(BlockState state, ServerWorld world, BlockOre oreBlock, @Nullable TileEntity tile, boolean isFromMiner) {
		LootContext.Builder builder = (new LootContext.Builder(world)).withRandom(world.rand).withParameter(LootParameters.ORIGIN, Vector3d.ofCenter(pos)).withParameter(LootParameters.TOOL, ItemStack.EMPTY).withNullableParameter(LootParameters.BLOCK_ENTITY, tile);
		List <ItemStack> drops = isFromMiner ? oreBlock.getDropsViaMiner(state, builder) : oreBlock.getDrops(state, builder);
		return drops.isEmpty() ? emptyStack() : drops.get(0);
	}

	// -------- Data (NBT & Packets) methods

	@Override
	public CompoundNBT write(CompoundNBT parentNBTTagCompound) {
		super.write(parentNBTTagCompound); // The super call is required to save and load the tile's location
		
		parentNBTTagCompound.putInt("amount", amount);
		return parentNBTTagCompound;
	}

	@Override
	public void read(BlockState blockState, CompoundNBT nbtTagCompound) {
		super.read(blockState, nbtTagCompound); // The super call is required to save and load the tile's location
		
		amount = nbtTagCompound.getInt("amount");
	}

}
