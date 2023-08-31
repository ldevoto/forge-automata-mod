package com.primis.automata.entities.goals;

import com.mojang.logging.LogUtils;
import com.primis.automata.entities.LumberjackAutomata;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import java.util.EnumSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;

public class FindNearTreesGoal extends Goal {
    protected final LumberjackAutomata mob;
    public final double speedModifier;
    private AutomataStatus status;
    private AutomataStatus lastStatus;
    private final AutomataStatus.IdleStatus idleStatus;
    private final AutomataStatus.FindingTreesStatus findingNearTreesStatus;
    private final AutomataStatus.FindingTreesStatus findingFarTreesStatus;
    private final AutomataStatus.ChoppingTreeStatus choppingTreeStatus;
    private final AutomataStatus.MovingToBlockPos movingToBlockPosStatus;

    private static final Logger LOGGER = LogUtils.getLogger();

    public FindNearTreesGoal(LumberjackAutomata lumberjackAutomata) {
        this.mob = lumberjackAutomata;
        this.speedModifier = lumberjackAutomata.getAttributeValue(Attributes.FLYING_SPEED);
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.JUMP));
        this.idleStatus = new AutomataStatus.IdleStatus(this);
        this.findingNearTreesStatus = new AutomataStatus.FindingNearTreesStatus(this, 2);
        this.findingFarTreesStatus = new AutomataStatus.FindingTreesStatus(this, 16);
        this.choppingTreeStatus = new AutomataStatus.ChoppingTreeStatus(this);
        this.movingToBlockPosStatus = new AutomataStatus.MovingToBlockPos(this);
        this.status = idleStatus;
        this.lastStatus = idleStatus;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public boolean canUse() {
        return status.canUse();
    }

    @Override
    public void start() {
        status.start();
    }

    @Override
    public boolean canContinueToUse() {
        return status.canContinueToUse();
    }

    @Override
    public void tick() {
        status.tick();
    }

    private void changeStatus(AutomataStatus newStatus) {
        LOGGER.info("Changing status from {} to {}", status.getClass().getSimpleName(), newStatus.getClass().getSimpleName());
        lastStatus = status;
        status = newStatus;
        status.onChange();
    }

    /**
     * Se mueve a un espacio vacío y luego tala en el nivel inferior
     * @param blockPos
     */
    private void moveToAndThenChop(BlockPos blockPos) {
        movingToBlockPosStatus.setTarget(blockPos);
        movingToBlockPosStatus.setOnReachStatus(choppingTreeStatus);
        choppingTreeStatus.setOrigin(blockPos == null ? null : blockPos.below());
        changeStatus(movingToBlockPosStatus);
    }

    private void findFarTrees() {
        findingFarTreesStatus.setCenter(mob.blockPosition());
        changeStatus(findingFarTreesStatus);
    }

    private void findNearTrees() {
        changeStatus(findingNearTreesStatus);
    }

    private void idle() {
        changeStatus(idleStatus);
    }

    private static Optional<BlockState> findHighestLogOrLeave(LevelReader level, BlockPos.MutableBlockPos heightBlockPos) {
        int maxHeight = level.getHeight(Heightmap.Types.OCEAN_FLOOR, heightBlockPos.getX(), heightBlockPos.getZ());
        heightBlockPos.setY(maxHeight - 1);
        if (isValidTarget(level, heightBlockPos)) {
            return Optional.of(level.getBlockState(heightBlockPos));
        }
        return Optional.empty();
    }

    protected static boolean isValidTarget(LevelReader level, BlockPos pos) {
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

    private static boolean isALeave(BlockState blockState) {
        return blockState.getMaterial().equals(Material.LEAVES)
                && blockState.getBlockHolder().unwrapKey().map(ResourceKey::location).map(ResourceLocation::getPath).orElse("").endsWith("_leaves");
    }

    private static boolean isALog(BlockState blockState) {
        return blockState.getMaterial().equals(Material.WOOD)
                && blockState.getBlockHolder().unwrapKey().map(ResourceKey::location).map(ResourceLocation::getPath).orElse("").endsWith("_log");
    }

    private abstract static class AutomataStatus {
        protected final FindNearTreesGoal goal;
        protected final LumberjackAutomata mob;

        public AutomataStatus(FindNearTreesGoal goal) {
            this.goal = goal;
            this.mob = goal.mob;
        }

        boolean canUse() {
            return false;
        }

        void start() {

        }

        boolean canContinueToUse() {
            return true;
        }

        void onChange() {

        }

        abstract void tick();

        private static class IdleStatus extends AutomataStatus {

            public IdleStatus(FindNearTreesGoal goal) {
                super(goal);
            }

            @Override
            boolean canUse() {
                return true;
            }

            @Override
            boolean canContinueToUse() {
                return true;
            }

            @Override
            void tick() {
                goal.findFarTrees();
            }
        }

        private static class MovingToBlockPos extends AutomataStatus {
            private BlockPos blockPos;
            private AutomataStatus onReachStatus;
            private int ticksInPlace;
            private static final int MIN_TICKS_IN_PLACE = 20;
            private static final double MAX_IN_PLACE_DISTANCE = 0.8D;

            public MovingToBlockPos(FindNearTreesGoal goal) {
                super(goal);
            }

            @Override
            public void tick() {
                if (mob.getNavigation().isDone()) {
                    // El navigator detectó que llegó pero es posible que el mob venga con inersia y se pase. Por eso
                    // se espera una cierta cantidad de tiempo antes de chequear si efectivamente el mob está a una distancia
                    // aceptable del objetivo.

                    if (!blockPos.closerToCenterThan(this.mob.position(), MAX_IN_PLACE_DISTANCE)) {
                        moveToTarget();
                        return;
                    }
                    mob.setDeltaMovement(Vec3.ZERO);
                    //LOGGER.info("Move finished. target {}, current position {}", this.blockPos, mob.blockPosition());
                    goal.changeStatus(onReachStatus);
                } else if (mob.getNavigation().isStuck()) {
                    unStack();
                }
            }

            @Override
            void onChange() {
                moveToTarget();
            }

            private void unStack() {
                LOGGER.info("Unstacking. target {}, current position {}", this.blockPos, mob.position());
                setTarget(getAnyNearAirPosition());
                moveToTarget();
            }

            private void moveToTarget() {
                ticksInPlace = 0;
                if (this.blockPos == null) {
                    goal.idle();
                    return;
                }
                PathNavigation navigation = this.mob.getNavigation();
                boolean hasAPath = navigation.moveTo(navigation.createPath(this.blockPos, 0), goal.speedModifier);
                if (!hasAPath && !blockPos.closerToCenterThan(this.mob.position(), MAX_IN_PLACE_DISTANCE)) {
                    unStack();
                }
            }

            private BlockPos getAnyNearAirPosition() {
                BlockPos.MutableBlockPos blockPos = new BlockPos.MutableBlockPos().set(mob.blockPosition());
                for (int i = 0; i < 10; i++) {
                    blockPos.move(Direction.getRandom(mob.getRandom()));
                    if (mob.level.isEmptyBlock(blockPos)) {
                        return blockPos.immutable();
                    }
                }
                return null;
            }

            public void setTarget(BlockPos blockPos) {
                this.blockPos = blockPos;
            }

            public void setOnReachStatus(AutomataStatus onReachStatus) {
                this.onReachStatus = onReachStatus;
            }
        }

        private static class FindingTreesStatus extends AutomataStatus {
            private Queue<HeightBlockPos> heightBlockPosQueue = new PriorityQueue<>();
            private final int maxDist;
            private BlockPos center;

            public FindingTreesStatus(FindNearTreesGoal goal, int maxDist) {
                super(goal);
                this.maxDist = maxDist;
                this.center = mob.blockPosition();
            }

            @Override
            public void tick() {
                if (heightBlockPosQueue.isEmpty()) {
                    spiralTraversal(mob.level, center, maxDist);
                }
                if (heightBlockPosQueue.isEmpty()) {
                    goal.idle();
                    return;
                }
                BlockPos blockPos = null;
                while (!heightBlockPosQueue.isEmpty()) {
                    var blockpos = heightBlockPosQueue.remove().pos();
                    if (isValidTarget(mob.level, blockpos)) {
                        blockPos = blockpos.above();
                        break;
                    }
                }
                moveAndChop(blockPos);
            }

            public boolean spiralTraversal(LevelReader level, BlockPos start, int maxDist) {
                LOGGER.info("Spiral traversal from {} with max distance {}", start, maxDist);
                heightBlockPosQueue = new PriorityQueue<>();
                Direction[] directions = {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};
                Vec3i startOffset = Vec3i.ZERO.relative(Direction.WEST).relative(Direction.NORTH);
                BlockPos.MutableBlockPos heightBlockPos = new BlockPos.MutableBlockPos().set(start);
                Optional<BlockState> optionalBlockState = findHighestLogOrLeave(level, heightBlockPos);

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
                            optionalBlockState = findHighestLogOrLeave(level, heightBlockPos);
                            if (optionalBlockState.isPresent()) {
                                heightBlockPosQueue.add(new HeightBlockPos(heightBlockPos.immutable()));
                            }
                        }
                    }
                }
                LOGGER.info("Found {} blocks", heightBlockPosQueue.size());
                return !heightBlockPosQueue.isEmpty();
            }

            protected void moveAndChop(BlockPos blockPos) {
                goal.findingNearTreesStatus.setCenter(blockPos);
                goal.moveToAndThenChop(blockPos);
            }

            public void setCenter(BlockPos center) {
                this.center = center;
            }
        }

        private static class FindingNearTreesStatus extends FindingTreesStatus {

            public FindingNearTreesStatus(FindNearTreesGoal goal, int maxDist) {
                super(goal, maxDist);
            }

            @Override
            protected void moveAndChop(BlockPos blockPos) {
                if (blockPos != null) {
                    goal.moveToAndThenChop(blockPos);
                }
            }
        }

        private static class ChoppingTreeStatus extends AutomataStatus {
            private boolean isChopping;
            private BlockPos startPosition = null;
            private BlockPos currentChoppedPosition = null;
            private Queue<BlockPos> chopStack = new LinkedList<>();
            private static final int MAX_CHOP_DISTANCE = 2;

            public ChoppingTreeStatus(FindNearTreesGoal goal) {
                super(goal);
            }

            @Override
            public void tick() {
                if (isChopping) {
                    nextChoppingStep();
                } else if (!chopStack.isEmpty()) {
                    startChopping();
                } else {
                    fillChopStack(mob.level, startPosition, MAX_CHOP_DISTANCE);
                    if (chopStack.isEmpty()) {
                        goal.findNearTrees();
                    }
                }
            }

            @Override
            void onChange() {
                currentChoppedPosition = null;
                chopStack = new LinkedList<>();
            }

            private void nextChoppingStep() {
                // TODO: Chop logic
                BlockState blockstate = mob.level.getBlockState(currentChoppedPosition);
                mob.level.removeBlock(currentChoppedPosition, false);
                mob.level.gameEvent(GameEvent.BLOCK_DESTROY, currentChoppedPosition, GameEvent.Context.of(this.mob, blockstate));
                //this.enderman.setCarriedBlock(blockstate.getBlock().defaultBlockState());
                isChopping = false;
            }

            private void startChopping() {
                isChopping = true;
                currentChoppedPosition = chopStack.remove();
            }

            private void fillChopStack(LevelReader level, BlockPos start, int maxDist) {
                chopStack = new LinkedList<>();
                Direction[] directions = {Direction.EAST, Direction.SOUTH, Direction.WEST, Direction.NORTH};
                Vec3i startOffset = Vec3i.ZERO.relative(Direction.WEST).relative(Direction.NORTH);
                BlockPos.MutableBlockPos blockPosToChop = new BlockPos.MutableBlockPos().set(start);
                if (isValidTarget(level, blockPosToChop)) {
                    chopStack.add(blockPosToChop.immutable());
                }

                for (int dist = 1; dist <= maxDist; dist++) {
                    BlockPos.MutableBlockPos currentPos = start.mutable();
                    currentPos.move(startOffset.multiply(dist));
                    for (Direction direction : directions) {
                        for (int j = 0; j < dist * 2; j++) {
                            currentPos.move(direction);
                            blockPosToChop.set(currentPos);
                            if (isValidTarget(level, blockPosToChop)) {
                                chopStack.add(blockPosToChop.immutable());
                            }
                        }
                    }
                }
                //LOGGER.info("Found {} blocks to chop", chopStack.size());
            }

            public void setOrigin(BlockPos blockPos) {
                this.startPosition = blockPos;
            }
        }
    }


    private record HeightBlockPos(BlockPos pos) implements Comparable<HeightBlockPos> {
        @Override
        public int compareTo(HeightBlockPos o) {
            return -Integer.compare(pos.getY(), o.pos().getY());
        }
    }
}
