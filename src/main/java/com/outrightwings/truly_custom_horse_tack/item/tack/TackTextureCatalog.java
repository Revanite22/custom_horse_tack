package com.outrightwings.truly_custom_horse_tack.item.tack;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

/** Ordered, immutable texture description; preserves the original texture and color schema. */
public final class TackTextureCatalog {
    private TackTextureCatalog() {}
    public record Layer(Identifier texture, int color) {}
    public static List<Layer> layers(ItemStack stack) {
        List<Layer> result = new ArrayList<>();
        result.add(new Layer(Identifier.parse("truly_custom_horse_tack:textures/entity/horse/armor/custom_tack.png"), -1));
        var patterns = TackItemData.patterns(TackItemData.read(stack));
        if (patterns != null) for (var tag : patterns) {
            if (!(tag instanceof CompoundTag layer)) continue;
            var pattern = TackPattern.getTackPattern(layer.getStringOr("Pattern", ""));
            if (pattern == null) continue;
            int color = layer.getIntOr("Color", 0);
            int rgb = color < DyeColor.values().length ? DyeColor.byId(color).getTextureDiffuseColor() : color;
            result.add(new Layer(pattern.getArmorTextureLocation(), 0xFF000000 | (rgb & 0xFFFFFF)));
            if (pattern.getOverlayTextureLocation() != null) result.add(new Layer(pattern.getOverlayTextureLocation(), -1));
        }
        return List.copyOf(result);
    }
}
