package com.outrightwings.truly_custom_horse_tack.mixin;

import com.outrightwings.truly_custom_horse_tack.entity.HorseFlight;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "travelRidden(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/phys/Vec3;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;travel(Lnet/minecraft/world/phys/Vec3;)V"))
    private void tcht$boostGliding(Player controller, Vec3 input, CallbackInfo ci) {
        // Vanilla already sets ridden speed, travels with computed riddenInput, and then
        // applies block effects. Preserve the 1.02 boost without brittle local capture.
        HorseFlight.boostRiddenMovement((LivingEntity)(Object)this);
    }
}
