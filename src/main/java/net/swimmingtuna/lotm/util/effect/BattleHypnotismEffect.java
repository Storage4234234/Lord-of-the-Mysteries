package net.swimmingtuna.lotm.util.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.util.BeyonderUtil;

public class BattleHypnotismEffect extends MobEffect {
    public BattleHypnotismEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory,color);
    }
    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (!livingEntity.level().isClientSide) {
            double radius = 5.0 + amplifier;
            for (Mob mob : livingEntity.level().getEntitiesOfClass(Mob.class, livingEntity.getBoundingBox().inflate(radius * 2))) {
                if (mob != livingEntity) {
                    mob.setTarget(livingEntity);
                }
            }
            if (!(livingEntity instanceof Player)) {
                livingEntity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,1,2));
            }
            super.applyEffectTick(livingEntity, amplifier);
        }
    }
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier){return true;}
    
    public static void battleHypnotismTickCheck(LivingEvent.LivingTickEvent event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        if (!event.getEntity().hasEffect(ModEffects.BATTLEHYPNOTISM.get())) {
            return;
        } else if (event.getEntity() instanceof Mob mob && mob.getTarget() != null && mob.getTarget().is(event.getEntity())) {
            for (LivingEntity living : mob.level().getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(10))) {
                if (living instanceof Player && !BeyonderUtil.areAllies(mob, living)) {
                    mob.setTarget(living);
                }
            }
        }
    }
}

