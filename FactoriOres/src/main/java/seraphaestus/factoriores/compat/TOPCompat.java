package seraphaestus.factoriores.compat;

import java.util.function.Function;

import javax.annotation.Nonnull;

import mcjty.theoneprobe.api.Color;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.IProgressStyle;
import mcjty.theoneprobe.api.ITheOneProbe;
import mcjty.theoneprobe.api.NumberFormat;
import mcjty.theoneprobe.api.ProbeMode;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fluids.FluidStack;
import seraphaestus.factoriores.ConfigHandler;
import seraphaestus.factoriores.FactoriOres;
import seraphaestus.factoriores.block.BlockOre;
import seraphaestus.factoriores.tile.TileEntityOre;
import seraphaestus.factoriores.tile.TileEntityOreFluid;

public class TOPCompat implements Function<ITheOneProbe, Void> {

	// Reference for how to implement compat in this way is from Flux Networks
	
    @Override
    public Void apply(@Nonnull ITheOneProbe iTheOneProbe) {
        iTheOneProbe.registerProvider(new OreDepositInfoProvider());
        return null;
    }

    public static class OreDepositInfoProvider implements IProbeInfoProvider {

        @Override
        public String getID() {
            return FactoriOres.MOD_ID;
        }

        @Override
    	public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, PlayerEntity player, World world, BlockState blockState, IProbeHitData data) {
        	TileEntity tileEntity = world.getTileEntity(data.getPos());
    		if (tileEntity instanceof TileEntityOre) {
    			TileEntityOre tileEntityOre = (TileEntityOre)tileEntity;
    			BlockOre blockOre = (BlockOre)blockState.getBlock();
    			int amount = tileEntityOre.getAmount();
    			
    			if (tileEntity instanceof TileEntityOreFluid) {
    				addProbeInfoOreFluid(probeInfo, world, blockState, blockOre, tileEntityOre, amount, blockOre.amountMax);
    			} else {
    				addProbeInfoOre(probeInfo, amount, blockOre.amountMax);
    			}
    			
    			if (amount == TileEntityOre.AMOUNT_DUMMY) {
    				probeInfo.text(new TranslationTextComponent(FactoriOres.MOD_ID + ".tooltip.ore_block_dummy").mergeStyle(TextFormatting.GRAY));
    			} else if (amount == TileEntityOre.AMOUNT_INFINITE) { 
    				probeInfo.text(new TranslationTextComponent(FactoriOres.MOD_ID + ".tooltip.ore_block_infinite").mergeStyle(TextFormatting.GRAY));
    			}
    			
    			if (blockOre.requiresLixiviant()) {
    				probeInfo.text(new TranslationTextComponent(FactoriOres.MOD_ID + ".tooltip.ore_requires_lixiviant").mergeStyle(TextFormatting.GREEN));
    			}
    		}
    	}
        
        private void addProbeInfoOre(IProbeInfo probeInfo, int amount, int amountMax) {
        	if (amount <= 0) return;
    		final IProgressStyle progressStyle = probeInfo.defaultProgressStyle()
    	            .width(100)
    	            .height(12)
    	            .showText(true)
    	            .filledColor(0xffdba570)
    	            .alternateFilledColor(0xff8c633b);
        	probeInfo.progress(amount, amount, progressStyle);
        }
        
        private void addProbeInfoOreFluid(IProbeInfo probeInfo, World world, BlockState blockState, BlockOre blockOre, TileEntityOre tileEntityOre, int amount, int amountMax) {
        	TileEntityOreFluid teOreFluid = (TileEntityOreFluid)tileEntityOre;
			int amountMB = amount * ConfigHandler.COMMON.fluidDepositAmount.get();
			int amountMaxMB = amountMax * ConfigHandler.COMMON.fluidDepositAmount.get();
			
			FluidStack fluidStack = (FluidStack)teOreFluid.getDrop(blockState, (ServerWorld)world, blockOre, null, true);
			fluidStack = new FluidStack(fluidStack.getFluid(), amountMB <= 0 ? amountMaxMB : amountMB);
			Color color = new Color(fluidStack.getFluid().getAttributes().getColor(fluidStack));
        	if(fluidStack.getFluid() == Fluids.LAVA) {
    			color = new Color(255, 139, 27);
        	}
        	
			IFormattableTextComponent text = new StringTextComponent("" + amountMB).appendString("mB");
        	
        	IFormattableTextComponent prefix = (IFormattableTextComponent) fluidStack.getDisplayName();
			if (amountMB > 0)
				prefix = prefix.appendString(": ");
        	
        	IProgressStyle progressStyle = probeInfo.defaultProgressStyle()
        			.numberFormat(NumberFormat.NONE)
        			.borderlessColor(color, color.darker().darker())
        			.prefix(prefix);
        	if (amountMB > 0) progressStyle = progressStyle.suffix(text);
        	
			probeInfo.tankSimple(amountMaxMB, fluidStack, progressStyle);
        }
    }
}
