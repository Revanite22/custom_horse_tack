package checks;

import com.outrightwings.truly_custom_horse_tack.Config;
import com.outrightwings.truly_custom_horse_tack.block.ModBlocks;
import com.outrightwings.truly_custom_horse_tack.item.MixedDye;
import com.outrightwings.truly_custom_horse_tack.item.tack.*;
import com.outrightwings.truly_custom_horse_tack.screen.*;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.item.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class SaddlerMenuTest {
    static final BlockPos POS = new BlockPos(3, 65, 9);
    ServerLevel level;
    ServerPlayer player;
    Inventory inventory;
    SaddlerBlockMenu menu;

    @BeforeEach
    void setup() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        ItemFixtureMod.bindFixtureComponents();
        if (!Items.RED_DYE.builtInRegistryHolder().areComponentsBound()) Items.RED_DYE.builtInRegistryHolder()
                .bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64)
                        .set(DataComponents.DYE, DyeColor.RED).build());
        if (!Items.DIAMOND.builtInRegistryHolder().areComponentsBound()) Items.DIAMOND.builtInRegistryHolder()
                .bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64).build());
        level = mock(ServerLevel.class);
        player = mock(ServerPlayer.class);
        when(player.level()).thenReturn(level);
        when(player.isWithinBlockInteractionRange(POS, 4.0)).thenReturn(true);
        when(level.getBlockState(POS)).thenReturn(ModBlocks.SADDLER.get().defaultBlockState());
        inventory = new Inventory(player, new EntityEquipment()) {
            // Exercise vanilla return logic without a network connection in this unit fixture.
            @Override public void placeItemBackInInventory(ItemStack stack) { super.placeItemBackInInventory(stack, false); }
        };
        when(player.getInventory()).thenReturn(inventory);
        menu = (SaddlerBlockMenu) ModBlocks.SADDLER.get().defaultBlockState()
                .getMenuProvider(level, POS).createMenu(7, inventory, player);
        Config.consume_dye = true;
    }

    @AfterEach void resetConfig() { Config.consume_dye = false; }

    static ItemStack item(String name) {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("truly_custom_horse_tack:" + name)));
    }

    static ItemStack decorated(int count) {
        var stack = item("custom_tack");
        var root = new CompoundTag(); root.putString("future_root", "retain");
        var layers = new ListTag();
        for (int i = 0; i < count; i++) {
            var layer = new CompoundTag();
            layer.putString("Pattern", "future_pattern_" + i);
            layer.putInt("Color", i % 2 == 0 ? 0x123456 : DyeColor.RED.getId());
            layer.putString("future_layer", "retain");
            layers.add(layer);
        }
        root.put("Patterns", layers); TackItemData.write(stack, root);
        return stack;
    }

    void prepare(ItemStack input, ItemStack dye) {
        menu.getSaddleSlot().set(input);
        menu.getDyeSlot().set(dye);
    }

    void select(TackPattern pattern) {
        int index = menu.getSelectablePatterns().indexOf(pattern);
        assertTrue(index >= 0);
        assertTrue(menu.clickMenuButton(player, index));
    }

    ItemStack take() {
        ItemStack result = menu.getResultSlot().remove(1);
        assertFalse(result.isEmpty());
        menu.getResultSlot().onTake(player, result);
        return result;
    }

    @Test void preservesSlotLayoutAndValidation() {
        assertEquals(ModMenus.SADDLER_BLOCK_MENU.get(), menu.getType());
        assertEquals(40, menu.slots.size());
        int[][] positions = {{9, 15}, {9, 33}, {9, 51}, {32, 35}, {8, 84}, {8, 142}};
        int[] indices = {0, 1, 2, 3, 4, 31};
        for (int i = 0; i < indices.length; i++) {
            assertEquals(positions[i][0], menu.getSlot(indices[i]).x);
            assertEquals(positions[i][1], menu.getSlot(indices[i]).y);
        }
        assertTrue(menu.getSaddleSlot().mayPlace(item("custom_tack")));
        assertFalse(menu.getSaddleSlot().mayPlace(new ItemStack(Items.DIAMOND)));
        assertTrue(menu.getDyeSlot().mayPlace(new ItemStack(Items.RED_DYE)));
        assertTrue(menu.getDyeSlot().mayPlace(item("mixable_dye")));
        assertTrue(menu.getPatternSlot().mayPlace(item("head_tack_pattern")));
        assertFalse(menu.getResultSlot().mayPlace(item("custom_tack")));
    }

    @Test void appendsDyeIdAfterAllExistingLayersAndUnknownFieldsWithoutMutatingInput() {
        ItemStack input = decorated(8);
        CompoundTag before = TackItemData.read(input);
        prepare(input, new ItemStack(Items.RED_DYE, 2));
        select(TackPattern.REINS);
        var result = menu.getResultSlot().getItem();
        var data = TackItemData.read(result);
        assertEquals("retain", data.getStringOr("future_root", ""));
        var layers = TackItemData.patterns(data);
        assertEquals(9, layers.size());
        for (int i = 0; i < 8; i++) assertEquals(TackItemData.patterns(before).get(i), layers.get(i));
        var last = (CompoundTag) layers.get(8);
        assertEquals("reins", last.getStringOr("Pattern", ""));
        assertEquals(DyeColor.RED.getId(), last.getIntOr("Color", -1));
        assertEquals(before, TackItemData.read(input));
        assertEquals(2, menu.getDyeSlot().getItem().getCount()); // Selection consumes nothing.
    }

    @Test void mixedDyeRetainsLegacyRgbAndDamagesOnlyWhenResultIsTaken() {
        ItemStack dye = item("mixable_dye");
        var legacy = new CompoundTag(); var display = new CompoundTag();
        display.putInt("color", 0x123456); legacy.put("display", display);
        legacy.putString("future_dye", "retain"); TackItemData.write(dye, legacy);
        dye.setDamageValue(7);
        prepare(decorated(1), dye); select(TackPattern.REINS);
        assertEquals(7, dye.getDamageValue());
        var result = take();
        var layers = TackItemData.patterns(TackItemData.read(result));
        assertEquals(0x123456, ((CompoundTag) layers.get(1)).getIntOr("Color", -1));
        assertEquals(8, menu.getDyeSlot().getItem().getDamageValue());
        assertEquals(legacy, TackItemData.read(menu.getDyeSlot().getItem()));
        assertTrue(menu.getSaddleSlot().getItem().isEmpty());
    }

    @Test void modernRgbAndLastUseOfMixedDyeArePreserved() {
        ItemStack dye = item("mixable_dye");
        ((MixedDye) dye.getItem()).setColor(dye, 0xFEDCBA);
        dye.setDamageValue(15);
        prepare(decorated(0), dye); select(TackPattern.REINS);
        var result = take();
        assertEquals(0xFEDCBA, ((CompoundTag) TackItemData.patterns(TackItemData.read(result)).get(0)).getIntOr("Color", -1));
        assertTrue(menu.getDyeSlot().getItem().isEmpty());
    }

    @Test void vanillaDyeConsumptionAndDisabledConsumptionMatchTheConfig() {
        prepare(decorated(0), new ItemStack(Items.RED_DYE, 2)); select(TackPattern.REINS); take();
        assertEquals(1, menu.getDyeSlot().getItem().getCount());
        Config.consume_dye = false;
        ItemStack dye = item("mixable_dye"); dye.setDamageValue(7);
        prepare(decorated(0), dye); select(TackPattern.REINS); take();
        assertEquals(7, menu.getDyeSlot().getItem().getDamageValue());
    }

    @Test void patternGroupsKeepTheirOrderAndReconcileSelectionWhenPatternItemChanges() {
        assertEquals(98, TackPattern.values().length);
        prepare(decorated(0), new ItemStack(Items.RED_DYE));
        assertEquals(TackPattern.REINS, menu.getSelectablePatterns().getFirst());
        assertFalse(menu.getSelectablePatterns().contains(TackPattern.WINGS));
        select(TackPattern.REINS);
        menu.getPatternSlot().set(item("winter_tack_pattern"));
        assertEquals(TackPattern.REINS, menu.getSelectablePatterns().get(menu.getSelectedSlotIndex()));
        menu.getPatternSlot().set(item("head_tack_pattern"));
        assertEquals(-1, menu.getSelectedSlotIndex());
        assertTrue(menu.getResultSlot().getItem().isEmpty());
        select(TackPattern.BRIDLE);
        menu.getPatternSlot().set(item("impossible_tack_pattern"));
        assertTrue(menu.getSelectablePatterns().contains(TackPattern.WINGS));
        assertTrue(menu.getResultSlot().getItem().isEmpty());
    }

    @Test void invalidButtonsOrInputsNeverProduceAnOutputOrOverwriteMalformedData() {
        prepare(decorated(0), new ItemStack(Items.RED_DYE));
        assertFalse(menu.clickMenuButton(player, -1));
        assertFalse(menu.clickMenuButton(player, Integer.MAX_VALUE));
        select(TackPattern.REINS);
        menu.getDyeSlot().set(ItemStack.EMPTY);
        assertFalse(menu.clickMenuButton(player, 0));
        assertTrue(menu.getResultSlot().getItem().isEmpty());
        var input = decorated(0); var malformed = TackItemData.read(input);
        malformed.putString("Patterns", "unknown format"); TackItemData.write(input, malformed);
        prepare(input, new ItemStack(Items.RED_DYE)); select(TackPattern.REINS);
        assertTrue(menu.getResultSlot().getItem().isEmpty());
        assertEquals(malformed, TackItemData.read(input));
        menu.getSaddleSlot().set(new ItemStack(Items.DIAMOND));
        assertTrue(menu.getSelectablePatterns().isEmpty());
    }

    @Test void quickMoveRoutesAllThreeInputsAndOutputWithoutLosingCustomization() {
        inventory.setItem(9, decorated(1));
        inventory.setItem(10, new ItemStack(Items.RED_DYE, 2));
        inventory.setItem(11, item("winter_tack_pattern"));
        assertFalse(menu.quickMoveStack(player, 4).isEmpty());
        assertFalse(menu.quickMoveStack(player, 5).isEmpty());
        assertFalse(menu.quickMoveStack(player, 6).isEmpty());
        select(TackPattern.REINS);
        var result = menu.quickMoveStack(player, 3);
        assertEquals(2, TackItemData.patterns(TackItemData.read(result)).size());
        assertEquals(2, TackItemData.patterns(TackItemData.read(inventory.getItem(8))).size());
        assertTrue(menu.getSaddleSlot().getItem().isEmpty());
        assertEquals(1, menu.getDyeSlot().getItem().getCount());
        assertFalse(menu.getPatternSlot().getItem().isEmpty());
        assertTrue(menu.quickMoveStack(player, -1).isEmpty());
        assertTrue(menu.quickMoveStack(player, 40).isEmpty());
    }

    @Test void fullInventoryRejectsOutputTransferWithoutConsumingInputs() {
        for (int i = 0; i < 36; i++) inventory.setItem(i, new ItemStack(Items.DIAMOND, 64));
        prepare(decorated(1), new ItemStack(Items.RED_DYE, 2)); select(TackPattern.REINS);
        var expected = menu.getResultSlot().getItem().copy();
        assertTrue(menu.quickMoveStack(player, 3).isEmpty());
        assertTrue(ItemStack.matches(expected, menu.getResultSlot().getItem()));
        assertFalse(menu.getSaddleSlot().getItem().isEmpty());
        assertEquals(2, menu.getDyeSlot().getItem().getCount());
    }

    @Test void clientSelectionDoesNotConsumeInputs() {
        when(level.isClientSide()).thenReturn(true);
        prepare(decorated(1), new ItemStack(Items.RED_DYE, 2)); select(TackPattern.REINS);
        menu.getResultSlot().onTake(player, menu.getResultSlot().getItem().copy());
        assertFalse(menu.getSaddleSlot().getItem().isEmpty());
        assertEquals(2, menu.getDyeSlot().getItem().getCount());
    }

    @Test void workstationContextValidatesDistanceAndReturnsOnlyInputsOnClose() {
        assertTrue(menu.stillValid(player));
        when(player.isWithinBlockInteractionRange(POS, 4.0)).thenReturn(false);
        assertFalse(menu.stillValid(player));
        when(player.isWithinBlockInteractionRange(POS, 4.0)).thenReturn(true);
        var original = decorated(2); var data = TackItemData.read(original);
        prepare(original, new ItemStack(Items.RED_DYE, 2)); select(TackPattern.REINS);
        menu.removed(player);
        assertEquals(data, TackItemData.read(inventory.getItem(0)));
        assertEquals(2, inventory.getItem(1).getCount());
        assertEquals(2, inventory.getNonEquipmentItems().stream().filter(s -> !s.isEmpty()).count());
        assertTrue(menu.getSaddleSlot().getItem().isEmpty());
    }
}
