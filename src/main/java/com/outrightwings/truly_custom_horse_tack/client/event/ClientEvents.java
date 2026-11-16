package com.outrightwings.truly_custom_horse_tack.client.event;

import com.outrightwings.truly_custom_horse_tack.Main;
import com.outrightwings.truly_custom_horse_tack.client.renderer.HorseTackIntegration;
import net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent;
import com.outrightwings.truly_custom_horse_tack.client.renderer.DisplayRenderers;
import com.outrightwings.truly_custom_horse_tack.client.renderer.TextureCache;
import com.outrightwings.truly_custom_horse_tack.client.color.TackItemTint;
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
        DisplayRenderers.register(event, TextureCache::getTexture);
    }
    @SubscribeEvent
    public static void registerHorseRenderState(RegisterRenderStateModifiersEvent event) {
        HorseTackIntegration.registerStateModifiers(event);
    }

    @SubscribeEvent
    public static void registerHorseLayers(EntityRenderersEvent.AddLayers event) {
        HorseTackIntegration.registerLayers(event);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.ItemTintSources event) {
        TackItemTint.register(event);
    }
}
