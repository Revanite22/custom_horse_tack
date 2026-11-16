package checks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.outrightwings.truly_custom_horse_tack.client.item.tack.TackModels;
import com.outrightwings.truly_custom_horse_tack.client.renderer.*;
import com.outrightwings.truly_custom_horse_tack.client.renderer.model.SpecialTack.*;
import com.outrightwings.truly_custom_horse_tack.client.util.HorseModelRotations;
import com.outrightwings.truly_custom_horse_tack.item.CustomTackItem;
import com.outrightwings.truly_custom_horse_tack.item.tack.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.junit.jupiter.api.*;
import sekelsta.horse_colors.client.renderer.*;
import sekelsta.horse_colors.entity.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class HorseRenderingTest {
    static final Identifier TEXTURE = Identifier.parse("truly_custom_horse_tack:fixture_tack");
    static final Identifier ORIGINAL = Identifier.parse("horse_colors:fixture_original");
    static final HorseTackRenderData.Motion STILL = motion(0, 0, 0, false);

    @BeforeAll static void setup() {
        net.minecraft.SharedConstants.tryDetectVersion(); net.minecraft.server.Bootstrap.bootStrap();
        ItemFixtureMod.bindFixtureComponents();
        for (Item item : List.of(Items.END_ROD, Items.ELYTRA, Items.RED_BANNER)) {
            if (!item.builtInRegistryHolder().areComponentsBound()) item.builtInRegistryHolder()
                    .bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 1).build());
        }
    }

    static HorseTackRenderData.Motion motion(float eating, float rearing, float open, boolean flap) {
        return new HorseTackRenderData.Motion(45, 0, 0, 0, eating, rearing, open, flap, 0.25f);
    }
    static ItemStack tack() {
        return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("truly_custom_horse_tack:custom_tack")));
    }
    static ItemStack layers(String... names) {
        ItemStack stack = tack(); CompoundTag root = new CompoundTag(); ListTag patterns = new ListTag();
        for (int i=0; i<names.length; i++) {
            CompoundTag layer = new CompoundTag(); layer.putString("Pattern", names[i]);
            layer.putInt("Color", i == 0 ? DyeColor.RED.getId() : 0x123456); layer.putString("future", "keep"); patterns.add(layer);
        }
        root.put("Patterns", patterns); root.putString("future_root", "keep"); TackItemData.write(stack, root); return stack;
    }
    static AbstractHorseGenetic horse(Class<? extends AbstractHorseGenetic> type) {
        var horse = mock(type); Level level = mock(Level.class); when(horse.level()).thenReturn(level);
        when(level.getGameTime()).thenReturn(125L); when(horse.getItemBySlot(any())).thenReturn(ItemStack.EMPTY);
        when(horse.getFallFlyingTicks()).thenReturn(20); when(horse.isFallFlying()).thenReturn(true); return horse;
    }
    static GeneticHorseRenderState state(ItemStack body) {
        var state = new GeneticHorseRenderState(); state.bodyArmorItem = body;
        state.armorTexture = ORIGINAL; state.saddleTexture = ORIGINAL; state.armorTint = 123; state.saddleTint = 456;
        state.legacyHeadRotation = 45; state.legacyPitch = 10; state.partialTick = 0.5f;
        return state;
    }

    @Test void orderedTextureCatalogPreservesBaseOverlayDyesRgbAndUnknownData() {
        var stack = layers("bridle", "unknown_future_pattern", "reins"); var before = TackItemData.read(stack);
        var textures = TackTextureCatalog.layers(stack);
        assertEquals(4, textures.size());
        assertTrue(textures.get(0).texture().getPath().endsWith("custom_tack.png")); assertEquals(-1, textures.get(0).color());
        assertTrue(textures.get(1).texture().getPath().endsWith("patterns/bridle.png"));
        assertEquals(HorseTackRenderData.color(DyeColor.RED.getId()), textures.get(1).color());
        assertTrue(textures.get(2).texture().getPath().endsWith("bridle_overlay.png")); assertEquals(-1, textures.get(2).color());
        assertTrue(textures.get(3).texture().getPath().endsWith("patterns/reins.png")); assertEquals(0xFF123456, textures.get(3).color());
        assertEquals(before, TackItemData.read(stack)); assertThrows(UnsupportedOperationException.class, () -> textures.clear());
        assertEquals(98, TackPattern.values().length);
    }

    @Test void rhgLayerGroupRetainsTheCatalogOrderAndExactColorChannels() {
        var stack = layers("bridle", "reins"); var catalog = TackTextureCatalog.layers(stack);
        // Only the texture-building method is called; no constructor, equipment mutation or GPU upload.
        var custom = mock(CustomTackItem.class, CALLS_REAL_METHODS);
        var composed = custom.getTextureLayers(stack);
        assertEquals(catalog.size(), composed.layers.size());
        for (int i=0; i<catalog.size(); i++) {
            var expected = catalog.get(i); var actual = composed.layers.get(i);
            assertEquals(expected.texture().toString(), actual.name);
            assertEquals((expected.color() >> 16 & 255) / 255.0f, actual.color.r);
            assertEquals((expected.color() >> 8 & 255) / 255.0f, actual.color.g);
            assertEquals((expected.color() & 255) / 255.0f, actual.color.b);
            assertEquals(1, actual.color.a);
        }
    }

    @Test void customBodyTackUsesTheSameSnapshotPathForHorseDonkeyAndMule() {
        for (var type : List.of(HorseGeneticEntity.class, DonkeyGeneticEntity.class, MuleGeneticEntity.class)) {
            var horse = horse(type); var body = layers("horn_color", "wings_butterfly"); var before = TackItemData.read(body);
            var state = state(body);
            HorseTackIntegration.extract(horse, state, stack -> TEXTURE);
            var data = state.getRenderData(HorseTackIntegration.DATA);
            assertEquals(TEXTURE, data.texture()); assertNull(state.armorTexture); assertNull(state.saddleTexture);
            assertEquals(2, data.patterns().size()); assertEquals(0xFF123456, data.patterns().get(1).color());
            assertEquals(1, data.motion().wingOpen()); assertTrue(data.motion().flap());
            assertEquals(0.255f, data.motion().phase());
            assertEquals(before, TackItemData.read(body));
            verify(horse, never()).setItemSlot(any(), any());
            TackItemData.write(body, new CompoundTag()); assertEquals(2, data.patterns().size());
        }
    }

    @Test void ordinaryEquipmentKeepsRhgTexturesAndReusedStatesLoseOldTack() {
        var horse = horse(HorseGeneticEntity.class); var state = state(layers("horn"));
        HorseTackIntegration.extract(horse, state, stack -> TEXTURE);
        state.bodyArmorItem = ItemStack.EMPTY; state.armorTexture = ORIGINAL; state.saddleTexture = ORIGINAL;
        HorseTackIntegration.extract(horse, state, stack -> { fail("No tack texture should be requested"); return null; });
        var data = state.getRenderData(HorseTackIntegration.DATA);
        assertNull(data.texture()); assertTrue(data.patterns().isEmpty()); assertTrue(data.equipment().isEmpty());
        assertEquals(ORIGINAL, state.armorTexture); assertEquals(ORIGINAL, state.saddleTexture);
        assertEquals(123, state.armorTint); assertEquals(456, state.saddleTint);
    }

    @Test void equippedVisualSnapshotsKeepOrderColorAndBannerComponentsWithoutStorageChanges() {
        var horse = horse(DonkeyGeneticEntity.class);
        when(horse.getItemBySlot(EquipmentSlot.MAINHAND)).thenReturn(new ItemStack(Items.RED_BANNER));
        when(horse.getItemBySlot(EquipmentSlot.OFFHAND)).thenReturn(new ItemStack(Items.END_ROD));
        var ribbon = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("truly_custom_horse_tack:ribbon")));
        TackItemData.setColor(ribbon, 0xFEDCBA);
        when(horse.getItemBySlot(EquipmentSlot.HEAD)).thenReturn(ribbon);
        var state = state(ItemStack.EMPTY); HorseTackIntegration.extract(horse, state, stack -> TEXTURE);
        var visuals = state.getRenderData(HorseTackIntegration.DATA).equipment();
        assertEquals(3, visuals.size()); assertInstanceOf(HorseTackRenderData.Banner.class, visuals.get(0));
        assertEquals(DyeColor.RED, ((HorseTackRenderData.Banner)visuals.get(0)).base());
        assertEquals("horn", ((HorseTackRenderData.Decoration)visuals.get(1)).pattern());
        assertEquals(0xFFFEDCBA, ((HorseTackRenderData.Decoration)visuals.get(2)).color());
        verify(horse, never()).setItemSlot(any(), any());
    }

    @Test void headRibbonAndBellsRetainRestEatingAndRearingTransformsWithoutPoseLeakage() {
        var horn = new HornModel(HornModel.createBodyLayer().bakeRoot(), HornModel.HORN_TYPE.DYED);
        horn.setupAnim(STILL); var part = horn.root().getChild("horn");
        assertEquals(4, part.y); assertEquals(-10, part.z); assertEquals(20 * 0.017453292F, part.yRot);
        horn.setupAnim(motion(1, 0, 0, false)); assertEquals(11, part.y); assertEquals(-10, part.z);
        assertEquals(2.1816616F, part.xRot);
        horn.setupAnim(motion(0, 1, 0, false)); assertEquals(-6, part.y); assertEquals(-1, part.z);
        assertEquals(0.2617994F, part.xRot);
        horn.setupAnim(STILL); assertEquals(4, part.y); assertEquals(-10, part.z);
        var ribbon = new RibbonModel(RibbonModel.createBodyLayerLeft().bakeRoot()); ribbon.setupAnim(motion(1, 0, 0, false));
        var root = ribbon.root().getChild("ribbon"); assertEquals(-root.xRot, root.getChild("flower").xRot);
        assertEquals(-root.xRot, root.getChild("longs").xRot);
        var bells = new BellsRearModel(BellsRearModel.createBodyLayer().bakeRoot()); bells.setupAnim(motion(0, 1, 0, false));
        assertEquals(-0.7853982F, bells.root().getChild("bells").xRot);
        assertEquals(0.7853982F, bells.root().getChild("bells").getChild("FW2").xRot);
        assertEquals(180, HorseModelRotations.updateHorseRotation(170, -170, 0.5f));
    }

    @Test void wingsRetainFoldedAndOpenAnglesAndAQueuedStateCanRestoreItsPose() {
        var model = new WingsModel(WingsModel.createBodyLayer().bakeRoot(), WingsModel.WING_TYPE.BUTTERFLY);
        var left = model.root().getChild("wings").getChild("left_wing");
        var right = model.root().getChild("wings").getChild("right_wing");
        model.setupAnim(STILL); assertEquals(Mth.DEG_TO_RAD * 90, left.xRot); assertEquals(Mth.DEG_TO_RAD * 332.5f, left.yRot);
        model.setupAnim(motion(0, 0, 1, false)); assertEquals(Mth.DEG_TO_RAD * 126, left.xRot);
        assertEquals(left.yRot, -right.yRot); assertEquals(left.zRot, -right.zRot);
        model.setupAnim(STILL); assertEquals(Mth.DEG_TO_RAD * 90, left.xRot);
    }

    @Test void decorativeMaterialSelectionPreservesUntintedOverlaysAndCatalogEntries() {
        assertEquals(9, TackModels.values().length);
        var collector = mock(OrderedSubmitNodeCollector.class);
        TackModels.getModel("wings_butterfly").submit(STILL, new PoseStack(), collector, 1234, 77, 0xFF123456);
        var calls = mockingDetails(collector).getInvocations().stream().filter(i -> i.getMethod().getName().equals("submitModel")).toList();
        assertEquals(2, calls.size());
        assertEquals(0xFF123456, (int)calls.get(0).getArgument(6)); assertEquals(-1, (int)calls.get(1).getArgument(6));
        assertEquals(77, (int)calls.get(1).getArgument(8));
        assertSame(RenderTypes.entityCutout(Identifier.parse("truly_custom_horse_tack:textures/entity/horse/special_tack/wings_butterfly_overlay.png")), calls.get(1).getArgument(3));
        assertNull(TackModels.getModel("unknown_future_pattern"));
    }

    @Test void tackLayerQueuesEquipmentBodyAndPatternsWithOriginalStateAndLight() {
        var armor = new HorseGeneticModel(HorseGeneticModel.createArmorLayer().bakeRoot());
        RenderLayerParent<GeneticHorseRenderState, HorseGeneticModel> parent = () -> armor;
        var layer = new HorseTackLayer(parent, armor, mock(SpriteGetter.class));
        var state = state(layers("horn_color")); state.outlineColor = 42;
        var horse = horse(MuleGeneticEntity.class);
        when(horse.getItemBySlot(EquipmentSlot.MAINHAND)).thenReturn(new ItemStack(Items.END_ROD));
        HorseTackIntegration.extract(horse, state, stack -> TEXTURE);
        var collector = mock(SubmitNodeCollector.class); var body = mock(OrderedSubmitNodeCollector.class); var details = mock(OrderedSubmitNodeCollector.class);
        var equipment = mock(OrderedSubmitNodeCollector.class);
        when(collector.order(0)).thenReturn(equipment); when(collector.order(1)).thenReturn(body); when(collector.order(2)).thenReturn(details);
        PoseStack pose = new PoseStack(); Matrix4f before = new Matrix4f(pose.last().pose());
        layer.submit(pose, collector, 1234, state, 0, 0);
        var calls = mockingDetails(body).getInvocations().stream().filter(i -> i.getMethod().getName().equals("submitModel")).toList();
        assertEquals(1, calls.size()); assertSame(armor, calls.get(0).getArgument(0)); assertSame(state, calls.get(0).getArgument(1));
        assertSame(RenderTypes.entityTranslucent(TEXTURE), calls.get(0).getArgument(3)); assertEquals(1234, (int)calls.get(0).getArgument(4));
        assertEquals(42, (int)calls.get(0).getArgument(8));
        assertEquals(1, mockingDetails(details).getInvocations().stream().filter(i -> i.getMethod().getName().equals("submitModel")).count());
        var equipmentCalls = mockingDetails(equipment).getInvocations().stream().filter(i -> i.getMethod().getName().equals("submitModel")).toList();
        var detailCalls = mockingDetails(details).getInvocations().stream().filter(i -> i.getMethod().getName().equals("submitModel")).toList();
        assertEquals(1, equipmentCalls.size());
        assertTrue(equipmentCalls.getFirst().getSequenceNumber() < calls.getFirst().getSequenceNumber());
        assertTrue(calls.getFirst().getSequenceNumber() < detailCalls.getFirst().getSequenceNumber());
        assertEquals(before, pose.last().pose());
        armor.setupAnim(state); // Actual RHG Build #2 geometry accepts its own extracted state.
    }
    static class ModifierProbe extends net.neoforged.neoforge.client.renderstate.RegisterRenderStateModifiersEvent {
        Class<?> target;
        java.util.function.BiConsumer<AbstractHorseGenetic, GeneticHorseRenderState> modifier;
        @Override @SuppressWarnings("unchecked")
        public <E extends net.minecraft.world.entity.Entity, S extends net.minecraft.client.renderer.entity.state.EntityRenderState> void registerEntityModifier(
                Class<? extends net.minecraft.client.renderer.entity.EntityRenderer<? extends E, ? extends S>> type, java.util.function.BiConsumer<E, S> callback) {
            target = type; modifier = (java.util.function.BiConsumer<AbstractHorseGenetic, GeneticHorseRenderState>)(java.util.function.BiConsumer<?, ?>)callback;
        }
    }

    @Test void modifierRegistrationUsesRhgsActualRendererAndRunsAfterItsBodySnapshot() {
        var probe = new ModifierProbe(); HorseTackIntegration.registerStateModifiers(probe);
        assertSame(HorseGeneticRenderer.class, probe.target);
        var state = state(ItemStack.EMPTY); probe.modifier.accept(horse(HorseGeneticEntity.class), state);
        assertNotNull(state.getRenderData(HorseTackIntegration.DATA));
        assertEquals(ORIGINAL, state.armorTexture);
    }

    @Test void bannerComponentsAndPatternOrderAreCapturedAndQueuedWithOriginalGeometry() {
        var holder = net.minecraft.core.Holder.direct(new net.minecraft.world.level.block.entity.BannerPattern(Identifier.parse("minecraft:stripe_bottom"), "fixture"));
        var mutable = new ArrayList<net.minecraft.world.level.block.entity.BannerPatternLayers.Layer>();
        mutable.add(new net.minecraft.world.level.block.entity.BannerPatternLayers.Layer(holder, DyeColor.BLUE));
        var banner = new HorseTackRenderData.Banner(DyeColor.RED, new net.minecraft.world.level.block.entity.BannerPatternLayers(mutable));
        mutable.clear(); assertEquals(1, banner.patterns().layers().size());
        var flag = new FlagModel(FlagModel.createBodyLayer().bakeRoot(), mock(SpriteGetter.class));
        var collector = mock(OrderedSubmitNodeCollector.class); var pose = new PoseStack(); Matrix4f before = new Matrix4f(pose.last().pose());
        flag.submitBanner(STILL, pose, collector, 1234, 42, banner);
        var calls = mockingDetails(collector).getInvocations().stream().filter(i -> i.getMethod().getName().equals("submitModel")).toList();
        assertEquals(5, calls.size()); // Pole, bar, fabric, banner base, ordered mask.
        assertEquals(0xFF000000 | DyeColor.RED.getTextureDiffuseColor(), (int)calls.get(3).getArgument(6));
        assertEquals(0xFF000000 | DyeColor.BLUE.getTextureDiffuseColor(), (int)calls.get(4).getArgument(6));
        assertEquals(before, pose.last().pose());
    }

}
