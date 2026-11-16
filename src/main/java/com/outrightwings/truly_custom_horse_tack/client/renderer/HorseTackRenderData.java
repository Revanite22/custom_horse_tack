package com.outrightwings.truly_custom_horse_tack.client.renderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import java.util.ArrayList;
import java.util.List;

/** Immutable per-frame values, independent of live equipment and entity mutation. */
@OnlyIn(Dist.CLIENT)
public record HorseTackRenderData(Identifier texture, Motion motion, List<Decoration> patterns,
                                  List<Visual> equipment) {
    public HorseTackRenderData {
        patterns = List.copyOf(patterns); equipment = List.copyOf(equipment);
    }
    public record Motion(float headRotation, float pitch, float limbSwing, float limbSpeed,
                         float eating, float rearing, float wingOpen, boolean flap, float phase) {}
    public sealed interface Visual permits Decoration, Banner {}
    public record Decoration(String pattern, int color) implements Visual {}
    public record Banner(DyeColor base, BannerPatternLayers patterns) implements Visual {
        public Banner { patterns = new BannerPatternLayers(List.copyOf(patterns.layers())); }
    }

    public static int color(int legacyColor) {
        int rgb = legacyColor < DyeColor.values().length ? DyeColor.byId(legacyColor).getTextureDiffuseColor() : legacyColor;
        return 0xFF000000 | (rgb & 0xFFFFFF);
    }

    public static List<Decoration> patterns(ItemStack stack) {
        var layers = TackItemData.patterns(TackItemData.read(stack));
        if (layers == null) return List.of();
        List<Decoration> result = new ArrayList<>();
        for (var tag : layers) if (tag instanceof CompoundTag layer) {
            result.add(new Decoration(layer.getStringOr("Pattern", ""), color(layer.getIntOr("Color", 0))));
        }
        return List.copyOf(result);
    }
}
