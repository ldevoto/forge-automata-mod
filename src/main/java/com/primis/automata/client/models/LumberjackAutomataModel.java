package com.primis.automata.client.models;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.primis.automata.constants.Names;
import com.primis.automata.entities.LumberjackAutomata;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

public class LumberjackAutomataModel extends EntityModel<LumberjackAutomata> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(Names.MOD_ID, Names.ENTITY_LUMBERJACK_REGISTRY), "main");
	private final ModelPart lumberjack;

	public LumberjackAutomataModel(ModelPart root) {
		this.lumberjack = root.getChild("lumberjack");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition lumberjack = partdefinition.addOrReplaceChild("lumberjack", CubeListBuilder.create(), PartPose.offset(0.0F, 22.0F, -0.5F));

		PartDefinition head = lumberjack.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -1.6F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.7F, 0.5F));

		PartDefinition left_arm = lumberjack.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(4, 4).addBox(0.0F, -0.2F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(1.1F, -0.4F, 0.7F));

		PartDefinition right_arm = lumberjack.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -0.2F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.1F, -0.4F, 0.7F));

		PartDefinition left_foot = lumberjack.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(4, 7).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 1.0F, 0.5F));

		PartDefinition right_leg = lumberjack.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 7).addBox(-0.5F, 0.0F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 1.0F, 0.5F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}

	@Override
	public void setupAnim(LumberjackAutomata entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		lumberjack.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}