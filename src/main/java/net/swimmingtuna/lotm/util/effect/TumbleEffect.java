package net.swimmingtuna.lotm.util.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class TumbleEffect extends MobEffect {

    public TumbleEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide()) {
            Vec3 motion = entity.getDeltaMovement();
            if (entity.tickCount % 50 == 0) {
                entity.addEffect(new MobEffectInstance(ModEffects.TUMBLE.get(), 5, 1, false, false));
            }
            double factor = 1.01D + (amplifier * 0.05D); // Increase slipperiness per level
            entity.setDeltaMovement(motion.x * factor, motion.y, motion.z * factor);
            entity.hurtMarked = true;
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}

