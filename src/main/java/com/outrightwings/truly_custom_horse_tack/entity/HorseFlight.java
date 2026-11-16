package com.outrightwings.truly_custom_horse_tack.entity;

import com.outrightwings.truly_custom_horse_tack.item.tack.TackItemData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.gameevent.GameEvent;
import sekelsta.horse_colors.entity.AbstractHorseGenetic;

/** Read-only eligibility/animation rules plus the original glide event and ridden boost. */
public final class HorseFlight {
    private HorseFlight() {}

    public static boolean hasWingTack(ItemStack stack) {
        var patterns = TackItemData.patterns(TackItemData.read(stack));
        if (patterns != null) for (var tag : patterns)
            if (tag instanceof CompoundTag layer && layer.getStringOr("Pattern", "").contains("wings")) return true;
        return false;
    }

    public static boolean usableLegGlider(ItemStack stack) {
        // Elytra normally declares CHEST. The original mod deliberately equips it in LEGS.
        return stack.has(DataComponents.GLIDER) && !stack.nextDamageWillBreak();
    }

    public static boolean canFly(AbstractHorseGenetic horse) {
        return !horse.onGround() && !horse.isPassenger() && !horse.hasEffect(MobEffects.LEVITATION)
                && horse.getControllingPassenger() != null
                && (usableLegGlider(horse.getItemBySlot(EquipmentSlot.LEGS)) || hasWingTack(horse.getArmor()));
    }

    public static boolean ignoresFallDamage(AbstractHorseGenetic horse) {
        // Preserve legacy immunity even for a broken elytra or an unridden horse.
        return horse.getItemBySlot(EquipmentSlot.LEGS).is(Items.ELYTRA) || hasWingTack(horse.getArmor());
    }

    public static void glideEvent(AbstractHorseGenetic horse, boolean eligible) {
        if (!horse.level().isClientSide() && eligible && (horse.getFallFlyingTicks() + 1) % 10 == 0)
            horse.gameEvent(GameEvent.ELYTRA_GLIDE);
    }

    public static void boostRiddenMovement(LivingEntity entity) {
        if (entity.isFallFlying() && entity.isLocalInstanceAuthoritative())
            entity.setDeltaMovement(entity.getDeltaMovement().multiply(1.02f, 1.02f, 1.02f));
    }
}
