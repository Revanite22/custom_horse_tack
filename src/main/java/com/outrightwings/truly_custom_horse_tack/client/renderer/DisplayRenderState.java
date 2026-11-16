package com.outrightwings.truly_custom_horse_tack.client.renderer;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import java.util.ArrayList;
import java.util.List;

/** Extracted values only: submission never reads a live inventory or NBT object. */
@OnlyIn(Dist.CLIENT)
public class DisplayRenderState extends BlockEntityRenderState {
    public enum Kind {
        HEAD_STAND(true, false), HEAD_STAND_WALL(true, true), RACK(false, false), RACK_WALL(false, true);
        public final boolean stand;
        public final boolean wall;
        Kind(boolean stand, boolean wall) { this.stand = stand; this.wall = wall; }
    }

    public record Detail(String pattern, int color) {}
    public float rotation;
    public Identifier tackTexture;
    public List<Detail> details = List.of();

    public static List<Detail> extractDetails(ItemStack stack) {
        var layers = TackItemData.patterns(TackItemData.read(stack));
        if (layers == null) return List.of();
        List<Detail> result = new ArrayList<>();
        for (var layer : layers) {
            if (!(layer instanceof CompoundTag tag)) continue;
            int color = tag.getIntOr("Color", 0);
            int rgb = color < DyeColor.values().length ? DyeColor.byId(color).getTextureDiffuseColor() : color;
            result.add(new Detail(tag.getStringOr("Pattern", ""), 0xFF000000 | (rgb & 0xFFFFFF)));
        }
        return List.copyOf(result);
    }
}
