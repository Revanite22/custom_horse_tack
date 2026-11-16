package com.outrightwings.truly_custom_horse_tack.item;

import com.outrightwings.truly_custom_horse_tack.Main;
import com.outrightwings.truly_custom_horse_tack.block.ModBlocks;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.minecraft.world.item.*;


public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Main.MODID);

    public static final DeferredItem<StatPotion> INCREASE_JUMP_POTION = ITEMS.registerItem("increase_jump_potion", properties -> new StatPotion(properties,true,true));
    public static final DeferredItem<StatPotion> DECREASE_JUMP_POTION = ITEMS.registerItem("decrease_jump_potion", properties -> new StatPotion(properties,false,true));
    public static final DeferredItem<StatPotion> INCREASE_SPEED_POTION = ITEMS.registerItem("increase_speed_potion", properties -> new StatPotion(properties,true,false));
    public static final DeferredItem<StatPotion> DECREASE_SPEED_POTION = ITEMS.registerItem("decrease_speed_potion", properties -> new StatPotion(properties,false,false));
    public static final DeferredItem<HorseStick> HORSE_STICK = ITEMS.registerItem("horse_stick", HorseStick::new);

    public static final DeferredItem<CustomTackItem> CUSTOM_TACK_ITEM = ITEMS.registerItem("custom_tack", CustomTackItem::new);
    public static final DeferredItem<Item> WINTER_TACK_PATTERN = ITEMS.registerItem("winter_tack_pattern", properties -> new TackPatternItem("winter", properties.stacksTo(1)));
    public static final DeferredItem<Item> RACE_TACK_PATTERN = ITEMS.registerItem("race_tack_pattern", properties -> new TackPatternItem("race", properties.stacksTo(1)));
    public static final DeferredItem<Item> IMPOSSIBLE_TACK_PATTERN = ITEMS.registerItem("impossible_tack_pattern", properties -> new TackPatternItem("impossible", properties.stacksTo(1)));
    public static final DeferredItem<Item> CUTE_TACK_PATTERN = ITEMS.registerItem("cute_tack_pattern", properties -> new TackPatternItem("cute", properties.stacksTo(1)));
    public static final DeferredItem<Item> HEAD_TACK_PATTERN = ITEMS.registerItem("head_tack_pattern", properties -> new TackPatternItem("head", properties.stacksTo(1)));
    public static final DeferredItem<Item> REINS_TACK_PATTERN = ITEMS.registerItem("reins_tack_pattern", properties -> new TackPatternItem("reins", properties.stacksTo(1)));
    public static final DeferredItem<Item> BODY_TACK_PATTERN = ITEMS.registerItem("body_tack_pattern", properties -> new TackPatternItem("body", properties.stacksTo(1)));
    public static final DeferredItem<Item> FEET_TACK_PATTERN = ITEMS.registerItem("feet_tack_pattern", properties -> new TackPatternItem("feet", properties.stacksTo(1)));
    public static final DeferredItem<Item> WINGS_TACK_PATTERN = ITEMS.registerItem("wings_tack_pattern", properties -> new TackPatternItem("wings", properties.stacksTo(1)));

    public static final DeferredItem<Item> SADDLE_RACK = ITEMS.registerItem("saddle_rack", properties -> new StandingAndWallBlockItem(ModBlocks.SADDLE_RACK.get(), ModBlocks.SADDLE_RACK_WALL.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<Item> HEAD_STAND = ITEMS.registerItem("head_stand", properties -> new StandingAndWallBlockItem(ModBlocks.HEAD_STAND.get(), ModBlocks.HEAD_STAND_WALL.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));

    public static final DeferredItem<Item> MIXABLE_DYE = ITEMS.registerItem("mixable_dye", properties -> new MixedDye(properties.stacksTo(1)));
    public static final DeferredItem<Item> RIBBON = ITEMS.registerItem("ribbon", properties -> new Ribbon(properties.stacksTo(1)));

    public static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().equals(CreativeModeTabs.FUNCTIONAL_BLOCKS)) {
            event.accept(BuiltInRegistries.ITEM.getValue(ModBlocks.SADDLER.getId()));
            event.accept(SADDLE_RACK);
            event.accept(HEAD_STAND);
        } else if (event.getTabKey().equals(CreativeModeTabs.COMBAT)) {
            event.accept(CUSTOM_TACK_ITEM);
            event.accept(INCREASE_JUMP_POTION);
            event.accept(DECREASE_JUMP_POTION);
            event.accept(INCREASE_SPEED_POTION);
            event.accept(DECREASE_SPEED_POTION);
            event.accept(HORSE_STICK);
            event.accept(RIBBON);
        } else if (event.getTabKey().equals(CreativeModeTabs.INGREDIENTS)) {
            event.accept(WINTER_TACK_PATTERN);
            event.accept(CUTE_TACK_PATTERN);
            event.accept(RACE_TACK_PATTERN);
            event.accept(HEAD_TACK_PATTERN);
            event.accept(BODY_TACK_PATTERN);
            event.accept(FEET_TACK_PATTERN);
            event.accept(REINS_TACK_PATTERN);
            event.accept(WINGS_TACK_PATTERN);
            event.accept(MIXABLE_DYE);
            event.accept(ModBannerPatterns.HORSE_BANNER_PATTERN);
        } else if (event.getTabKey().equals(CreativeModeTabs.COLORED_BLOCKS)) {
            BuiltInRegistries.ITEM.stream().filter(item -> item.getDescriptionId().contains("jump") && !item.getDescriptionId().contains("potion")).forEach(event::accept);
        }

        // RHG registers its own creative entries; do not add them a second time.
    }
}
