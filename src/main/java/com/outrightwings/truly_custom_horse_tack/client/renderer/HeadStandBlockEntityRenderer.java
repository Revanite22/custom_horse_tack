package com.outrightwings.truly_custom_horse_tack.client.renderer;

import com.outrightwings.truly_custom_horse_tack.Main;
import com.outrightwings.truly_custom_horse_tack.block.entity.HeadStandBlockEntity;
import com.outrightwings.truly_custom_horse_tack.client.renderer.model.HeadStandModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class HeadStandBlockEntityRenderer extends DisplayModelEntityRenderer<HeadStandBlockEntity> {
    public HeadStandBlockEntityRenderer(BlockEntityRendererProvider.Context context, Function<ItemStack, Identifier> tackTextures) {
        super(new HeadStandModel(HeadStandModel.createBodyLayer().bakeRoot()),
                Identifier.fromNamespaceAndPath(Main.MODID, "textures/entity/stand.png"), DisplayRenderState.Kind.HEAD_STAND, tackTextures);
    }
}
