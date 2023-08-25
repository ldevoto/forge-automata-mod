package com.primis.automata.entities;

import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class LumberjackAutomata extends PathfinderMob {

    private Optional<GlobalPos> SpawnPosition = Optional.empty();

    public LumberjackAutomata(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }
}
