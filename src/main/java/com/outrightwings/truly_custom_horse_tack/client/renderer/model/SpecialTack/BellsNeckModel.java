package com.outrightwings.truly_custom_horse_tack.client.renderer.model.SpecialTack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.outrightwings.truly_custom_horse_tack.Main;
import com.outrightwings.truly_custom_horse_tack.client.renderer.HorseTackRenderData.Motion;
import com.outrightwings.truly_custom_horse_tack.client.util.HorseModelRotations;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class BellsNeckModel extends SpecialTackModel {
    private final ModelPart bells;
    private final ModelPart[] sideBells;
    private final Identifier texture = id("bell"), overlay = null;
    public BellsNeckModel(ModelPart root) {
        super(root); bells = root.getChild("bells");
        sideBells = new ModelPart[]{bells.getChild("FW"), bells.getChild("BW"), bells.getChild("FE"), bells.getChild("BE")};
    }
    @Override public void setupAnim(Motion state) {
        super.setupAnim(state); HorseModelRotations.rotateModelWithBody(bells, state);
        for (var bell : sideBells) bell.xRot = state.rearing() * 0.7853982F;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bells = partdefinition.addOrReplaceChild("bells", CubeListBuilder.create(), PartPose.offset(-1f, 11.0F, 9.0F));

        PartDefinition CE = bells.addOrReplaceChild("CE", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, 0.5F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 2.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -1.5F, -19.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition CC = bells.addOrReplaceChild("CC", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, 0.5F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 2.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -0.5F, -19.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition CW = bells.addOrReplaceChild("CW", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, 0.5F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 2.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -1.5F, -19.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition FW = bells.addOrReplaceChild("FW", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 1.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.5F, -3.0F, -16.0F));

        PartDefinition BW = bells.addOrReplaceChild("BW", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 1.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.5F, -6.0F, -13.0F));

        PartDefinition FE = bells.addOrReplaceChild("FE", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 1.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.5F, -3.0F, -16.0F));

        PartDefinition BE = bells.addOrReplaceChild("BE", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 1.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.5F, -6.0F, -13.0F));
        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override public void submit(Motion state, PoseStack pose, OrderedSubmitNodeCollector collector, int light, int outline, int color) {
        surface(collector, state, pose, light, outline, texture, -1);
        surface(collector, state, pose, light, outline, overlay, -1);
    }
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(Main.MODID, "textures/entity/horse/special_tack/"+path+".png"); }
}
