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
public class HornModel extends SpecialTackModel {
    private final ModelPart horn;
    private final Identifier texture, overlay;
    public enum HORN_TYPE { ITEM, DYED }
    public HornModel(ModelPart root, HORN_TYPE type) {
        super(root); horn = root.getChild("horn");
        texture = type == HORN_TYPE.DYED ? id("horn_color") : null;
        overlay = type == HORN_TYPE.DYED ? null : id("horn");
    }
    @Override public void setupAnim(Motion state) {
        super.setupAnim(state); HorseModelRotations.rotateModelWithHead(horn, state);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        // -0.5F, -8.5F, -2.5F
        PartDefinition horn = partdefinition.addOrReplaceChild("horn", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -15.0F, 2.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 4.0F, -10.0F, 0.5235988F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }

    @Override public void submit(Motion state, PoseStack pose, OrderedSubmitNodeCollector collector, int light, int outline, int color) {
        surface(collector, state, pose, light, outline, texture, color);
        surface(collector, state, pose, light, outline, overlay, -1);
    }
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(Main.MODID, "textures/entity/horse/special_tack/"+path+".png"); }
}
