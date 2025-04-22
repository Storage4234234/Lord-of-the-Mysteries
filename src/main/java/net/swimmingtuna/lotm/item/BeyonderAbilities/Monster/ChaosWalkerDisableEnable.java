package net.swimmingtuna.lotm.item.BeyonderAbilities.Monster;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.*;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SendParticleS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.Earthquake.isOnSurface;
import static net.swimmingtuna.lotm.util.BeyonderUtil.getCoordinateAtLeastAway;

public class ChaosWalkerDisableEnable extends SimpleAbilityItem {
    public ChaosWalkerDisableEnable(Properties properties) {
        super(properties, BeyonderClassInit.MONSTER, 3, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        enableOrDisableDangerSense(player);
        return InteractionResult.SUCCESS;
    }

    public static void enableOrDisableDangerSense(LivingEntity player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            boolean chaosWalkerCombat = tag.getBoolean("monsterChaosWalkerCombat");
            tag.putBoolean("monsterChaosWalkerCombat", !chaosWalkerCombat);
            if (player instanceof Player pPlayer) {
                pPlayer.displayClientMessage(Component.literal("Chaos Walker Combat is: " + (chaosWalkerCombat ? "Off" : "On")).withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY), true);
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, toggles your chaos walking. If enabled, while in combat (below 75% HP), you will randomly get a premonition of chaos occuring all around you, with one safe spot that won't be affected."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("None").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("MONSTER_ABILITY", ChatFormatting.GRAY);
    }

    public static void onChaosWalkerCombat(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity instanceof Player pPlayer) {
                BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(pPlayer);
                int sequence = holder.getSequence();
                CompoundTag tag = pPlayer.getPersistentData();
                int occursion = tag.getInt("chaosWalkerCalamityOccursion");
                if (pPlayer.getHealth() <= pPlayer.getMaxHealth() * 0.75 && pPlayer.tickCount % 500 == 0 && tag.getInt("chaosWalkerCombat") == 0 && tag.getBoolean("monsterChaosWalkerCombat")) {
                    tag.putInt("chaosWalkerCombat", 300);
                    Random random = new Random();
                    int radius = (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.CHAOSWALKERCOMBAT.get());
                    tag.putInt("chaosWalkerSafeX", (int) (pPlayer.getX() + (random.nextInt(radius) - (radius * 0.5))));
                    tag.putInt("chaosWalkerSafeZ", (int) (pPlayer.getZ() + (random.nextInt(radius) - (radius * 0.5))));
                    tag.putInt("chaosWalkerRadius", radius);
                }
                int chaosWalkerCounter = tag.getInt("chaosWalkerCombat");
                int chaosWalkerSafeX = tag.getInt("chaosWalkerSafeX");
                int chaosWalkerSafeZ = tag.getInt("chaosWalkerSafeZ");
                int surfaceY1 = pPlayer.level().getHeight(Heightmap.Types.WORLD_SURFACE, chaosWalkerSafeX, chaosWalkerSafeZ) + 1;
                int radius = tag.getInt("chaosWalkerRadius");
                if (chaosWalkerCounter >= 1) {
                    tag.putInt("chaosWalkerCombat", chaosWalkerCounter - 1);
                    if (chaosWalkerCounter % 100 == 0) {
                        pPlayer.sendSystemMessage(Component.literal("A calamity will fall everywhere around you in " + chaosWalkerCounter / 20 + " seconds, move to X: " + chaosWalkerSafeX + " Z: " + chaosWalkerSafeZ).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (chaosWalkerCounter == 80 || chaosWalkerCounter == 60 || chaosWalkerCounter == 40 || chaosWalkerCounter == 20) {
                        pPlayer.sendSystemMessage(Component.literal("A calamity will fall everywhere around you in " + chaosWalkerCounter / 20 + " seconds, move to X: " + chaosWalkerSafeX + " Z: " + chaosWalkerSafeZ).withStyle(ChatFormatting.DARK_RED).withStyle(ChatFormatting.BOLD));
                    }
                    if (pPlayer instanceof ServerPlayer serverPlayer) {
                        if (chaosWalkerCounter % 20 == 0 || (occursion >= 1 && occursion % 20 == 0)) {
                            spawnParticleCylinder(serverPlayer, chaosWalkerSafeX, surfaceY1, chaosWalkerSafeZ, 150, 10);
                        }
                    }
                }
                if (chaosWalkerCounter == 1) {
                    tag.putInt("chaosWalkerCalamityOccursion", 120);
                }
                if (occursion >= 1) {
                    tag.putInt("chaosWalkerCalamityOccursion", occursion - 1);
                    if (occursion % 3 == 0) {
                        Random random = new Random();
                        int randomInt = random.nextInt(100);
                        if (randomInt >= 85) {
                            int farAwayX = getCoordinateAtLeastAway(chaosWalkerSafeX, 60, radius);
                            int farAwayZ = getCoordinateAtLeastAway(chaosWalkerSafeZ, 60, radius);
                            int randomInt2 = (int) ((Math.random() * radius) - (double) radius / 2);
                            int surfaceY = pPlayer.level().getHeight(Heightmap.Types.WORLD_SURFACE, farAwayX, farAwayZ) + 1;
                            MeteorEntity.summonMeteorAtPositionWithScale(pPlayer, pPlayer.getX() + randomInt2, pPlayer.getY() - 50, pPlayer.getZ() + randomInt2, farAwayX, surfaceY - 50, farAwayZ, 4);
                        }
                        if (randomInt >= 70 && randomInt <= 84) {
                            int farAwayX = getCoordinateAtLeastAway(chaosWalkerSafeX, 30, radius);
                            int farAwayZ = getCoordinateAtLeastAway(chaosWalkerSafeZ, 30, radius);
                            int surfaceY = pPlayer.level().getHeight(Heightmap.Types.WORLD_SURFACE, farAwayX, farAwayZ) + 1;
                            TornadoEntity tornado = new TornadoEntity(EntityInit.TORNADO_ENTITY.get(), pPlayer.level());
                            tornado.setTornadoRadius(50 - (sequence * 7));
                            tornado.setTornadoHeight(80 - (sequence * 10));
                            tornado.setOwner(pPlayer);
                            tornado.setTornadoPickup(false);
                            tornado.setTornadoLifecount(100);
                            tornado.teleportTo(farAwayX, surfaceY, pPlayer.getZ());
                            pPlayer.level().addFreshEntity(tornado);
                        }
                        if (randomInt >= 50 && randomInt <= 69) {
                            int farAwayX = getCoordinateAtLeastAway(chaosWalkerSafeX, 20, radius);
                            int farAwayZ = getCoordinateAtLeastAway(chaosWalkerSafeZ, 20, radius);
                            int surfaceY = pPlayer.level().getHeight(Heightmap.Types.WORLD_SURFACE, farAwayX, farAwayZ) + 1;
                            LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), pPlayer.level());
                            lightningEntity.setSpeed(5);
                            lightningEntity.setDamage(15);
                            Vec3 targetPos = new Vec3(farAwayX, surfaceY, farAwayZ);
                            lightningEntity.setTargetPos(targetPos);
                            lightningEntity.setBranchOut(true);
                            lightningEntity.setDeltaMovement(0, -3, 0);
                            lightningEntity.setNewStartPos(new Vec3(farAwayX, surfaceY + 100, farAwayZ));
                            lightningEntity.setOwner(pPlayer);
                            lightningEntity.setMaxLength(80);
                            lightningEntity.setNoUp(true);
                            pPlayer.level().addFreshEntity(lightningEntity);
                        }
                        if (randomInt >= 30 && randomInt <= 49) {
                            int farAwayX = getCoordinateAtLeastAway(chaosWalkerSafeX, 40 - (sequence * 5), radius);
                            int farAwayZ = getCoordinateAtLeastAway(chaosWalkerSafeZ, 40 - (sequence * 5), radius);
                            int surfaceY = pPlayer.level().getHeight(Heightmap.Types.WORLD_SURFACE, farAwayX, farAwayZ) + 1;
                            int columnHeight = 100 - (sequence * 10);
                            AABB effectBox = new AABB(farAwayX - (20 - sequence * 2), surfaceY, farAwayZ - (20 - sequence * 2), farAwayX + (20 - sequence * 2), surfaceY + columnHeight, farAwayZ + (20 - sequence * 2));
                            List<LivingEntity> affectedEntities = pPlayer.level().getEntitiesOfClass(LivingEntity.class, effectBox);
                            for (LivingEntity entity : affectedEntities) {
                                if (entity == pPlayer) continue;
                                entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 2, false, true));
                                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2, false, true));
                                spawnParticleCylinderServerSide((ServerPlayer) pPlayer, farAwayX - 10, farAwayZ - 10, 100 - (sequence * 10), 10);
                            }
                        }
                        if (randomInt >= 15 && randomInt <= 29) {
                            for (int i = 0; i < 20; i++) {
                                int farAwayX = getCoordinateAtLeastAway(chaosWalkerSafeX, 20, radius);
                                int farAwayZ = getCoordinateAtLeastAway(chaosWalkerSafeZ, 20, radius);
                                int surfaceY = pPlayer.level().getHeight(Heightmap.Types.WORLD_SURFACE, farAwayX, farAwayZ) + 1;
                                StoneEntity stoneEntity = new StoneEntity(EntityInit.STONE_ENTITY.get(), pPlayer.level());
                                stoneEntity.teleportTo(farAwayX, surfaceY + 150 + 30, farAwayZ);
                                stoneEntity.setOwner(pPlayer);
                                stoneEntity.setDeltaMovement(0, -4, 0);
                                pPlayer.level().addFreshEntity(stoneEntity);
                            }
                        }
                        if (randomInt <= 14) {
                            int farAwayX = getCoordinateAtLeastAway(chaosWalkerSafeX, 25, radius);
                            int farAwayZ = getCoordinateAtLeastAway(chaosWalkerSafeZ, 25, radius);
                            int surfaceY = pPlayer.level().getHeight(Heightmap.Types.WORLD_SURFACE, farAwayX, farAwayZ) + 1;
                            Level level = pPlayer.level();
                            int spawnCount = 120 - (sequence * 10);
                            Random random1 = new Random();
                            BlockPos playerPos = pPlayer.blockPosition();
                            for (int i = 0; i < spawnCount; i++) {
                                int offsetX = random1.nextInt(21) - 10;
                                int offsetZ = random1.nextInt(21) - 10;
                                BlockPos spawnPos = new BlockPos(farAwayX, surfaceY, farAwayZ);
                                if (!level.isEmptyBlock(spawnPos) && isOnSurface(level, spawnPos)) {
                                    LavaEntity lavaEntity = new LavaEntity(EntityInit.LAVA_ENTITY.get(), level);
                                    lavaEntity.teleportTo(spawnPos.getX(), spawnPos.getY() + 3, spawnPos.getZ());
                                    lavaEntity.setDeltaMovement(0, 3 + (Math.random() * 3), 0);
                                    lavaEntity.setLavaXRot(random1.nextInt(18));
                                    lavaEntity.setOwner(pPlayer);
                                    lavaEntity.setLavaYRot(random1.nextInt(18));
                                    ScaleData scaleData = ScaleTypes.BASE.getScaleData(lavaEntity);
                                    scaleData.setScale(1.0f + random1.nextFloat() * 2.0f);
                                    level.addFreshEntity(lavaEntity);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void spawnParticleCylinder(ServerPlayer player, int centerX, int centerY, int centerZ,
                                              int height, int radius) {
        for (int y = centerY; y <= height; y++) {
            for (double angle = 0; angle < 360; angle += 10) { // Adjust the step size for density
                double radians = Math.toRadians(angle);
                double x = centerX + radius * Math.cos(radians);
                double z = centerZ + radius * Math.sin(radians);
                LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleTypes.HAPPY_VILLAGER, x, y, z, 0, 0, 0), player);
            }
        }
    }

    private static void spawnParticleCylinderServerSide(ServerPlayer player, int centerX, int centerZ, int height,
                                                        int radius) {
        for (int y = 0; y <= height; y++) {
            for (double angle = 0; angle < 360; angle += 10) {
                double radians = Math.toRadians(angle);
                double x = centerX + radius * Math.cos(radians);
                double z = centerZ + radius * Math.sin(radians);
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ASH, x, y, z, 0, 0, 0, 0, 0);
                }
            }
        }
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null && !livingEntity.getPersistentData().getBoolean("monsterChaosWalkerCombat")) {
            return 85;
        } else if (livingEntity.getPersistentData().getBoolean("monsterChaosWalkerCombat") && target == null) {
            return 20;
        }
        return 0;
    }
}
