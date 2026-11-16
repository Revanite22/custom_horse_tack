package com.outrightwings.truly_custom_horse_tack.item.recipe;

import com.outrightwings.truly_custom_horse_tack.Main;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, Main.MODID);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TackDuplicateRecipe>> DUPLICATE_TACK =
            RECIPES.register("duplicate_tack", () -> TackDuplicateRecipe.SERIALIZER);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TackMergeRecipe>> MERGE_TACK =
            RECIPES.register("merge_tack", () -> TackMergeRecipe.SERIALIZER);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TackDyeRecipe>> DYE_ITEM =
            RECIPES.register("dye_item", () -> TackDyeRecipe.SERIALIZER);

}
