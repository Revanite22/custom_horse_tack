package checks;

import com.mojang.datafixers.util.Pair;
import com.outrightwings.truly_custom_horse_tack.entity.*;
import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import io.netty.buffer.Unpooled;
import net.minecraft.core.*;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.*;
import sekelsta.horse_colors.compat.HorseSaveData;
import sekelsta.horse_colors.entity.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class EquipmentFlightTest {
    static RegistryAccess lookup;
    static class Harness {
        final AbstractHorseGenetic horse;
        final ServerLevel level = mock(ServerLevel.class);
        final EntityEquipment gear = new EntityEquipment();
        final List<ItemStack> drops = new ArrayList<>();
        Harness(Class<? extends AbstractHorseGenetic> type) {
            horse = mock(type, invocation -> {
                if (Set.of("canGlide", "updateFallFlying", "causeFallDamage").contains(invocation.getMethod().getName())) return invocation.callRealMethod();
                return RETURNS_DEFAULTS.answer(invocation);
            }); when(horse.level()).thenReturn(level);
            when(horse.getItemBySlot(any())).thenAnswer(call -> gear.get(call.getArgument(0)));
            when(horse.getArmor()).thenAnswer(call -> gear.get(EquipmentSlot.BODY));
            doAnswer(call -> { gear.set(call.getArgument(0), call.getArgument(1)); return null; }).when(horse).setItemSlot(any(), any());
            when(horse.spawnAtLocation(eq(level), any(ItemStack.class))).thenAnswer(call -> { drops.add(call.<ItemStack>getArgument(1).copy()); return null; });
        }
    }
    @BeforeAll static void setup() {
        net.minecraft.SharedConstants.tryDetectVersion(); net.minecraft.server.Bootstrap.bootStrap();
        ItemFixtureMod.bindFixtureComponents();
        for (Item item : List.of(Items.END_ROD, Items.ELYTRA, Items.RED_BANNER, Items.BLUE_BANNER, Items.IRON_AXE, Items.SADDLE)) {
            if (!item.builtInRegistryHolder().areComponentsBound()) item.builtInRegistryHolder()
                    .bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64).build());
        }
        lookup = RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }
    static ItemStack item(String id) { return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("truly_custom_horse_tack:"+id))); }
    static ItemStack tack(String name) {
        var stack = item("custom_tack"); var data = new CompoundTag(); var list = new ListTag(); var layer = new CompoundTag();
        layer.putString("Pattern", name); layer.putInt("Color", 0x123456); layer.putString("future", "keep"); list.add(layer);
        data.put("Patterns", list); data.putString("future_root", "keep"); TackItemData.write(stack, data); return stack;
    }
    static ItemStack glider(int damage) {
        var stack = new ItemStack(Items.ELYTRA); stack.set(DataComponents.GLIDER, Unit.INSTANCE);
        stack.set(DataComponents.MAX_STACK_SIZE, 1); stack.set(DataComponents.MAX_DAMAGE, 432); stack.set(DataComponents.DAMAGE, damage); return stack;
    }
    static void ridden(Harness h) { when(h.horse.getControllingPassenger()).thenReturn(mock(LivingEntity.class)); }

    @Test void accessoriesUseOriginalSlotOrderOnAllThreeGeneticEquinesWithoutTouchingBodyOrSaddle() {
        for (var type : List.of(HorseGeneticEntity.class, DonkeyGeneticEntity.class, MuleGeneticEntity.class)) {
            var h = new Harness(type); var body = tack("reins"); var saddle = new ItemStack(Items.SADDLE);
            h.gear.set(EquipmentSlot.BODY, body); h.gear.set(EquipmentSlot.SADDLE, saddle);
            var banner = new ItemStack(Items.RED_BANNER, 2); var rod = new ItemStack(Items.END_ROD, 2); var ribbon = item("ribbon");
            TackItemData.setColor(ribbon, 0xFEDCBA);
            assertTrue(HorseAccessories.interact(h.horse, banner)); assertTrue(HorseAccessories.interact(h.horse, rod));
            assertTrue(HorseAccessories.interact(h.horse, ribbon));
            assertTrue(h.gear.get(EquipmentSlot.MAINHAND).is(Items.RED_BANNER));
            assertTrue(h.gear.get(EquipmentSlot.OFFHAND).is(Items.END_ROD));
            assertEquals(0xFEDCBA, TackItemData.getColor(h.gear.get(EquipmentSlot.FEET)));
            assertEquals(1, banner.getCount()); assertEquals(1, rod.getCount());
            assertSame(body, h.gear.get(EquipmentSlot.BODY)); assertSame(saddle, h.gear.get(EquipmentSlot.SADDLE));
        }
    }
    @Test void duplicateBannerAndOccupiedSlotsDoNotConsumeOrReplaceItems() {
        var h = new Harness(HorseGeneticEntity.class);
        HorseAccessories.interact(h.horse, new ItemStack(Items.RED_BANNER));
        var blue = new ItemStack(Items.BLUE_BANNER); assertTrue(HorseAccessories.interact(h.horse, blue));
        assertEquals(1, blue.getCount()); assertTrue(h.gear.get(EquipmentSlot.OFFHAND).isEmpty());
        for (var slot : HorseAccessories.DECORATIVE_SLOTS) h.gear.set(slot, new ItemStack(Items.RED_BANNER));
        var ribbon = item("ribbon"); HorseAccessories.interact(h.horse, ribbon); assertEquals(1, ribbon.getCount());
        assertFalse(HorseAccessories.interact(h.horse, tack("reins"))); // Leave normal BODY equipping to RHG.
    }
    @Test void clientsAcknowledgeAccessoryActionsWithoutSplittingOrDropping() {
        var h = new Harness(HorseGeneticEntity.class); when(h.level.isClientSide()).thenReturn(true);
        var rod = new ItemStack(Items.END_ROD, 2); assertTrue(HorseAccessories.interact(h.horse, rod));
        assertEquals(2, rod.getCount()); assertTrue(h.gear.isEmpty());
        h.gear.set(EquipmentSlot.MAINHAND, new ItemStack(Items.END_ROD));
        assertTrue(HorseAccessories.interact(h.horse, new ItemStack(Items.IRON_AXE)));
        assertTrue(h.drops.isEmpty()); assertFalse(h.gear.get(EquipmentSlot.MAINHAND).isEmpty());
    }
    @Test void elytraUsesLegsAndIsNotReplacedWhenOccupied() {
        var h = new Harness(DonkeyGeneticEntity.class); var first = glider(7);
        assertTrue(HorseAccessories.interact(h.horse, first)); assertTrue(first.isEmpty());
        assertEquals(7, h.gear.get(EquipmentSlot.LEGS).getDamageValue());
        var other = glider(9); HorseAccessories.interact(h.horse, other); assertEquals(1, other.getCount());
        assertTrue(h.gear.get(EquipmentSlot.CHEST).isEmpty());
    }
    @Test void axeRemovalDropsOneFromEachAccessoryAndPreservesCustomizationBodyAndSaddle() {
        var h = new Harness(MuleGeneticEntity.class); var body = tack("wings"); var saddle = new ItemStack(Items.SADDLE);
        h.gear.set(EquipmentSlot.BODY, body); h.gear.set(EquipmentSlot.SADDLE, saddle);
        var ribbon = item("ribbon"); TackItemData.setColor(ribbon, 0x123456);
        h.gear.set(EquipmentSlot.HEAD, ribbon); h.gear.set(EquipmentSlot.LEGS, glider(7));
        h.gear.set(EquipmentSlot.MAINHAND, new ItemStack(Items.END_ROD, 3));
        assertTrue(HorseAccessories.interact(h.horse, new ItemStack(Items.IRON_AXE)));
        assertEquals(3, h.drops.size()); assertEquals(2, h.gear.get(EquipmentSlot.MAINHAND).getCount());
        assertEquals(7, h.drops.get(1).getDamageValue()); assertEquals(0x123456, TackItemData.getColor(h.drops.get(2)));
        assertSame(body, h.gear.get(EquipmentSlot.BODY)); assertSame(saddle, h.gear.get(EquipmentSlot.SADDLE));
        HorseAccessories.interact(h.horse, new ItemStack(Items.IRON_AXE)); HorseAccessories.interact(h.horse, new ItemStack(Items.IRON_AXE));
        assertFalse(HorseAccessories.interact(h.horse, new ItemStack(Items.IRON_AXE))); // RHG chest removal can now run.
    }
    @Test void flightRequiresRiderAirborneNotPassengerAndNoLevitation() {
        var h = new Harness(HorseGeneticEntity.class); h.gear.set(EquipmentSlot.BODY, tack("wings_butterfly"));
        assertFalse(HorseFlight.canFly(h.horse)); ridden(h); assertTrue(HorseFlight.canFly(h.horse));
        when(h.horse.onGround()).thenReturn(true); assertFalse(HorseFlight.canFly(h.horse));
        when(h.horse.onGround()).thenReturn(false); when(h.horse.isPassenger()).thenReturn(true); assertFalse(HorseFlight.canFly(h.horse));
        when(h.horse.isPassenger()).thenReturn(false); when(h.horse.hasEffect(MobEffects.LEVITATION)).thenReturn(true); assertFalse(HorseFlight.canFly(h.horse));
    }
    @Test void legElytraHonorsDurabilityAndWingTackRemainsAnIndependentSource() {
        var h = new Harness(DonkeyGeneticEntity.class); ridden(h);
        h.gear.set(EquipmentSlot.LEGS, glider(430)); assertTrue(HorseFlight.canFly(h.horse));
        h.gear.set(EquipmentSlot.LEGS, glider(431)); assertFalse(HorseFlight.canFly(h.horse));
        h.gear.set(EquipmentSlot.BODY, tack("wings_dyed")); assertTrue(HorseFlight.canFly(h.horse));
        assertEquals(431, h.gear.get(EquipmentSlot.LEGS).getDamageValue());
    }
    @Test void fallProtectionRetainsLegacyUnriddenAndBrokenElytraRules() {
        var h = new Harness(MuleGeneticEntity.class); assertFalse(HorseFlight.ignoresFallDamage(h.horse));
        h.gear.set(EquipmentSlot.LEGS, glider(431)); assertTrue(HorseFlight.ignoresFallDamage(h.horse));
        assertFalse(HorseFlight.canFly(h.horse)); h.gear.set(EquipmentSlot.LEGS, ItemStack.EMPTY);
        var body = tack("future_wings_variant"); var before = TackItemData.read(body); h.gear.set(EquipmentSlot.BODY, body);
        assertTrue(HorseFlight.ignoresFallDamage(h.horse)); assertEquals(before, TackItemData.read(body));
    }
    @Test void glideEventsKeepTenTickServerCadenceWithoutDurabilityWear() {
        var h = new Harness(HorseGeneticEntity.class); h.gear.set(EquipmentSlot.LEGS, glider(7));
        when(h.horse.getFallFlyingTicks()).thenReturn(8); HorseFlight.glideEvent(h.horse, true); verify(h.horse, never()).gameEvent(GameEvent.ELYTRA_GLIDE);
        when(h.horse.getFallFlyingTicks()).thenReturn(9); HorseFlight.glideEvent(h.horse, true); verify(h.horse).gameEvent(GameEvent.ELYTRA_GLIDE);
        clearInvocations(h.horse); when(h.level.isClientSide()).thenReturn(true); HorseFlight.glideEvent(h.horse, true);
        verify(h.horse, never()).gameEvent(GameEvent.ELYTRA_GLIDE); assertEquals(7, h.gear.get(EquipmentSlot.LEGS).getDamageValue());
    }
    @Test void riddenBoostRetainsFactorAndNativeMovementAuthority() throws Exception {
        Vec3 riddenInput = new Vec3(0.25, 0, 0.75);
        var entity = mock(LivingEntity.class, invocation -> {
            if (invocation.getMethod().getName().equals("getRiddenInput")) return riddenInput;
            if (invocation.getMethod().getName().equals("getRiddenSpeed")) return 0.4f;
            return RETURNS_DEFAULTS.answer(invocation);
        }); var level = mock(Level.class);
        var field = Entity.class.getDeclaredField("level"); field.setAccessible(true); field.set(entity, level);
        when(entity.isFallFlying()).thenReturn(true); when(entity.getDeltaMovement()).thenReturn(new Vec3(1, -2, 3));
        when(entity.canSimulateMovement()).thenReturn(true);
        var travel = LivingEntity.class.getDeclaredMethod("travelRidden", net.minecraft.world.entity.player.Player.class, Vec3.class);
        travel.setAccessible(true); travel.invoke(entity, mock(net.minecraft.world.entity.player.Player.class), Vec3.ZERO);
        verify(entity).setSpeed(0.4f); verify(entity).travel(riddenInput);
        verify(entity).setDeltaMovement(new Vec3(1, -2, 3).multiply(1.02f, 1.02f, 1.02f));
        clearInvocations(entity); when(entity.isClientAuthoritative()).thenReturn(true); HorseFlight.boostRiddenMovement(entity);
        verify(entity, never()).setDeltaMovement(any(Vec3.class));
    }
    @Test void nativeEquipmentCodecKeepsAllAccessorySlotsAndCustomization() {
        var gear = new EntityEquipment(); gear.set(EquipmentSlot.MAINHAND, new ItemStack(Items.END_ROD));
        gear.set(EquipmentSlot.LEGS, glider(7)); var ribbon = item("ribbon"); TackItemData.setColor(ribbon, 0xFEDCBA);
        gear.set(EquipmentSlot.HEAD, ribbon); var tack = tack("wings"); gear.set(EquipmentSlot.BODY, tack);
        var ops = lookup.createSerializationContext(NbtOps.INSTANCE); var encoded = EntityEquipment.CODEC.encodeStart(ops, gear).getOrThrow();
        var decoded = EntityEquipment.CODEC.parse(ops, encoded).getOrThrow();
        for (var slot : EquipmentSlot.values()) assertTrue(ItemStack.matches(gear.get(slot), decoded.get(slot)), slot.toString());
    }
    static CompoundTag legacyItem(String id, CompoundTag data) {
        var item = new CompoundTag(); item.putString("id", id); item.putByte("Count", (byte)1); item.put("tag", data); return item;
    }
    @Test void rhgMigrationAdapterPreservesLegacyHandArmorAccessoryRecords() {
        var root = new CompoundTag(); root.putString("id", "horse_colors:horse_genetic"); root.putInt("DataVersion", 3465);
        var hands = new ListTag(); hands.add(legacyItem("minecraft:end_rod", new CompoundTag())); hands.add(new CompoundTag()); root.put("HandItems", hands);
        var armor = new ListTag(); armor.add(new CompoundTag()); var dye = new CompoundTag(); dye.putInt("Damage", 7);
        armor.add(legacyItem("minecraft:elytra", dye)); armor.add(new CompoundTag());
        var custom = new CompoundTag(); var display = new CompoundTag(); display.putInt("color", 0x123456); custom.put("display", display); custom.putString("future", "keep");
        armor.add(legacyItem("truly_custom_horse_tack:ribbon", custom)); root.put("ArmorItems", armor);
        var modern = HorseSaveData.modernHorse(root); var encoded = modern.get("equipment"); assertNotNull(encoded);
        var gear = EntityEquipment.CODEC.parse(lookup.createSerializationContext(NbtOps.INSTANCE), encoded).getOrThrow();
        assertTrue(gear.get(EquipmentSlot.MAINHAND).is(Items.END_ROD)); assertEquals(7, gear.get(EquipmentSlot.LEGS).getDamageValue());
        assertEquals(0x123456, TackItemData.getColor(gear.get(EquipmentSlot.HEAD)));
        assertEquals("keep", TackItemData.read(gear.get(EquipmentSlot.HEAD)).getStringOr("future", ""));
    }
    @Test void nativeEquipmentPacketPreservesSlotIdsStackComponentsAndRemoval() {
        var before = tack("wings"); var packet = new ClientboundSetEquipmentPacket(12, List.of(
                Pair.of(EquipmentSlot.BODY, before), Pair.of(EquipmentSlot.LEGS, glider(7)), Pair.of(EquipmentSlot.HEAD, ItemStack.EMPTY)));
        var buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), lookup);
        try {
            ClientboundSetEquipmentPacket.STREAM_CODEC.encode(buffer, packet); var decoded = ClientboundSetEquipmentPacket.STREAM_CODEC.decode(buffer);
            assertEquals(12, decoded.getEntity()); assertTrue(ItemStack.matches(before, decoded.getSlots().get(0).getSecond()));
            assertEquals(EquipmentSlot.LEGS, decoded.getSlots().get(1).getFirst()); assertEquals(7, decoded.getSlots().get(1).getSecond().getDamageValue());
            assertTrue(decoded.getSlots().get(2).getSecond().isEmpty());
        } finally { buffer.release(); }
    }
    @Test void productionMixinsAreActuallyAppliedByTheFixtureBootstrap() throws Exception {
        assertNotNull(AbstractHorseGenetic.class.getDeclaredMethod("canGlide"));
        assertTrue(Arrays.stream(LivingEntity.class.getDeclaredMethods()).anyMatch(method -> method.getName().contains("tcht$boostGliding")));
    }
    @Test void transformedHorseOverrideAllowsLegGliderAndCancelsImmuneFallDamage() throws Exception {
        var h = new Harness(HorseGeneticEntity.class); ridden(h); h.gear.set(EquipmentSlot.LEGS, glider(7));
        var canGlide = AbstractHorseGenetic.class.getDeclaredMethod("canGlide"); canGlide.setAccessible(true);
        assertEquals(true, canGlide.invoke(h.horse));
        h.gear.set(EquipmentSlot.LEGS, glider(431)); assertEquals(false, canGlide.invoke(h.horse));
        assertFalse(h.horse.causeFallDamage(20.0, 1.0f, null)); // Immunity returns before damage-source use.
    }
    @Test void injectedAutomaticStartAndGlideUpdateAreServerAuthoritative() throws Exception {
        var h = new Harness(MuleGeneticEntity.class); ridden(h); h.gear.set(EquipmentSlot.BODY, tack("wings"));
        var start = Arrays.stream(AbstractHorseGenetic.class.getDeclaredMethods()).filter(m -> m.getName().contains("tcht$startGliding")).findFirst().orElseThrow();
        start.setAccessible(true); start.invoke(h.horse, new org.spongepowered.asm.mixin.injection.callback.CallbackInfo("aiStep", false));
        var flags = mockingDetails(h.horse).getInvocations().stream().filter(i -> i.getMethod().getName().equals("setSharedFlag")).toList();
        assertEquals(1, flags.size()); assertEquals(7, (int)flags.getFirst().getArgument(0)); assertEquals(true, flags.getFirst().getArgument(1));
        clearInvocations(h.horse); when(h.horse.getFallFlyingTicks()).thenReturn(9);
        var update = AbstractHorseGenetic.class.getDeclaredMethod("updateFallFlying"); update.setAccessible(true); update.invoke(h.horse);
        verify(h.horse).gameEvent(GameEvent.ELYTRA_GLIDE);
        clearInvocations(h.horse); when(h.level.isClientSide()).thenReturn(true);
        start.invoke(h.horse, new org.spongepowered.asm.mixin.injection.callback.CallbackInfo("aiStep", false)); update.invoke(h.horse);
        assertFalse(mockingDetails(h.horse).getInvocations().stream().anyMatch(i -> i.getMethod().getName().equals("setSharedFlag")));
    }

}
