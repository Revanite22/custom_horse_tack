package com.outrightwings.truly_custom_horse_tack.item.recipe;

import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import java.util.ArrayList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.DyeRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** Vanilla dye mixing with the legacy display.color fallback on a working copy. */
public final class TackDyeRecipe extends CustomRecipe {
    public static final RecipeSerializer<TackDyeRecipe> SERIALIZER = new RecipeSerializer<>(
            DyeRecipe.MAP_CODEC.xmap(TackDyeRecipe::new, recipe -> recipe.delegate),
            DyeRecipe.STREAM_CODEC.map(TackDyeRecipe::new, recipe -> recipe.delegate));
    private final DyeRecipe delegate;

    public TackDyeRecipe(DyeRecipe delegate) { this.delegate = delegate; }

    @Override
    public boolean matches(CraftingInput input, Level level) { return delegate.matches(input, level); }

    @Override
    public ItemStack assemble(CraftingInput input) {
        var copies = new ArrayList<ItemStack>(input.size());
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack copy = input.getItem(slot).copy();
            if (!copy.has(DataComponents.DYED_COLOR) && TackItemData.hasColor(copy)) {
                TackItemData.setColor(copy, TackItemData.getColor(copy));
            }
            copies.add(copy);
        }
        return delegate.assemble(CraftingInput.of(input.width(), input.height(), copies));
    }

    @Override
    public RecipeSerializer<TackDyeRecipe> getSerializer() { return SERIALIZER; }
}
