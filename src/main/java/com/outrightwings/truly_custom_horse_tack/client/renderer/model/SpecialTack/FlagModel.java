package com.outrightwings.truly_custom_horse_tack.client.renderer.model.SpecialTack;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.blaze3d.vertex.PoseStack;
import com.outrightwings.truly_custom_horse_tack.client.renderer.HorseTackRenderData;
import com.outrightwings.truly_custom_horse_tack.client.renderer.HorseTackRenderData.Motion;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.OrderedSubmitNodeCollector;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import org.joml.Quaternionf;

@OnlyIn(Dist.CLIENT)
public class FlagModel extends SpecialTackModel {
    private final Model.Simple pole, bar;
    private final Model<Motion> fabric;
    private final SpriteGetter sprites;

    public FlagModel(ModelPart root, SpriteGetter sprites) {
        super(root); this.sprites = sprites;
        pole = new Model.Simple(root.getChild("pole"), RenderTypes::entitySolid);
        bar = new Model.Simple(root.getChild("bar"), RenderTypes::entitySolid);
        fabric = new Model<Motion>(root.getChild("flag"), RenderTypes::entitySolid) {
            @Override public void setupAnim(Motion state) {
                super.setupAnim(state);
                root().xRot = (-0.0125F + 0.01F * Mth.cos(((float)Math.PI * 2F) * state.phase())) * (float)Math.PI;
                root().x = 0; root().y = -42;
            }
        };
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        partdefinition.addOrReplaceChild("flag", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, 0.0F, -0.5F, 20.0F, 40.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -62.0F, -1.5F, 0.0F, 0.0F, 1.5708F));
        partdefinition.addOrReplaceChild("pole", CubeListBuilder.create().texOffs(44, 0).addBox(8.0F, -12.0F, 7.0F, 2.0F, 42.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(8.0F, 8.0F, -8.0F, 0.0F, 0.0F, -3.1416F));
        partdefinition.addOrReplaceChild("bar", CubeListBuilder.create().texOffs(0, 42).addBox(-19.0F, -32.0F, 7.0F, 20.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-32.0F, -23.0F, -8.0F, 0.0F, 0.0F, 1.5708F));
        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override public void submit(Motion state, PoseStack pose, OrderedSubmitNodeCollector collector, int light, int outline, int color) {
        submitBanner(state, pose, collector, light, outline, new HorseTackRenderData.Banner(DyeColor.WHITE, BannerPatternLayers.EMPTY));
    }

    public void submitBanner(Motion state, PoseStack pose, OrderedSubmitNodeCollector collector, int light, int outline, HorseTackRenderData.Banner banner) {
        pose.pushPose();
        pose.mulPose(new Quaternionf(-0.03f, 0.70f, -0.03, 0.70f));
        pose.translate(Mth.lerp(state.rearing(), 0.3f, 0.3f-0.43f), Mth.lerp(state.rearing(), -0.2f, -0.2f-0.56f), -0.35f);
        pose.scale(0.6666667F, 0.6666667F, 0.6666667F);
        collector.submitModel(pole, Unit.INSTANCE, pose, light, OverlayTexture.NO_OVERLAY, -1, Sheets.BANNER_BASE, sprites, outline, null);
        collector.submitModel(bar, Unit.INSTANCE, pose, light, OverlayTexture.NO_OVERLAY, -1, Sheets.BANNER_BASE, sprites, outline, null);
        collector.submitModel(fabric, state, pose, light, OverlayTexture.NO_OVERLAY, -1, Sheets.BANNER_BASE, sprites, outline, null);
        pattern(state, pose, collector, light, outline, Sheets.BANNER_PATTERN_BASE, banner.base());
        // The original banner renderer rendered at most 16 pattern masks.
        for (int i = 0; i < Math.min(16, banner.patterns().layers().size()); i++) {
            var layer = banner.patterns().layers().get(i);
            pattern(state, pose, collector, light, outline, Sheets.getBannerSprite(layer.pattern()), layer.color());
        }
        pose.popPose();
    }

    private void pattern(Motion state, PoseStack pose, OrderedSubmitNodeCollector collector, int light, int outline, SpriteId sprite, DyeColor color) {
        collector.submitModel(fabric, state, pose, sprite.renderType(RenderTypes::bannerPattern), light, OverlayTexture.NO_OVERLAY,
                0xFF000000 | color.getTextureDiffuseColor(), sprites.get(sprite), outline, null);
    }
}
