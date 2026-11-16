package com.outrightwings.truly_custom_horse_tack.item;

import com.outrightwings.truly_custom_horse_tack.Main;
import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;

public class MixedDye extends Item {
    public MixedDye(Properties properties) {
        super(properties.durability(16));
    }

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

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                               Consumer<Component> tooltip, TooltipFlag flag) {
        if (hasCustomColor(stack)) {
            int color = getColor(stack);
            tooltip.accept(Component.literal(String.format("#%s%s%s",
                    Integer.toHexString(color >> 16 & 255), Integer.toHexString(color >> 8 & 255),
                    Integer.toHexString(color & 255))).withStyle(ChatFormatting.GRAY));
        } else {
            tooltip.accept(Component.translatable("tooltip." + Main.MODID + ".no_dye").withStyle(ChatFormatting.GRAY));
        }
    }
}
