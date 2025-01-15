package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior;


import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.entity.DawnRayEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;

public class LightOfDawn extends SimpleAbilityItem {


    public LightOfDawn(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        sunriseGleam(player);
        return InteractionResult.SUCCESS;
    }

    public static void sunriseGleam(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            int sequence = BeyonderUtil.getSequence(livingEntity);
            int maxLifetime = 500 - (sequence * 50);

            for (int i = 0; i < 5; i++) {
                // Calculate angle for this ray (72 degrees apart = 360/5)
                float angle = i * 72f;

                DawnRayEntity ray = new DawnRayEntity(
                        livingEntity.level(),
                        livingEntity.getX(),
                        livingEntity.getY() + 10, // Spawn height
                        livingEntity.getZ()
                );

                ray.setAngle(angle);
                ray.setYRot(angle); // Add this line to set initial rotation
                ray.setMaxLifetime(maxLifetime);
                ray.setRotationSpeed(5); // Make sure to set rotation speed

                livingEntity.level().addFreshEntity(ray);
            }
        }
    }
}

