package com.primis.automata.events;

import com.primis.automata.AutomataMod;
import com.primis.automata.entities.LumberjackAutomata;
import com.primis.automata.register.EntityRegisterer;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AutomataMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCommonEvents {

    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(EntityRegisterer.LUMBERJACK_AUTOMATA_ENTITY.get(), LumberjackAutomata.getLumberjackAttributes().build());
    }
}
