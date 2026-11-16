package com.outrightwings.truly_custom_horse_tack.client.renderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.outrightwings.truly_custom_horse_tack.block.ModBlocks;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import java.util.function.Function;

/** Standalone registration boundary; no horse renderer, menu or GPU texture construction. */
@OnlyIn(Dist.CLIENT)
public final class DisplayRenderers {
    private DisplayRenderers() {}

    public static void register(EntityRenderersEvent.RegisterRenderers event, Function<ItemStack, Identifier> tackTextures) {
        event.registerBlockEntityRenderer(ModBlocks.HEAD_STAND_BE.get(), context -> new HeadStandBlockEntityRenderer(context, tackTextures));
        event.registerBlockEntityRenderer(ModBlocks.SADDLE_RACK_BE.get(), context -> new SaddleRackBlockEntityRenderer(context, tackTextures));
        event.registerBlockEntityRenderer(ModBlocks.HEAD_STAND_WALL_BE.get(), context -> new HeadStandWallBlockEntityRenderer(context, tackTextures));
        event.registerBlockEntityRenderer(ModBlocks.SADDLE_RACK_WALL_BE.get(), context -> new SaddleRackWallBlockEntityRenderer(context, tackTextures));
    }
}
