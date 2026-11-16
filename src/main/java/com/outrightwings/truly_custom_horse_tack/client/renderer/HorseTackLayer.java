package com.outrightwings.truly_custom_horse_tack.client.renderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import com.outrightwings.truly_custom_horse_tack.client.item.tack.TackModels;
import com.outrightwings.truly_custom_horse_tack.client.renderer.model.SpecialTack.FlagModel;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import sekelsta.horse_colors.client.renderer.GeneticHorseRenderState;
import sekelsta.horse_colors.client.renderer.HorseGeneticModel;

/** Adds tack to RHG's existing model/scale pipeline, without replacing its renderer. */
@OnlyIn(Dist.CLIENT)
public class HorseTackLayer extends RenderLayer<GeneticHorseRenderState, HorseGeneticModel> {
    private final HorseGeneticModel armor;
    private final FlagModel flag;

    public HorseTackLayer(RenderLayerParent<GeneticHorseRenderState, HorseGeneticModel> parent, HorseGeneticModel armor, SpriteGetter sprites) {
        super(parent); this.armor = armor;
        flag = new FlagModel(FlagModel.createBodyLayer().bakeRoot(), sprites);
    }

    @Override public void submit(PoseStack pose, SubmitNodeCollector collector, int light, GeneticHorseRenderState state, float yaw, float pitch) {
        HorseTackRenderData data = state.getRenderData(HorseTackIntegration.DATA);
        if (data == null) return;
        var equipment = collector.order(0);
        for (var visual : data.equipment()) {
            if (visual instanceof HorseTackRenderData.Banner banner) {
                flag.submitBanner(data.motion(), pose, equipment, light, state.outlineColor, banner);
            } else if (visual instanceof HorseTackRenderData.Decoration decoration) {
                var model = TackModels.getModel(decoration.pattern());
                if (model != null) model.submit(data.motion(), pose, equipment, light, state.outlineColor, decoration.color());
            }
        }
        if (data.texture() != null) collector.order(1).submitModel(armor, state, pose, RenderTypes.entityTranslucent(data.texture()),
                light, OverlayTexture.NO_OVERLAY, -1, null, state.outlineColor, null);
        var details = collector.order(2);
        for (var pattern : data.patterns()) {
            var model = TackModels.getModel(pattern.pattern());
            if (model != null) model.submit(data.motion(), pose, details, light, state.outlineColor, pattern.color());
        }
    }
}
