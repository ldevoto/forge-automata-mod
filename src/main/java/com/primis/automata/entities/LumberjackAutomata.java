package com.primis.automata.entities;

import com.mojang.logging.LogUtils;
import com.primis.automata.entities.goals.FindNearTreesGoal;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

public class LumberjackAutomata extends PathfinderMob {
    private static final Logger LOGGER = LogUtils.getLogger();
    public float harvestSpeed;

    public LumberjackAutomata(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.moveControl = new FlyingMoveControl(this, 10, true);
        this.getNavigation().setMaxVisitedNodesMultiplier(10.0F);
        this.harvestSpeed = 1.0F;
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, level);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanPassDoors(false);
        return flyingpathnavigation;
    }

    public static AttributeSupplier.Builder getLumberjackAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.MOVEMENT_SPEED, 0.6D)
                .add(Attributes.FLYING_SPEED, 0.8D)
                .add(Attributes.FOLLOW_RANGE, 64.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.8D));
        this.goalSelector.addGoal(4, new FindNearTreesGoal(this));
    }

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        return false;
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
        /* Automatas has no fall damage */
    }

    @Override
    public void die(DamageSource pDamageSource) {
        super.die(pDamageSource);
    }

    @Override
    public void checkDespawn() {
        super.checkDespawn();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        pCompound.putBoolean("PersistenceRequired", true);
        super.readAdditionalSaveData(pCompound);
    }
}
