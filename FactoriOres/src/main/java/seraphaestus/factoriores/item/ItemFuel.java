package seraphaestus.factoriores.item;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import seraphaestus.factoriores.Registrar;

public class ItemFuel extends Item {

	public ItemFuel(Properties properties) {
		super(properties);
	}

	@Override
    public int getBurnTime(ItemStack stack) {
		if (stack.isItemEqual(Registrar.coalNugget.getDefaultInstance())) return 200;

        return super.getBurnTime(stack);
    }

}
