package com.outrightwings.truly_custom_horse_tack.item;

import com.outrightwings.truly_custom_horse_tack.Main;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModBannerPatterns {
    // Banner patterns are a data-pack registry in 26.1.2; definitions live in data/.
    public static final ResourceKey<BannerPattern> HORSE_HEAD = pattern("horse_head");
    public static final ResourceKey<BannerPattern> UNICORN = pattern("unicorn");
    public static final ResourceKey<BannerPattern> HORSE_REARING = pattern("horse_rearing");
    public static final ResourceKey<BannerPattern> HORSE_RUNNING = pattern("horse_running");
    public static final ResourceKey<BannerPattern> HORSE_RUNNING_FLIPPED = pattern("horse_running_flipped");
    public static final TagKey<BannerPattern> HORSE_PATTERNS = TagKey.create(
            Registries.BANNER_PATTERN, Identifier.fromNamespaceAndPath(Main.MODID, "pattern_item/horse"));

    public static final DeferredItem<Item> HORSE_BANNER_PATTERN = ModItems.ITEMS.registerItem(
            "horse_banner_pattern", properties -> new Item(properties.stacksTo(1)
                    .delayedComponent(DataComponents.PROVIDES_BANNER_PATTERNS,
                            context -> context.getOrThrow(HORSE_PATTERNS))));

    public static void register() {
        // Force the item factory's class initialization before attaching ITEMS.
    }

    private static ResourceKey<BannerPattern> pattern(String name) {
        return ResourceKey.create(Registries.BANNER_PATTERN, Identifier.fromNamespaceAndPath(Main.MODID, name));
    }
}
