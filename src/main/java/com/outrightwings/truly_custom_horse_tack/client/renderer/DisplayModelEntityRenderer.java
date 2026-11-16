package com.outrightwings.truly_custom_horse_tack.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.outrightwings.truly_custom_horse_tack.Main;
import com.outrightwings.truly_custom_horse_tack.block.SingleInventoryBlock;
import com.outrightwings.truly_custom_horse_tack.block.entity.SingleInventoryBlockEntity;
import com.outrightwings.truly_custom_horse_tack.client.renderer.model.DisplayModel;
import com.outrightwings.truly_custom_horse_tack.client.renderer.model.DisplayTackParts;
import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public abstract class DisplayModelEntityRenderer<T extends SingleInventoryBlockEntity> implements BlockEntityRenderer<T, DisplayRenderState> {
    protected final DisplayModel displayModel;
    protected final Identifier standTexture;
    private static final Identifier SADDLE_TEXTURE = Identifier.fromNamespaceAndPath(Main.MODID, "textures/entity/horse/armor/saddle.png");
    private final Function<ItemStack, Identifier> tackTextures;
    private final DisplayTackParts details;

    protected DisplayModelEntityRenderer(DisplayModel model, Identifier texture, DisplayRenderState.Kind kind, Function<ItemStack, Identifier> tackTextures) {
        this.displayModel = model;
        this.standTexture = texture;
        this.tackTextures = tackTextures;
        this.details = new DisplayTackParts(kind);
    }

    @Override public DisplayRenderState createRenderState() { return new DisplayRenderState(); }

    @Override
    public void extractRenderState(T blockEntity, DisplayRenderState state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
        state.rotation = ((SingleInventoryBlock) blockEntity.getBlockState().getBlock()).getRotation(blockEntity.getBlockState());
        ItemStack tack = blockEntity.getItem(0).copy();
        state.tackTexture = TackItemData.isTack(tack) ? tackTextures.apply(tack) : tack.is(Items.SADDLE) ? SADDLE_TEXTURE : null;
        state.details = DisplayRenderState.extractDetails(tack);
    }

    /** Original quaternion/translation order, retained for every display orientation. */
    public static void transform(PoseStack pose, float degrees) {
        pose.mulPose(new Quaternionf(0, 0, 1, 0));
        pose.translate(-0.5, -1.5, 0.5);
        pose.mulPose(new Quaternionf(0, 1, 0, 0).rotateAxis((float)Math.toRadians(degrees + 180), new Vector3f(0, 1, 0)));
    }

    @Override
    public void submit(DisplayRenderState state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        transform(pose, state.rotation);
        collector.submitModelPart(displayModel.stand, pose, RenderTypes.entityCutout(standTexture), state.lightCoords,
                OverlayTexture.NO_OVERLAY, null, -1, state.breakProgress);
        if (state.tackTexture != null) {
            collector.submitModelPart(displayModel.body, pose, RenderTypes.entityTranslucent(state.tackTexture), state.lightCoords,
                    OverlayTexture.NO_OVERLAY, null, -1, state.breakProgress);
        }
        for (var detail : state.details) {
            for (var surface : details.surfaces(detail.pattern())) {
                collector.submitModelPart(surface.part(), pose, RenderTypes.entityCutout(surface.texture()), state.lightCoords,
                        OverlayTexture.NO_OVERLAY, null, surface.tinted() ? detail.color() : -1, state.breakProgress);
            }
        }
        pose.popPose();
    }
}
