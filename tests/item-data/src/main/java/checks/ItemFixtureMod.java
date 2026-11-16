package checks;

import com.outrightwings.truly_custom_horse_tack.block.entity.SingleInventoryBlockEntity;
import com.outrightwings.truly_custom_horse_tack.block.ModBlocks;
import com.outrightwings.truly_custom_horse_tack.screen.ModMenus;
import com.outrightwings.truly_custom_horse_tack.item.MixedDye;
import com.outrightwings.truly_custom_horse_tack.item.TackPatternItem;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import com.outrightwings.truly_custom_horse_tack.item.recipe.TackDuplicateRecipe;
import com.outrightwings.truly_custom_horse_tack.item.recipe.TackMergeRecipe;
import com.outrightwings.truly_custom_horse_tack.item.recipe.TackDyeRecipe;
import net.neoforged.fml.common.Mod;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

@Mod("tack_item_fixture")
public class ItemFixtureMod {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems("truly_custom_horse_tack");
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(Registries.RECIPE_SERIALIZER, "truly_custom_horse_tack");
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, "tack_item_fixture");
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<Display>> DISPLAY =
            BLOCK_ENTITIES.register("display", () -> new BlockEntityType<>(Display::new, Blocks.STONE));

    static {
        for (String name : new String[]{
            "body_tack_pattern",
            "custom_tack",
            "cute_tack_pattern",
            "feet_tack_pattern",
            "head_stand",
            "head_tack_pattern",
            "horse_banner_pattern",
            "impossible_tack_pattern",
            "jump_black",
            "jump_blue",
            "jump_brown",
            "jump_cyan",
            "jump_gray",
            "jump_green",
            "jump_light_blue",
            "jump_light_gray",
            "jump_lime",
            "jump_magenta",
            "jump_orange",
            "jump_pink",
            "jump_purple",
            "jump_red",
            "jump_white",
            "jump_yellow",
            "mixable_dye",
            "race_tack_pattern",
            "reins_tack_pattern",
            "ribbon",
            "saddle_rack",
            "saddler",
            "wings_tack_pattern",
            "winter_tack_pattern"
        }) {
            if (name.equals("mixable_dye")) ITEMS.registerItem(name, MixedDye::new);
            else if (name.endsWith("_tack_pattern")) ITEMS.registerItem(name,
                    properties -> new TackPatternItem(name.replace("_tack_pattern", ""), properties.stacksTo(1)));
            else ITEMS.registerSimpleItem(name);
        }
        RECIPES.register("duplicate_tack", () -> TackDuplicateRecipe.SERIALIZER);
        RECIPES.register("merge_tack", () -> TackMergeRecipe.SERIALIZER);
        RECIPES.register("dye_item", () -> TackDyeRecipe.SERIALIZER);
    }

    public ItemFixtureMod(IEventBus bus) {
        ModMenus.MENUS.register(bus);
        ModBlocks.BLOCKS.register(bus);
        ModBlocks.BLOCK_ENTITIES.register(bus);
        ITEMS.register(bus);
        RECIPES.register(bus);
        BLOCK_ENTITIES.register(bus);
    }

    /** Bind fixture defaults once; preserve actual mixed-dye durability and pattern stack limits. */
    public static void bindFixtureComponents() {
        for (var holder : ITEMS.getEntries()) {
            var ref = holder.get().builtInRegistryHolder();
            if (ref.areComponentsBound()) continue;
            var components = DataComponentMap.builder();
            if (holder.get() instanceof MixedDye) {
                components.set(DataComponents.MAX_STACK_SIZE, 1)
                        .set(DataComponents.MAX_DAMAGE, 16).set(DataComponents.DAMAGE, 0);
            } else if (holder.get() instanceof TackPatternItem) {
                components.set(DataComponents.MAX_STACK_SIZE, 1);
            } else components.set(DataComponents.MAX_STACK_SIZE, 64);
            ref.bindComponents(components.build());
        }
    }

    public static class Display extends SingleInventoryBlockEntity {
        public Display(BlockPos pos, BlockState state) { super(DISPLAY.get(), pos, state); }
    }
}
