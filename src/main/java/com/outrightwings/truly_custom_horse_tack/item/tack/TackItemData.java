package com.outrightwings.truly_custom_horse_tack.item.tack;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.DyedItemColor;

/** Component boundary; legacy pattern names, order and integer colors stay intact. */
public final class TackItemData {
    private static final ResourceKey<Item> CUSTOM_TACK = ResourceKey.create(
            Registries.ITEM, Identifier.parse("truly_custom_horse_tack:custom_tack"));
    private static final int DEFAULT_COLOR = 0xA06540;

    private TackItemData() {}

    /** Retains custom-tack subclass recognition without a client-helper dependency. */
    public interface TackItem {}

    public static boolean isTack(ItemStack stack) {
        return !stack.isEmpty() && (stack.getItem() instanceof TackItem
                || stack.getItem().builtInRegistryHolder().is(CUSTOM_TACK));
    }

    public static CompoundTag read(ItemStack stack) {
        return stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    public static void write(ItemStack stack, CompoundTag data) {
        CustomData.set(DataComponents.CUSTOM_DATA, stack, data);
    }

    public static ListTag patterns(CompoundTag data) {
        return data != null && data.get("Patterns") instanceof ListTag list ? list : null;
    }

    public static boolean removeLastPattern(ItemStack stack) {
        CompoundTag data = read(stack);
        ListTag list = patterns(data);
        if (list == null || list.isEmpty()) return false;
        list.remove(list.size() - 1);
        write(stack, data);
        return true;
    }

    public static boolean hasColor(ItemStack stack) {
        return stack.has(DataComponents.DYED_COLOR) || legacyColor(stack) != null;
    }

    public static int getColor(ItemStack stack) {
        DyedItemColor color = stack.get(DataComponents.DYED_COLOR);
        if (color != null) return color.rgb(); // No opaque alpha bits: callers expect raw RGB.
        Integer legacy = legacyColor(stack);
        return legacy != null ? legacy : DEFAULT_COLOR;
    }

    private static Integer legacyColor(ItemStack stack) {
        // Some converted mod items retain display.color in CUSTOM_DATA.
        return read(stack).getCompoundOrEmpty("display").getInt("color").orElse(null);
    }

    public static void setColor(ItemStack stack, int color) {
        stack.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        removeLegacyColor(stack);
    }

    public static void clearColor(ItemStack stack) {
        stack.remove(DataComponents.DYED_COLOR);
        removeLegacyColor(stack);
    }

    private static void removeLegacyColor(ItemStack stack) {
        CompoundTag data = read(stack);
        Tag displayTag = data.get("display");
        if (displayTag instanceof CompoundTag display && display.contains("color")) {
            display.remove("color");
            if (display.isEmpty()) data.remove("display");
            write(stack, data);
        }
    }
}
