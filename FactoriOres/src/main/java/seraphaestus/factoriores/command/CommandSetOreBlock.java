package seraphaestus.factoriores.command;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.BlockPosArgument;
import net.minecraft.command.arguments.BlockStateArgument;
import net.minecraft.command.arguments.BlockStateInput;
import net.minecraft.inventory.IClearable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import seraphaestus.factoriores.FactoriOres;
import seraphaestus.factoriores.Registrar;
import seraphaestus.factoriores.block.BlockOre;
import seraphaestus.factoriores.block.BlockOreFluid;
import seraphaestus.factoriores.tile.TileEntityOre;

public class CommandSetOreBlock {

	private static final String ID = "setblockoredeposit";
	private static final int RANDOM_AMOUNT = -999;
	private static final SimpleCommandExceptionType FAILED_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("commands." + ID + ".failed"));
	private static final SimpleCommandExceptionType FAILED_TYPE_EXCEPTION = new SimpleCommandExceptionType(new TranslationTextComponent("commands." + ID + ".failed_on_type"));
	
	public static void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal(ID)
				.requires((commandSource) -> {return commandSource.hasPermissionLevel(2);})
				
		.then(Commands.argument("pos", BlockPosArgument.blockPos())
			.then(Commands.argument("block", BlockStateArgument.blockState())
				.executes((commandSource) -> {return 
						setOreBlock(commandSource.getSource(), 
						 BlockPosArgument.getLoadedBlockPos(commandSource, "pos"), 
						 BlockStateArgument.getBlockState(commandSource, "block"), RANDOM_AMOUNT);})
				.then(Commands.argument("amount", IntegerArgumentType.integer(1, Integer.MAX_VALUE))
					.executes((commandSource) -> {return 
							setOreBlock(commandSource.getSource(), 
							 BlockPosArgument.getLoadedBlockPos(commandSource, "pos"), 
							 BlockStateArgument.getBlockState(commandSource, "block"), 
							 IntegerArgumentType.getInteger(commandSource, "amount"));})
				)
				.then(Commands.literal("infinite")
					.executes((commandSource) -> {return 
							setOreBlock(commandSource.getSource(), 
							 BlockPosArgument.getLoadedBlockPos(commandSource, "pos"), 
							 BlockStateArgument.getBlockState(commandSource, "block"), TileEntityOre.AMOUNT_INFINITE);})
				)
				.then(Commands.literal("dummy")
					.executes((commandSource) -> {return 
							setOreBlock(commandSource.getSource(), 
							 BlockPosArgument.getLoadedBlockPos(commandSource, "pos"), 
							 BlockStateArgument.getBlockState(commandSource, "block"), TileEntityOre.AMOUNT_DUMMY);})
				)
			)
		)
		);
    }

	// adapted from SetBlockCommand
	private static int setOreBlock(CommandSource commandSource, BlockPos blockPos, BlockStateInput stateIn, int amount) throws CommandSyntaxException {
		ServerWorld serverworld = commandSource.getWorld();
		TileEntity tileEntity = serverworld.getTileEntity(blockPos);
		IClearable.clearObj(tileEntity);
		
		if (stateIn.getState().getBlock() instanceof BlockOre == false) 
			throw FAILED_TYPE_EXCEPTION.create();
		
		if (!stateIn.place(serverworld, blockPos, 2)) {
			throw FAILED_EXCEPTION.create();
		} else {
			//set the amount
			tileEntity = serverworld.getTileEntity(blockPos);
			if (amount != RANDOM_AMOUNT && tileEntity instanceof TileEntityOre) {
				FactoriOres.LOGGER.debug("test");
				((TileEntityOre)tileEntity).setAmount(amount);
			}
			serverworld.notifyNeighborsOfStateChange(blockPos, stateIn.getState().getBlock());
			commandSource.sendFeedback(new TranslationTextComponent("commands." + ID + ".success", blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
			return 1;
		}
	}
	
	public static Collection<String> getOreIDs() {
		List<String> output = new ArrayList<String>();
		for (BlockOre oreBlock : Registrar.oreDeposits) {
			output.add(FactoriOres.MOD_ID + ":" + oreBlock.getID());
		}
		for (BlockOreFluid fluidDeposit : Registrar.fluidDeposits) {
			output.add(FactoriOres.MOD_ID + ": " + fluidDeposit.getID());
		}
		return Lists.newArrayList();
	}
}
