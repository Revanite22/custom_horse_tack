package com.outrightwings.truly_custom_horse_tack.client.renderer;

import com.outrightwings.truly_custom_horse_tack.Main;
import com.outrightwings.truly_custom_horse_tack.block.entity.SaddleRackBlockEntity;
import com.outrightwings.truly_custom_horse_tack.client.renderer.model.RackModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import java.util.function.Function;

@OnlyIn(Dist.CLIENT)
public class SaddleRackBlockEntityRenderer extends DisplayModelEntityRenderer<SaddleRackBlockEntity> {
    public SaddleRackBlockEntityRenderer(BlockEntityRendererProvider.Context context, Function<ItemStack, Identifier> tackTextures) {
        super(new RackModel(RackModel.createBodyLayer().bakeRoot()),
                Identifier.fromNamespaceAndPath(Main.MODID, "textures/entity/rack.png"), DisplayRenderState.Kind.RACK, tackTextures);
    }
}
