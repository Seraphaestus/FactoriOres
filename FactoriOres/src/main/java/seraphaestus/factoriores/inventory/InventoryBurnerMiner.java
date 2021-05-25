package seraphaestus.factoriores.inventory;

import java.util.function.Predicate;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraftforge.common.ForgeHooks;

public class InventoryBurnerMiner extends InventoryMiner {
	
	public static InventoryBurnerMiner createForTileEntity(int size, Predicate<PlayerEntity> canPlayerAccessInventoryLambda, Notify markDirtyNotificationLambda) {
		return new InventoryBurnerMiner(size, canPlayerAccessInventoryLambda, markDirtyNotificationLambda);
	}

	public static InventoryBurnerMiner createForClientSideContainer(int size) {
		return new InventoryBurnerMiner(size);
	}
	
	private InventoryBurnerMiner(int size) {
		super(size);
	}

	private InventoryBurnerMiner(int size, Predicate<PlayerEntity> canPlayerAccessInventoryLambda, Notify markDirtyNotificationLambda) {
		super(size, canPlayerAccessInventoryLambda, markDirtyNotificationLambda);
	}

	
	// -------- Sided inventory methods --------
	
	@Override
	public int[] getSlotsForFace(Direction side) {
		return new int[] {0, 1};
	}

	@Override
	public boolean canInsertItem(int slot, ItemStack item, Direction side) {
		if (slot == 1) return ForgeHooks.getBurnTime(item) > 0;
		else return false;
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack item, Direction side) {
		if (slot == 1) return false;
		else return true;
	}
}
