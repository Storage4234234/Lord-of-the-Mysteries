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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class RagingBlows extends SimpleAbilityItem {

    public RagingBlows(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 8, 45, 300);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        ragingBlows(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    public static void ragingBlows(LivingEntity player) {
        if (!player.level().isClientSide()) {
            CompoundTag persistentData = player.getPersistentData();
            int ragingBlows = persistentData.getInt("ragingBlows");
            persistentData.putInt("ragingBlows", 1);
            ragingBlows = 1;
        }
    }

    public static void ragingBlowsTick(LivingEntity livingEntity) {
        //RAGING BLOWS
        CompoundTag tag = livingEntity.getPersistentData();
        int sequence = BeyonderUtil.getSequence(livingEntity);
        boolean sailorLightning = tag.getBoolean("SailorLightning");
        int ragingBlows = tag.getInt("ragingBlows");
        int ragingBlowsRadius = (27 - (sequence * 3));
        if (ragingBlows >= 1) {
            RagingBlows.spawnRagingBlowsParticles(livingEntity);
            tag.putInt("ragingBlows", ragingBlows + 1);
        }
        if (ragingBlows >= 6 && ragingBlows <= 96 && ragingBlows % 6 == 0) {
            livingEntity.level().playSound(null, livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 0.5F, 0.5F);
            Vec3 playerLookVector = livingEntity.getViewVector(1.0F);
            Vec3 playerPos = livingEntity.position();
            for (LivingEntity entity : livingEntity.level().getEntitiesOfClass(LivingEntity.class, new AABB(playerPos.x - ragingBlowsRadius, playerPos.y - ragingBlowsRadius, playerPos.z - ragingBlowsRadius, playerPos.x + ragingBlowsRadius, playerPos.y + ragingBlowsRadius, playerPos.z + ragingBlowsRadius))) {
                if (entity != livingEntity && playerLookVector.dot(entity.position().subtract(playerPos)) > 0 && !BeyonderUtil.areAllies(livingEntity, entity)) {
                    float damage = BeyonderUtil.getDamage(livingEntity).get(ItemInit.RAGING_BLOWS.get());
                    entity.hurt(entity.damageSources().generic(), damage);
                    double ragingBlowsX = livingEntity.getX() - entity.getX();
                    double ragingBlowsZ = livingEntity.getZ() - entity.getZ();
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
            ragingBlows = 0;
            tag.putInt("ragingBlows", 0);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, unleash a fury of blows in the direction you're looking for a period of time"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("45").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("15 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void spawnRagingBlowsParticles(LivingEntity player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            Vec3 playerPos = player.position();
            Vec3 playerLookVector = player.getViewVector(1.0F);
            int radius = (27 - (BeyonderUtil.getSequence(player) * 3));
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
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }
}