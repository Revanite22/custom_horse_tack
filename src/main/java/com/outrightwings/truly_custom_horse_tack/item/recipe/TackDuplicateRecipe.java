package com.outrightwings.truly_custom_horse_tack.item.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.CraftingInput;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.codec.StreamCodec;
import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public class TackDuplicateRecipe extends CustomRecipe {
    public static final TackDuplicateRecipe INSTANCE = new TackDuplicateRecipe();
    public static final RecipeSerializer<TackDuplicateRecipe> SERIALIZER = new RecipeSerializer<>(
            MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    public TackDuplicateRecipe() {}

    Tuple<ItemStack,Integer> findDecorated(CraftingInput container, boolean decorated){
        ItemStack found = null;
        int index = -1;
        for(int i = 0; i < container.size(); i++){
            ItemStack stack = container.getItem(i);
            if(TackItemData.isTack(stack)){
                var data = TackItemData.read(stack);
                // A malformed Patterns field is not blank tack and must not be consumed.
                if (data.contains("Patterns") && TackItemData.patterns(data) == null) continue;
                int listSize = TackItemData.patterns(data) == null ? -1 : TackItemData.patterns(data).size();
                if(decorated && listSize > 0){
                    found = stack;
                    index = i;
                    break;
                }
                else if(!decorated && listSize < 1){
                    found = stack;
                    index = i;
                    break;
                }
            }
        }
        return new Tuple<>(found,index);
    }
    int itemCount(CraftingInput container){
        int count = 0;
        for(int i = 0; i < container.size(); i++){
            ItemStack stack = container.getItem(i);
            if(!stack.isEmpty()) count++;
        }
        return count;
    }
    @Override
    public boolean matches(CraftingInput container, Level level) {
        return matchesInput(container);
    }

    private boolean matchesInput(CraftingInput container) {
        ItemStack tackWithPatterns = findDecorated(container, true).getA();
        ItemStack tackWithoutPatterns = findDecorated(container, false).getA();
        return tackWithPatterns != null && tackWithoutPatterns != null && itemCount(container) == 2;
    }

    @Override
    public ItemStack assemble(CraftingInput container) {
        if (!matchesInput(container)) return ItemStack.EMPTY;
        return findDecorated(container,true).getA().copy();
    }
    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput container){
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(container.size(), ItemStack.EMPTY);
        if (!matchesInput(container)) return nonnulllist;
        var tack = findDecorated(container,true);
        nonnulllist.set(tack.getB(),tack.getA().copy());
        return nonnulllist;
    }

    @Override
    public RecipeSerializer<TackDuplicateRecipe> getSerializer() {
        return SERIALIZER;
    }
}
