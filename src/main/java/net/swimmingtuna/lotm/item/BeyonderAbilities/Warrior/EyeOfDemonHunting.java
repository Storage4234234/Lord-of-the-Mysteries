package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SendDustParticleS2C;
import net.swimmingtuna.lotm.networking.packet.SyncAntiConcealmentPacketS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ClientData.ClientAntiConcealmentData;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import virtuoel.pehkui.api.ScaleTypes;

public class EyeOfDemonHunting extends SimpleAbilityItem {


    public EyeOfDemonHunting(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        enableOrDisableEyeOfDemonHunting(player);
        return InteractionResult.SUCCESS;
    }

    public static void enableOrDisableEyeOfDemonHunting(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            CompoundTag tag = livingEntity.getPersistentData();
            boolean x = tag.getBoolean("demonHuntingEye");
            tag.putBoolean("demonHuntingEye", !x);
            if (livingEntity instanceof Player player) {
                player.displayClientMessage(Component.literal("Eye of Demon Hunting turned " + (x ? "off" : "on")).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD), true);
            }
        }
    }


    public static void eyeTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        CompoundTag tag = entity.getPersistentData();
        if (!entity.level().isClientSide()) {
            if (entity instanceof ServerPlayer serverPlayer) {
                if (!serverPlayer.level().isClientSide() && tag.getBoolean("demonHuntingEye") && entity.tickCount % 20 == 0) {
                    for (LivingEntity living : serverPlayer.level().getEntitiesOfClass(LivingEntity.class, serverPlayer.getBoundingBox().inflate(80))) {
                        if (living == serverPlayer) {
                            continue;
                        }
                        float healthPercentage = (living.getHealth() / living.getMaxHealth()) * 100;
                        float entityScale = ScaleTypes.BASE.getScaleData(living).getScale();
                        float entityWidth = living.getBbWidth();
                        double radius = Math.max(0.5, (entityScale * entityWidth) / 1.5);
                        float r, g, b;
                        if (healthPercentage > 66) {
                            r = 0.0F;
                            g = 1.0F;
                            b = 0.0F;
                        } else if (healthPercentage > 33) {
                            r = 1.0F;
                            g = 0.55F;
                            b = 0.0F;
                        } else {
                            r = 1.0F;
                            g = 0.0F;
                            b = 0.0F;
                        }
                        double x = living.getX();
                        double y = living.getY() + (living.getBbHeight() * entityScale) / 2;
                        double z = living.getZ();
                        int particleCount = Math.max(50, (int) (50 * entityScale));
                        for (int i = 0; i < particleCount; i++) {
                            double phi = Math.random() * 2 * Math.PI;
                            double theta = Math.acos(1 - 2 * Math.random());
                            double offsetX = radius * Math.sin(theta) * Math.cos(phi);
                            double offsetY = radius * Math.cos(theta);
                            double offsetZ = radius * Math.sin(theta) * Math.sin(phi);
                            LOTMNetworkHandler.sendToPlayer(new SendDustParticleS2C(r, g, b, 1.0F, x + offsetX, y + offsetY, z + offsetZ, 0.0, 0.0, 0.0), serverPlayer);
                        }
                    }
                }
                BeyonderClass pathway = BeyonderUtil.getPathway(serverPlayer);
                if (pathway != null && serverPlayer.tickCount % 100 == 0) {
                    if (pathway == BeyonderClassInit.WARRIOR.get() && BeyonderUtil.getSequence(serverPlayer) <= 3) {
                        LOTMNetworkHandler.sendToAllPlayers(new SyncAntiConcealmentPacketS2C(true, serverPlayer.getUUID()));
                    }
                }
            }
            if (entity.tickCount % 10 == 0 && BeyonderUtil.getSequence(entity) <= 4 && BeyonderUtil.getPathway(entity) == BeyonderClassInit.WARRIOR.get()) {
                Vec3 eyePosition = entity.getEyePosition();
                Vec3 lookVector = entity.getLookAngle();
                Vec3 reachVector = eyePosition.add(lookVector.x * 35, lookVector.y * 35, lookVector.z * 35);
                AABB searchBox = entity.getBoundingBox().inflate(150);
                EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(entity.level(), entity, eyePosition, reachVector, searchBox, livingEntity -> !livingEntity.isSpectator() && livingEntity.isPickable(), 0.0f);
                if (entityHit != null && entityHit.getEntity() instanceof LivingEntity livingEntity && !BeyonderUtil.isAllyOf(entity, livingEntity)) {
                    BeyonderClass pathway = BeyonderUtil.getPathway(livingEntity);
                    if (pathway != null) {
                        if (pathway == BeyonderClassInit.SPECTATOR.get()) {
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 100, 1, true, true));
                        } else if (pathway == BeyonderClassInit.SAILOR.get()) {
                            BeyonderUtil.applyMobEffect(entity, ModEffects.ABILITY_WEAKNESS.get(), 100, 1, true, true);
                        } else if (pathway == BeyonderClassInit.SEER.get()) {

                        } else if (pathway == BeyonderClassInit.APPRENTICE.get()) {

                        } else if (pathway == BeyonderClassInit.MARAUDER.get()) {

                        } else if (pathway == BeyonderClassInit.SECRETSSUPPLICANT.get()) {

                        } else if (pathway == BeyonderClassInit.BARD.get()) {

                        } else if (pathway == BeyonderClassInit.READER.get()) {

                        } else if (pathway == BeyonderClassInit.SLEEPLESS.get()) {

                        } else if (pathway == BeyonderClassInit.WARRIOR.get()) {
                            if (entity.hasEffect(MobEffects.DAMAGE_BOOST)) {
                                BeyonderUtil.applyMobEffect(entity, MobEffects.DAMAGE_BOOST, 100, Math.min(6, entity.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier() + 1), true, true);
                            }
                        } else if (pathway == BeyonderClassInit.HUNTER.get()) {

                        } else if (pathway == BeyonderClassInit.ASSASSIN.get()) {

                        } else if (pathway == BeyonderClassInit.SAVANT.get()) {

                        } else if (pathway == BeyonderClassInit.MYSTERYPRYER.get()) {

                        } else if (pathway == BeyonderClassInit.CORPSECOLLECTOR.get()) {

                        } else if (pathway == BeyonderClassInit.LAWYER.get()) {

                        } else if (pathway == BeyonderClassInit.MONSTER.get()) {
                            tag.putDouble("luck", Math.min(100, tag.getDouble("luck") + 2));
                        } else if (pathway == BeyonderClassInit.APOTHECARY.get()) {

                        } else if (pathway == BeyonderClassInit.PLANTER.get()) {

                        } else if (pathway == BeyonderClassInit.ARBITER.get()) {

                        } else if (pathway == BeyonderClassInit.PRISONER.get()) {

                        } else if (pathway == BeyonderClassInit.CRIMINAL.get()) {

                        }
                    }
                }
            }

        }

    }
}
