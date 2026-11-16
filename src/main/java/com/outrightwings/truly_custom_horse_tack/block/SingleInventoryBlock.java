package com.outrightwings.truly_custom_horse_tack.block;

import com.outrightwings.truly_custom_horse_tack.block.entity.HeadStandBlockEntity;
import com.outrightwings.truly_custom_horse_tack.block.entity.SingleInventoryBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public abstract class SingleInventoryBlock extends Block implements EntityBlock {
    public SingleInventoryBlock(Properties prop) {
        super(prop);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, InteractionHand hand, BlockHitResult hit) {
        return transferItem(stack, level, pos, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        return transferItem(ItemStack.EMPTY, level, pos, player);
    }

    private InteractionResult transferItem(ItemStack stack, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide()) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof SingleInventoryBlockEntity stand) {
                if (stack.isEmpty()) {
                    ItemStack removed = stand.removeItem(0, 1);
                    player.addItem(removed);
                    // A full player inventory must not destroy the display's item.
                    if (!removed.isEmpty()) stand.setItem(0, removed);
                } else if (stand.validItem(stack) && stand.isEmpty()) {
                    stand.setItem(0, stack.copyWithCount(1));
                    stack.shrink(1);
                }
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        return InteractionResult.SUCCESS;
    }
    // Modern BlockEntity.preRemoveSideEffects drops Container contents automatically.
    // Retaining the legacy onRemove drop path would drop the inventory twice.
    public abstract float getRotation(BlockState state);
}
