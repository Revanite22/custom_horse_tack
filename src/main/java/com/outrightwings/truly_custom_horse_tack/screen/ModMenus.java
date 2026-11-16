package com.outrightwings.truly_custom_horse_tack.screen;

import com.outrightwings.truly_custom_horse_tack.Main;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, Main.MODID);
    public static final DeferredHolder<MenuType<?>, MenuType<SaddlerBlockMenu>> SADDLER_BLOCK_MENU = MENUS.register("saddler_block_menu",()->new MenuType<SaddlerBlockMenu>(SaddlerBlockMenu::new, FeatureFlags.DEFAULT_FLAGS));

}
