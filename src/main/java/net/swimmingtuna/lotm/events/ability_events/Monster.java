package net.swimmingtuna.lotm.events.ability_events;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.DomainOfDecay;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.DomainOfProvidence;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;

public class Monster {
    public static void monsterDomainIntHandler(Player player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            int sequence = holder.getCurrentSequence();
            int maxRadius = 250 - (holder.getCurrentSequence() * 45);
            int radius = tag.getInt("monsterDomainRadius");
            if (player.tickCount % 500 == 0) {
                tag.putInt("monsterDomainMaxRadius", maxRadius);
            }
            if (player.isShiftKeyDown() && (player.getMainHandItem().getItem() instanceof DomainOfDecay || player.getMainHandItem().getItem() instanceof DomainOfProvidence)) {
                player.displayClientMessage(Component.literal("Current Domain Radius is " + radius).withStyle(BeyonderUtil.getStyle(player)),true);
                tag.putInt("monsterDomainRadius", radius + 5);
            } if (radius > maxRadius) {
                player.displayClientMessage(Component.literal("Current Domain Radius is 0").withStyle(BeyonderUtil.getStyle(player)),true);
                tag.putInt("monsterDomainRadius", 0);
            }
        }
    }



    public static void monsterLuckPoisonAttacker(LivingEntity pPlayer) {
        if (pPlayer.tickCount % 100 == 0) {
            if (pPlayer.getPersistentData().getInt("luckAttackerPoisoned") >= 1) {
                for (Player player : pPlayer.level().getEntitiesOfClass(Player.class, pPlayer.getBoundingBox().inflate(50))) {
                    if (player.getPersistentData().getInt("attackedMonster") >= 1) {
                        player.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(), 60, 1, false, false));
                        player.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 3, false, false));
                        pPlayer.getPersistentData().putInt("luckAttackerPoisoned", pPlayer.getPersistentData().getInt("luckAttackerPoisoned") - 1);
                    }
                }
            }
        }
    }

    public static void monsterLuckIgnoreMobs(PlayerMobEntity pPlayer) {
        if (pPlayer.tickCount % 40 == 0) {
            if (pPlayer.getPersistentData().getInt("luckIgnoreMobs") >= 1) {
                for (Mob mob : pPlayer.level().getEntitiesOfClass(Mob.class, pPlayer.getBoundingBox().inflate(20))) {
                    if (mob.getTarget() == pPlayer) {
                        for (LivingEntity livingEntity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().inflate(50))) {
                            if (livingEntity != null) {
                                mob.setTarget(livingEntity);
                            } else
                                mob.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(), 60, 1, false, false));
                        }
                        pPlayer.getPersistentData().putInt("luckIgnoreMobs", pPlayer.getPersistentData().getInt("luckIgnoreMobs") - 1);
                    }
                }
            }
        }
    }
}
