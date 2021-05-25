package seraphaestus.factoriores.inventory;

import java.util.function.Predicate;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.items.ItemStackHandler;

public class InventoryMiner implements ISidedInventory {
	
	public static InventoryMiner createForTileEntity(int size, Predicate<PlayerEntity> canPlayerAccessInventoryLambda, Notify markDirtyNotificationLambda) {
		return new InventoryMiner(size, canPlayerAccessInventoryLambda, markDirtyNotificationLambda);
	}

	public static InventoryMiner createForClientSideContainer(int size) {
		return new InventoryMiner(size);
	}

	// ----Methods used to load / save the contents to NBT

	public CompoundNBT serializeNBT() {
		return minerComponentContents.serializeNBT();
	}

	public void deserializeNBT(CompoundNBT nbt) {
		minerComponentContents.deserializeNBT(nbt);
	}

	// ------------- linking methods -------------

	/**
	 * sets the function that the container should call in order to decide if the
	 * given player can access the container's contents not. The lambda function is
	 * only used on the server side
	 */
	public void setCanPlayerAccessInventoryLambda(Predicate<PlayerEntity> canPlayerAccessInventoryLambda) {
		this.canPlayerAccessInventoryLambda = canPlayerAccessInventoryLambda;
	}

	public void setMarkDirtyNotificationLambda(Notify markDirtyNotificationLambda) {
		this.markDirtyNotificationLambda = markDirtyNotificationLambda;
	}

	public void setOpenInventoryNotificationLambda(Notify openInventoryNotificationLambda) {
		this.openInventoryNotificationLambda = openInventoryNotificationLambda;
	}

	public void setCloseInventoryNotificationLambda(Notify closeInventoryNotificationLambda) {
		this.closeInventoryNotificationLambda = closeInventoryNotificationLambda;
	}

	// ---------- These methods are used by the container to ask whether certain
	// actions are permitted

	@Override
	public boolean isUsableByPlayer(PlayerEntity player) {
		return canPlayerAccessInventoryLambda.test(player); // on the client, this does nothing. on the server, ask our
															// parent TileEntity.
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return minerComponentContents.isItemValid(index, stack);
	}

	// ----- Methods used to inform the parent tile entity that something has
	// happened to the contents

	@FunctionalInterface
	public interface Notify { // Some folks use Runnable, but I prefer not to use it for non-thread-related
								// tasks
		void invoke();
	}

	@Override
	public void markDirty() {
		markDirtyNotificationLambda.invoke();
	}

	@Override
	public void openInventory(PlayerEntity player) {
		openInventoryNotificationLambda.invoke();
	}

	@Override
	public void closeInventory(PlayerEntity player) {
		closeInventoryNotificationLambda.invoke();
	}

	// ---------These following methods are called by Vanilla container methods to
	// manipulate the inventory contents ---

	@Override
	public int getSizeInventory() {
		return minerComponentContents.getSlots();
	}

	@Override
	public boolean isEmpty() {
		for (int i = 0; i < minerComponentContents.getSlots(); ++i) {
			if (!minerComponentContents.getStackInSlot(i).isEmpty())
				return false;
		}
		return true;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return minerComponentContents.getStackInSlot(index);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (count < 0)
			throw new IllegalArgumentException("count should be >= 0:" + count);
		return minerComponentContents.extractItem(index, count, false);
	}

	@Override
	public ItemStack removeStackFromSlot(int index) {
		int maxPossibleItemStackSize = minerComponentContents.getSlotLimit(index);
		return minerComponentContents.extractItem(index, maxPossibleItemStackSize, false);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		minerComponentContents.setStackInSlot(index, stack);
	}

	@Override
	public void clear() {
		for (int i = 0; i < minerComponentContents.getSlots(); ++i) {
			minerComponentContents.setStackInSlot(i, ItemStack.EMPTY);
		}
	}

	// --------- useful functions that aren't in IInventory but are useful anyway

	public ItemStack increaseStackSize(int index, ItemStack itemStackToInsert) {
		ItemStack leftoverItemStack = minerComponentContents.insertItem(index, itemStackToInsert, false);
		return leftoverItemStack;
	}

	public boolean doesItemStackFit(int index, ItemStack itemStackToInsert) {
		ItemStack leftoverItemStack = minerComponentContents.insertItem(index, itemStackToInsert, true);
		return leftoverItemStack.isEmpty();
	}

	// ---------

	protected InventoryMiner(int size) {
		this.minerComponentContents = new ItemStackHandler(size);
	}

	protected InventoryMiner(int size, Predicate<PlayerEntity> canPlayerAccessInventoryLambda, Notify markDirtyNotificationLambda) {
		this.minerComponentContents = new ItemStackHandler(size);
		this.canPlayerAccessInventoryLambda = canPlayerAccessInventoryLambda;
		this.markDirtyNotificationLambda = markDirtyNotificationLambda;
	}

	protected Predicate<PlayerEntity> canPlayerAccessInventoryLambda = x -> true;

	protected Notify markDirtyNotificationLambda = () -> {
	};

	protected Notify openInventoryNotificationLambda = () -> {
	};

	protected Notify closeInventoryNotificationLambda = () -> {
	};

	public final ItemStackHandler minerComponentContents;

	// -------- Sided inventory methods --------
	
	@Override
	public int[] getSlotsForFace(Direction side) {
		return new int[] {0};
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack item, Direction side) {
		return false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack item, Direction side) {
		return true;
	}
}
