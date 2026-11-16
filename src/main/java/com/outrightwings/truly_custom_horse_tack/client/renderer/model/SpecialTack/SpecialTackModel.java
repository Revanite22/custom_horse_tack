package com.outrightwings.truly_custom_horse_tack.client.renderer.model.SpecialTack;

import com.mojang.blaze3d.vertex.PoseStack;
import com.outrightwings.truly_custom_horse_tack.client.renderer.HorseTackRenderData.Motion;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public abstract class SpecialTackModel extends Model<Motion> {
    protected SpecialTackModel(ModelPart root) { super(root, RenderTypes::entityCutout); }

    protected void surface(OrderedSubmitNodeCollector collector, Motion state, PoseStack pose,
                           int light, int outline, Identifier texture, int color) {
        if (texture != null) collector.submitModel(this, state, pose, RenderTypes.entityCutout(texture),
                light, OverlayTexture.NO_OVERLAY, color, null, outline, null);
    }

    public abstract void submit(Motion state, PoseStack pose, OrderedSubmitNodeCollector collector, int light, int outline, int color);
}
