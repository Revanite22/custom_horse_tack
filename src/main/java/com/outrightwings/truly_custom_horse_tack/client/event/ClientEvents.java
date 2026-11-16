package com.outrightwings.truly_custom_horse_tack.client.event;

import com.outrightwings.truly_custom_horse_tack.Main;
import com.outrightwings.truly_custom_horse_tack.block.ModBlocks;
import com.outrightwings.truly_custom_horse_tack.client.renderer.HeadStandBlockEntityRenderer;
import com.outrightwings.truly_custom_horse_tack.client.renderer.HeadStandWallBlockEntityRenderer;
import com.outrightwings.truly_custom_horse_tack.client.renderer.SaddleRackBlockEntityRenderer;
import com.outrightwings.truly_custom_horse_tack.client.renderer.SaddleRackWallBlockEntityRenderer;
import com.outrightwings.truly_custom_horse_tack.item.ModItems;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.world.item.DyeableLeatherItem;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import com.outrightwings.truly_custom_horse_tack.screen.ModMenus;
import com.outrightwings.truly_custom_horse_tack.screen.SaddlerBlockScreen;

@EventBusSubscriber(modid = Main.MODID, value = Dist.CLIENT)
public class ClientEvents {
    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.SADDLER_BLOCK_MENU.get(), SaddlerBlockScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(ModBlocks.HEAD_STAND_BE.get(),HeadStandBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.SADDLE_RACK_BE.get(), SaddleRackBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.HEAD_STAND_WALL_BE.get(), HeadStandWallBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlocks.SADDLE_RACK_WALL_BE.get(), SaddleRackWallBlockEntityRenderer::new);
    }
    @SubscribeEvent
    // Legacy item tint callback is retained for the rendering migration.
    public static void registerItemColors(RegisterColorHandlersEvent.Item event){
        ItemColors itemColors = event.getItemColors();

        event.register((stack, tintIndex) -> tintIndex > 0 ? -1 : ((DyeableLeatherItem) stack.getItem()).getColor(stack),
                ModItems.MIXABLE_DYE.get(),
                ModItems.RIBBON.get()
        );
    }
}
