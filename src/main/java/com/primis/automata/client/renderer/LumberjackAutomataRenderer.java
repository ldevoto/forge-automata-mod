package com.primis.automata.client.renderer;

import com.primis.automata.client.models.LumberjackAutomataModel;
import com.primis.automata.constants.Names;
import com.primis.automata.entities.LumberjackAutomata;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class LumberjackAutomataRenderer extends MobRenderer<LumberjackAutomata, LumberjackAutomataModel> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(Names.MOD_ID, Names.ENTITY_LUMBERJACK_TEXTURE);

    public LumberjackAutomataRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new LumberjackAutomataModel(ctx.bakeLayer(LumberjackAutomataModel.LAYER_LOCATION)), 0.1f);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull LumberjackAutomata pEntity) {
        return TEXTURE;
    }
}
