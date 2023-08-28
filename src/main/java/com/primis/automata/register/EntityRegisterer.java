package com.primis.automata.register;

import com.primis.automata.constants.Names;
import com.primis.automata.entities.LumberjackAutomata;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityRegisterer {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Names.MOD_ID);
    public static final RegistryObject<EntityType<LumberjackAutomata>> LUMBERJACK_AUTOMATA_ENTITY = ENTITIES.register(Names.ENTITY_LUMBERJACK_REGISTRY,
            () -> EntityType.Builder.of(LumberjackAutomata::new, MobCategory.CREATURE).sized(0.5f, 0.5f).build(Names.ENTITY_LUMBERJACK_ID));
}
