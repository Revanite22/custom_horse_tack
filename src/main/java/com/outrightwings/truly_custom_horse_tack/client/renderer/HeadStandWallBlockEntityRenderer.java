package com.outrightwings.truly_custom_horse_tack.client.renderer;

import com.outrightwings.truly_custom_horse_tack.Main;
import com.outrightwings.truly_custom_horse_tack.block.entity.HeadStandWallBlockEntity;
import com.outrightwings.truly_custom_horse_tack.client.renderer.model.HeadStandWallModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class HeadStandWallBlockEntityRenderer extends DisplayModelEntityRenderer<HeadStandWallBlockEntity> {
    public HeadStandWallBlockEntityRenderer(BlockEntityRendererProvider.Context context, Function<ItemStack, Identifier> tackTextures) {
        super(new HeadStandWallModel(HeadStandWallModel.createBodyLayer().bakeRoot()),
                Identifier.fromNamespaceAndPath(Main.MODID, "textures/entity/stand.png"), DisplayRenderState.Kind.HEAD_STAND_WALL, tackTextures);
    }
}
