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
public class WingsModel extends SpecialTackModel {
    private final ModelPart wings, leftWing, rightWing;
    private final Identifier texture, overlay;
    public enum WING_TYPE { ITEM, DYED, BUTTERFLY, VEX }
    public WingsModel(ModelPart root, WING_TYPE type) {
        super(root); wings = root.getChild("wings");
        leftWing = wings.getChild("left_wing"); rightWing = wings.getChild("right_wing");
        texture = switch (type) { case DYED -> id("wings_dyed"); case BUTTERFLY -> id("wings_butterfly"); case VEX -> id("wings_vex"); default -> null; };
        overlay = type == WING_TYPE.ITEM ? id("wings") : type == WING_TYPE.BUTTERFLY ? id("wings_butterfly_overlay") : null;
    }
    @Override public void setupAnim(Motion state) {
        super.setupAnim(state); HorseModelRotations.rotateModelWithBody(wings, state);
        float x = Mth.lerp(state.wingOpen(), 90, 126);
        float y = Mth.lerp(state.wingOpen(), 332.5f, 292);
        float z = Mth.lerp(state.wingOpen(), 270, 125);
        if (state.flap() && state.wingOpen() == 1) {
            float wobble = (4 * Mth.cos(((float)Math.PI * 2F) * state.phase())) * (float)Math.PI;
            x += wobble; y += wobble;
        }
        leftWing.setRotation(Mth.DEG_TO_RAD * x, Mth.DEG_TO_RAD * y, Mth.DEG_TO_RAD * z);
        rightWing.setRotation(Mth.DEG_TO_RAD * x, Mth.DEG_TO_RAD * -y, Mth.DEG_TO_RAD * -z);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition wings = partdefinition.addOrReplaceChild("wings", CubeListBuilder.create(), PartPose.offset(-1F, 11.0F, 9.0F));

        PartDefinition right_wing = wings.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 22).addBox(-6.0F, -2.0F, 0.0F, 10.0F, 20.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-6.0F, -7.0F, -13.0F, 1.5708F, 0.48F, 1.5708F));

        PartDefinition left_wing = wings.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.0F, 0.0F, 10.0F, 20.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, -7.0F, -13.0F, 1.5708F, -0.48F, -1.5708F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override public void submit(Motion state, PoseStack pose, OrderedSubmitNodeCollector collector, int light, int outline, int color) {
        surface(collector, state, pose, light, outline, texture, color);
        surface(collector, state, pose, light, outline, overlay, -1);
    }
    private static Identifier id(String path) { return Identifier.fromNamespaceAndPath(Main.MODID, "textures/entity/horse/special_tack/"+path+".png"); }
}
