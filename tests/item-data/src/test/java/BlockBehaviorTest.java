package com.outrightwings.truly_custom_horse_tack.block;

import checks.ItemFixtureMod;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.outrightwings.truly_custom_horse_tack.block.entity.SingleInventoryBlockEntity;
import net.minecraft.core.*;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.*;
import org.junit.jupiter.api.*;
import java.nio.file.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;

public class BlockBehaviorTest {
    static final BlockPos POS = new BlockPos(0, 64, 0);
    static final BlockHitResult HIT = new BlockHitResult(Vec3.atCenterOf(POS), Direction.UP, POS, false);

    @BeforeAll
    static void setup() {
        net.minecraft.SharedConstants.tryDetectVersion();
        net.minecraft.server.Bootstrap.bootStrap();
        ItemFixtureMod.bindFixtureComponents();
        if (!Items.DIAMOND.builtInRegistryHolder().areComponentsBound()) Items.DIAMOND.builtInRegistryHolder()
                .bindComponents(DataComponentMap.builder().set(DataComponents.MAX_STACK_SIZE, 64).build());
    }

    static List<SingleInventoryBlock> displays() {
        return List.of((SingleInventoryBlock) ModBlocks.HEAD_STAND.get(),
                (SingleInventoryBlock) ModBlocks.HEAD_STAND_WALL.get(),
                (SingleInventoryBlock) ModBlocks.SADDLE_RACK.get(),
                (SingleInventoryBlock) ModBlocks.SADDLE_RACK_WALL.get());
    }

    static BlockPlaceContext placement(Direction face, Direction horizontal, float yaw) {
        BlockPlaceContext context = mock(BlockPlaceContext.class);
        when(context.getClickedFace()).thenReturn(face);
        when(context.getHorizontalDirection()).thenReturn(horizontal);
        when(context.getRotation()).thenReturn(yaw);
        return context;
    }

    @Test
    void productionRegistryAndSavedStateSchemasRemainIntact() {
        assertEquals(21, ModBlocks.BLOCKS.getEntries().size());
        assertEquals(4, ModBlocks.BLOCK_ENTITIES.getEntries().size());
        for (var holder : ModBlocks.BLOCKS.getEntries()) {
            assertEquals("truly_custom_horse_tack", holder.getId().getNamespace());
            var block = holder.get();
            Set<String> properties = new HashSet<>();
            block.getStateDefinition().getProperties().forEach(p -> properties.add(p.getName()));
            if (block instanceof JumpBlock) {
                assertEquals(Set.of("up", "north", "east", "south", "west", "waterlogged"), properties);
                assertEquals(324, block.getStateDefinition().getPossibleStates().size());
            } else if (block instanceof HeadStandBlock) {
                assertEquals(Set.of("rotation"), properties);
                assertEquals(16, block.getStateDefinition().getPossibleStates().size());
            } else {
                assertEquals(Set.of("facing"), properties);
                assertEquals(4, block.getStateDefinition().getPossibleStates().size());
            }
            for (var state : block.getStateDefinition().getPossibleStates()) {
                var encoded = BlockState.CODEC.encodeStart(JsonOps.INSTANCE, state).getOrThrow();
                assertSame(state, BlockState.CODEC.parse(JsonOps.INSTANCE, encoded).getOrThrow());
            }
        }
    }

    @Test
    void headStandRetainsSixteenYawPositionsAndTransforms() {
        HeadStandBlock block = (HeadStandBlock) ModBlocks.HEAD_STAND.get();
        for (int i = -16; i < 32; i++) {
            var state = block.getStateForPlacement(placement(Direction.UP, Direction.NORTH, i * 22.5f));
            assertEquals(i & 15, state.getValue(HeadStandBlock.ROTATION));
            assertEquals((i & 15) * 22.5f, block.getRotation(state));
            assertEquals((i + 4) & 15, state.rotate(Rotation.CLOCKWISE_90).getValue(HeadStandBlock.ROTATION));
            assertSame(state, state.mirror(Mirror.LEFT_RIGHT).mirror(Mirror.LEFT_RIGHT));
        }
    }

    @Test
    void wallPlacementRetainsClockwiseFacingAndVerticalFallback() {
        for (Block block : List.of(ModBlocks.HEAD_STAND_WALL.get(), ModBlocks.SADDLE_RACK_WALL.get())) {
            for (Direction face : Direction.values()) {
                var state = block.getStateForPlacement(placement(face, Direction.SOUTH, 0));
                Direction expected = face.getAxis().isHorizontal() ? face.getClockWise() : Direction.NORTH;
                assertEquals(expected, state.getValue(Rotate4Block.FACING));
                assertEquals(expected.getClockWise(), state.rotate(Rotation.CLOCKWISE_90).getValue(Rotate4Block.FACING));
            }
        }
    }

    @Test
    void floorRackAndSaddlerRetainPlacementAndDisplayAngles() {
        SaddleRackBlock rack = (SaddleRackBlock) ModBlocks.SADDLE_RACK.get();
        SaddlerBlock saddler = (SaddlerBlock) ModBlocks.SADDLER.get();
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            var context = placement(Direction.UP, facing, 0);
            assertEquals(facing.getClockWise(), rack.getStateForPlacement(context).getValue(Rotate4Block.FACING));
            assertEquals(facing.getOpposite(), saddler.getStateForPlacement(context).getValue(SaddlerBlock.FACING));
        }
        float[] rackAngles = {180, 270, 0, 90};
        float[] wallAngles = {270, 0, 90, 180};
        Direction[] directions = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
        for (int i = 0; i < directions.length; i++) {
            assertEquals(rackAngles[i], rack.getRotation(rack.defaultBlockState().setValue(Rotate4Block.FACING, directions[i])));
            Rotate4Block wall = (Rotate4Block) ModBlocks.HEAD_STAND_WALL.get();
            assertEquals(wallAngles[i], wall.getRotation(wall.defaultBlockState().setValue(Rotate4Block.FACING, directions[i])));
        }
    }

    @Test
    void displayShapesAndBlockEntityFactoriesRetainTheirIdentity() {
        for (var block : displays()) {
            var state = block.defaultBlockState();
            var entity = (SingleInventoryBlockEntity) block.newBlockEntity(POS, state);
            assertNotNull(entity);
            assertTrue(entity.getType().isValid(state));
            assertEquals("truly_custom_horse_tack", BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(entity.getType()).getNamespace());
            for (var possible : block.getStateDefinition().getPossibleStates()) {
                var shape = possible.getShape(EmptyBlockGetter.INSTANCE, POS);
                assertFalse(shape.isEmpty());
                assertTrue(shape.bounds().minY >= 0 && shape.bounds().maxY <= 1);
            }
        }
    }

    @Test
    void jumpGeometryRetainsThinRailsAndOneBlockCollisionAcrossEveryState() {
        JumpBlock jump = (JumpBlock) ModBlocks.JUMP_WHITE.get();
        for (var state : jump.getStateDefinition().getPossibleStates()) {
            VoxelShape outline = state.getShape(EmptyBlockGetter.INSTANCE, POS);
            VoxelShape collision = state.getCollisionShape(EmptyBlockGetter.INSTANCE, POS);
            assertFalse(Shapes.joinIsNotEmpty(outline, collision, BooleanOp.ONLY_FIRST));
            if (!collision.isEmpty()) assertEquals(1.0, collision.max(Direction.Axis.Y));
            assertFalse(Shapes.joinIsNotEmpty(collision, Shapes.block(), BooleanOp.ONLY_FIRST));
        }
        var northLow = jump.defaultBlockState().setValue(WallBlock.UP, false).setValue(WallBlock.NORTH, WallSide.LOW);
        assertEquals(13 / 16.0, northLow.getShape(EmptyBlockGetter.INSTANCE, POS).max(Direction.Axis.Y));
        assertEquals(7 / 16.0, northLow.getShape(EmptyBlockGetter.INSTANCE, POS).min(Direction.Axis.X));
        assertEquals(9 / 16.0, northLow.getShape(EmptyBlockGetter.INSTANCE, POS).max(Direction.Axis.X));
        var straight = northLow.setValue(WallBlock.SOUTH, WallSide.LOW);
        assertEquals(13 / 16.0, straight.getShape(EmptyBlockGetter.INSTANCE, POS).max(Direction.Axis.Y));
    }

    @Test
    void vanillaWallUpdatesDispatchToTheJumpTagRestriction() throws Exception {
        var white = (JumpBlock) ModBlocks.JUMP_WHITE.get();
        var red = ModBlocks.JUMP_RED.get();
        var tag = BlockTags.create(Identifier.parse("truly_custom_horse_tack:simple_jumps"));
        var holder = red.builtInRegistryHolder();
        var original = holder.tags().toList();
        // Unit fixture has no server datapack reload. Bind only this holder temporarily.
        var bindTags = Holder.Reference.class.getDeclaredMethod("bindTags", Collection.class);
        bindTags.setAccessible(true);
        bindTags.invoke(holder, List.of(tag));
        try {
            LevelReader level = mock(LevelReader.class);
            when(level.getBlockState(any())).thenReturn(Blocks.AIR.defaultBlockState());
            var state = white.defaultBlockState();
            var connected = state.updateShape(level, mock(ScheduledTickAccess.class), POS, Direction.NORTH,
                    POS.north(), red.defaultBlockState(), RandomSource.create());
            assertEquals(WallSide.LOW, connected.getValue(WallBlock.NORTH));
            var disconnected = connected.updateShape(level, mock(ScheduledTickAccess.class), POS, Direction.NORTH,
                    POS.north(), Blocks.STONE.defaultBlockState(), RandomSource.create());
            assertEquals(WallSide.NONE, disconnected.getValue(WallBlock.NORTH));
            assertFalse(white.connectsTo(Blocks.COBBLESTONE_WALL.defaultBlockState(), true, Direction.SOUTH));
        } finally {
            bindTags.invoke(holder, original);
        }
    }

    @Test
    void jumpsRetainWaterloggingAndScheduledFluidUpdates() {
        var state = ModBlocks.JUMP_WHITE.get().defaultBlockState().setValue(WallBlock.WATERLOGGED, true);
        assertTrue(state.getFluidState().is(Fluids.WATER));
        LevelReader level = mock(LevelReader.class);
        ScheduledTickAccess ticks = mock(ScheduledTickAccess.class);
        state.updateShape(level, ticks, POS, Direction.DOWN, POS.below(), Blocks.AIR.defaultBlockState(), RandomSource.create());
        verify(ticks).scheduleTick(POS, Fluids.WATER, Fluids.WATER.getTickDelay(level));
    }

    @Test
    void displayInsertionRetrievalAndFullInventoryKeepCustomizedItems() {
        var tack = BuiltInRegistries.ITEM.getValue(Identifier.parse("truly_custom_horse_tack:custom_tack"));
        for (var block : displays()) {
            var state = block.defaultBlockState();
            var entity = (SingleInventoryBlockEntity) block.newBlockEntity(POS, state);
            Level level = mock(Level.class);
            when(level.getBlockEntity(POS)).thenReturn(entity);
            Player player = mock(Player.class);
            var stack = new ItemStack(tack, 3);
            CompoundTag data = new CompoundTag(); data.putString("unknown", "keep");
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
            block.useItemOn(stack, state, level, POS, player, InteractionHand.MAIN_HAND, HIT);
            assertEquals(2, stack.getCount());
            assertEquals(1, entity.getItem(0).getCount());
            assertEquals(data, entity.getItem(0).get(DataComponents.CUSTOM_DATA).copyTag());
            block.useWithoutItem(state, level, POS, player, HIT); // Player mock accepts nothing.
            assertEquals(1, entity.getItem(0).getCount());
            when(player.addItem(any())).thenAnswer(call -> { call.<ItemStack>getArgument(0).setCount(0); return true; });
            block.useWithoutItem(state, level, POS, player, HIT);
            assertTrue(entity.isEmpty());
            block.useItemOn(new ItemStack(Items.DIAMOND), state, level, POS, player, InteractionHand.MAIN_HAND, HIT);
            assertTrue(entity.isEmpty());
        }
    }

    @Test
    void saddlerOpensExistingProviderOnlyOnServer() {
        var block = (SaddlerBlock) ModBlocks.SADDLER.get();
        Level level = mock(Level.class);
        Player player = mock(Player.class);
        var state = block.defaultBlockState();
        block.useWithoutItem(state, level, POS, player, HIT);
        verify(player).openMenu(any(MenuProvider.class));
        assertEquals("menu.title.truly_custom_horse_tack.saddler_block_menu", state.getMenuProvider(level, POS).getDisplayName().getString());
        clearInvocations(player);
        when(level.isClientSide()).thenReturn(true);
        block.useWithoutItem(state, level, POS, player, HIT);
        verifyNoInteractions(player);
    }

    @Test
    void modernBlockLootAndTagResourcesCoverTheOriginalContent() throws Exception {
        Path data = Path.of(System.getProperty("tack.dataDirectory"));
        Path loot = data.resolve("truly_custom_horse_tack/loot_table/blocks");
        try (var files = Files.list(loot)) {
            var tables = files.toList();
            assertEquals(21, tables.size());
            for (var path : tables) {
                var json = JsonParser.parseString(Files.readString(path));
                assertTrue(LootTable.DIRECT_CODEC.parse(JsonOps.INSTANCE, json).isSuccess(), path.toString());
            }
        }
        var jumps = JsonParser.parseString(Files.readString(data.resolve("truly_custom_horse_tack/tags/block/simple_jumps.json"))).getAsJsonObject().getAsJsonArray("values");
        assertEquals(16, jumps.size());
        for (var entry : jumps) assertInstanceOf(JumpBlock.class, BuiltInRegistries.BLOCK.getValue(Identifier.parse(entry.getAsString())));
        var pickaxe = JsonParser.parseString(Files.readString(data.resolve("minecraft/tags/block/mineable/pickaxe.json"))).getAsJsonObject();
        assertEquals("#truly_custom_horse_tack:simple_jumps", pickaxe.getAsJsonArray("values").get(0).getAsString());
        var axe = JsonParser.parseString(Files.readString(data.resolve("minecraft/tags/block/mineable/axe.json"))).getAsJsonObject();
        assertEquals(5, axe.getAsJsonArray("values").size());
    }
}
