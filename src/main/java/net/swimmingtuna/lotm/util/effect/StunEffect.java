package net.swimmingtuna.lotm.util.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;

public class StunEffect extends MobEffect {
    public StunEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory,color);
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level().isClientSide()) {
            double x = livingEntity.getX();
            double y = livingEntity.getY();
            double z = livingEntity.getZ();
            livingEntity.teleportTo(x, y, z);
            livingEntity.setDeltaMovement(0,0,0);

        }
        super.applyEffectTick(livingEntity, amplifier);
    }

    public static void livingNoMoveEffect(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity instanceof Player) {
            return;
        }
        if (entity.hasEffect(ModEffects.STUN.get()) || entity.hasEffect(ModEffects.PARALYSIS.get()) || entity.hasEffect(ModEffects.AWE.get())) {
            entity.setDeltaMovement(0, entity.getDeltaMovement().y, 0);
            if (entity instanceof Mob mob) {
                mob.getNavigation().stop();

            }
        }
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
