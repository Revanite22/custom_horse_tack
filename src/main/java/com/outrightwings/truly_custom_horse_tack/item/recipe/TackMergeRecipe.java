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

public class TackMergeRecipe extends CustomRecipe {
    public static final TackMergeRecipe INSTANCE = new TackMergeRecipe();
    public static final RecipeSerializer<TackMergeRecipe> SERIALIZER = new RecipeSerializer<>(
            MapCodec.unit(INSTANCE), StreamCodec.unit(INSTANCE));

    public TackMergeRecipe() {}

    Tuple<ItemStack,Integer> findDecorated(CraftingInput container, int prev, boolean decorated){
        ItemStack found = null;
        int index = -1;
        for(int i = 0; i < container.size(); i++){
            ItemStack stack = container.getItem(i);
            if(TackItemData.isTack(stack)){
                var data = TackItemData.read(stack);
                // A malformed Patterns field is not blank tack and must not be consumed.
                if (data.contains("Patterns") && TackItemData.patterns(data) == null) continue;
                int listSize = TackItemData.patterns(data) == null ? -1 : TackItemData.patterns(data).size();
                if(((listSize > 0 && decorated)||(listSize <= 0 && !decorated) )&& prev != i  ){
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
        var tackA = findDecorated(container, -100,true);
        var tackB = findDecorated(container, tackA.getB(),true);
        var tackC = findDecorated(container, -100,false);
        return tackC.getA() != null && tackB.getA() != null && tackA.getA() != null&& itemCount(container) == 3;
    }

    @Override
    public ItemStack assemble(CraftingInput container) {
        if (!matchesInput(container)) return ItemStack.EMPTY;
        var tackA = findDecorated(container, -100,true);
        var tackB = findDecorated(container, tackA.getB(),true);
        ItemStack tackWithPatterns = tackA.getA();
        ItemStack tackWithoutPatterns = tackB.getA();

        ItemStack result = tackWithPatterns.copy();
        var data = TackItemData.read(result);
        var patterns = TackItemData.patterns(data);
        patterns.addAll(TackItemData.patterns(TackItemData.read(tackWithoutPatterns)).copy());
        TackItemData.write(result, data);
        return result;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput container){
        NonNullList<ItemStack> nonnulllist = NonNullList.withSize(container.size(), ItemStack.EMPTY);
        if (!matchesInput(container)) return nonnulllist;
        var tackA = findDecorated(container, -100,true);
        var tackB = findDecorated(container, tackA.getB(),true);
        nonnulllist.set(tackA.getB(), tackA.getA().copy());
        nonnulllist.set(tackB.getB(), tackB.getA().copy());
        return nonnulllist;
    }

    @Override
    public RecipeSerializer<TackMergeRecipe> getSerializer() {
        return SERIALIZER;
    }
}
