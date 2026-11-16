package checks;

import com.google.gson.JsonParser;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.MapCodec;
import com.outrightwings.truly_custom_horse_tack.block.*;
import com.outrightwings.truly_custom_horse_tack.block.entity.SingleInventoryBlockEntity;
import com.outrightwings.truly_custom_horse_tack.client.color.TackItemTint;
import com.outrightwings.truly_custom_horse_tack.client.renderer.*;
import com.outrightwings.truly_custom_horse_tack.client.renderer.model.*;
import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import net.minecraft.client.color.item.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.*;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.util.*;
import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class DisplayRenderingTest {
    static final Identifier COMPOSITE = Identifier.parse("truly_custom_horse_tack:fixture_composite");
    static final BlockPos POS = new BlockPos(0, 64, 0);

    @BeforeAll static void setup() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        ItemFixtureMod.bindFixtureComponents();
        if (!Items.SADDLE.builtInRegistryHolder().areComponentsBound()) Items.SADDLE.builtInRegistryHolder()
                .bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 1).build());
    }

    static ItemStack tack(String... patterns) {
        var stack = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("truly_custom_horse_tack:custom_tack")));
        CompoundTag data = new CompoundTag(); data.putString("future", "keep");
        ListTag layers = new ListTag();
        for (int i=0; i<patterns.length; i++) {
            CompoundTag layer = new CompoundTag();
            layer.putString("Pattern", patterns[i]);
            layer.putInt("Color", i % 2 == 0 ? DyeColor.RED.getId() : 0x123456);
            layers.add(layer);
        }
        data.put("Patterns", layers); TackItemData.write(stack, data); return stack;
    }

    static class Registrations extends EntityRenderersEvent.RegisterRenderers {
        final Map<BlockEntityType<?>, BlockEntityRendererProvider<?, ?>> providers = new HashMap<>();
        @Override public <T extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(
                BlockEntityType<? extends T> type, BlockEntityRendererProvider<T, S> provider) {
            assertNull(providers.put(type, provider), "Each original display gets exactly one provider");
        }
    }

    static Registrations registrations() {
        Registrations probe = new Registrations();
        DisplayRenderers.register(probe, stack -> COMPOSITE);
        return probe;
    }

    @SuppressWarnings("unchecked")
    static DisplayModelEntityRenderer<SingleInventoryBlockEntity> renderer(Registrations registrations, BlockEntityType<?> type) {
        return (DisplayModelEntityRenderer<SingleInventoryBlockEntity>) registrations.providers.get(type).create(null);
    }

    @Test void registersAllFourOriginalTypesWithWorkingFactories() {
        var registrations = registrations();
        assertEquals(Set.of(ModBlocks.HEAD_STAND_BE.get(), ModBlocks.HEAD_STAND_WALL_BE.get(),
                ModBlocks.SADDLE_RACK_BE.get(), ModBlocks.SADDLE_RACK_WALL_BE.get()), registrations.providers.keySet());
        assertInstanceOf(HeadStandBlockEntityRenderer.class, renderer(registrations, ModBlocks.HEAD_STAND_BE.get()));
        assertInstanceOf(HeadStandWallBlockEntityRenderer.class, renderer(registrations, ModBlocks.HEAD_STAND_WALL_BE.get()));
        assertInstanceOf(SaddleRackBlockEntityRenderer.class, renderer(registrations, ModBlocks.SADDLE_RACK_BE.get()));
        assertInstanceOf(SaddleRackWallBlockEntityRenderer.class, renderer(registrations, ModBlocks.SADDLE_RACK_WALL_BE.get()));
    }

    @Test void extractsEveryDisplayOrientationWithoutRetainingLiveCustomization() {
        var registrations = registrations();
        for (var block : List.of(ModBlocks.HEAD_STAND.get(), ModBlocks.HEAD_STAND_WALL.get(), ModBlocks.SADDLE_RACK.get(), ModBlocks.SADDLE_RACK_WALL.get())) {
            SingleInventoryBlock display = (SingleInventoryBlock) block;
            for (var blockState : block.getStateDefinition().getPossibleStates()) {
                var entity = (SingleInventoryBlockEntity) display.newBlockEntity(POS, blockState);
                var input = tack("horn_color", "ribbon_ear_left", "unknown_future_pattern");
                entity.setItem(0, input);
                var render = renderer(registrations, entity.getType());
                var state = render.createRenderState();
                render.extractRenderState(entity, state, 0.5f, Vec3.ZERO, null);
                assertEquals(display.getRotation(blockState), state.rotation);
                assertEquals(COMPOSITE, state.tackTexture);
                assertEquals(3, state.details.size());
                assertEquals("horn_color", state.details.get(0).pattern());
                assertEquals(0xFF000000 | DyeColor.RED.getTextureDiffuseColor(), state.details.get(0).color());
                assertEquals(0xFF123456, state.details.get(1).color());
                assertEquals("unknown_future_pattern", state.details.get(2).pattern());
                assertThrows(UnsupportedOperationException.class, () -> state.details.clear());
                entity.clearContent();
                assertEquals(3, state.details.size());
                assertEquals(POS, state.blockPos);
                assertEquals(15728880, state.lightCoords);
                render.extractRenderState(entity, state, 0, Vec3.ZERO, null);
                assertNull(state.tackTexture);
                assertTrue(state.details.isEmpty());
            }
        }
    }

    @Test void saddleAndMalformedDataHaveIndependentSafeSnapshots() {
        var entity = ModBlocks.HEAD_STAND_BE.get().create(POS, ModBlocks.HEAD_STAND.get().defaultBlockState());
        var render = renderer(registrations(), entity.getType());
        entity.setItem(0, new ItemStack(Items.SADDLE));
        var state = render.createRenderState(); render.extractRenderState(entity, state, 0, Vec3.ZERO, null);
        assertEquals(Identifier.parse("truly_custom_horse_tack:textures/entity/horse/armor/saddle.png"), state.tackTexture);
        assertTrue(state.details.isEmpty());
        var input = tack(); var malformed = TackItemData.read(input); malformed.putString("Patterns", "preserve");
        TackItemData.write(input, malformed);
        assertTrue(DisplayRenderState.extractDetails(input).isEmpty());
        assertEquals(malformed, TackItemData.read(input));
    }

    @Test void transformRetainsBlockCenterGroundPlaneAndAllSixteenHeadings() {
        for (int i=0; i<16; i++) {
            float angle = i * 22.5f;
            PoseStack pose = new PoseStack(); DisplayModelEntityRenderer.transform(pose, angle);
            Matrix4f matrix = pose.last().pose();
            var origin = matrix.transformPosition(new Vector3f());
            assertEquals(0.5f, origin.x, 0.00001f); assertEquals(1.5f, origin.y, 0.00001f); assertEquals(0.5f, origin.z, 0.00001f);
            var down = matrix.transformDirection(new Vector3f(0, 1, 0));
            assertEquals(-1, down.y, 0.00001f);
            var forward = matrix.transformDirection(new Vector3f(0, 0, 1));
            assertEquals(-(float)Math.sin(Math.toRadians(angle)), forward.x, 0.00001f);
            assertEquals((float)Math.cos(Math.toRadians(angle)), forward.z, 0.00001f);
            assertEquals(0, matrix.transformPosition(new Vector3f(0, 1.5f, 0)).y, 0.00001f);
        }
    }

    @Test void decorativePartsKeepTheirOriginalDisplayRestrictionsPosesAndOverlays() {
        for (var kind : DisplayRenderState.Kind.values()) {
            var parts = new DisplayTackParts(kind);
            if (kind.stand) {
                assertTrue(parts.surfaces("wings").isEmpty());
                var horn = parts.surfaces("horn").getFirst();
                assertFalse(horn.tinted()); assertEquals(18, horn.part().y); assertEquals(-5, horn.part().z);
                assertEquals(0.261799f, horn.part().xRot);
                assertTrue(parts.surfaces("horn_color").getFirst().tinted());
                for (var name : List.of("ribbon_ear", "ribbon_ear_left")) {
                    var ribbon = parts.surfaces(name).getFirst().part();
                    assertEquals(-ribbon.xRot, ribbon.getChild("flower").xRot);
                    assertEquals(-ribbon.xRot, ribbon.getChild("longs").xRot);
                    assertEquals(9, ribbon.getAllParts().size());
                }
            } else {
                assertTrue(parts.surfaces("horn").isEmpty());
                assertEquals(8, parts.surfaces("bells_neck").getFirst().part().getAllParts().size());
                assertEquals(10, parts.surfaces("bells_rear").getFirst().part().getAllParts().size());
                var surfaces = parts.surfaces("wings_butterfly");
                assertEquals(2, surfaces.size()); assertTrue(surfaces.get(0).tinted()); assertFalse(surfaces.get(1).tinted());
                assertTrue(surfaces.get(1).texture().getPath().endsWith("wings_butterfly_overlay.png"));
                var wings = surfaces.getFirst().part();
                assertEquals(kind.wall ? 23 : 17, wings.y); assertEquals(kind.wall ? 5 : 6, wings.z);
                assertEquals(0, wings.xRot);
                assertEquals(wings.getChild("left_wing").yRot, -wings.getChild("right_wing").yRot);
            }
            assertTrue(parts.surfaces("unknown_future_pattern").isEmpty());
        }
    }

    @Test void submissionKeepsMaterialOrderLightBreakOverlayAndPoseIsolation() {
        var entity = ModBlocks.HEAD_STAND_BE.get().create(POS, ModBlocks.HEAD_STAND.get().defaultBlockState());
        entity.setItem(0, tack("horn", "horn_color", "ribbon_ear"));
        var renderer = renderer(registrations(), entity.getType());
        var state = renderer.createRenderState();
        var breaking = new ModelFeatureRenderer.CrumblingOverlay(3, new PoseStack().last().copy());
        renderer.extractRenderState(entity, state, 0, Vec3.ZERO, breaking); state.lightCoords = 1234;
        var collector = mock(SubmitNodeCollector.class);
        PoseStack pose = new PoseStack(); Matrix4f before = new Matrix4f(pose.last().pose());
        renderer.submit(state, pose, collector, null);
        assertEquals(before, pose.last().pose());
        var calls = mockingDetails(collector).getInvocations().stream().filter(i -> i.getMethod().getName().equals("submitModelPart")).toList();
        assertEquals(5, calls.size());
        assertSame(RenderTypes.entityCutout(Identifier.parse("truly_custom_horse_tack:textures/entity/stand.png")), calls.get(0).getArgument(2));
        assertSame(RenderTypes.entityTranslucent(COMPOSITE), calls.get(1).getArgument(2));
        assertEquals(-1, (int)calls.get(2).getArgument(6)); // Plain horn ignores dye tint.
        assertEquals(0xFF123456, (int)calls.get(3).getArgument(6));
        for (var call : calls) {
            assertEquals(1234, (int)call.getArgument(3)); assertEquals(OverlayTexture.NO_OVERLAY, (int)call.getArgument(4));
            assertSame(breaking, call.getArgument(7));
        }
    }

    @Test void itemTintKeepsLegacyAndComponentRgbWithOpaqueAlpha() {
        var stack = tack();
        assertEquals(0xFFA06540, TackItemTint.INSTANCE.calculate(stack, null, null));
        CompoundTag root = TackItemData.read(stack); CompoundTag display = new CompoundTag(); display.putInt("color", 0x123456);
        root.put("display", display); TackItemData.write(stack, root);
        assertEquals(0xFF123456, TackItemTint.INSTANCE.calculate(stack, null, null));
        TackItemData.setColor(stack, 0xFEDCBA);
        assertEquals(0xFFFEDCBA, TackItemTint.INSTANCE.calculate(stack, null, null));
    }

    @Test void modernItemDefinitionsDecodeWithRegisteredTintAndKeepOriginalModelIds() throws Exception {
        ItemTintSources.bootstrap(); ItemModels.bootstrap();
        var field = ItemTintSources.class.getDeclaredField("ID_MAPPER"); field.setAccessible(true);
        @SuppressWarnings("unchecked")
        var mapper = (ExtraCodecs.LateBoundIdMapper<Identifier, MapCodec<? extends ItemTintSource>>) field.get(null);
        TackItemTint.register(new RegisterColorHandlersEvent.ItemTintSources(mapper));
        Path assets = Path.of(System.getProperty("tack.assetDirectory"));
        for (var name : List.of("mixable_dye", "ribbon")) {
            var json = JsonParser.parseString(Files.readString(assets.resolve("items/"+name+".json")));
            assertNotNull(ClientItem.CODEC.parse(JsonOps.INSTANCE, json).getOrThrow());
            var model = json.getAsJsonObject().getAsJsonObject("model");
            assertEquals("truly_custom_horse_tack:item/"+name, model.get("model").getAsString());
            assertSame(TackItemTint.INSTANCE, ItemTintSources.CODEC.parse(JsonOps.INSTANCE, model.getAsJsonArray("tints").get(0)).getOrThrow());
            if (name.equals("mixable_dye")) {
                var white = ItemTintSources.CODEC.parse(JsonOps.INSTANCE, model.getAsJsonArray("tints").get(1)).getOrThrow();
                assertEquals(-1, white.calculate(tack(), null, null));
            }
        }
    }
}
