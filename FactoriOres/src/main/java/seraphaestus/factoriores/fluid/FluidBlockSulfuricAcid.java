package seraphaestus.factoriores.fluid;

import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class FluidBlockSulfuricAcid extends FlowingFluidBlock {

	public FluidBlockSulfuricAcid(Supplier<? extends FlowingFluid> supplier, Properties properties) {
        super(supplier, properties);
    }
	
	@Override
    public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
        if (entityIn instanceof LivingEntity) {
        	if (entityIn instanceof ArmorStandEntity ||
        		entityIn instanceof AbstractSkeletonEntity ||
        		entityIn instanceof WitherEntity) return;
        	
        	LivingEntity entity = (LivingEntity) entityIn;
        	
        	float damage = 3.0f;
        	if (entityIn instanceof SlimeEntity) damage *= 2;
        	else if (entityIn instanceof IronGolemEntity) damage /= 2;
        	
        	entity.attackEntityFrom(DamageSource.LAVA, damage);
            return;
        }
    }
	
	@Override
    public boolean isFlammable(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return true;
    }

    @Override
    public int getFlammability(BlockState state, IBlockReader world, BlockPos pos, Direction face) {
        return 80;
    }
}
