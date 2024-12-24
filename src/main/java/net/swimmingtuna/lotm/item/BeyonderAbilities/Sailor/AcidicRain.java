package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class AcidicRain extends SimpleAbilityItem {

    public AcidicRain(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 5, 175, 500);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        acidicRain(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    private static void acidicRain(Player player) {
        if (!player.level().isClientSide()) {
            player.getPersistentData().putInt("sailorAcidicRain", 1);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, summons an acidic rain around the player that persists for 15 seconds\n" +
                "Spirituality Used: 175\n" +
                "Cooldown: 25 seconds").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void acidicRain(Player player, int sequence) {
        //ACIDIC RAIN
        int acidicRain = player.getPersistentData().getInt("sailorAcidicRain");
        if (acidicRain <= 0) {
            return;
        }
        player.getPersistentData().putInt("sailorAcidicRain", acidicRain + 1);
        AcidicRain.spawnAcidicRainParticles(player);
        double radius1 = 50 - (sequence * 7);
        double radius2 = 10 - sequence;


        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius1))) {
            if (entity == player) {
                continue;
            }
            if (entity.hasEffect(MobEffects.POISON)) {
                int poisonAmp = entity.getEffect(MobEffects.POISON).getAmplifier();
                if (poisonAmp == 0) {
                    entity.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1, false, false));
                }
            } else {
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 1, false, false));
            }
        }

        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius2))) {
            if (entity == player) {
                continue;
            }
            if (entity.hasEffect(MobEffects.POISON)) {
                int poisonAmp = entity.getEffect(MobEffects.POISON).getAmplifier();
                if (poisonAmp <= 2) {
                    entity.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 2, false, false));
                }
            } else {
                entity.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 2, false, false));
            }
        }


        if (acidicRain > 300) {
            player.getPersistentData().putInt("sailorAcidicRain", 0);
        }
    }

    public static void spawnAcidicRainParticles(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            int sequence = holder.getCurrentSequence();
            double x = player.getX();
            double y = player.getY() + 5;
            double z = player.getZ();
            int maxRadius = 50 - (sequence * 7);
            int maxParticles = 250 - (sequence * 30);
            BeyonderUtil.spawnParticlesInSphere(serverLevel, x, y, z, maxRadius, maxParticles, 0, -3, 0, ParticleInit.ACIDRAIN_PARTICLE.get());
        }
    }
    public static void spawnAcidicRainParticlesPM(PlayerMobEntity player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            int sequence = player.getCurrentSequence();
            double x = player.getX();
            double y = player.getY() + 5;
            double z = player.getZ();
            int maxRadius = 50 - (sequence * 7);
            int maxParticles = 250 - (sequence * 30);
            BeyonderUtil.spawnParticlesInSphere(serverLevel, x, y, z, maxRadius, maxParticles, 0, -3, 0, ParticleInit.ACIDRAIN_PARTICLE.get());
        }
    }
}