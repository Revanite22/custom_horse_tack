package com.outrightwings.truly_custom_horse_tack.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public abstract class Rotate4Block extends SingleInventoryBlock{
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public Rotate4Block(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    @Override
    public float getRotation(BlockState state) {
        Direction direction = state.getValue(FACING).getClockWise();
        float angle;
        if (direction == Direction.NORTH) {
            angle = 180;
        }else if(direction == Direction.EAST){
            angle = 270;
        }else if(direction == Direction.SOUTH){
            angle = 0;
        }else{
            angle = 90;
        }

        return angle;
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        // Preserve the original wall orientation and NORTH fallback on vertical faces.
        return this.defaultBlockState().setValue(FACING,
                face.getAxis().isHorizontal() ? face.getClockWise() : Direction.NORTH);
    }
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
