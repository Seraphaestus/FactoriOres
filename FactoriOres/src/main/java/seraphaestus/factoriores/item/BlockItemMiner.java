package seraphaestus.factoriores.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.state.Property;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.World;

//This class exists to override block placement so that miners are placed one block above the target ore block
public class BlockItemMiner extends BlockItem {

	public BlockItemMiner(Block block, Properties properties) {
		super(block, properties);
	}
	
	protected BlockPos getPos(BlockItemUseContext context) {
		return context.getPos().up();
	}
	
	//the following methods are copied from BlockItem, with context.getPos() replaced with this.getPos(context)
	
	@Override
	public ActionResultType tryPlace(BlockItemUseContext context) {
		if (!context.canPlace()) {
			return ActionResultType.FAIL;
		} else {
			BlockItemUseContext blockitemusecontext = this.getBlockItemUseContext(context);
			if (blockitemusecontext == null) {
				return ActionResultType.FAIL;
			} else {
				BlockState blockstate = this.getStateForPlacement(blockitemusecontext);
				if (blockstate == null) {
					return ActionResultType.FAIL;
				} else if (!this.placeBlock(blockitemusecontext, blockstate)) {
					return ActionResultType.FAIL;
				} else {
					BlockPos blockpos = getPos(blockitemusecontext); // EDIT
					World world = blockitemusecontext.getWorld();
					PlayerEntity playerentity = blockitemusecontext.getPlayer();
					ItemStack itemstack = blockitemusecontext.getItem();
					BlockState blockstate1 = world.getBlockState(blockpos);
					Block block = blockstate1.getBlock();
					if (block == blockstate.getBlock()) {
						blockstate1 = this.func_219985_a(blockpos, world, itemstack, blockstate1);
						this.onBlockPlaced(blockpos, world, playerentity, itemstack, blockstate1);
						block.onBlockPlacedBy(world, blockpos, blockstate1, playerentity, itemstack);
						if (playerentity instanceof ServerPlayerEntity) {
							CriteriaTriggers.PLACED_BLOCK.trigger((ServerPlayerEntity) playerentity, blockpos, itemstack);
						}
					}

					SoundType soundtype = blockstate1.getSoundType(world, blockpos, context.getPlayer());
					world.playSound(playerentity, blockpos, this.getPlaceSound(blockstate1, world, blockpos, context.getPlayer()), SoundCategory.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					if (playerentity == null || !playerentity.abilities.isCreativeMode) {
						itemstack.shrink(1);
					}

					return ActionResultType.func_233537_a_(world.isRemote); // success(isRemote)
				}
			}
		}
	}
	
	@Override
	protected boolean canPlace(BlockItemUseContext context, BlockState state) {
		PlayerEntity playerentity = context.getPlayer();
		ISelectionContext iselectioncontext = playerentity == null ? ISelectionContext.dummy() : ISelectionContext.forEntity(playerentity);
		// "placedBlockCollides" = canPlace
		return (!this.checkPosition() || state.isValidPosition(context.getWorld(), this.getPos(context)))
				&& context.getWorld().placedBlockCollides(state, this.getPos(context), iselectioncontext); // EDITx2
	}
	
	@Override
	protected boolean placeBlock(BlockItemUseContext context, BlockState state) {
		return context.getWorld().setBlockState(getPos(context), state, 8 | 2 | 1); // EDIT
	}
	
	//the following methods were copied because the original is private, but called in tryPlace
	private BlockState func_219985_a(BlockPos pos, World world, ItemStack stack, BlockState state) {
		BlockState blockstate = state;
		CompoundNBT compoundnbt = stack.getTag();
		if (compoundnbt != null) {
			CompoundNBT compoundnbt1 = compoundnbt.getCompound("BlockStateTag");
			StateContainer<Block, BlockState> statecontainer = state.getBlock().getStateContainer();

			for (String s : compoundnbt1.keySet()) {
				Property<?> property = statecontainer.getProperty(s);
				if (property != null) {
					String s1 = compoundnbt1.get(s).getString();
					blockstate = func_219988_a(blockstate, property, s1);
				}
			}
		}

		if (blockstate != state) {
			world.setBlockState(pos, blockstate, 2);
		}

		return blockstate;
	}
	
	private static <T extends Comparable<T>> BlockState func_219988_a(BlockState state, Property<T> property, String value) {
		return property.parseValue(value).map((lamValue) -> {
			return state.with(property, lamValue);
		}).orElse(state);
	}
}
