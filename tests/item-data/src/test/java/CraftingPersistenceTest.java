package checks;

import com.outrightwings.truly_custom_horse_tack.block.entity.DisplayInventoryData;
import com.outrightwings.truly_custom_horse_tack.item.recipe.TackDuplicateRecipe;
import com.outrightwings.truly_custom_horse_tack.item.recipe.TackMergeRecipe;
import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import java.nio.file.*;
import java.util.*;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class CraftingPersistenceTest {
    static HolderLookup.Provider lookup;
    static Item tack;

    @BeforeAll
    static void setup() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        tack = BuiltInRegistries.ITEM.getValue(Identifier.parse("truly_custom_horse_tack:custom_tack"));
        assertNotSame(Items.AIR, tack, "Fixture items must actually be registered");
        ItemFixtureMod.bindFixtureComponents();
        if (!Items.SADDLE.builtInRegistryHolder().areComponentsBound()) {
            Items.SADDLE.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
                    .set(DataComponents.MAX_STACK_SIZE, 64).build());
        }
        if (!Items.RED_DYE.builtInRegistryHolder().areComponentsBound()) {
            Items.RED_DYE.builtInRegistryHolder().bindComponents(DataComponentMap.builder()
                    .set(DataComponents.MAX_STACK_SIZE, 64).set(DataComponents.DYE, DyeColor.RED).build());
        }
        lookup = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    static ItemStack decorated(String pattern, int color) {
        ItemStack stack = new ItemStack(tack);
        CompoundTag data = new CompoundTag();
        data.putString("unknown_root", "retain");
        ListTag list = new ListTag();
        CompoundTag layer = new CompoundTag();
        layer.putString("Pattern", pattern);
        layer.putInt("Color", color);
        layer.putString("unknown_field", "retain");
        list.add(layer);
        data.put("Patterns", list);
        TackItemData.write(stack, data);
        return stack;
    }

    static CraftingInput grid(ItemStack... items) {
        List<ItemStack> slots = new ArrayList<>(Collections.nCopies(9, ItemStack.EMPTY));
        for (int i=0;i<items.length;i++) slots.set(i, items[i]);
        return CraftingInput.of(3, 3, slots);
    }

    @Test void duplicationConsumesOnlyBlankAndPreservesSource() {
        ItemStack source = decorated("unknown_pattern", 0x123456);
        ItemStack blank = new ItemStack(tack);
        var input = grid(source, blank);
        var recipe = TackDuplicateRecipe.INSTANCE;
        assertTrue(recipe.matches(input, null));
        ItemStack result = recipe.assemble(input);
        assertEquals(TackItemData.read(source), TackItemData.read(result));
        var remaining = recipe.getRemainingItems(input);
        assertTrue(ItemStack.isSameItemSameComponents(source, remaining.get(0)));
        assertTrue(remaining.get(1).isEmpty());
        assertEquals(1, source.getCount());
        TackItemData.removeLastPattern(result);
        assertEquals(1, TackItemData.patterns(TackItemData.read(source)).size());
        assertFalse(recipe.matches(grid(blank, new ItemStack(tack)), null));
        assertFalse(recipe.matches(grid(source, decorated("other", 14)), null));
        assertFalse(recipe.matches(grid(source, blank, new ItemStack(Items.SADDLE)), null));
    }

    @Test void mergePreservesLayerOrderAndBothOriginals() {
        ItemStack first = decorated("first", 14), second = decorated("unknown_second", 0x112233);
        var input = grid(first, second, new ItemStack(tack));
        var recipe = TackMergeRecipe.INSTANCE;
        assertTrue(recipe.matches(input, null));
        ItemStack result = recipe.assemble(input);
        ListTag layers = TackItemData.patterns(TackItemData.read(result));
        assertEquals(2, layers.size());
        assertEquals(TackItemData.patterns(TackItemData.read(first)).get(0), layers.get(0));
        assertEquals(TackItemData.patterns(TackItemData.read(second)).get(0), layers.get(1));
        var remaining = recipe.getRemainingItems(input);
        assertTrue(ItemStack.isSameItemSameComponents(first, remaining.get(0)));
        assertTrue(ItemStack.isSameItemSameComponents(second, remaining.get(1)));
        assertTrue(remaining.get(2).isEmpty());
        assertEquals(1, TackItemData.patterns(TackItemData.read(first)).size());
        assertFalse(recipe.matches(grid(first, second), null));
        assertFalse(recipe.matches(grid(first, second, new ItemStack(tack), new ItemStack(tack)), null));
    }

    @Test void malformedCustomizationIsNotConsumedAsBlank() {
        ItemStack invalid = new ItemStack(tack);
        CompoundTag data = new CompoundTag(); data.putString("Patterns", "retain malformed record");
        TackItemData.write(invalid, data);
        var input = grid(decorated("saddle", 0), invalid);
        assertFalse(TackDuplicateRecipe.INSTANCE.matches(input, null));
        assertTrue(TackDuplicateRecipe.INSTANCE.assemble(input).isEmpty());
        assertEquals(data, TackItemData.read(invalid));
        assertTrue(TackMergeRecipe.INSTANCE.assemble(input).isEmpty());
    }

    static ItemFixtureMod.Display display() {
        return new ItemFixtureMod.Display(BlockPos.ZERO, Blocks.STONE.defaultBlockState());
    }

    @Test void displaySaveAndUpdateTagsPreserveItemsAndClearRemovedStacks() {
        var source = display();
        ItemStack stored = decorated("unknown_layer", 0x010203);
        source.setItem(0, stored);
        CompoundTag saved = source.saveWithoutMetadata(lookup);
        var restored = display();
        restored.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, lookup, saved));
        assertTrue(ItemStack.isSameItemSameComponents(stored, restored.getItem(0)));
        var client = display();
        client.handleUpdateTag(TagValueInput.create(ProblemReporter.DISCARDING, lookup, source.getUpdateTag(lookup)));
        assertTrue(ItemStack.isSameItemSameComponents(stored, client.getItem(0)));
        assertEquals(1, restored.removeItem(0, 1).getCount());
        assertTrue(restored.isEmpty());
        CompoundTag empty = restored.saveWithoutMetadata(lookup);
        client.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, lookup, empty));
        assertTrue(client.isEmpty(), "An old saved item must not resurrect after removal");
        ItemStack legacyCount = new ItemStack(Items.SADDLE, 3);
        restored.setItem(0, legacyCount);
        assertEquals(1, restored.removeItem(0,1).getCount());
        assertEquals(2, restored.getItem(0).getCount());
        assertEquals(2, restored.removeItemNoUpdate(0).getCount());
        assertTrue(restored.isEmpty());
    }

    @Test void legacyDisplayInventoryConvertsPatternsWithoutDataLoss() {
        CompoundTag root = new CompoundTag();
        ListTag items = new ListTag(); CompoundTag item = new CompoundTag();
        item.putByte("Slot", (byte)0); item.putString("id", "truly_custom_horse_tack:custom_tack");
        item.putByte("Count", (byte)1);
        CompoundTag original = TackItemData.read(decorated("unknown_legacy_layer", 5));
        item.put("tag", original.copy()); items.add(item); root.put("Items", items);
        var loaded = DisplayInventoryData.load(root, lookup);
        assertFalse(loaded.stack().isEmpty());
        assertEquals(original, TackItemData.read(loaded.stack()));
        assertTrue(loaded.preserved().isEmpty());
        var block = display();
        block.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, lookup, root));
        var next = display();
        next.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, lookup, block.saveWithoutMetadata(lookup)));
        assertEquals(original, TackItemData.read(next.getItem(0)));
    }

    @Test void unavailableItemsRemainArchivedWithoutOverridingCurrentEquipment() {
        CompoundTag root = new CompoundTag(); ListTag list = new ListTag(); CompoundTag missing = new CompoundTag();
        missing.putByte("Slot",(byte)0); missing.putString("id", "missing_mod:missing_item");
        missing.putByte("Count",(byte)1); missing.put("tag", TackItemData.read(decorated("unknown", 0x334455)));
        list.add(missing); root.put("Items", list);
        var block = display(); block.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, lookup, root));
        assertTrue(block.isEmpty());
        block.setItem(0, new ItemStack(Items.SADDLE));
        CompoundTag saved = block.saveWithoutMetadata(lookup);
        assertEquals(missing, saved.getListOrEmpty(DisplayInventoryData.PRESERVED_ITEMS).get(0));
        var next = display(); next.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, lookup, saved));
        assertTrue(next.getItem(0).is(Items.SADDLE));
        assertEquals(saved, next.saveWithoutMetadata(lookup));
    }

    @Test void recipeSerializersRoundTripJsonAndNetwork() {
        var ops = lookup.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        for (Recipe<?> recipe : List.of(TackDuplicateRecipe.INSTANCE, TackMergeRecipe.INSTANCE)) {
            var json = Recipe.CODEC.encodeStart(ops, recipe).getOrThrow();
            assertSame(recipe, Recipe.CODEC.parse(ops, json).getOrThrow());
            var buffer = new net.minecraft.network.RegistryFriendlyByteBuf(
                    io.netty.buffer.Unpooled.buffer(), (RegistryAccess)lookup);
            try {
                Recipe.STREAM_CODEC.encode(buffer, recipe);
                assertSame(recipe, Recipe.STREAM_CODEC.decode(buffer));
                assertEquals(0, buffer.readableBytes());
            } finally { buffer.release(); }
        }
    }

    @Test void allRecipeResourcesDecodeWithTargetCodecs() throws Exception {
        // Unit tests do not run a data-pack reload. Supply named holders only for
        // the five recipe tags verified in vanilla/NeoForge's published resources.
        // This validates codec structure, not live tag membership.
        var items = lookup.lookupOrThrow(net.minecraft.core.registries.Registries.ITEM);
        Set<String> knownTags = Set.of("c:dyes", "minecraft:planks", "minecraft:wooden_slabs",
                "minecraft:wool", "minecraft:wool_carpets");
        var recipeItems = new HolderLookup.RegistryLookup.Delegate<Item>() {
            public HolderLookup.RegistryLookup<Item> parent() { return items; }
            public Optional<HolderSet.Named<Item>> get(net.minecraft.tags.TagKey<Item> key) {
                var existing = items.get(key);
                if (existing.isPresent()) return existing;
                return knownTags.contains(key.location().toString())
                        ? Optional.of(HolderSet.emptyNamed(items, key)) : Optional.empty();
            }
        };
        var recipeLookup = HolderLookup.Provider.create(lookup.listRegistries().map(registry ->
                registry.key().equals(net.minecraft.core.registries.Registries.ITEM) ? recipeItems : registry));
        var ops = recipeLookup.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        Path directory = Path.of(System.getProperty("tack.recipeDirectory"));
        try (var paths = Files.list(directory)) {
            List<Path> files = paths.filter(p -> p.toString().endsWith(".json")).sorted().toList();
            assertEquals(35, files.size(), "All 33 original recipes and both formerly implicit dye operations");
            for (Path path : files) {
                var json = com.google.gson.JsonParser.parseString(Files.readString(path));
                assertNotNull(Recipe.CODEC.parse(ops, json).getOrThrow(), path.toString());
            }
        }
    }

    @Test void malformedAndOutOfRangeInventoryRecordsStayRecoverable() {
        CompoundTag root = new CompoundTag();
        ListTag records = new ListTag();
        CompoundTag outOfRange = new CompoundTag();
        outOfRange.putInt("Slot", 7); outOfRange.putString("opaque", "retain me");
        records.add(outOfRange); root.put("Items", records);
        var block = display(); block.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, lookup, root));
        assertTrue(block.isEmpty());
        assertEquals(records, block.saveWithoutMetadata(lookup).getListOrEmpty(DisplayInventoryData.PRESERVED_ITEMS));
        CompoundTag malformed = new CompoundTag(); malformed.putString("Items", "unknown inventory representation");
        block.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, lookup, malformed));
        assertEquals(malformed.get("Items"), block.saveWithoutMetadata(lookup).getListOrEmpty(DisplayInventoryData.PRESERVED_ITEMS).get(0));
    }

    @Test void dyeMixingIncludesLegacyColorWithoutMutatingInputs() throws Exception {
        var ops = lookup.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        for (String name : List.of("mixable_dye", "ribbon")) {
            Path path = Path.of(System.getProperty("tack.recipeDirectory"), name + "_dyed.json");
            var json = com.google.gson.JsonParser.parseString(Files.readString(path)).getAsJsonObject();
            // A direct red-dye ingredient avoids a full server tag reload in this fixture.
            json.addProperty("dye", "minecraft:red_dye");
            CraftingRecipe recipe = (CraftingRecipe)Recipe.CODEC.parse(ops, json).getOrThrow();
            ItemStack source = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("truly_custom_horse_tack:" + name)));
            source.set(DataComponents.MAX_STACK_SIZE, 1);
            source.set(DataComponents.MAX_DAMAGE, 16); source.set(DataComponents.DAMAGE, 7);
            CompoundTag data = new CompoundTag(); CompoundTag display = new CompoundTag();
            display.putInt("color", 0x112233); display.putString("unknown_display", "retain");
            data.put("display", display); data.putString("unknown_root", "retain");
            TackItemData.write(source, data);
            var input = grid(source, new ItemStack(Items.RED_DYE));
            assertTrue(recipe.matches(input, null));
            ItemStack result = recipe.assemble(input);
            int expected = net.minecraft.world.item.component.DyedItemColor.applyDyes(
                    new net.minecraft.world.item.component.DyedItemColor(0x112233), List.of(DyeColor.RED)).rgb();
            assertEquals(expected, TackItemData.getColor(result));
            assertEquals(data, TackItemData.read(source), "Recipe must not mutate its source");
            assertEquals(7, result.getDamageValue());
            assertEquals("retain", TackItemData.read(result).getCompoundOrEmpty("display").getStringOr("unknown_display", ""));
            assertFalse(TackItemData.read(result).getCompoundOrEmpty("display").contains("color"));
            assertTrue(recipe.getRemainingItems(input).stream().allMatch(ItemStack::isEmpty));
            var encoded = Recipe.CODEC.encodeStart(ops, recipe).getOrThrow();
            assertNotNull(Recipe.CODEC.parse(ops, encoded).getOrThrow());
        }
    }

    @Test void displayDropsCarryOnlyUnresolvedRecordsAcrossPlacement() {
        CompoundTag root = new CompoundTag(); ListTag list = new ListTag(); CompoundTag missing = new CompoundTag();
        missing.putByte("Slot", (byte)0); missing.putString("id", "missing_mod:missing_item");
        missing.putByte("Count", (byte)1); missing.put("tag", TackItemData.read(decorated("unknown", 0x334455)));
        list.add(missing); root.put("Items", list);
        var block = display(); block.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, lookup, root));
        block.setItem(0, decorated("active_item_drops_separately", 14));
        CompoundTag carried = block.collectComponents().get(DataComponents.CUSTOM_DATA).copyTag();
        assertFalse(carried.contains("Items"), "Usable contents must not be duplicated into the display drop");
        assertEquals(list, carried.getListOrEmpty(DisplayInventoryData.PRESERVED_ITEMS));
        carried.putString("foreign_item_data", "retain");
        ItemStack displayItem = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("truly_custom_horse_tack:saddle_rack")));
        displayItem.set(DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(carried));
        var placed = display(); placed.applyComponentsFromItemStack(displayItem);
        assertTrue(placed.isEmpty());
        CompoundTag saved = placed.saveWithoutMetadata(lookup);
        assertEquals(list, saved.getListOrEmpty(DisplayInventoryData.PRESERVED_ITEMS));
        var reloaded = display(); reloaded.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, lookup, saved));
        assertEquals(carried, reloaded.collectComponents().get(DataComponents.CUSTOM_DATA).copyTag());
    }

    @Test void displayLootTablesDecodeWithoutChangingDropIdentity() throws Exception {
        var ops = lookup.createSerializationContext(com.mojang.serialization.JsonOps.INSTANCE);
        Path directory = Path.of(System.getProperty("tack.displayLootDirectory"));
        for (String name : List.of("saddle_rack", "saddle_rack_wall", "head_stand", "head_stand_wall")) {
            var json = com.google.gson.JsonParser.parseString(Files.readString(directory.resolve(name + ".json")));
            assertNotNull(net.minecraft.world.level.storage.loot.LootTable.DIRECT_CODEC.parse(ops, json).getOrThrow());
            String drop = json.getAsJsonObject().getAsJsonArray("pools").get(0).getAsJsonObject()
                    .getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString();
            assertEquals("truly_custom_horse_tack:" + name.replace("_wall", ""), drop);
        }
    }
}
