package com.outrightwings.truly_custom_horse_tack.client.renderer;

import com.google.common.collect.Maps;
import com.outrightwings.truly_custom_horse_tack.item.CustomTackItem;
import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import sekelsta.horse_colors.client.renderer.CustomLayeredTexture;
import sekelsta.horse_colors.client.renderer.TextureLayer;

import java.util.Map;
@OnlyIn(Dist.CLIENT)
public class TextureCache {
    private static final Map<CompoundTag, Identifier> LAYER_CACHE = Maps.newHashMap();

    public static Identifier getTexture(ItemStack armor) {
        if(armor.getItem() instanceof CustomTackItem custom){
            CompoundTag key = TackItemData.read(armor);
            Identifier resourcelocation = LAYER_CACHE.get(key);
            if (resourcelocation == null) {
                TextureLayer l = custom.getTextureLayers(armor);
                resourcelocation = Identifier.parse(l.getUniqueName());
                Minecraft.getInstance().getTextureManager().registerAndLoad(resourcelocation,
                        new CustomLayeredTexture(resourcelocation, custom.getTextureLayers(armor)));
                // CUSTOM_DATA is copied: later customization cannot corrupt the map key.
                LAYER_CACHE.put(key, resourcelocation);
            }
            return resourcelocation;
        }
        return null;
    }
}
