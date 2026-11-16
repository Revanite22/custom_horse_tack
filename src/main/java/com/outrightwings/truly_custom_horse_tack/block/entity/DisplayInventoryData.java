package com.outrightwings.truly_custom_horse_tack.block.entity;

import com.mojang.serialization.Dynamic;
import com.mojang.serialization.MapCodec;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;

/** Registry-aware adapter for the displays' existing one-slot Items list. */
public final class DisplayInventoryData {
    public static final MapCodec<CompoundTag> ROOT = MapCodec.assumeMapUnsafe(CompoundTag.CODEC);
    public static final String PRESERVED_ITEMS = "tcht_preserved_items";

    private DisplayInventoryData() {}

    public record Loaded(ItemStack stack, ListTag preserved) {}

    public static Loaded load(CompoundTag input, HolderLookup.Provider lookup) {
        ItemStack stack = ItemStack.EMPTY;
        ListTag preserved = new ListTag();
        Tag archived = input.get(PRESERVED_ITEMS);
        if (archived instanceof ListTag list) preserved.addAll(list.copy());
        else if (archived != null) preserved.add(archived.copy());

        Tag items = input.get("Items");
        if (items == null) return new Loaded(stack, preserved);
        if (!(items instanceof ListTag list)) {
            preserved.add(items.copy());
            return new Loaded(stack, preserved);
        }
        CompoundTag previous = null;
        for (Tag raw : list) {
            if (raw instanceof CompoundTag item && item.getIntOr("Slot", -1) == 0) {
                try {
                    CompoundTag modern = item.copy();
                    if (modern.contains("Count")) {
                        modern = (CompoundTag) DataFixers.getDataFixer().update(References.ITEM_STACK,
                                new Dynamic<>(NbtOps.INSTANCE, modern),
                                modern.getIntOr("DataVersion", input.getIntOr("DataVersion", 3465)),
                                SharedConstants.getCurrentVersion().dataVersion().version()).getValue();
                    }
                    var decoded = ItemStack.CODEC.parse(lookup.createSerializationContext(NbtOps.INSTANCE), modern).result();
                    if (decoded.isPresent()) {
                        // Vanilla's container reader uses the last valid slot record.
                        // Retain earlier duplicate records rather than destroying them.
                        if (previous != null) preserved.add(previous.copy());
                        stack = decoded.get();
                        previous = item;
                        continue;
                    }
                } catch (RuntimeException invalidItem) {
                    // A missing mod or malformed item must not erase the original record.
                }
            }
            preserved.add(raw.copy());
        }
        if (!preserved.isEmpty()) {
            com.mojang.logging.LogUtils.getLogger().warn(
                    "Display inventory has unavailable or unrecognized records; retained in {}", PRESERVED_ITEMS);
        }
        return new Loaded(stack, preserved);
    }

    public static void save(ValueOutput output, NonNullList<ItemStack> stacks, ListTag preserved) {
        ContainerHelper.saveAllItems(output, stacks);
        if (!preserved.isEmpty()) {
            CompoundTag archive = new CompoundTag();
            archive.put(PRESERVED_ITEMS, preserved.copy());
            output.store(ROOT, archive);
        }
    }
}
