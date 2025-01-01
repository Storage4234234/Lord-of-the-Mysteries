package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class RagingBlows extends SimpleAbilityItem {

    public RagingBlows(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 8, 20, 200);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        ragingBlows(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    public static void ragingBlows(CompoundTag playerPersistentData, PlayerMobEntity player) {
        //RAGING BLOWS
        boolean sailorLightning = playerPersistentData.getBoolean("SailorLightning");
        int ragingBlows = playerPersistentData.getInt("ragingBlows");
        int sequence = player.getCurrentSequence();
        int ragingBlowsRadius = (25 - (sequence * 3));
        int damage = 20 - sequence * 2;
        if (ragingBlows >= 1) {
            RagingBlows.spawnRagingBlowsParticlesPM(player);
            playerPersistentData.putInt("ragingBlows", ragingBlows + 1);
        }
        if (ragingBlows >= 6 && ragingBlows <= 96 && ragingBlows % 6 == 0) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 0.5F, 0.5F);
            Vec3 playerLookVector = player.getViewVector(1.0F);
            Vec3 playerPos = player.position();
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, new AABB(playerPos.x - ragingBlowsRadius, playerPos.y - ragingBlowsRadius, playerPos.z - ragingBlowsRadius, playerPos.x + ragingBlowsRadius, playerPos.y + ragingBlowsRadius, playerPos.z + ragingBlowsRadius))) {
                if (entity != player && playerLookVector.dot(entity.position().subtract(playerPos)) > 0) {
                    entity.hurt(entity.damageSources().generic(), damage);
                    double ragingBlowsX = player.getX() - entity.getX();
                    double ragingBlowsZ = player.getZ() - entity.getZ();
                    entity.knockback(0.25, ragingBlowsX, ragingBlowsZ);
                    if (sequence <= 7) {
                        double chanceOfDamage = (100.0 - (sequence * 12.5));
                        if (Math.random() * 100 < chanceOfDamage && sailorLightning) {
                            LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, entity.level());
                            lightningBolt.moveTo(entity.getX(), entity.getY(), entity.getZ());
                            entity.level().addFreshEntity(lightningBolt);
                        }
                    }
                }
            }
        }
        if (ragingBlows >= 100) {
            RagingBlows.spawnRagingBlowsParticlesPM(player);
            playerPersistentData.putInt("ragingBlows", 0);
        }
    }

    public static void ragingBlows(Player player) {
        if (!player.level().isClientSide()) {
            CompoundTag persistentData = player.getPersistentData();
            persistentData.putInt("ragingBlows", 1);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("""
                Upon use, causes the user to shoot punches powerfully all around the user, damaging everything around them
                Spirituality Used: 20
                Cooldown: 10 seconds""").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void ragingBlows(CompoundTag playerPersistentData, BeyonderHolder holder, Player player) {
        //RAGING BLOWS
        boolean sailorLightning = playerPersistentData.getBoolean("SailorLightning");
        int ragingBlows = playerPersistentData.getInt("ragingBlows");
        int ragingBlowsRadius = (27 - (holder.getCurrentSequence() * 3));
        int damage = 20 - holder.getCurrentSequence() * 2;
        if (ragingBlows >= 1) {
            RagingBlows.spawnRagingBlowsParticles(player);
            playerPersistentData.putInt("ragingBlows", ragingBlows + 1);
        }
        if (ragingBlows >= 6 && ragingBlows <= 96 && ragingBlows % 6 == 0) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 0.5F, 0.5F);
            Vec3 playerLookVector = player.getViewVector(1.0F);
            Vec3 playerPos = player.position();
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, new AABB(playerPos.x - ragingBlowsRadius, playerPos.y - ragingBlowsRadius, playerPos.z - ragingBlowsRadius, playerPos.x + ragingBlowsRadius, playerPos.y + ragingBlowsRadius, playerPos.z + ragingBlowsRadius))) {
                if (entity != player && playerLookVector.dot(entity.position().subtract(playerPos)) > 0) {
                    entity.hurt(entity.damageSources().generic(), damage);
                    double ragingBlowsX = player.getX() - entity.getX();
                    double ragingBlowsZ = player.getZ() - entity.getZ();
                    entity.knockback(0.25, ragingBlowsX, ragingBlowsZ);
                    if (holder.getCurrentSequence() <= 7) {
                        addLightningBolt(holder, sailorLightning, entity);
                    }
                }
            }
        }
        if (ragingBlows >= 100) {
            playerPersistentData.putInt("ragingBlows", 0);
        }
    }

    public static void addLightningBolt(BeyonderHolder holder, boolean sailorLightning, LivingEntity entity) {
        double chanceOfDamage = (100.0 - (holder.getCurrentSequence() * 12.5));
        if (Math.random() * 100 < chanceOfDamage && sailorLightning) {
            LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, entity.level());
            lightningBolt.moveTo(entity.getX(), entity.getY(), entity.getZ());
            entity.level().addFreshEntity(lightningBolt);
        }
    }

    public static void spawnRagingBlowsParticles(Player player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            Vec3 playerPos = player.position();
            Vec3 playerLookVector = player.getViewVector(1.0F);
            int radius = (27 - (holder.getCurrentSequence() * 3));
            CompoundTag persistentData = player.getPersistentData();
            int particleCounter = persistentData.getInt("ragingBlowsParticleCounter");

            if (particleCounter < 7) {
                double randomDistance = Math.random() * radius;
                Vec3 randomOffset = playerLookVector.scale(randomDistance);

                // Add random horizontal offset
                double randomHorizontalOffset = Math.random() * Math.PI * 2; // Random angle between 0 and 2π
                randomOffset = randomOffset.add(new Vec3(Math.cos(randomHorizontalOffset) * radius / 4, 0, Math.sin(randomHorizontalOffset) * radius / 4));

                // Add random vertical offset
                double randomVerticalOffset = Math.random() * Math.PI / 2 - Math.PI / 4; // Random angle between -π/4 and π/4
                randomOffset = randomOffset.add(new Vec3(0, Math.sin(randomVerticalOffset) * radius / 4, 0));

                double randomX = playerPos.x + randomOffset.x;
                double randomY = playerPos.y + randomOffset.y;
                double randomZ = playerPos.z + randomOffset.z;

                // Check if the random offset vector is in front of the player
                if (playerLookVector.dot(randomOffset) > 0) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, randomX,randomY, randomZ, 0, 0,0,0,0);
                }

                particleCounter++;
                persistentData.putInt("ragingBlowsParticleCounter", particleCounter);
            } else {
                persistentData.putInt("ragingBlowsParticleCounter", 0);
            }
        }
    }
    public static void spawnRagingBlowsParticlesPM(PlayerMobEntity player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 playerPos = player.position();
            Vec3 playerLookVector = player.getViewVector(1.0F);
            int radius = (25 - (player.getCurrentSequence() * 3));
            CompoundTag persistentData = player.getPersistentData();
            int particleCounter = persistentData.getInt("ragingBlowsParticleCounter");

            if (particleCounter < 7) {
                double randomDistance = Math.random() * radius;
                Vec3 randomOffset = playerLookVector.scale(randomDistance);

                // Add random horizontal offset
                double randomHorizontalOffset = Math.random() * Math.PI * 2; // Random angle between 0 and 2π
                randomOffset = randomOffset.add(new Vec3(Math.cos(randomHorizontalOffset) * radius / 4, 0, Math.sin(randomHorizontalOffset) * radius / 4));

                // Add random vertical offset
                double randomVerticalOffset = Math.random() * Math.PI / 2 - Math.PI / 4; // Random angle between -π/4 and π/4
                randomOffset = randomOffset.add(new Vec3(0, Math.sin(randomVerticalOffset) * radius / 4, 0));

                double randomX = playerPos.x + randomOffset.x;
                double randomY = playerPos.y + randomOffset.y;
                double randomZ = playerPos.z + randomOffset.z;

                // Check if the random offset vector is in front of the player
                if (playerLookVector.dot(randomOffset) > 0) {
                    serverLevel.sendParticles(ParticleTypes.EXPLOSION, randomX,randomY, randomZ, 0, 0,0,0,0);
                }

                particleCounter++;
                persistentData.putInt("ragingBlowsParticleCounter", particleCounter);
            } else {
                persistentData.putInt("ragingBlowsParticleCounter", 0);
            }
        }
    }
}