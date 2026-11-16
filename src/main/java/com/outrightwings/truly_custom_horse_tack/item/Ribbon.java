package com.outrightwings.truly_custom_horse_tack.item;

import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

public class Ribbon extends Item {
    public Ribbon(Properties properties) { super(properties); }

    public boolean hasCustomColor(ItemStack stack) { return TackItemData.hasColor(stack); }
    public int getColor(ItemStack stack) { return TackItemData.getColor(stack); }
    public void setColor(ItemStack stack, int color) { TackItemData.setColor(stack, color); }
    public void clearColor(ItemStack stack) { TackItemData.clearColor(stack); }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState block = context.getLevel().getBlockState(context.getClickedPos());
        if (block.is(Blocks.WATER_CAULDRON) && hasCustomColor(context.getItemInHand())) {
            if (!context.getLevel().isClientSide()) {
                clearColor(context.getItemInHand());
                LayeredCauldronBlock.lowerFillLevel(block, context.getLevel(), context.getClickedPos());
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
