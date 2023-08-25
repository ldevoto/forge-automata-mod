package com.primis.automata.register;

import com.primis.automata.AutomataMod;
import com.primis.automata.entities.LumberjackAutomata;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegisterer {
    public static final String LUMBERJACK_AUTOMATA_NAME = "lumberjack";
    public static final String LUMBERJACK_AUTOMATA_ID = AutomataMod.MOD_ID + ":" + LUMBERJACK_AUTOMATA_NAME;

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, AutomataMod.MOD_ID);
    public static final RegistryObject<EntityType<LumberjackAutomata>> LUMBERJACK_AUTOMATA_ENTITY = ENTITIES.register("lumberjack_automata_entity", () -> EntityType.Builder.of(LumberjackAutomata::new, MobCategory.CREATURE).sized(1.0f, 1.0f).build(LUMBERJACK_AUTOMATA_ID));
}
