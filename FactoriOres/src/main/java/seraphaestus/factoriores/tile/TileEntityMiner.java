package seraphaestus.factoriores.tile;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler.FluidAction;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import seraphaestus.factoriores.ConfigHandler;
import seraphaestus.factoriores.Registrar;
import seraphaestus.factoriores.block.BlockMiner;
import seraphaestus.factoriores.block.BlockOre;
import seraphaestus.factoriores.data.StateDataMiner;
import seraphaestus.factoriores.inventory.InventoryMiner;

public abstract class TileEntityMiner extends TileEntityBase implements ITickableTileEntity {

	public InventoryMiner items;
	protected static final int OUTPUT_SLOT = 0;
	protected StateDataMiner minerStateData;

	public TileEntityMiner(TileEntityType<? extends TileEntityMiner> type) {
		super(type);
		init();
	}

	protected void init() {
		items = InventoryMiner.createForTileEntity(numSlots(), this::canPlayerAccessInventory, this::markDirty);
		minerStateData = new StateDataMiner();
		minerStateData.miningTotalTime = getTotalMiningTime();
		handlers = SidedInvWrapper.create(items, Direction.UP);
	}

	public boolean canPlayerAccessInventory(PlayerEntity player) {
		if (this.world.getTileEntity(this.pos) != this) return false;
		
		final double MAXIMUM_DISTANCE_SQ = 8.0 * 8.0;
		return player.getDistanceSq(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < MAXIMUM_DISTANCE_SQ;
	}

	protected boolean doMining() {
		if (world.isRemote)
			return false;

		boolean hasChanged = false;
		BlockState state = world.getBlockState(pos);
		boolean isEnabled = state.get(BlockMiner.ENABLED);
		
		if (isEnabled) {
			++minerStateData.miningProgress;
			drainLixiviant(ConfigHandler.COMMON.lixiviantDrainedPerUnitProgress.get());
			hasChanged = onProgressMade() ? true : hasChanged;
			if (minerStateData.miningProgress == minerStateData.miningTotalTime) {
				minerStateData.miningProgress = 0;
				minerStateData.miningTotalTime = getTotalMiningTime();
				mineBlock();
				hasChanged = true;
			}
		} else if (canKickstart()) {
			doKickstart();
		} else if (minerStateData.miningProgress > 0) {
			minerStateData.miningProgress = MathHelper.clamp(minerStateData.miningProgress - 2, 0, minerStateData.miningTotalTime);
		}
		
		boolean newIsEnabled = calculateIsEnabled();
		if (isEnabled != newIsEnabled) {
			world.setBlockState(pos, state.with(BlockMiner.ENABLED, newIsEnabled));
		}

		return hasChanged;
	}
	
	protected boolean calculateIsEnabled() {
		ItemStack outputSlot = this.items.getStackInSlot(OUTPUT_SLOT);
		return placedOnOre() && hasClearance() 
				&& outputSlot.getCount() < outputSlot.getMaxStackSize()
				&& !this.world.isBlockPowered(this.pos)
				&& hasSufficientLixiviant();
	}
	
	protected void updateIsEnabled() {
		boolean isEnabled = this.calculateIsEnabled();
		BlockState state = world.getBlockState(pos);
		if (isEnabled != state.get(BlockMiner.ENABLED)) {
			world.setBlockState(pos, state.with(BlockMiner.ENABLED, Boolean.valueOf(isEnabled)), 4);	//TODO: try change to 2 | 4?
		}
	}
	
	protected void mineBlock() {
		TileEntityOre selectedOre = selectOreFromInRange();
		if (selectedOre == null) return;
		if (selectedOre instanceof TileEntityOreFluid && tankUnavailable()) return;
		
		Object oreStack = selectedOre.peek(this);
		if (oreStack instanceof ItemStack) {
			ItemStack oreItem = (ItemStack)oreStack;
			selectedOre.decrement(this);
			if (oreItem.isEmpty()) return;
			ItemStack outputSlot = items.getStackInSlot(OUTPUT_SLOT);
			if (outputSlot.isEmpty()) {
				items.setInventorySlotContents(OUTPUT_SLOT, oreItem.copy());
			} else if (outputSlot.getItem() == oreItem.getItem()) {
				items.increaseStackSize(OUTPUT_SLOT, oreItem.copy());
			}
		} else if (oreStack instanceof FluidStack) {
			FluidStack fluidStack = (FluidStack)oreStack;
			if (fluidStack.isEmpty()) return;
			IFluidHandler tank = getTank();
			if (tank != null) {
				int overflow = fluidStack.getAmount() - tank.fill(fluidStack, FluidAction.SIMULATE);
				if (overflow <= 0) {
					selectedOre.decrement(this);
					tank.fill(fluidStack, FluidAction.EXECUTE);
				}
			}
		}
	}
	
	protected TileEntityOre selectOreFromInRange() {
		List<BlockPos> oresInRange = this.getOresInRange();
		BlockPos oreBelow = getOrePos();
		//calculate maxAmount
		int maxAmount = 0;
		TileEntityOre infiniteOre = null;
		TileEntityOre dummyOre = null;
		for (BlockPos pos : oresInRange) {
			TileEntity tileEntity = this.world.getTileEntity(pos);
			if (tileEntity instanceof TileEntityOre) {
				TileEntityOre teOre = (TileEntityOre)tileEntity;
				int amount = teOre.amount;
				if (amount == TileEntityOre.AMOUNT_DUMMY) dummyOre = teOre;
				else if (amount == TileEntityOre.AMOUNT_INFINITE) infiniteOre = teOre;
				else if (amount > maxAmount) maxAmount = amount;
			}
		}
		//check if any ores with infinite/dummy amount value are present, and if so, skip the rest and return that
		if (infiniteOre != null) return infiniteOre;
		else if (dummyOre != null) return dummyOre;
		//create list of ores with maxAmount
		List<BlockPos> candidateOres = new ArrayList<BlockPos>();
		for (BlockPos pos : oresInRange) {
			TileEntity tileEntity = this.world.getTileEntity(pos);
			if (tileEntity instanceof TileEntityOre) {
				TileEntityOre teOre = (TileEntityOre)tileEntity;
				if (teOre.amount == maxAmount) candidateOres.add(pos);
			}
		}
		
		if (candidateOres.isEmpty()) return null;
		
		//select from candidate list
			//remove the oreBelow IF it's one of our candidates and it's not the only one (this ensures the oreBelow will be depleted last)
		if (candidateOres.contains(oreBelow) && candidateOres.size() > 1) {
			candidateOres.remove(oreBelow);
		}
		int randomIndex = world.rand.nextInt(candidateOres.size());
		BlockPos targetPos = candidateOres.get(randomIndex);
		TileEntity tileEntity = this.world.getTileEntity(targetPos);
		if (tileEntity instanceof TileEntityOre)
			return (TileEntityOre) tileEntity;
			else return null;
	}
	
	/**
	 * This is used to check the presense of resources which would be consumed in onBecomeEnabled
	 * Return false if the miner doesn't have behaviour where resources are consumed to kickstart it
	 */
	protected boolean canKickstart() {
		return false;
	}
	
	/**
	 * This is used to consume some resources which, after consumtion, will cause calculateIsEnabled to return true
	 */
	protected void doKickstart() {
	}

	protected BlockPos getOrePos() {
		return pos.down().down();
	}

	public BlockState getOreState() {
		return world.getBlockState(getOrePos());
	}

	protected List<BlockPos> getOresInRange() {
		final BlockPos origin = getOrePos();
		final int range = this.getRange();
		int deltaY = 0;
		List<BlockPos> output = new ArrayList<BlockPos>();
		while (deltaY < ConfigHandler.COMMON.minerMaxDepth.get()) {
			boolean anyOresThisLayer = false;
			for (int x = -range; x <= range; x++) {
				for (int z = -range; z <= range; z++) {
					BlockPos pos = origin.add(x, -deltaY, z);
					if (World.isValid(pos)) {
						if (world.getBlockState(pos).getBlock() instanceof BlockOre) {
							output.add(pos);
							anyOresThisLayer = true;
						}
					}
				}
			}
			deltaY++;
			if (World.isYOutOfBounds(origin.getY())) break;	//check if the new y pos is invalid and if so, immediately break
			if (!anyOresThisLayer) break;	//break if the continous area of ores below the miner ends
		}
		return output;
	}

	protected boolean placedOnOre() {
		return getOreState().getBlock() instanceof BlockOre;
	}

	protected boolean hasClearance() {
		BlockPos posBelow = pos.down();
		BlockState blockBelow = world.getBlockState(posBelow);
		return blockBelow.getBlock().isAir(blockBelow, world, posBelow);
	}

	protected boolean onProgressMade() {
		return false;
	}

	protected abstract int getTotalMiningTime();
	
	public int getRange() {
		return 0;	// 0 = 1x1, 1 = 3x3, 2 = 5x5, etc.
	}
	
	protected int numSlots() {
		return 1;
	}

	public ItemStack retrieveItems() {
		markDirty();
		return items.decrStackSize(OUTPUT_SLOT, 64);
	}
	
	public void dropAllContents(World world, BlockPos blockPos) {
		InventoryHelper.dropInventoryItems(world, blockPos, items);
	}
	
	// -------- Lixiviant & Fluid methods
	
	protected boolean hasSufficientLixiviant() {
		return ((BlockOre)getOreState().getBlock()).requiresLixiviant() ? getTankWithLixiviant() != null : true;
	}
	
	protected boolean tankUnavailable() {
		IFluidHandler tank = getTank();
		if (tank == null) return true;	// no tank
		FluidStack testStack = tank.drain(1, FluidAction.SIMULATE);
		if (testStack.isEmpty()) testStack = new FluidStack(Fluids.WATER, 1);
		int fluidFilled = tank.fill(testStack, FluidAction.SIMULATE);
		if (fluidFilled == 0) return true;	// tank full
		return false;
	}
	
	protected IFluidHandler getTank() {
		TileEntity tankTE = this.world.getTileEntity(pos.up());
		if (tankTE == null) return null;
		
		LazyOptional<IFluidHandler> targetCapability = tankTE.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
		return targetCapability.orElse(null);
	}
	
	protected IFluidHandler getTankWithLixiviant() {
		if (getWorld().isRemote) return null;
		
		IFluidHandler tank = getTank();
		if (tank == null) return null;
		
		for (int i = 0; i < tank.getTanks(); i++) {
			FluidStack fluid = tank.getFluidInTank(i);
			if (Registrar.tagLixiviant.contains(fluid.getFluid())) {
				return tank;
			}
		}
		return null;
	}
	
	protected void drainLixiviant(int progressMade) {
		Block block = getOreState().getBlock();
		if (!(block instanceof BlockOre)) return;
		BlockOre blockOre = (BlockOre)block;
		if (!blockOre.requiresLixiviant()) return;
		IFluidHandler tank = getTankWithLixiviant();
		if (tank == null) return;
		tank.drain(progressMade, FluidAction.EXECUTE);
	}
	
	// -------- Client methods
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		BlockPos pos = getTileEntity().getPos();
		return new AxisAlignedBB(pos.add(0, -1, 0), pos.add(1, 1, 1));
	}

	// -------- Data (NBT & Packets) methods

	@Override
	public CompoundNBT write(CompoundNBT parentNBTTagCompound) {
		super.write(parentNBTTagCompound); // The super call is required to save and load the tile's location

		minerStateData.putIntoNBT(parentNBTTagCompound);
		parentNBTTagCompound.put("items", items.serializeNBT());
		return parentNBTTagCompound;
	}

	@Override
	public void read(BlockState blockState, CompoundNBT nbtTagCompound) {
		super.read(blockState, nbtTagCompound); // The super call is required to save and load the tile's location

		minerStateData.readFromNBT(nbtTagCompound);

		CompoundNBT inventoryNBT = nbtTagCompound.getCompound("items");
		items.deserializeNBT(inventoryNBT);

		if (items.getSizeInventory() != numSlots()) {
			throw new IllegalArgumentException("Corrupted NBT: Number of inventory slots did not match expected.");
		}
	}

	// -------- Expose for automation

	LazyOptional<? extends IItemHandler>[] handlers;
	
	@Nonnull
	@Override
	public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
		if (!this.removed && cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
			return handlers[0].cast();
		}
		return super.getCapability(cap, side);
	}

	@Override
	public void remove() {
	  super.remove();
	  for (int x = 0; x < handlers.length; x++)
	        handlers[x].invalidate();
	}

}
