package net.swimmingtuna.lotm.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public class MobAttackMixin {
    @Inject(method = "doHurtTarget", at = @At("HEAD"))
    private void preventMeleeAttack(Entity pEntity, CallbackInfoReturnable<Boolean> ci) {
        Mob mob = (Mob)(Object)this;
        CompoundTag tag = mob.getPersistentData();
        int inTwilight = tag.getInt("inTwilight");
        if (inTwilight >= 1) {
            ci.cancel();
        }
    }
    @Inject(method = "serverAiStep", at = @At("HEAD"), cancellable = true)
    private void preventAIUpdates(CallbackInfo ci) {
        Mob mob = (Mob)(Object)this;
        CompoundTag tag = mob.getPersistentData();
        double twilightX = tag.getDouble("twilightManifestationX");
        double twilightY = tag.getDouble("twilightManifestationY");
        double twilightZ = tag.getDouble("twilightManifestationZ");
        int inTwilight = tag.getInt("inTwilight");
        int unableToUseAbility = tag.getInt("unableToUseAbility");

        if (twilightX != 0 || twilightY != 0 || twilightZ != 0 || inTwilight >= 1 || unableToUseAbility == 1) {
            ci.cancel();
        }
    }
}