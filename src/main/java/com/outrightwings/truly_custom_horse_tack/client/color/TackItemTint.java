package com.outrightwings.truly_custom_horse_tack.client.color;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import com.mojang.serialization.MapCodec;
import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

/** Modern item models retain both DYED_COLOR and converted display.color compatibility. */
@OnlyIn(Dist.CLIENT)
public record TackItemTint() implements ItemTintSource {
    public static final TackItemTint INSTANCE = new TackItemTint();
    public static final MapCodec<TackItemTint> CODEC = MapCodec.unit(INSTANCE);
    public static final Identifier ID = Identifier.parse("truly_custom_horse_tack:tack_dye");

    public static void register(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(ID, CODEC);
    }

    @Override public int calculate(ItemStack stack, ClientLevel level, LivingEntity owner) {
        return 0xFF000000 | (TackItemData.getColor(stack) & 0xFFFFFF);
    }

    @Override public MapCodec<TackItemTint> type() { return CODEC; }
}
