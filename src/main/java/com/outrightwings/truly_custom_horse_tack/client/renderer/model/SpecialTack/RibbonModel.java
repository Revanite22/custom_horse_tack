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
public class RibbonModel extends SpecialTackModel {
    private final ModelPart ribbon, longs, flower;
    private final Identifier texture = id("ribbon"), overlay = null;
    public RibbonModel(ModelPart root) {
        super(root); ribbon = root.getChild("ribbon"); longs = ribbon.getChild("longs"); flower = ribbon.getChild("flower");
    }
    @Override public void setupAnim(Motion state) {
        super.setupAnim(state); HorseModelRotations.rotateModelWithHead(ribbon, state);
        longs.xRot = -ribbon.xRot; flower.xRot = longs.xRot;
    }

    public static LayerDefinition createBodyLayerRight() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ribbon = partdefinition.addOrReplaceChild("ribbon", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, -10.0F));
        PartDefinition longs = ribbon.addOrReplaceChild("longs", CubeListBuilder.create().texOffs(2, 5).addBox(-0.01F, 1.0005F, -0.5032F, 0.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.75F, -7.0005F, 4.3032F, -0.7854F, 0.0F, 0.0F));

        PartDefinition LongR_r1 = longs.addOrReplaceChild("LongR_r1", CubeListBuilder.create().texOffs(4, 5).addBox(0.0F, 1.0424F, -0.6328F, 0.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.01F, 0.0005F, -0.0032F, -0.1309F, 0.0F, 0.0F));

        PartDefinition LongL_r1 = longs.addOrReplaceChild("LongL_r1", CubeListBuilder.create().texOffs(0, 5).addBox(0.0F, 1.0609F, -0.36F, 0.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0005F, -0.0032F, 0.1309F, 0.0F, 0.0F));

        PartDefinition flower = ribbon.addOrReplaceChild("flower", CubeListBuilder.create().texOffs(0, 2).addBox(-0.313F, -0.9813F, -1.0013F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.937F, -7.0187F, 4.3013F, -0.7854F, 0.0F, 0.0F));

        PartDefinition S_r1 = flower.addOrReplaceChild("S_r1", CubeListBuilder.create().texOffs(6, -1).addBox(0.1057F, -1.0F, -0.2734F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0544F, 0.0187F, 1.0821F, 0.0F, -0.4363F, 0.0F));

        PartDefinition D_r1 = flower.addOrReplaceChild("D_r1", CubeListBuilder.create().texOffs(0, -1).addBox(0.1057F, -1.0F, -0.2734F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.1956F, 1.0021F, -0.0013F, -1.5708F, 0.0F, 0.4363F));

        PartDefinition U_r1 = flower.addOrReplaceChild("U_r1", CubeListBuilder.create().texOffs(4, -1).addBox(0.1057F, -1.0F, -0.7266F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0229F, -1.0583F, -0.0013F, -1.5708F, 0.0F, -0.4363F));

        PartDefinition N_r1 = flower.addOrReplaceChild("N_r1", CubeListBuilder.create().texOffs(2, -1).addBox(0.1057F, -1.0F, -0.7266F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.0229F, 0.0187F, -1.0783F, 0.0F, 0.4363F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    public static LayerDefinition createBodyLayerLeft() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition ribbon = partdefinition.addOrReplaceChild("ribbon", CubeListBuilder.create(), PartPose.offset(0.0F, 4.0F, -10.0F));

        PartDefinition longs = ribbon.addOrReplaceChild("longs", CubeListBuilder.create().texOffs(2, 5).addBox(-0.4833F, 1.2432F, -0.5032F, 0.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(3.2833F, -7.1721F, 4.4748F));

        PartDefinition LongR_r1 = longs.addOrReplaceChild("LongR_r1", CubeListBuilder.create().texOffs(4, 5).addBox(0.0F, 1.0424F, -0.6328F, 0.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5233F, 0.2432F, -0.0032F, -0.1309F, 0.0F, 0.0F));

        PartDefinition LongL_r1 = longs.addOrReplaceChild("LongL_r1", CubeListBuilder.create().texOffs(0, 5).addBox(0.0F, 1.0609F, -0.36F, 0.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.5333F, 0.2432F, -0.0032F, 0.1309F, 0.0F, 0.0F));

        PartDefinition flower = ribbon.addOrReplaceChild("flower", CubeListBuilder.create().texOffs(0, 2).addBox(-1.0779F, -0.9843F, -1.0013F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(3.3279F, -7.0166F, 4.2991F));

        PartDefinition S_r1 = flower.addOrReplaceChild("S_r1", CubeListBuilder.create().texOffs(6, -1).addBox(0.1057F, -1.0F, -0.2734F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3105F, 0.0157F, 1.0821F, 0.0F, -0.4363F, -3.1416F));

        PartDefinition D_r1 = flower.addOrReplaceChild("D_r1", CubeListBuilder.create().texOffs(0, -1).addBox(0.1209F, -1.0F, -0.1678F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3102F, 0.925F, -0.0013F, -1.5708F, 3.1416F, -0.4363F));

        PartDefinition U_r1 = flower.addOrReplaceChild("U_r1", CubeListBuilder.create().texOffs(4, -1).addBox(0.1057F, -1.0F, -0.7266F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3879F, -1.0614F, -0.0013F, -1.5708F, 3.1416F, 0.4363F));

        PartDefinition N_r1 = flower.addOrReplaceChild("N_r1", CubeListBuilder.create().texOffs(2, -1).addBox(0.1057F, -1.0F, -0.7266F, 0.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-0.3879F, 0.0157F, -1.0783F, 0.0F, 0.4363F, -3.1416F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override public void submit(Motion state, PoseStack pose, OrderedSubmitNodeCollector collector, int light, int outline, int color) {
        surface(collector, state, pose, light, outline, texture, color);
        surface(collector, state, pose, light, outline, overlay, -1);
    }
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(Main.MODID, "textures/entity/horse/special_tack/"+path+".png"); }
}
