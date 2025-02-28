package net.swimmingtuna.lotm.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityTickMixin {
    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void onTick(CallbackInfo ci) {
        Entity entity = (Entity)(Object)this;
        CompoundTag tag = entity.getPersistentData();
        int timer = tag.getInt("twilightManifestationTimer");
        if (!entity.level().isClientSide()) {
            if (timer > 1) {
                tag.putInt("twilightManifestationTimer", timer - 1);
                if (tag.getInt("unableToUseAbility") == 0) {
                    tag.putInt("unableToUseAbility", 1);
                }
            } else if (timer == 1) {
                tag.putInt("unableToUseAbility", 0);
                tag.putInt("twilightManifestationTimer", 0);
                tag.putDouble("twilightManifestationX", 0);
                tag.putDouble("twilightManifestationY", 0);
                tag.putDouble("twilightManifestationZ", 0);
            }
            double twilightX = tag.getDouble("twilightManifestationX");
            double twilightY = tag.getDouble("twilightManifestationY");
            double twilightZ = tag.getDouble("twilightManifestationZ");
            int inTwilight = tag.getInt("inTwilight");
            if (twilightX != 0 || twilightY != 0 || twilightZ != 0 || inTwilight >= 1) {
                if (entity instanceof LivingEntity living) {
                    living.getDeltaMovement().multiply(0, 0, 0);
                    living.setDeltaMovement(0, 0, 0);
                    living.xo = living.getX();
                    living.yo = living.getY();
                    living.zo = living.getZ();
                    living.xOld = living.getX();
                    living.yOld = living.getY();
                    living.zOld = living.getZ();
                }
                ci.cancel();
            }
        }
    }
}