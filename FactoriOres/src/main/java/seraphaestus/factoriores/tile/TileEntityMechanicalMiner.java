package seraphaestus.factoriores.tile;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.simibubi.create.content.contraptions.base.KineticTileEntity;
import com.simibubi.create.content.contraptions.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
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
import seraphaestus.factoriores.FactoriOres;
import seraphaestus.factoriores.Registrar;
import seraphaestus.factoriores.block.BlockMechanicalMiner;
import seraphaestus.factoriores.block.BlockOre;
import seraphaestus.factoriores.data.StateDataMiner;
import seraphaestus.factoriores.inventory.InventoryMiner;

public class TileEntityMechanicalMiner extends KineticTileEntity implements IHaveGoggleInformation {

	public InventoryMiner items;
	protected static final int OUTPUT_SLOT = 0;
	protected static int NUM_SLOTS;
	
	public static final int STRESS = 4;

	protected StateDataMiner minerStateData;
	
	public TileEntityMechanicalMiner(TileEntityType<? extends TileEntityMechanicalMiner> type) {
		super(type);
		init();
	}

	protected void init() {
		NUM_SLOTS = 1;
		items = InventoryMiner.createForTileEntity(NUM_SLOTS, this::canPlayerAccessInventory, this::markDirty);
		minerStateData = new StateDataMiner();
		minerStateData.miningTotalTime = getTotalMiningTime();
		handlers = SidedInvWrapper.create(items, Direction.UP);
	}
	
	public boolean canPlayerAccessInventory(PlayerEntity player) {
		if (this.world.getTileEntity(this.pos) != this)
			return false;
		final double X_CENTRE_OFFSET = 0.5;
		final double Y_CENTRE_OFFSET = 0.5;
		final double Z_CENTRE_OFFSET = 0.5;
		final double MAXIMUM_DISTANCE_SQ = 8.0 * 8.0;
		return player.getDistanceSq(pos.getX() + X_CENTRE_OFFSET, pos.getY() + Y_CENTRE_OFFSET,
				pos.getZ() + Z_CENTRE_OFFSET) < MAXIMUM_DISTANCE_SQ;
	}
	
	@Override
	public void tick() {
		super.tick();
		
		if (world.isRemote)
			return;
		
		int miningProgressFlag = minerStateData.miningProgress;
		boolean hasChanged = false;
		BlockState state = world.getBlockState(pos);
		boolean isEnabled = state.get(BlockMechanicalMiner.ENABLED);
		
		if (isEnabled) {
			minerStateData.miningProgress += getProcessingSpeed();
			if (minerStateData.miningProgress >= minerStateData.miningTotalTime) {
				minerStateData.miningProgress -= minerStateData.miningTotalTime;
				minerStateData.miningTotalTime = getTotalMiningTime();
				mineBlock();
				hasChanged = true;
			}
		} else if (minerStateData.miningProgress > 0) {
			minerStateData.miningProgress = MathHelper.clamp(minerStateData.miningProgress - 2, 0, minerStateData.miningTotalTime);
		}
		
		if (minerStateData.miningProgress != miningProgressFlag) 
			sendData();
		
		boolean newIsEnabled = calculateIsEnabled();
		if (isEnabled != newIsEnabled) {
			world.setBlockState(pos, state.with(BlockMechanicalMiner.ENABLED, newIsEnabled));
		}

		if (hasChanged)
			this.markDirty();
	}
	
	protected int getProcessingSpeed() {
		return MathHelper.clamp((int)Math.abs(getSpeed()), 1, 512);
	}
	
	protected boolean calculateIsEnabled() {
		ItemStack outputSlot = this.items.getStackInSlot(OUTPUT_SLOT);
		return placedOnOre() && hasClearance() 
				&& outputSlot.getCount() < outputSlot.getMaxStackSize()
				&& !this.world.isBlockPowered(this.pos)
				&& hasSufficientLixiviant()
				//Mechanical miner specific conditions:
				&& this.getSpeed() != 0 && this.isSpeedRequirementFulfilled();
	}

	protected void updateIsEnabled() {
		boolean isEnabled = this.calculateIsEnabled();
		BlockState state = world.getBlockState(pos);
		if (isEnabled != state.get(BlockMechanicalMiner.ENABLED)) {
			world.setBlockState(pos, state.with(BlockMechanicalMiner.ENABLED, Boolean.valueOf(isEnabled)), 4);
		}
	}
	
	protected void mineBlock() {
		TileEntityOre selectedOre = selectOreFromInRange();
		if (selectedOre instanceof TileEntityOreFluid && tankUnavailable()) return;
		
		Object oreStack = selectedOre.decrement(this);
		if (oreStack instanceof ItemStack) {
			ItemStack oreItem = (ItemStack)oreStack;
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
				tank.fill(fluidStack, FluidAction.EXECUTE);
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
	
	protected int getTotalOreAmount() {
		List<BlockPos> oresInRange = getOresInRange();
		int totalAmount = 0;
		for (BlockPos pos : oresInRange) {
			TileEntity tileEntity = this.world.getTileEntity(pos);
			if (tileEntity instanceof TileEntityOre) {
				TileEntityOre teOre = (TileEntityOre)tileEntity;
				int amount = teOre.amount;
				if (amount == TileEntityOre.AMOUNT_DUMMY || amount == TileEntityOre.AMOUNT_INFINITE) return -1;
				totalAmount += amount;
			}
		}
		return totalAmount;
	}

	protected boolean placedOnOre() {
		return getOreState().getBlock() instanceof BlockOre;
	}

	protected boolean hasClearance() {
		BlockPos posBelow = pos.down();
		BlockState blockBelow = world.getBlockState(posBelow);
		return blockBelow.getBlock().isAir(blockBelow, world, posBelow);
	}

	protected int getTotalMiningTime() {
		return ConfigHandler.COMMON.minerSpeedMechanical.get() * 32;
		// the *32 comes from the fact that we want to slow down processing speed by 32x, but we can't divide it by 32 because it has to be an int and we don't want to lose precision
	}
	
	public int getRange() {
		return ConfigHandler.COMMON.minerRangeMechanical.get();
	}

	public ItemStack retrieveItems() {
		markDirty();
		return items.decrStackSize(OUTPUT_SLOT, 64);
	}
	
	@Override
	public boolean addToGoggleTooltip(List<ITextComponent> tooltip, boolean isPlayerSneaking) {
		boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
		
		if (placedOnOre()) {
			String time = getOreDepletionETA();
			if (time != null) {
				tooltip.add(new StringTextComponent(spacing).append(new TranslationTextComponent(FactoriOres.MOD_ID + ".tooltip.ore_miner_info").formatted(TextFormatting.WHITE)));
				tooltip.add(new StringTextComponent(spacing).append(new TranslationTextComponent(FactoriOres.MOD_ID + ".tooltip.ore_depletion_eta").formatted(TextFormatting.GRAY)));
				tooltip.add(new StringTextComponent(spacing).append(new StringTextComponent(time).formatted(TextFormatting.AQUA))
						.append(Lang.translate("gui.goggles.at_current_speed").formatted(TextFormatting.DARK_GRAY)));
			}
			added = true;
		}

		return added;
	}
	
	private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ISO_LOCAL_TIME;
			
	private String getOreDepletionETA() {
		if (!placedOnOre() || this.getSpeed() == 0) return null;
		int amount = getTotalOreAmount();
		if (amount <= 0) return null;
		int speed = getProcessingSpeed();
		int progress = minerStateData.miningProgress;
		int target = getTotalMiningTime();
		long numSeconds = (long)((amount * target - progress) / (20f * speed));
		if (numSeconds > 86400) {
			//more than 1 day
			return new TranslationTextComponent(FactoriOres.MOD_ID + ".tooltip.ore_depletion_days").toString();
		}
		LocalTime time = LocalTime.ofSecondOfDay(numSeconds);
		String output = time.format(timeFormatter);
		return " " + output + " ";
	}
	
	public float calculateStressApplied() {
		float impact = STRESS;
		this.lastStressApplied = impact;
		return impact;
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

	// -------- Data (NBT & Packets) methods

	@Override
	public void write(CompoundNBT parentNBTTagCompound, boolean clientPacket) {
		minerStateData.putIntoNBT(parentNBTTagCompound);
		parentNBTTagCompound.put("items", items.serializeNBT());
		super.write(parentNBTTagCompound, clientPacket);
	}

	@Override
	public void fromTag(BlockState blockState, CompoundNBT nbtTagCompound, boolean clientPacket) {
		minerStateData.readFromNBT(nbtTagCompound);

		CompoundNBT inventoryNBT = nbtTagCompound.getCompound("items");
		items.deserializeNBT(inventoryNBT);

		if (items.getSizeInventory() != NUM_SLOTS)
			throw new IllegalArgumentException("Corrupted NBT: Number of inventory slots did not match expected.");
		super.fromTag(blockState, nbtTagCompound, clientPacket);
	}
	
	public void dropAllContents(World world, BlockPos blockPos) {
		InventoryHelper.dropInventoryItems(world, blockPos, items);
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
