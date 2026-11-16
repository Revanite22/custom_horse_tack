package com.outrightwings.truly_custom_horse_tack;

import com.outrightwings.truly_custom_horse_tack.block.ModBlocks;
import com.outrightwings.truly_custom_horse_tack.item.ModBannerPatterns;
import com.outrightwings.truly_custom_horse_tack.item.ModItems;
import com.outrightwings.truly_custom_horse_tack.item.recipe.ModRecipes;
import com.outrightwings.truly_custom_horse_tack.screen.ModMenus;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(Main.MODID)
public class Main {
    public static final String MODID = "truly_custom_horse_tack";

    public Main(IEventBus modEventBus, ModContainer modContainer) {
        // Populate all item factories before attaching the register to the bus.
        ModBlocks.registerBlockItems();
        ModBannerPatterns.register();
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ENTITIES.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModRecipes.RECIPES.register(modEventBus);
        modEventBus.addListener(ModItems::addToCreativeTab);
        modEventBus.addListener(Config::onLoad);
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
}
