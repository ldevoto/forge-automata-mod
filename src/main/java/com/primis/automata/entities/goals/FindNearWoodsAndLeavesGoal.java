package com.primis.automata.entities.goals;

import com.mojang.logging.LogUtils;
import com.primis.automata.entities.LumberjackAutomata;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.EnumSet;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;

public class FindNearWoodsAndLeavesGoal extends Goal {
    protected final LumberjackAutomata mob;
    public final double speedModifier;
    protected int nextStartTick;
    protected int tryTicks;
    private int maxStayTicks;
    protected BlockPos blockPos = BlockPos.ZERO;
    private boolean reachedTarget;
    private Queue<HeightBlockPos> heightBlockPosQueue = new PriorityQueue<>();

    private static final Integer MAX_DIST = 16;
    private static final Logger LOGGER = LogUtils.getLogger();

    public FindNearWoodsAndLeavesGoal(LumberjackAutomata lumberjackAutomata) {
        this.mob = lumberjackAutomata;
        this.speedModifier = lumberjackAutomata.getAttributeValue(Attributes.FLYING_SPEED);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        return this.findNearestBlock();
        /*if (this.nextStartTick > 0) {
            --this.nextStartTick;
            return false;
        } else {
            this.nextStartTick = this.generateNextStartTick(this.mob);
            return this.findNearestBlock();
        }*/
    }

    protected int generateNextStartTick(@NotNull PathfinderMob pCreature) {
        return reducedTickDelay(200 + pCreature.getRandom().nextInt(200));
    }

    protected boolean findNearestBlock() {
        return spiralTraversal(mob.level, mob.blockPosition(), MAX_DIST);
    }

    @Override
    public boolean canContinueToUse() {
        //return this.tryTicks >= -this.maxStayTicks && this.tryTicks <= 1200 && this.isValidTarget(this.mob.level, this.blockPos);
        return !heightBlockPosQueue.isEmpty();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void start() {
        this.blockPos = heightBlockPosQueue.remove().pos();
        BlockPos blockpos = this.getMoveToTarget();
        moveTo(blockpos);
        //this.reachedTarget = false;
        //this.tryTicks = 0;
        //this.maxStayTicks = this.mob.getRandom().nextInt(this.mob.getRandom().nextInt(1200) + 1200) + 1200;
    }

    @Override
    public void tick() {
        if (this.mob.getNavigation().isDone()) {
            if (!heightBlockPosQueue.isEmpty()) {
                this.blockPos = heightBlockPosQueue.remove().pos();
                LOGGER.info("Dequeue {}", this.blockPos);
                moveTo(this.getMoveToTarget());
            } else {
                this.blockPos = null;
                LOGGER.info("Queue is empty");
            }
        } else if (this.mob.getNavigation().isStuck()) {
            LOGGER.info("Stuck at {}", this.mob.blockPosition());
            moveTo(this.getMoveToTarget());
        }
        /*BlockPos blockpos = this.getMoveToTarget();
            if (this.mob.getNavigation().isDone()) {
                LOGGER.info("Done at {}", this.mob.blockPosition());
            } else if (this.mob.getNavigation().isStuck()) {
                LOGGER.info("Stuck at {}", this.mob.blockPosition());
            } else {
                LOGGER.info("Progress at {}", this.mob.blockPosition());
            }
        if (!blockpos.closerToCenterThan(this.mob.position(), this.acceptedDistance())) {
            this.reachedTarget = false;
            ++this.tryTicks;
            if (this.shouldRecalculatePath() && !this.mob.getNavigation().isInProgress()) {
                LOGGER.info("Stuck at {}, trying to move to {}", this.mob.blockPosition(), blockpos);
                moveTo(blockpos);
            }
            LOGGER.info("Trying to reach target {}", blockpos);
        } else {
            this.reachedTarget = true;
            --this.tryTicks;
        }
        if (this.reachedTarget) {
            LOGGER.info("Reached target {}", this.blockPos);
        }*/
    }

    private void moveTo(BlockPos blockpos) {
        PathNavigation navigation = this.mob.getNavigation();
        navigation.moveTo(navigation.createPath(blockpos, 0), this.speedModifier);
    }

    protected BlockPos getMoveToTarget() {
        return this.blockPos.above();
    }

    public boolean shouldRecalculatePath() {
        return this.tryTicks % 40 == 0;
    }

    public double acceptedDistance() {
        return 1D;
    }

    public boolean spiralTraversal(LevelReader level, BlockPos start, int maxDist) {
        heightBlockPosQueue = new PriorityQueue<>();
        Direction[] directions = {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};
        Vec3i startOffset = Vec3i.ZERO.relative(Direction.WEST).relative(Direction.NORTH);
        BlockPos.MutableBlockPos heightBlockPos = new BlockPos.MutableBlockPos().set(start);
        Optional<BlockState> optionalBlockState = getLogOrLeave(level, heightBlockPos);

        if (optionalBlockState.isPresent()) {
            heightBlockPosQueue.add(new HeightBlockPos(heightBlockPos.immutable()));
        }

        for (int dist = 1; dist <= maxDist; dist++) {
            BlockPos.MutableBlockPos currentPos = start.mutable();
            currentPos.move(startOffset.multiply(dist));
            for (Direction direction : directions) {
                for (int j = 0; j < dist * 2; j++) {
                    currentPos.move(direction);
                    heightBlockPos.set(currentPos);
                    optionalBlockState = getLogOrLeave(level, heightBlockPos);
                    if (optionalBlockState.isPresent()) {
                        heightBlockPosQueue.add(new HeightBlockPos(heightBlockPos.immutable()));
                    }
                }
            }
        }
        LOGGER.info("Found {} blocks", heightBlockPosQueue.size());
        return !heightBlockPosQueue.isEmpty();
    }

    private Optional<BlockState> getLogOrLeave(LevelReader level, BlockPos.MutableBlockPos heightBlockPos) {
        int maxHeight = level.getHeight(Heightmap.Types.OCEAN_FLOOR, heightBlockPos.getX(), heightBlockPos.getZ());
        heightBlockPos.setY(maxHeight - 1);
        if (isValidTarget(level, heightBlockPos)) {
            return Optional.of(level.getBlockState(heightBlockPos));
        }
        return Optional.empty();
    }

    protected boolean isValidTarget(LevelReader level, BlockPos pos) {
        BlockState blockState = level.getBlockState(pos);
        if (blockState.isAir()) {
            return false;
        }
        if (blockState.is(BlockTags.DIRT)) {
            return false;
        }
        if (isALog(blockState)) {
            return true;
        }
        if (isALeave(blockState)) {
            return true;
        }
        return false;
    }

    private boolean isALeave(BlockState blockState) {
        return blockState.getMaterial().equals(Material.LEAVES)
                && blockState.getBlockHolder().unwrapKey().map(ResourceKey::location).map(ResourceLocation::getPath).orElse("").endsWith("_leaves");
    }

    private static boolean isALog(BlockState blockState) {
        return blockState.getMaterial().equals(Material.WOOD)
                && blockState.getBlockHolder().unwrapKey().map(ResourceKey::location).map(ResourceLocation::getPath).orElse("").endsWith("_log");
    }

    private record HeightBlockPos(BlockPos pos) implements Comparable<HeightBlockPos> {
        @Override
        public int compareTo(HeightBlockPos o) {
            return -Integer.compare(pos.getY(), o.pos().getY());
        }
    }
}
