package seraphaestus.factoriores.event;

import static net.minecraft.util.text.TextFormatting.GRAY;

import java.util.ArrayList;
import java.util.List;

import com.simibubi.create.AllItems;
import com.simibubi.create.content.contraptions.base.IRotate;
import com.simibubi.create.content.contraptions.base.IRotate.SpeedLevel;
import com.simibubi.create.content.contraptions.base.IRotate.StressImpact;
import com.simibubi.create.foundation.config.AllConfigs;
import com.simibubi.create.foundation.config.CKinetics;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipHelper;
import com.simibubi.create.foundation.ponder.PonderTooltipHandler;
import com.simibubi.create.foundation.utility.Lang;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import seraphaestus.factoriores.FactoriOres;
import seraphaestus.factoriores.Registrar;
import seraphaestus.factoriores.tile.TileEntityMechanicalMiner;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientTooltipHandler {
	
	// copied from Create's ClientEvents as a workaround for the fact that Create tooltips are currently tied to a hardcoded item.create translation key prefix
	@SubscribeEvent
	public static void addToItemTooltip(ItemTooltipEvent event) {
		if (!FactoriOres.CREATE_ACTIVE) return;
		if (!AllConfigs.CLIENT.tooltips.get()) return;
		if (event.getPlayer() == null) return;

		ItemStack stack = event.getItemStack();
		if (stack.isItemEqual(Registrar.blockMechanicalMiner.asStack())) {
			
			if (TooltipHelper.hasTooltip(stack, event.getPlayer())) {
				List<ITextComponent> itemTooltip = event.getToolTip();
				List<ITextComponent> toolTip = new ArrayList<>();
				toolTip.add(itemTooltip.remove(0));
				TooltipHelper.getTooltip(stack)
					.addInformation(toolTip);
				itemTooltip.addAll(0, toolTip);
			}
			
			BlockItem item = (BlockItem)stack.getItem();
			List<ITextComponent> kineticStats = getMechMinerKineticStats(item.getBlock());
			if (!kineticStats.isEmpty()) {
				event.getToolTip()
					.add(new StringTextComponent(""));
				event.getToolTip()
					.addAll(kineticStats);
			}

			PonderTooltipHandler.addToTooltip(event.getToolTip(), stack);
		}
	}
	
	// Also copied; see above
	public static List<ITextComponent> getMechMinerKineticStats(Block block) {
		List<ITextComponent> list = new ArrayList<>();

		CKinetics config = AllConfigs.SERVER.kinetics;
		SpeedLevel minimumRequiredSpeedLevel = ((IRotate) block).getMinimumRequiredSpeedLevel();
		boolean hasSpeedRequirement = minimumRequiredSpeedLevel != SpeedLevel.NONE;
		
		boolean hasGlasses = AllItems.GOGGLES.get() == Minecraft.getInstance().player.getItemStackFromSlot(EquipmentSlotType.HEAD).getItem();

		ITextComponent rpmUnit = Lang.translate("generic.unit.rpm");
		if (hasSpeedRequirement) {
			List<ITextComponent> speedLevels =
				Lang.translatedOptions("tooltip.speedRequirement", "none", "medium", "high");
			int index = minimumRequiredSpeedLevel.ordinal();
			IFormattableTextComponent level =
				new StringTextComponent(ItemDescription.makeProgressBar(3, index)).formatted(minimumRequiredSpeedLevel.getTextColor());

			if (hasGlasses)
				level.append(String.valueOf(minimumRequiredSpeedLevel.getSpeedValue()))
					.append(rpmUnit)
					.append("+");
			else
				level.append(speedLevels.get(index));

			list.add(Lang.translate("tooltip.speedRequirement")
				.formatted(GRAY));
			list.add(level);
		}
		
		//stress impact
		if (!(((IRotate) block).hideStressImpact())) {
			List<ITextComponent> stressLevels = Lang.translatedOptions("tooltip.stressImpact", "low", "medium", "high");
			double impact = TileEntityMechanicalMiner.STRESS;
			StressImpact impactId = impact >= config.highStressImpact.get() ? StressImpact.HIGH
				: (impact >= config.mediumStressImpact.get() ? StressImpact.MEDIUM : StressImpact.LOW);
			int index = impactId.ordinal();
			IFormattableTextComponent level =
				new StringTextComponent(ItemDescription.makeProgressBar(3, index)).formatted(impactId.getAbsoluteColor());

			if (hasGlasses)
				level.append(impact + "x ")
					.append(rpmUnit);
			else
				level.append(stressLevels.get(index));

			list.add(Lang.translate("tooltip.stressImpact")
				.formatted(GRAY));
			list.add(level);
		}

		return list;
	}
	
}
