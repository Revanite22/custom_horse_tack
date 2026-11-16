package com.outrightwings.truly_custom_horse_tack.client.renderer.model;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

/** Display-only copies of the original geometry factories. Horse rendering is deliberately unchanged. */
@OnlyIn(Dist.CLIENT)
public final class DisplayTackGeometry {
    private DisplayTackGeometry() {}

    public static LayerDefinition createBellsNeckLayer() {
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

    public static LayerDefinition createBellsRearLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition bells = partdefinition.addOrReplaceChild("bells", CubeListBuilder.create(), PartPose.offset(-1f, 11.0F, 9.0F));

        PartDefinition CE = bells.addOrReplaceChild("CE", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, 0.5F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 2.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -1.5F, 5.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition CC = bells.addOrReplaceChild("CC", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, 0.5F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 2.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -0.5F, 5.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition CW = bells.addOrReplaceChild("CW", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, 0.5F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 2.0F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -1.5F, 5.5F, 0.0F, -1.5708F, 0.0F));

        PartDefinition FW2 = bells.addOrReplaceChild("FW2", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 1.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(7.0F, -2.0F, 3.0F));

        PartDefinition FW = bells.addOrReplaceChild("FW", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 1.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.5F, -4.1F, -1.0F));

        PartDefinition BW = bells.addOrReplaceChild("BW", CubeListBuilder.create().texOffs(0, 0).addBox(-0.4F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 1.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(6.5F, -6.0F, -5.0F));

        PartDefinition FE2 = bells.addOrReplaceChild("FE2", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 1.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, -2.0F, 3.0F));

        PartDefinition FE = bells.addOrReplaceChild("FE", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 1.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.5F, -4.1F, -1.0F));

        PartDefinition BE = bells.addOrReplaceChild("BE", CubeListBuilder.create().texOffs(0, 0).addBox(-0.6F, 0.0F, -1.0F, 1.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(0.0F, 1.5F, -0.5F, 0.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(-4.5F, -6.0F, -5.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    public static LayerDefinition createWingsLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition wings = partdefinition.addOrReplaceChild("wings", CubeListBuilder.create(), PartPose.offset(-1F, 11.0F, 9.0F));

        PartDefinition right_wing = wings.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 22).addBox(-6.0F, -2.0F, 0.0F, 10.0F, 20.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -7.0F, -13.0F, 1.5708F, 0.48F, 1.5708F));

        PartDefinition left_wing = wings.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.0F, 0.0F, 10.0F, 20.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, -7.0F, -13.0F, 1.5708F, -0.48F, -1.5708F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    public static LayerDefinition createHornLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        // -0.5F, -8.5F, -2.5F
        PartDefinition horn = partdefinition.addOrReplaceChild("horn", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -15.0F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, -10.0F, 0.5235988F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    public static LayerDefinition createRibbonRightLayer() {
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

    public static LayerDefinition createRibbonLeftLayer() {
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
}
