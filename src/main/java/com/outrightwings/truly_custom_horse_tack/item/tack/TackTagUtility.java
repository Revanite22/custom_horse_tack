package com.outrightwings.truly_custom_horse_tack.item.tack;

import com.outrightwings.truly_custom_horse_tack.client.item.tack.TackModels;
import com.outrightwings.truly_custom_horse_tack.client.renderer.model.SpecialTack.SpecialTackModel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class TackTagUtility {
    public static Tuple<Integer,String> getColorAndPatternByIndex(CompoundTag patternList, int index){
        if(patternList != null){
            ListTag listtag = TackItemData.patterns(patternList);
            if (listtag == null || index < 0 || index >= listtag.size()
                    || !(listtag.get(index) instanceof CompoundTag tag)) return null;
            int colorTag = tag.getIntOr("Color", 0);
            String patternTag = tag.getStringOr("Pattern", "");
            return new Tuple<Integer,String>(colorTag,patternTag);
        }
        return null;
    }
    public static boolean removeLastPattern(CompoundTag patternList){
        if(patternList != null){
            ListTag listtag = TackItemData.patterns(patternList);
            if (listtag == null) return false;
            int length = listtag.size()-1;
            if(length >= 0){
                listtag.remove(length);
                return true;
            }
        }
        return false;
    }
    public static int getPatternListSize(CompoundTag patternList){
        ListTag listtag = null;
        if (patternList != null && TackItemData.patterns(patternList) != null) {
            listtag = TackItemData.patterns(patternList);
        }
        return listtag != null ? listtag.size() : -1;
    }
    public static float[] getColorFromColorTag(int colID){
        if(colID < DyeColor.values().length){
            DyeColor dye = DyeColor.byId(colID);
            int rgb = dye.getTextureDiffuseColor();
            return new float[]{(rgb >> 16 & 255) / 255.0F, (rgb >> 8 & 255) / 255.0F, (rgb & 255) / 255.0F};
        }
        else{
            float[] colors = new float[3];
            colors[0] = (float)(colID >> 16 & 255) / 255.0F;
            colors[1] = (float)(colID >> 8 & 255) / 255.0F;
            colors[2] = (float)(colID & 255) / 255.0F;
            return colors;
        }
    }
    public static ArrayList<Tuple<SpecialTackModel,float[]>> getModels(CompoundTag patternList){
        ArrayList<Tuple<SpecialTackModel,float[]>> list = new ArrayList<>();
        ListTag listtag = null;
        if (patternList != null && TackItemData.patterns(patternList) != null) {
            listtag = TackItemData.patterns(patternList);
        }
        if(listtag != null){
            listtag.forEach(tag -> {
                if (!(tag instanceof CompoundTag)) return;
                SpecialTackModel model;
                float[] color;
                TackPattern pattern = TackPattern.getTackPattern(((CompoundTag)tag).getStringOr("Pattern", ""));
                model = pattern != null ? TackModels.getModel(pattern.name): null;
                color = getColorFromColorTag(((CompoundTag)tag).getIntOr("Color", 0));
                if(model != null){
                    list.add(new Tuple<>(model,color));
                }
            });
        }
        return list;
    }
    public static boolean has(CompoundTag patternList, String check){
        boolean has = false;
        ListTag listtag = null;
        if (patternList != null && TackItemData.patterns(patternList) != null) {
            listtag = TackItemData.patterns(patternList);
        }
        if(listtag != null){
            for (Tag tag : listtag) {
                if (tag instanceof CompoundTag && ((CompoundTag)tag).getStringOr("Pattern", "").contains(check)) {
                    has = true;
                    break;
                }
            }
        }
        return has;
    }
    public static ItemStack mergeTack(ItemStack baseTack, ItemStack mergeTack){
        CompoundTag baseTackTag = TackItemData.read(baseTack);
        CompoundTag outputSlotItemTag = TackItemData.read(mergeTack);
        if(outputSlotItemTag != null){
            ListTag listtag;
            listtag = TackItemData.patterns(outputSlotItemTag);
            ListTag existing = TackItemData.patterns(baseTackTag);
            if (listtag == null) return baseTack;
            if (existing == null) {
                // Never replace malformed/unknown legacy data with an empty list.
                if (baseTackTag.contains("Patterns")) return baseTack;
                existing = new ListTag();
            }
            existing.addAll(listtag.copy());
            baseTackTag.put("Patterns",existing);
            TackItemData.write(baseTack, baseTackTag);
        }
        return baseTack;
    }
}
