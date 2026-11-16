package com.outrightwings.truly_custom_horse_tack.item;

import com.outrightwings.truly_custom_horse_tack.Main;
import com.outrightwings.truly_custom_horse_tack.item.tack.TackPattern;
import com.outrightwings.truly_custom_horse_tack.item.tack.TackTagUtility;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Tuple;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.sounds.SoundEvents;
import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import java.util.function.Consumer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import sekelsta.horse_colors.client.renderer.TextureLayer;
import sekelsta.horse_colors.client.renderer.TextureLayerGroup;
import sekelsta.horse_colors.util.Color;


public class CustomTackItem extends Item implements TackItemData.TackItem {
    private static final Identifier EMPTY_LAYER = Identifier.fromNamespaceAndPath(Main.MODID, "textures/entity/horse/armor/custom_tack.png");

    public CustomTackItem(Properties properties) {
        super(properties.stacksTo(1)
                .attributes(ItemAttributeModifiers.EMPTY)
                .delayedComponent(DataComponents.EQUIPPABLE, lookup -> Equippable.builder(EquipmentSlot.BODY)
                        .setEquipSound(SoundEvents.HORSE_ARMOR)
                        .setAllowedEntities(lookup.getOrThrow(EntityTypeTags.CAN_WEAR_HORSE_ARMOR))
                        .setDamageOnHurt(false)
                        // RHG's interaction path retains taming and inventory restrictions.
                        .setEquipOnInteract(false)
                        .build()));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState block = context.getLevel().getBlockState(context.getClickedPos());
        if(block.is(Blocks.WATER_CAULDRON)){
            if (TackTagUtility.getPatternListSize(TackItemData.read(context.getItemInHand())) <= 0) {
                return InteractionResult.PASS;
            }
            if (context.getLevel().isClientSide()) return InteractionResult.SUCCESS;
            boolean removed = TackItemData.removeLastPattern(context.getItemInHand());
            if(removed){
                LayeredCauldronBlock.lowerFillLevel(block,context.getLevel(),context.getClickedPos());
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> list, TooltipFlag flag){
        CompoundTag tagData =  TackItemData.read(stack);
        for(int i = 0; i < TackTagUtility.getPatternListSize(tagData); i++) {
            Tuple<Integer, String> colorPattern = TackTagUtility.getColorAndPatternByIndex(tagData, i);

            if (colorPattern == null) {
                list.accept(Component.translatable("tack.truly_custom_horse_tack.pattern.unknown").withStyle(ChatFormatting.GRAY));
                continue;
            }
            var pattern = TackPattern.getTackPattern(colorPattern.getB());
            var patternName = pattern != null ? pattern.getTranslationKey() : "tack.truly_custom_horse_tack.pattern.unknown";

            MutableComponent color;
            if(colorPattern.getA() < DyeColor.values().length){
                var colorName = DyeColor.byId(colorPattern.getA());
                color = Component.translatable(String.format("color.minecraft.%s",colorName));
            } else {
                float[] rgb = TackTagUtility.getColorFromColorTag(colorPattern.getA());
                color = Component.literal(String.format("#%s%s%s", Integer.toHexString((int)(rgb[0]*255)),Integer.toHexString((int)(rgb[1]*255)),Integer.toHexString((int)(rgb[2]*255))));
            }
            list.accept(color.append(" ").append(Component.translatable(patternName)).withStyle(ChatFormatting.GRAY));
        }
        if(TackTagUtility.getPatternListSize(tagData) <= 0){
            list.accept(Component.translatable(String.format("tooltip.%s.%s", Main.MODID,"no_pattern")).withStyle(ChatFormatting.GRAY));
        }
    }
    public TextureLayerGroup getTextureLayers(ItemStack stack){
        TextureLayerGroup layerGroup = new TextureLayerGroup();

        CompoundTag tagData = TackItemData.read(stack);
        //This is to prevent crashing by  always having a blank base layer
        layerGroup.add(buildLayer(EMPTY_LAYER.toString(), Color.WHITE));
        for(int i = 0; i < TackTagUtility.getPatternListSize(tagData);i++) {
            Tuple<Integer, String> colorPattern = TackTagUtility.getColorAndPatternByIndex(tagData, i);

            if (colorPattern == null) continue;
            float[] rawColor = TackTagUtility.getColorFromColorTag(colorPattern.getA());
            Color color = new Color(rawColor[0],rawColor[1],rawColor[2]);

            TackPattern tackPattern = TackPattern.getTackPattern(colorPattern.getB());
            if (tackPattern != null) {
                layerGroup.add(buildLayer(tackPattern.getArmorTextureLocation().toString(),color));
                Identifier overlayLocation = tackPattern.getOverlayTextureLocation();
                if (overlayLocation != null) {
                    layerGroup.add(buildLayer(overlayLocation.toString(),Color.WHITE));
                }
            }
        }
        return layerGroup;
    }
    private TextureLayer buildLayer(String name, Color color){
        TextureLayer layer = new TextureLayer();
        layer.name = name;
        layer.color = color;
        return layer;
    }
}
