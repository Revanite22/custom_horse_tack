package com.outrightwings.truly_custom_horse_tack.mixin;

import com.outrightwings.truly_custom_horse_tack.entity.HorseAccessories;
import com.outrightwings.truly_custom_horse_tack.entity.HorseFlight;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import sekelsta.horse_colors.entity.AbstractHorseGenetic;

@Mixin(AbstractHorseGenetic.class)
public abstract class AbstractHorseGeneticMixin extends AbstractHorse {
    protected AbstractHorseGeneticMixin(EntityType<? extends AbstractHorse> type, Level level) { super(type, level); }
    @Unique private AbstractHorseGenetic tcht$horse() { return (AbstractHorseGenetic)(Object)this; }

    @Inject(method = "itemInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/InteractionHand;)Z",
            at = @At("HEAD"), remap = false, cancellable = true)
    private void tcht$interact(Player player, ItemStack held, InteractionHand hand, CallbackInfoReturnable<Boolean> cir) {
        if (HorseAccessories.interact(tcht$horse(), held)) cir.setReturnValue(true);
    }

    @Inject(method = "aiStep()V", at = @At("HEAD"), remap = false)
    private void tcht$startGliding(CallbackInfo ci) {
        // Modern LivingEntity only calls updateFallFlying while the flag is already set.
        // Restore the original automatic takeoff eligibility before that check.
        if (!level().isClientSide()) setSharedFlag(7, HorseFlight.canFly(tcht$horse()));
    }

    @Override protected boolean canGlide() { return HorseFlight.canFly(tcht$horse()); }

    @Override protected void updateFallFlying() {
        boolean eligible = HorseFlight.canFly(tcht$horse());
        HorseFlight.glideEvent(tcht$horse(), eligible);
        if (!level().isClientSide()) setSharedFlag(7, eligible);
        // Legacy horse gliding did not damage elytra; do not invoke vanilla wear here.
    }

    @Override public boolean causeFallDamage(double distance, float amount, DamageSource source) {
        return !HorseFlight.ignoresFallDamage(tcht$horse()) && super.causeFallDamage(distance, amount, source);
    }
}
