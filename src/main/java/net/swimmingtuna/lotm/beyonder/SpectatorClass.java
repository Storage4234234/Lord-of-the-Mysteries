package net.swimmingtuna.lotm.beyonder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.entity.MeteorEntity;
import net.swimmingtuna.lotm.entity.StoneEntity;
import net.swimmingtuna.lotm.entity.TornadoEntity;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.Earthquake;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.*;
import java.util.regex.Pattern;

public class SpectatorClass implements BeyonderClass {
    @Override
    public List<String> sequenceNames() {
        return List.of(
                "Visionary",
                "Author",
                "Discerner",
                "Dream Weaver",
                "Manipulator",
                "Dreamwalker",
                "Hypnotist",
                "Psychiatrist",
                "Telepathist",
                "Spectator"
        );
    }

    @Override
    public List<Integer> spiritualityLevels() {
        return List.of(10000, 5000, 3000, 1800, 1200, 700, 450, 300, 175, 125);
    }

    @Override
    public List<Integer> mentalStrength() {
        return List.of(630, 420, 320, 270, 220, 145, 110, 95, 70, 45);
    }

    @Override
    public List<Integer> spiritualityRegen() {
        return List.of(34, 22, 16, 12, 10, 8, 6, 5, 3, 2);
    }

    @Override
    public List<Double> maxHealth() {
        return List.of(45.0, 40.0, 40.0, 35.0, 32.0, 28.0, 28.0, 23.0, 20.0, 20.0);
    }

    @Override
    public void tick(LivingEntity player, int sequenceLevel) {
        if (!player.level().isClientSide() && player.isCrouching()) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 5, -1, false, false));
        }
        if (player.tickCount % 80 == 0) {
            System.out.println("Ticked for " + player.getName());
            if (sequenceLevel >= 0) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 30 * 20, -1, false, false));
            }
            if (sequenceLevel == 6) {
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 0, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.FIRE_RESISTANCE, 300, 0, false, false);
            } else if (sequenceLevel == 5) {
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 0, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.FIRE_RESISTANCE, 300, 0, false, false);
            } else if (sequenceLevel == 4) {
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 1, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.FIRE_RESISTANCE, 300, 0, false, false);
            } else if (sequenceLevel == 3) {
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 1, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.FIRE_RESISTANCE, 300, 0, false, false);
            } else if (sequenceLevel == 2) {
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 2, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 2, false, false);
                applyMobEffect(player, MobEffects.FIRE_RESISTANCE, 300, 0, false, false);

            } else if (sequenceLevel == 1) {
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 2, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 2, false, false);
                applyMobEffect(player, MobEffects.FIRE_RESISTANCE, 300, 0, false, false);

            } else if (sequenceLevel == 0) {
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 3, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 2, false, false);
                applyMobEffect(player, MobEffects.FIRE_RESISTANCE, 300, 0, false, false);
            }
        }
    }


    @Override
    public Multimap<Integer, Item> getItems() {
        HashMultimap<Integer, Item> items = HashMultimap.create();
        items.put(9, ItemInit.BEYONDER_ABILITY_USER.get());
        items.put(9, ItemInit.ALLY_MAKER.get());
        items.put(8, ItemInit.MIND_READING.get());
        items.put(7, ItemInit.AWE.get());
        items.put(7, ItemInit.FRENZY.get());
        items.put(7, ItemInit.PLACATE.get());
        items.put(6, ItemInit.BATTLE_HYPNOTISM.get());
        items.put(6, ItemInit.PSYCHOLOGICAL_INVISIBILITY.get());
        items.put(5, ItemInit.DREAM_WALKING.get());
        items.put(5, ItemInit.NIGHTMARE.get());
        items.put(4, ItemInit.APPLY_MANIPULATION.get());
        items.put(4, ItemInit.MANIPULATE_EMOTION.get());
        items.put(4, ItemInit.MANIPULATE_FONDNESS.get());
        items.put(4, ItemInit.MANIPULATE_MOVEMENT.get());
        items.put(4, ItemInit.DRAGON_BREATH.get());
        items.put(4, ItemInit.MENTAL_PLAGUE.get());
        items.remove(4, ItemInit.FRENZY.get());
        items.put(4, ItemInit.MIND_STORM.get());
        items.put(3, ItemInit.PLAGUE_STORM.get());
        items.put(3, ItemInit.CONSCIOUSNESS_STROLL.get());
        items.put(3, ItemInit.DREAM_WEAVING.get());
        items.put(2, ItemInit.DISCERN.get());
        items.put(2, ItemInit.DREAM_INTO_REALITY.get());
        items.put(1, ItemInit.PROPHECY.get());
        items.put(1, ItemInit.METEOR_SHOWER.get());
        items.put(1, ItemInit.METEOR_NO_LEVEL_SHOWER.get());
        items.put(0, ItemInit.ENVISION_BARRIER.get());
        items.put(0, ItemInit.ENVISION_LIFE.get());
        items.put(0, ItemInit.ENVISION_DEATH.get());
        items.put(0, ItemInit.ENVISION_HEALTH.get());
        items.put(0, ItemInit.ENVISION_LOCATION.get());
        items.put(0, ItemInit.ENVISION_WEATHER.get());
        items.put(0, ItemInit.ENVISION_KINGDOM.get());

        return items;
    }

    @Override
    public ChatFormatting getColorFormatting() {
        return ChatFormatting.AQUA;
    }


    public void applyMobEffect(LivingEntity pPlayer, MobEffect mobEffect, int duration, int amplifier, boolean ambient, boolean visible) {
        MobEffectInstance currentEffect = pPlayer.getEffect(mobEffect);
        MobEffectInstance newEffect = new MobEffectInstance(mobEffect, duration, amplifier, ambient, visible);
        if (currentEffect == null) {
            pPlayer.addEffect(newEffect);
        } else if (currentEffect.getAmplifier() < amplifier) {
            pPlayer.addEffect(newEffect);
        } else if (currentEffect.getAmplifier() == amplifier && duration >= currentEffect.getDuration()) {
            pPlayer.addEffect(newEffect);
        }
    }

    public static final Pattern PROPHECY_PATTERN = Pattern.compile("(\\w+) will (.*?) in (\\d+) (second|seconds|minute|minutes)");
    public static final Map<String, String> EVENT_TO_TAG = new HashMap<>();
    static {
        EVENT_TO_TAG.put("encounter a meteor", "spectatorProphesizedMeteor");
        EVENT_TO_TAG.put("encounter a tornado", "spectatorProphesizedTornado");
        EVENT_TO_TAG.put("encounter an earthquake", "spectatorProphesizedEarthquake");
        EVENT_TO_TAG.put("encounter a plague", "spectatorProphesizedPlague");
        EVENT_TO_TAG.put("have potion success", "spectatorProphesizedGuaranteedPotion");
        EVENT_TO_TAG.put("encounter weakness", "spectatorProphesizedWeakness");
        EVENT_TO_TAG.put("encounter a sinkhole", "spectatorProphesizedSinkhole");
        EVENT_TO_TAG.put("be healed", "spectatorProphesizedHealed");
        EVENT_TO_TAG.put("be lucky", "spectatorProphesizedLuck");
        EVENT_TO_TAG.put("be unlucky", "spectatorProphesizedMisfortune");
    }

    public static void prophecyTickEvent(LivingEvent.LivingTickEvent event) {
        if (!event.getEntity().level().isClientSide()) {
            LivingEntity livingEntity = event.getEntity();
            CompoundTag tag = livingEntity.getPersistentData();
            int meteor = tag.getInt("spectatorProphesizedMeteor");
            int tornado = tag.getInt("spectatorProphesizedTornado");
            int earthquake = tag.getInt("spectatorProphesizedEarthquake");
            int plague = tag.getInt("spectatorProphesizedPlague");
            int potion = tag.getInt("spectatorProphesizedGuaranteedPotion");
            int weakness = tag.getInt("spectatorProphesizedWeakness");
            int healed = tag.getInt("spectatorProphesizedHealed");
            int luck = tag.getInt("spectatorProphesizedLuck");
            int sinkhole = tag.getInt("spectatorProphesizedSinkhole");
            int misfortune = tag.getInt("spectatorProphesizedMisfortune");
            if (sinkhole == 1) {
                tag.putInt("spectatorProphecySinkholeOccurence", 80);
            }
            if (tag.getInt("spectatorProphecySinkholeOccurence") >= 1) {
                int sinkholeOccurence = tag.getInt("spectatorProphecySinkholeOccurence");
                tag.putInt("spectatorProphecySinkholeOccurence", sinkholeOccurence - 1);
                int x = tag.getInt("spectatorProphesizedSinkholeX");
                int y = tag.getInt("spectatorProphesizedSinkholeY");
                int z = tag.getInt("spectatorProphesizedSinkholeZ");
                if (x == 0 && y == 0 && z == 0) {
                    tag.putInt("spectatorProphesizedSinkholeX", (int) livingEntity.getX());
                    tag.putInt("spectatorProphesizedSinkholeY", (int) livingEntity.getY());
                    tag.putInt("spectatorProphesizedSinkholeZ", (int) livingEntity.getZ());
                }
                if (sinkholeOccurence == 1) {
                    tag.putInt("spectatorProphesizedSinkholeX",0);
                    tag.putInt("spectatorProphesizedSinkholeY",  0);
                    tag.putInt("spectatorProphesizedSinkholeZ", 0);
                }
                if (sinkholeOccurence != 1) {
                    int livingX = tag.getInt("spectatorProphesizedSinkholeX");
                    int livingY = tag.getInt("spectatorProphesizedSinkholeY");
                    int livingZ = tag.getInt("spectatorProphesizedSinkholeZ");
                    tag.putInt("spectatorProphecySinkholeOccurence", sinkholeOccurence - 1);
                    int currentDepth = tag.getInt("sinkholeProphecyCurrentDepth");
                    if (sinkholeOccurence == 80) {
                        currentDepth = 0;
                        tag.putInt("sinkholeProphecyCurrentDepth", 0);
                    }
                    int sinkholeRadius = (int) (BeyonderUtil.getDamage(livingEntity).get(ItemInit.PROPHECY.get()) * 1.5);
                    sinkholeRadius = Math.max(5, sinkholeRadius);
                    BlockPos center = new BlockPos(livingX, livingY, livingZ);
                    if (z % 2 == 0 && currentDepth < 40) {
                        currentDepth++;
                        tag.putInt("sinkholeProphecyCurrentDepth", currentDepth);
                        for (int i = -sinkholeRadius; i <= sinkholeRadius; i++) {
                            for (int j = -sinkholeRadius; j <= sinkholeRadius; j++) {
                                if (i * i + j * j <= sinkholeRadius * sinkholeRadius) {
                                    BlockPos pos = center.offset(i, 0, j);
                                    BlockPos targetPos = pos.offset(0, -currentDepth, 0);
                                    if (targetPos.getY() <= -50) {
                                        continue;
                                    }
                                    BlockState state = livingEntity.level().getBlockState(targetPos);
                                    if (!state.isAir() && state.getBlock() != Blocks.BEDROCK && !state.is(BlockTags.WITHER_IMMUNE)) {
                                        if (livingEntity.getRandom().nextInt(5) == 0 && livingEntity.level() instanceof ServerLevel serverLevel) {serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.05);
                                        }
                                        livingEntity.level().destroyBlock(targetPos, false);
                                    }
                                }
                            }
                        }
                    }
                    if (z % 10 == 0) {
                        for (int depth = 1; depth <= currentDepth; depth++) {
                            for (int i = -sinkholeRadius; i <= sinkholeRadius; i++) {
                                for (int j = -sinkholeRadius; j <= sinkholeRadius; j++) {
                                    if (i * i + j * j <= sinkholeRadius * sinkholeRadius) {
                                        BlockPos pos = center.offset(i, 0, j);
                                        BlockPos targetPos = pos.offset(0, -depth, 0);
                                        if (targetPos.getY() <= -50) {
                                            continue;
                                        }
                                        BlockState state = livingEntity.level().getBlockState(targetPos);
                                        if (!state.isAir() && state.getBlock() != Blocks.BEDROCK && !state.is(BlockTags.WITHER_IMMUNE)) {livingEntity.level().destroyBlock(targetPos, false);
                                        }
                                    }
                                }
                            }
                        }
                    }
                    List<LivingEntity> entities = livingEntity.level().getEntitiesOfClass(LivingEntity.class, new AABB(center.getX() - sinkholeRadius - 5, center.getY() - currentDepth - 5, center.getZ() - sinkholeRadius - 5, center.getX() + sinkholeRadius + 5, center.getY() + 10, center.getZ() + sinkholeRadius + 5));
                    for (LivingEntity entity : entities) {
                        double dx = center.getX() + 0.5 - entity.getX();
                        double dz = center.getZ() + 0.5 - entity.getZ();
                        double distance = Math.sqrt(dx * dx + dz * dz);
                        if (distance <= sinkholeRadius * 0.8) {
                            if (entity.getY() > center.getY() - currentDepth - 1) {
                                entity.setDeltaMovement(entity.getDeltaMovement().add(0, -0.4, 0));
                                entity.hurtMarked = true;
                            }
                        }
                        else if (distance <= sinkholeRadius * 2) {
                            dx = dx / distance;
                            dz = dz / distance;
                            double pullStrength = 0.1 * (1 - distance / (sinkholeRadius * 2));
                            entity.setDeltaMovement(entity.getDeltaMovement().add(dx * (pullStrength * 3), -0.1, dz * (pullStrength * 3)));
                            entity.hurtMarked = true;
                            if (entity instanceof Player player && !player.isCreative()) {
                                player.setSprinting(false);
                                if (player.isCrouching()) {
                                    player.setDeltaMovement(player.getDeltaMovement().scale(0.8));
                                    player.hurtMarked = true;
                                }
                            }
                        }
                    }
                }
            }
            if (meteor >= 1) {
                tag.putInt("spectatorProphesizedMeteor", meteor - 1);
            }
            if (sinkhole >= 1) {
                tag.putInt("spectatorProphesizedSinkhole", sinkhole -1 );
            }
            if (tornado >= 1) {
                tag.putInt("spectatorProphesizedTornado", tornado - 1);
            }
            if (earthquake >= 1) {
                tag.putInt("spectatorProphesizedEarthquake", earthquake - 1);
            }
            if (plague >= 1) {
                tag.putInt("spectatorProphesizedPlague", plague - 1);
            }
            if (potion >= 1) {
                tag.putInt("spectatorProphesizedGuaranteedPotion", potion - 1);
            }
            if (weakness >= 1) {
                tag.putInt("spectatorProphesizedWeakness", weakness - 1);
            }
            if (healed >= 1) {
                tag.putInt("spectatorProphesizedHealed", healed - 1);
            }
            if (luck >= 1) {
                tag.putInt("spectatorProphesizedLuck", luck - 1);
            }
            if (misfortune >= 1) {
                tag.putInt("spectatorProphesizedMisfortune", misfortune - 1);
            }
            if (meteor == 1) {
                MeteorEntity.summonMeteorAtPositionWithScale(livingEntity, livingEntity.getX(), livingEntity.getY() + 200, livingEntity.getZ(), livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), 9);
                for (int i = 0; i < 6; i++) {
                    float random = BeyonderUtil.getRandomInRange(150);
                    float random2 = BeyonderUtil.getRandomInRange(150);
                    MeteorEntity.summonMeteorAtPositionWithScale(livingEntity, livingEntity.getX() + random, livingEntity.getY() + 200, livingEntity.getZ() + random2, livingEntity.getX() + random2, livingEntity.getY(), livingEntity.getZ() + random2, 6 + (int) (Math.random() * 3));
                }
            }
            if (tornado == 1) {
                TornadoEntity tornadoEntity = new TornadoEntity(livingEntity.level(), livingEntity, 0, 0, 0);
                tornadoEntity.setTornadoHeight(100);
                tornadoEntity.setTornadoRadius(70);
                if (BeyonderUtil.getSequence(livingEntity) == 0) {
                    tornadoEntity.setTornadoLifecount(300);
                } else {
                    tornadoEntity.setTornadoLifecount(150);
                }
                tornadoEntity.setTornadoMov(livingEntity.getLookAngle().scale(0.5f).toVector3f());
                tornadoEntity.setTornadoRandom(true);
                tornadoEntity.setTornadoPickup(false);
                livingEntity.level().addFreshEntity(tornadoEntity);
            }
            if (earthquake == 1) {
                tag.putInt("prophesizedEarthquake", 300);
            }
            if (tag.getInt("prophesizedEarthquake") >= 1) {
                int prophesizedEarthquake = tag.getInt("prophesizedEarthquake");
                tag.putInt("prophesizedEarthquake", prophesizedEarthquake - 1);
                int radius = 80;
                if (prophesizedEarthquake % 20 == 0) {
                    for (LivingEntity entity : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate((radius)))) {
                        if (entity.onGround()) {
                            entity.hurt(livingEntity.damageSources().fall(), 20);
                        }
                    }
                }
                if (prophesizedEarthquake % 2 == 0) {
                    AABB checkArea = livingEntity.getBoundingBox().inflate(radius);
                    Random random = new Random();
                    for (BlockPos blockPos : BlockPos.betweenClosed(new BlockPos((int) checkArea.minX, (int) checkArea.minY, (int) checkArea.minZ), new BlockPos((int) checkArea.maxX, (int) checkArea.maxY, (int) checkArea.maxZ))) {
                        if (!livingEntity.level().getBlockState(blockPos).isAir() && Earthquake.isOnSurface(livingEntity.level(), blockPos)) {
                            if (random.nextInt(20) == 1) {
                                BlockState blockState = livingEntity.level().getBlockState(blockPos);
                                if (livingEntity.level() instanceof ServerLevel serverLevel) {
                                    serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, blockState),
                                            blockPos.getX(),
                                            blockPos.getY() + 1,
                                            blockPos.getZ(),
                                            0, 0.0, 0.0, 0, 0);
                                }
                            }
                            if (random.nextInt(4000) == 1) {
                                livingEntity.level().destroyBlock(blockPos, false);
                            } else if (random.nextInt(10000) == 2) {
                                StoneEntity stoneEntity = new StoneEntity(livingEntity.level(), livingEntity);
                                ScaleData scaleData = ScaleTypes.BASE.getScaleData(stoneEntity);
                                stoneEntity.teleportTo(blockPos.getX(), blockPos.getY() + 3, blockPos.getZ());
                                stoneEntity.setDeltaMovement(0, (3 + (Math.random() * (6 - 3))), 0);
                                stoneEntity.setStoneYRot((int) (Math.random() * 18));
                                stoneEntity.setStoneXRot((int) (Math.random() * 18));
                                scaleData.setScale((float) (1 + (Math.random()) * 2.0f));
                                livingEntity.level().addFreshEntity(stoneEntity);
                            }
                        }
                    }
                }
            }
            if (plague == 1) {
               for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(80))) {
                   BeyonderUtil.applyMobEffect(living, MobEffects.WITHER, 500, 5, true, true);
                   BeyonderUtil.applyMobEffect(living, ModEffects.NOREGENERATION.get(), 300, 1, true, true);
                   BeyonderUtil.applyMobEffect(living, MobEffects.WEAKNESS, 500, 3, true, true);
                   BeyonderUtil.applyMobEffect(living, MobEffects.CONFUSION, 300, 1, true, true);
               }
            }
            if (weakness == 1) {
                BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 1200, 1, true, true);
            }
            if (healed == 1) {
                livingEntity.setHealth(livingEntity.getMaxHealth());
                for (MobEffectInstance effect : new ArrayList<>(livingEntity.getActiveEffects())) {
                    if (effect.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                        livingEntity.removeEffect(effect.getEffect());
                    }
                }
            }
            if (luck == 1) {
                livingEntity.getPersistentData().putDouble("luck", livingEntity.getPersistentData().getDouble("luck") + 100);
            }
            if (misfortune == 1) {
                livingEntity.getPersistentData().putDouble("misfortune", livingEntity.getPersistentData().getDouble("misfortune") + 100);
            }
        }
    }


}
