package com.outrightwings.truly_custom_horse_tack.entity;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import sekelsta.horse_colors.entity.AbstractHorseGenetic;
import java.util.List;

/** Original slot order, using native equipment so vanilla/RHG own persistence and tracking. */
public final class HorseAccessories {
    public static final List<EquipmentSlot> DECORATIVE_SLOTS = List.of(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.FEET, EquipmentSlot.HEAD);
    public static final List<EquipmentSlot> REMOVABLE_SLOTS = List.of(EquipmentSlot.MAINHAND, EquipmentSlot.OFFHAND, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.HEAD);
    private static final Identifier RIBBON = Identifier.parse("truly_custom_horse_tack:ribbon");
    private HorseAccessories() {}

    public static boolean interact(AbstractHorseGenetic horse, ItemStack held) {
        if (held.isEmpty()) return false;
        boolean decoration = held.getItem() instanceof BannerItem || held.is(Items.END_ROD)
                || RIBBON.equals(BuiltInRegistries.ITEM.getKey(held.getItem()));
        if (decoration) {
            for (var slot : DECORATIVE_SLOTS) {
                ItemStack equipped = horse.getItemBySlot(slot);
                if (equipped.is(held.getItem())) break;
                if (equipped.isEmpty()) {
                    if (!horse.level().isClientSide()) horse.setItemSlot(slot, held.split(1));
                    break;
                }
                if (equipped.getItem() instanceof BannerItem && held.getItem() instanceof BannerItem) break;
            }
            return true; // Original interaction consumes the action even when all slots are occupied.
        }
        if (held.is(Items.ELYTRA)) {
            if (horse.getItemBySlot(EquipmentSlot.LEGS).isEmpty() && !horse.level().isClientSide())
                horse.setItemSlot(EquipmentSlot.LEGS, held.split(1));
            return true;
        }
        if (held.getItem() instanceof AxeItem) {
            boolean removed = false;
            for (var slot : REMOVABLE_SLOTS) {
                ItemStack equipped = horse.getItemBySlot(slot);
                if (equipped.isEmpty()) continue;
                removed = true;
                if (!horse.level().isClientSide() && horse.level() instanceof ServerLevel level) {
                    ItemStack remainder = equipped.copy();
                    ItemStack drop = remainder.split(1);
                    horse.setItemSlot(slot, remainder);
                    horse.spawnAtLocation(level, drop);
                }
            }
            // When no accessories remain, allow RHG's existing chest-removal interaction.
            return removed;
        }
        return false;
    }
}
