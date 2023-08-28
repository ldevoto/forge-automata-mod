package com.primis.automata.events;

import com.primis.automata.client.models.LumberjackAutomataModel;
import com.primis.automata.client.renderer.LumberjackAutomataRenderer;
import com.primis.automata.constants.Names;
import com.primis.automata.register.EntityRegisterer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Names.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ModClientEvents {

    @SubscribeEvent
    public static void entityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EntityRegisterer.LUMBERJACK_AUTOMATA_ENTITY.get(), LumberjackAutomataRenderer::new);
    }

    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LumberjackAutomataModel.LAYER_LOCATION, LumberjackAutomataModel::createBodyLayer);
    }
}
