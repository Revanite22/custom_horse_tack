package com.outrightwings.truly_custom_horse_tack.block;

import com.outrightwings.truly_custom_horse_tack.Main;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public class JumpBlock extends WallBlock {
    private static final MapCodec<JumpBlock> JUMP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            TagKey.codec(Registries.BLOCK).fieldOf("connectables").forGetter(block -> block.connectables),
            propertiesCodec()).apply(instance, JumpBlock::new));
    // WallBlock fixes its codec return type to MapCodec<WallBlock>.
    public static final MapCodec<WallBlock> CODEC = JUMP_CODEC.xmap(block -> block, block -> (JumpBlock) block);
    private final TagKey<Block> connectables;
    private final Function<BlockState, VoxelShape> jumpShapes;
    private final Function<BlockState, VoxelShape> jumpCollisionShapes;

    @Override
    public MapCodec<WallBlock> codec() {
        return CODEC;
    }

    public JumpBlock(String tag, Properties properties){
        this(BlockTags.create(Identifier.fromNamespaceAndPath(Main.MODID, tag)), properties);
    }

    private JumpBlock(TagKey<Block> connectables, Properties properties) {
        super(properties);
        this.connectables = connectables;
        // Original post/arm half-width 1, post/tall height 16, low outline 13.
        // Collision stays one block high so horses can jump over these walls.
        this.jumpShapes = makeJumpShapes(13);
        this.jumpCollisionShapes = makeJumpShapes(16);
    }

    private Function<BlockState, VoxelShape> makeJumpShapes(int lowHeight) {
        return getShapeForEachState(state -> {
            VoxelShape shape = state.getValue(UP) ? Block.box(7, 0, 7, 9, 16, 9) : Shapes.empty();
            for (var entry : PROPERTY_BY_DIRECTION.entrySet()) {
                WallSide side = state.getValue(entry.getValue());
                if (side == WallSide.NONE) continue;
                int height = side == WallSide.TALL ? 16 : lowHeight;
                VoxelShape arm = switch (entry.getKey()) {
                    case NORTH -> Block.box(7, 0, 0, 9, height, 9);
                    case SOUTH -> Block.box(7, 0, 7, 9, height, 16);
                    case WEST -> Block.box(0, 0, 7, 9, height, 9);
                    case EAST -> Block.box(7, 0, 7, 16, height, 9);
                    default -> throw new IllegalStateException("Non-horizontal wall side");
                };
                shape = Shapes.or(shape, arm);
            }
            return shape;
        }, WATERLOGGED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return jumpShapes.apply(state);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return jumpCollisionShapes.apply(state);
    }

    // Made protected by the single WallBlock access-transformer rule.
    @Override
    public boolean connectsTo(BlockState state, boolean bool, Direction direction) {
        return state.is(connectables);
    }
}
