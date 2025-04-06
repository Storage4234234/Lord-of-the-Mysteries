package net.swimmingtuna.lotm.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityCollisionMixin {
    @Inject(method = "collide", at = @At("HEAD"), cancellable = true)
    private void onCollide(Vec3 vec3, CallbackInfoReturnable<Vec3> cir) {
        Entity entity = (Entity)(Object)this;
        if (entity instanceof LivingEntity living && living.getPersistentData().contains("mercuryArmor")) {
            cir.setReturnValue(vec3);
        }
    }
}