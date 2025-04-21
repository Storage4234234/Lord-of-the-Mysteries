package net.swimmingtuna.lotm.beyonder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SendParticleS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import net.swimmingtuna.lotm.world.worlddata.CalamityEnhancementData;
import virtuoel.pehkui.api.ScaleTypes;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MonsterClass implements BeyonderClass {
    private int speed;
    private int resistance;
    private int strength;
    private int regen;

    @Override
    public List<String> sequenceNames() {
        return List.of(
                "Wheel of Fortune",
                "Snake of Mercury",
                "Soothsayer",
                "Chaoswalker",
                "Misfortune Mage",
                "Winner",
                "Calamity Priest",
                "Lucky One",
                "Robot",
                "Monster"
        );
    }

    @Override
    public List<Integer> spiritualityLevels() {
        return List.of(10000, 5000, 3000, 1800, 1200, 700, 450, 300, 175, 125);
    }

    @Override
    public List<Integer> mentalStrength() {
        return List.of(450, 320, 210, 175, 150, 110, 80, 70, 50, 33);
    }

    @Override
    public List<Integer> spiritualityRegen() {
        return List.of(34, 22, 16, 12, 10, 8, 6, 5, 3, 2);
    }

    @Override
    public List<Double> maxHealth() {
        return List.of(60.0, 45.0, 40.0, 40.0, 35.0, 25.0, 25.0, 25.0, 20.0, 20.0);
    }

    @Override
    public void tick(LivingEntity player, int sequenceLevel) {
        CompoundTag tag = player.getPersistentData();
        if (player.tickCount % 20 == 0) {
            if (sequenceLevel == 8 || sequenceLevel == 7) {
                if (player.getMainHandItem().getItem() instanceof SwordItem) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60, speed + 1, true, true);
                }
                if (player.getMainHandItem().getItem() instanceof AxeItem) {
                    applyMobEffect(player, MobEffects.DAMAGE_BOOST, 60, strength + 1, true, true);
                }
                if (player.getMainHandItem().getItem() instanceof PickaxeItem || player.getMainHandItem().getItem() instanceof ShovelItem) {
                    applyMobEffect(player, MobEffects.DIG_SPEED, 60, 1, true, true);
                }
                if (player.getMainHandItem().getItem() instanceof BowItem || player.getMainHandItem().getItem() instanceof CrossbowItem) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60, speed + 1, true, true);
                }
                if (player.getMainHandItem().getItem() instanceof ShieldItem || player.getOffhandItem().getItem() instanceof ShieldItem) {
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 1, true, true);
                }
            } else if (sequenceLevel == 6 || sequenceLevel == 5) {
                if (player.getMainHandItem().getItem() instanceof SwordItem) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60, speed + 1, true, true);
                    applyMobEffect(player, MobEffects.DIG_SPEED, 60, 0, true, true);
                }
                if (player.getMainHandItem().getItem() instanceof AxeItem) {
                    applyMobEffect(player, MobEffects.DAMAGE_BOOST, 60, strength + 1, true, true);
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 1, true, true);
                }
                if (player.getMainHandItem().getItem() instanceof PickaxeItem || player.getMainHandItem().getItem() instanceof ShovelItem) {
                    applyMobEffect(player, MobEffects.DIG_SPEED, 60, 2, true, true);
                }
                if (player.getMainHandItem().getItem() instanceof BowItem || player.getMainHandItem().getItem() instanceof CrossbowItem) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60, speed + 1, true, true);
                }
                if (player.getMainHandItem().getItem() instanceof ShieldItem || player.getOffhandItem().getItem() instanceof ShieldItem) {
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 1, true, true);
                }
            } else if (sequenceLevel <= 4) {
                if (player.getMainHandItem().getItem() instanceof SwordItem) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60, speed + 2, true, true);
                    applyMobEffect(player, MobEffects.DIG_SPEED, 60, 0, true, true);
                }
                if (player.getMainHandItem().getItem() instanceof AxeItem) {
                    applyMobEffect(player, MobEffects.DAMAGE_BOOST, 60, strength + 1, true, true);
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 1, true, true);
                }
                if (player.getMainHandItem().getItem() instanceof PickaxeItem || player.getMainHandItem().getItem() instanceof ShovelItem) {
                    applyMobEffect(player, MobEffects.DIG_SPEED, 60, 3, true, true);
                }
                if (player.getMainHandItem().getItem() instanceof BowItem || player.getMainHandItem().getItem() instanceof CrossbowItem) {
                    applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 60, speed + 2, true, true);
                    applyMobEffect(player, MobEffects.REGENERATION, 60, regen + 1, true, true);
                }
                if (player.getMainHandItem().getItem() instanceof ShieldItem || player.getOffhandItem().getItem() instanceof ShieldItem) {
                    applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 60, resistance + 1, true, true);
                }
            }
        }
        if (player.tickCount % 60 == 0) {
            if (sequenceLevel == 9) {
                applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 0, false, false);
                applyMobEffect(player, MobEffects.JUMP, 300, 0, false, false);
                speed = 0;
                resistance = -1;
                regen = -1;
                strength = -1;
            }
            if (sequenceLevel == 8) {
                applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 1, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 0, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 0, false, false);
                applyMobEffect(player, MobEffects.NIGHT_VISION, 300, 0, false, false);
                applyMobEffect(player, MobEffects.JUMP, 3000, 0, false, false);
                speed = 1;
                resistance = 0;
                regen = -1;
                strength = 0;
            } else if (sequenceLevel == 7) {
                applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 1, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 0, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300300, 1, false, false);
                applyMobEffect(player, MobEffects.NIGHT_VISION, 300, 0, false, false);
                applyMobEffect(player, MobEffects.JUMP, 300, 0, false, false);
                speed = 1;
                resistance = 0;
                regen = -1;
                strength = 1;
            } else if (sequenceLevel == 6) {
                applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 1, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 0, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 1, false, false);
                applyMobEffect(player, MobEffects.NIGHT_VISION, 300, 0, false, false);
                applyMobEffect(player, MobEffects.FIRE_RESISTANCE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.JUMP, 300, 1, false, false);
                speed = 1;
                resistance = 0;
                regen = -1;
                strength = 1;
            } else if (sequenceLevel == 5) {
                applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 2, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 0, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 2, false, false);
                applyMobEffect(player, MobEffects.NIGHT_VISION, 300, 0, false, false);
                applyMobEffect(player, MobEffects.FIRE_RESISTANCE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.WATER_BREATHING, 300, 1, false, false);
                applyMobEffect(player, MobEffects.JUMP, 300, 1, false, false);
                speed = 2;
                resistance = 0;
                regen = -1;
                strength = 2;
            } else if (sequenceLevel == 4) {
                applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 2, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 0, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 3, false, false);
                applyMobEffect(player, MobEffects.NIGHT_VISION, 300, 0, false, false);
                applyMobEffect(player, MobEffects.FIRE_RESISTANCE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.WATER_BREATHING, 300, 2, false, false);
                applyMobEffect(player, MobEffects.JUMP, 300, 1, false, false);
                speed = 2;
                resistance = 0;
                regen = -1;
                strength = 3;
            } else if (sequenceLevel == 3) {
                applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 2, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 0, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 3, false, false);
                applyMobEffect(player, MobEffects.NIGHT_VISION, 300, 0, false, false);
                applyMobEffect(player, MobEffects.FIRE_RESISTANCE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.WATER_BREATHING, 300, 2, false, false);
                applyMobEffect(player, MobEffects.JUMP, 300, 1, false, false);
                speed = 2;
                resistance = 0;
                regen = -1;
                strength = 3;
            } else if (sequenceLevel == 2) {
                applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 3, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 3, false, false);
                applyMobEffect(player, MobEffects.NIGHT_VISION, 300, 0, false, false);
                applyMobEffect(player, MobEffects.FIRE_RESISTANCE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.WATER_BREATHING, 300, 2, false, false);
                applyMobEffect(player, MobEffects.JUMP, 300, 1, false, false);
                speed = 3;
                resistance = 1;
                regen = -1;
                strength = 3;
            } else if (sequenceLevel == 1) {
                applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 3, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 4, false, false);
                applyMobEffect(player, MobEffects.NIGHT_VISION, 300, 0, false, false);
                applyMobEffect(player, MobEffects.FIRE_RESISTANCE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.WATER_BREATHING, 300, 2, false, false);
                applyMobEffect(player, MobEffects.JUMP, 300, 1, false, false);
                speed = 3;
                resistance = 1;
                regen = -1;
                strength = 4;
            } else if (sequenceLevel == 0) {
                applyMobEffect(player, MobEffects.MOVEMENT_SPEED, 300, 3, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_RESISTANCE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.DAMAGE_BOOST, 300, 4, false, false);
                applyMobEffect(player, MobEffects.NIGHT_VISION, 300, 0, false, false);
                applyMobEffect(player, MobEffects.FIRE_RESISTANCE, 300, 1, false, false);
                applyMobEffect(player, MobEffects.WATER_BREATHING, 300, 2, false, false);
                applyMobEffect(player, MobEffects.JUMP, 300, 1, false, false);
                speed = 3;
                resistance = 1;
                regen = -1;
                strength = 4;
            }
        }
    }

    @Override
    public Multimap<Integer, Item> getItems() {
        HashMultimap<Integer, Item> items = HashMultimap.create();
        items.put(9, ItemInit.BEYONDER_ABILITY_USER.get());
        items.put(9, ItemInit.ALLY_MAKER.get());
        items.put(9, ItemInit.SPIRITVISION.get());
        items.put(9, ItemInit.MONSTERDANGERSENSE.get());

        items.put(8, ItemInit.MONSTERPROJECTILECONTROL.get());

        items.put(7, ItemInit.LUCKPERCEPTION.get());

        items.put(6, ItemInit.PSYCHESTORM.get());

        items.put(5, ItemInit.LUCK_MANIPULATION.get());
        items.put(5, ItemInit.LUCKDEPRIVATION.get());
        items.put(5, ItemInit.LUCKGIFTING.get());
        items.put(5, ItemInit.MISFORTUNEBESTOWAL.get());
        items.put(5, ItemInit.LUCKFUTURETELLING.get());

        items.put(4, ItemInit.DECAYDOMAIN.get());
        items.put(4, ItemInit.PROVIDENCEDOMAIN.get());
        items.put(4, ItemInit.LUCKCHANNELING.get());
        items.put(4, ItemInit.LUCKDENIAL.get());
        items.put(4, ItemInit.MISFORTUNEMANIPULATION.get());
        items.put(4, ItemInit.MONSTERCALAMITYATTRACTION.get());

        items.put(3, ItemInit.CALAMITYINCARNATION.get());
        items.put(3, ItemInit.ENABLEDISABLERIPPLE.get());
        items.put(3, ItemInit.AURAOFCHAOS.get());
        items.put(3, ItemInit.CHAOSWALKERCOMBAT.get());
        items.put(3, ItemInit.MISFORTUNEREDIRECTION.get());
        items.put(3, ItemInit.MONSTERDOMAINTELEPORATION.get());

        items.put(2, ItemInit.WHISPEROFCORRUPTION.get());
        items.put(2, ItemInit.FORTUNEAPPROPIATION.get());
        items.put(2, ItemInit.FALSEPROPHECY.get());
        items.put(2, ItemInit.MISFORTUNEIMPLOSION.get());

        items.put(1, ItemInit.MONSTERREBOOT.get());
        items.put(1, ItemInit.FATEREINCARNATION.get());
        items.put(1, ItemInit.CYCLEOFFATE.get());
        items.put(1, ItemInit.CHAOSAMPLIFICATION.get());
        items.put(1, ItemInit.FATEDCONNECTION.get());
        items.put(1, ItemInit.REBOOTSELF.get());

        items.put(0, ItemInit.PROBABILITYMISFORTUNE.get());
        items.put(0, ItemInit.PROBABILITYFORTUNE.get());
        items.put(0, ItemInit.PROBABILITYFORTUNEINCREASE.get());
        items.put(0, ItemInit.PROBABILITYMISFORTUNEINCREASE.get());
        items.put(0, ItemInit.PROBABILITYWIPE.get());
        items.put(0, ItemInit.PROBABILITYEFFECT.get());
        items.put(0, ItemInit.PROBABILITYINFINITEFORTUNE.get());
        items.put(0, ItemInit.PROBABILITYINFINITEMISFORTUNE.get());

        return items;
    }

    @Override
    public ChatFormatting getColorFormatting() {
        return ChatFormatting.GRAY;
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

    public static void monsterLuckPoisonAttacker(LivingEntity pPlayer) {
        if (pPlayer.tickCount % 100 == 0) {
            if (pPlayer.getPersistentData().getInt("luckAttackerPoisoned") >= 1) {
                for (LivingEntity livingEntity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().inflate(50))) {
                    if (livingEntity != pPlayer) {
                        if (livingEntity.getPersistentData().getInt("attackedMonster") >= 1) {
                            livingEntity.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(), 60, 1, false, false));
                            livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 2, true, true));
                            livingEntity.getPersistentData().putInt("attackedMonster", 0);
                            pPlayer.getPersistentData().putInt("luckAttackerPoisoned", pPlayer.getPersistentData().getInt("luckAttackerPoisoned") - 1);
                        }
                    }
                }
            }
        }
    }

    public static void monsterLuckIgnoreMobs(LivingEntity pPlayer) {
        if (pPlayer.tickCount % 40 == 0) {
            if (pPlayer.getPersistentData().getInt("luckIgnoreMobs") >= 1) {
                for (Mob mob : pPlayer.level().getEntitiesOfClass(Mob.class, pPlayer.getBoundingBox().inflate(20))) {
                    if (mob.getTarget() == pPlayer) {
                        for (LivingEntity living : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().inflate(50))) {
                            if (living != null) {
                                mob.setTarget(living);
                            } else
                                mob.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(), 60, 1, false, false));
                        }
                        pPlayer.getPersistentData().putInt("luckIgnoreMobs", pPlayer.getPersistentData().getInt("luckIgnoreMobs") - 1);
                    }
                }
            }
        }
    }

    public static void decrementMonsterAttackEvent(LivingEntity livingEntity) {
        if (livingEntity.getPersistentData().getInt("attackedMonster") >= 1) {
            livingEntity.getPersistentData().putInt("attackedMonster", livingEntity.getPersistentData().getInt("attackedMonster") - 1);
        }
    }

    public static void monsterDodgeAttack(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        DamageSource source = event.getSource();
        Entity entitySource = source.getEntity();
        if (!livingEntity.level().isClientSide() && BeyonderUtil.isBeyonderCapable(livingEntity)) {
            if ((entitySource != null && entitySource != livingEntity) && !source.is(DamageTypes.CRAMMING) && !source.is(DamageTypes.STARVE) && !source.is(DamageTypes.FALL) && !source.is(DamageTypes.DROWN) && !source.is(DamageTypes.FELL_OUT_OF_WORLD) && !source.is(DamageTypes.ON_FIRE)) {
                if (BeyonderUtil.currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.MONSTER.get())) {
                    int randomChance = (int) ((Math.random() * 20) - BeyonderUtil.getSequence(livingEntity));
                    if (randomChance >= 13) {
                        double amount = event.getAmount();
                        double x = 0;
                        double z = 0;
                        Random random = new Random();
                        if (random.nextInt(2) == 0) {
                            x = Math.min(3, amount * -0.15);
                            z = Math.min(3, amount * -0.15);
                        } else {
                            x = Math.min(3, amount * 0.15);
                            z = Math.min(3, amount * 0.15);
                        }
                        livingEntity.setDeltaMovement(x, 1, z);
                        livingEntity.hurtMarked = true;
                        event.setAmount(0);
                        livingEntity.sendSystemMessage(Component.literal("A breeze of wind moved you out of the way of damage").withStyle(ChatFormatting.GREEN));
                    }
                }
            }
        }
    }

    public static void dodgeProjectiles(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity.getPersistentData().getInt("windMovingProjectilesCounter") >= 1) {
                for (Projectile projectile : livingEntity.level().getEntitiesOfClass(Projectile.class, livingEntity.getBoundingBox().inflate(100))) {
                    if (projectile.getPersistentData().getInt("windDodgeProjectilesCounter") == 0) {
                        if (projectile instanceof Arrow arrow && arrow.tickCount >= 100) {
                            return;
                        }
                        float scale = ScaleTypes.BASE.getScaleData(projectile).getScale();
                        double maxDistance = 6 * scale;
                        double deltaX = Math.abs(projectile.getX() - livingEntity.getX());
                        double deltaY = Math.abs(projectile.getY() - livingEntity.getY());
                        double deltaZ = Math.abs(projectile.getZ() - livingEntity.getZ());
                        if ((deltaX <= maxDistance && deltaY <= maxDistance && deltaZ <= maxDistance) && projectile.getOwner() != livingEntity) {
                            double mathRandom = (Math.random() + .4) - 0.2;
                            double x = projectile.getDeltaMovement().x() + (mathRandom * scale);
                            double y = projectile.getDeltaMovement().y() + (mathRandom * scale);
                            double z = projectile.getDeltaMovement().z() + (mathRandom * scale);
                            projectile.setDeltaMovement(x, y, z);
                            projectile.hurtMarked = true;
                            projectile.getPersistentData().putInt("windDodgeProjectilesCounter", 100);
                            livingEntity.getPersistentData().putInt("windMovingProjectilesCounter", livingEntity.getPersistentData().getInt("windMovingProjectilesCounter") - 1);
                            if (livingEntity instanceof Player player) {
                                player.displayClientMessage(Component.literal("A gust of wind moved a projectile headed towards you").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN), true);
                            }
                        }
                    } else {
                        projectile.getPersistentData().putInt("windDodgeProjectilesCounter", projectile.getPersistentData().getInt("windDodgeProjectilesCounter") - 1);
                    }
                }
            } else {
                if (BeyonderUtil.isBeyonderCapable(livingEntity)) {
                    if (livingEntity instanceof Player pPlayer) {
                        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(pPlayer);
                        int sequence = holder.getSequence();
                        if (BeyonderUtil.currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.MONSTER.get()) && holder.getSequence() <= 7) {
                            int reverseChance = (int) (Math.random() * 20 - sequence);
                            for (Projectile projectile : livingEntity.level().getEntitiesOfClass(Projectile.class, livingEntity.getBoundingBox().inflate(100))) {
                                if (projectile.getPersistentData().getInt("monsterReverseProjectiles") == 0) {
                                    if (projectile instanceof Arrow arrow && arrow.tickCount >= 80) {
                                        return;
                                    }
                                    if (reverseChance >= 10) {
                                        float scale = ScaleTypes.BASE.getScaleData(projectile).getScale();
                                        double maxDistance = 6 * scale;
                                        double deltaX = Math.abs(projectile.getX() - livingEntity.getX());
                                        double deltaY = Math.abs(projectile.getY() - livingEntity.getY());
                                        double deltaZ = Math.abs(projectile.getZ() - livingEntity.getZ());
                                        if ((deltaX <= maxDistance && deltaY <= maxDistance && deltaZ <= maxDistance) && projectile.getOwner() != livingEntity) {
                                            double x = projectile.getDeltaMovement().x() * -1;
                                            double y = projectile.getDeltaMovement().y() * -1;
                                            double z = projectile.getDeltaMovement().z() * -1;
                                            projectile.setDeltaMovement(x, y, z);
                                            projectile.hurtMarked = true;
                                            projectile.getPersistentData().putInt("monsterReverseProjectiles", 60);
                                            if (livingEntity instanceof Player player) {
                                                player.displayClientMessage(Component.literal("A strong breeze luckily reversed a projectile headed towards you").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN), true);
                                            }
                                        }
                                    }
                                } else {
                                    projectile.getPersistentData().putInt("monsterReverseProjectiles", projectile.getPersistentData().getInt("windDodgeProjectilesCounter") - 1);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void showMonsterParticles(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide() && livingEntity.tickCount % 100 == 0) {
            if (livingEntity instanceof ServerPlayer serverPlayer) {
                BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(serverPlayer);
                if (holder.getSequence() <= 2 && BeyonderUtil.currentPathwayMatches(livingEntity, BeyonderClassInit.MONSTER.get())) {
                    for (LivingEntity entities : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(50))) {
                        if (entities != serverPlayer) {
                            CompoundTag tag = entities.getPersistentData();
                            int cantUseAbility = tag.getInt("cantUseAbility");
                            int meteor = tag.getInt("luckMeteor");
                            int lotmLightning = tag.getInt("luckLightningLOTM");
                            int paralysis = tag.getInt("luckParalysis");
                            int unequipArmor = tag.getInt("luckUnequipArmor");
                            int wardenSpawn = tag.getInt("luckWarden");
                            int mcLightning = tag.getInt("luckLightningMC");
                            int poison = tag.getInt("luckPoison");
                            int tornadoInt = tag.getInt("luckTornado");
                            int stone = tag.getInt("luckStone");
                            int doubleDamage = tag.getInt("luckDoubleDamage");
                            int calamityMeteor = tag.getInt("calamityMeteor");
                            int calamityLightningStorm = tag.getInt("calamityLightningStorm");
                            int calamityLightningBolt = tag.getInt("calamityLightningBolt");
                            int calamityGroundTremor = tag.getInt("calamityGroundTremor");
                            int calamityGaze = tag.getInt("calamityGaze");
                            int calamityUndeadArmy = tag.getInt("calamityUndeadArmy");
                            int calamityBabyZombie = tag.getInt("calamityBabyZombie");
                            int calamityWindArmorRemoval = tag.getInt("calamityWindArmorRemoval");
                            int calamityBreeze = tag.getInt("calamityBreeze");
                            int calamityWave = tag.getInt("calamityWave");
                            int calamityExplosion = tag.getInt("calamityExplosion");
                            int calamityTornado = tag.getInt("calamityTornado");
                            int ignoreDamage = tag.getInt("luckIgnoreDamage");
                            int diamonds = tag.getInt("luckDiamonds");
                            int regeneration = tag.getInt("luckRegeneration");
                            int moveProjectiles = tag.getInt("windMovingProjectilesCounter");
                            int halveDamage = tag.getInt("luckHalveDamage");
                            int ignoreMobs = tag.getInt("luckIgnoreMobs");
                            int luckAttackerPoisoned = tag.getInt("luckAttackerPoisoned");
                            if (cantUseAbility >= 1) {
                                for (int i = 0; i < cantUseAbility; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.CANT_USE_ABILITY_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (meteor >= 1) {
                                int particleCount = Math.max(1, (int) 20 - (meteor / 2));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.METEOR_CALAMITY_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (lotmLightning >= 1) {
                                int particleCount = Math.max(1, (int) 15 - (lotmLightning));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.LOTM_LIGHTNING_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (paralysis >= 1) {
                                int particleCount = Math.max(1, (int) 15 - (paralysis));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.TRIP_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (unequipArmor >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (unequipArmor));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.WIND_UNEQUIP_ARMOR_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (wardenSpawn >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (wardenSpawn / 1.5));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.WARDEN_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (mcLightning >= 1) {
                                for (int i = 0; i < mcLightning; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.MC_LIGHTNING_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (poison >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (poison / 0.75));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.POISON_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (tornadoInt >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (tornadoInt * 0.75));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.TORNADO_CALAMITY_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (stone >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (tornadoInt / 0.5));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.FALLING_STONE_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (doubleDamage >= 1) {
                                for (int i = 0; i < doubleDamage; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.DOUBLE_DAMAGE_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityMeteor >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (calamityMeteor / 3.5));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.METEOR_CALAMITY_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityLightningStorm >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (calamityLightningStorm / 2.5));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.LIGHTNING_STORM_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityLightningBolt >= 1) {
                                int particleCount = Math.max(1, (int) 20 - (calamityLightningBolt * 2));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.LOTM_LIGHTNING_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityGroundTremor >= 1) {
                                int particleCount = Math.max(1, (int) 20 - (calamityGroundTremor / 2));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.GROUND_TREMOR_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityGaze >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (calamityGaze / 2.5));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.GOO_GAZE_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityUndeadArmy >= 1) {
                                int particleCount = Math.max(1, (int) 20 - (calamityUndeadArmy));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.UNDEAD_ARMY_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityBabyZombie >= 1) {
                                int particleCount = Math.max(1, (int) 20 - (calamityBabyZombie));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.BABY_ZOMBIE_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityWindArmorRemoval >= 1) {
                                int particleCount = Math.max(1, (int) 20 - (calamityWindArmorRemoval / 2));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.WIND_UNEQUIP_ARMOR_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityBreeze >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (calamityBreeze / 1.25));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.BREEZE_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityWave >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (calamityWave / 1.25));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.HEAT_WAVE_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityExplosion >= 1) {
                                int particleCount = Math.max(1, (int) 20 - (calamityExplosion / 3));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.EXPLOSION_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (calamityTornado >= 1) {
                                int particleCount = (int) Math.max(1, (int) 20 - (calamityTornado / 3.5));
                                for (int i = 0; i < particleCount; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.TORNADO_CALAMITY_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (ignoreDamage >= 1) {
                                for (int i = 0; i < ignoreDamage; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.IGNORE_DAMAGE_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (diamonds >= 1) {
                                for (int i = 0; i < diamonds; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.DIAMOND_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (regeneration >= 1) {
                                for (int i = 0; i < regeneration; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.REGENERATION_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (moveProjectiles >= 1) {
                                for (int i = 0; i < moveProjectiles; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.WIND_MOVE_PROJECTILES_PARTICLES.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (halveDamage >= 1) {
                                for (int i = 0; i < halveDamage; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.HALF_DAMAGE_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (ignoreMobs >= 1) {
                                for (int i = 0; i < ignoreMobs; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.IGNORE_MOBS_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                            if (luckAttackerPoisoned >= 1) {
                                for (int i = 0; i < luckAttackerPoisoned; i++) {
                                    double offsetX = entities.getX() + (Math.random() - 0.5) * 2;
                                    double offsetY = entities.getY() + Math.random();
                                    double offsetZ = entities.getZ() + (Math.random() - 0.5) * 2;
                                    LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.ATTACKER_POISONED_PARTICLE.get(), offsetX, offsetY, offsetZ, 0, 0, 0), serverPlayer);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void checkForProjectiles(Player player) {
        Level level = player.level();
        for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(100))) {
            boolean bothInSameDimension = projectile.getPersistentData().getBoolean("inSpiritWorld") == player.getPersistentData().getBoolean("inSpiritWorld");
            //if (bothInSameDimension) {
            List<Vec3> trajectory = predictProjectileTrajectory(projectile, player);
            float scale = ScaleTypes.BASE.getScaleData(projectile).getScale();
            double maxDistance = 20 * scale;
            double deltaX = Math.abs(projectile.getX() - player.getX());
            double deltaY = Math.abs(projectile.getY() - player.getY());
            double deltaZ = Math.abs(projectile.getZ() - player.getZ());
            if (deltaX <= maxDistance || deltaY <= maxDistance || deltaZ <= maxDistance) {
                if (player.level() instanceof ServerLevel serverLevel) {
                    drawParticleLine(serverLevel, (ServerPlayer) player, trajectory);
                }
            }
        }
        //}
    }

    public static void drawParticleLine(ServerLevel level, ServerPlayer player, List<Vec3> points) {
        int particleInterval = 2; // Only spawn a particle every 5 points
        for (int i = 0; i < points.size() - 1; i += particleInterval) {
            Vec3 start = points.get(i);
            Vec3 end = i + particleInterval < points.size() ? points.get(i + particleInterval) : points.get(points.size() - 1);
            Vec3 direction = end.subtract(start).normalize();
            double distance = start.distanceTo(end);
            Vec3 particlePosition = start.add(direction.scale(distance / 2));
            LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(DustParticleOptions.REDSTONE, particlePosition.x, particlePosition.y, particlePosition.z, 0, 0, 0), player);
        }
    }

    public static List<Vec3> predictProjectileTrajectory(Projectile projectile, LivingEntity player) {
        List<Vec3> trajectory = new ArrayList<>();
        Vec3 projectilePos = projectile.position();
        Vec3 projectileDelta = projectile.getDeltaMovement();
        Level level = projectile.level();
        boolean isArrow = projectile instanceof AbstractArrow;
        trajectory.add(projectilePos);
        int maxIterations = 1000;
        double maxDistance = 100.0;

        for (int i = 0; i < maxIterations; i++) {
            projectilePos = projectilePos.add(projectileDelta);
            trajectory.add(projectilePos);
            if (projectilePos.distanceTo(projectile.position()) > maxDistance) {
                break;
            }
            if (isArrow && projectile instanceof AbstractArrow arrow && arrow.tickCount <= 100) {
                projectileDelta = projectileDelta.scale(0.99F);
                projectileDelta = projectileDelta.add(0, -0.05, 0);
            } else {
                projectileDelta = projectileDelta.scale(0.99F);
                projectileDelta = projectileDelta.add(0, -0.03, 0);
            }
        }

        return trajectory;
    }

    public static void calamityUndeadArmy(LivingEntity pPlayer) {
        CompoundTag tag = pPlayer.getPersistentData();
        if (pPlayer.level() instanceof ServerLevel serverLevel) {
            int enhancement = CalamityEnhancementData.getInstance(serverLevel).getCalamityEnhancement();
            int x = tag.getInt("calamityUndeadArmyX");
            int y = tag.getInt("calamityUndeadArmyY");
            int z = tag.getInt("calamityUndeadArmyZ");
            int subtractX = (int) (x - pPlayer.getX());
            int subtractY = (int) (y - pPlayer.getY());
            int subtractZ = (int) (z - pPlayer.getZ());
            int surfaceY = pPlayer.level().getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
            int undeadArmyCounter = tag.getInt("calamityUndeadArmyCounter");
            if (undeadArmyCounter >= 1) {
                for (int i = 0; i < enhancement; i++) {
                    Random random = new Random();
                    ItemStack leatherHelmet = new ItemStack(Items.LEATHER_HELMET);
                    ItemStack leatherChestplate = new ItemStack(Items.LEATHER_CHESTPLATE);
                    ItemStack leatherLeggings = new ItemStack(Items.LEATHER_LEGGINGS);
                    ItemStack leatherBoots = new ItemStack(Items.LEATHER_BOOTS);
                    ItemStack ironHelmet = new ItemStack(Items.IRON_HELMET);
                    ItemStack ironChestplate = new ItemStack(Items.IRON_CHESTPLATE);
                    ItemStack ironLeggings = new ItemStack(Items.IRON_LEGGINGS);
                    ItemStack ironBoots = new ItemStack(Items.IRON_BOOTS);
                    ItemStack diamondHelmet = new ItemStack(Items.DIAMOND_HELMET);
                    ItemStack diamondChestplate = new ItemStack(Items.DIAMOND_CHESTPLATE);
                    ItemStack diamondLeggings = new ItemStack(Items.DIAMOND_LEGGINGS);
                    ItemStack diamondBoots = new ItemStack(Items.DIAMOND_BOOTS);
                    ItemStack netheriteHelmet = new ItemStack(Items.NETHERITE_HELMET);
                    ItemStack netheriteChestplate = new ItemStack(Items.NETHERITE_CHESTPLATE);
                    ItemStack netheriteLeggings = new ItemStack(Items.NETHERITE_LEGGINGS);
                    ItemStack netheriteBoots = new ItemStack(Items.NETHERITE_BOOTS);
                    ItemStack enchantedBow = new ItemStack(Items.BOW);
                    ItemStack woodSword = new ItemStack(Items.WOODEN_SWORD);
                    ItemStack ironSword = new ItemStack(Items.IRON_SWORD);
                    ItemStack diamondSword = new ItemStack(Items.DIAMOND_SWORD);
                    ItemStack netheriteSword = new ItemStack(Items.NETHERITE_SWORD);
                    Zombie zombie = new Zombie(EntityType.ZOMBIE, pPlayer.level());
                    Skeleton skeleton = new Skeleton(EntityType.SKELETON, pPlayer.level());
                    int randomPos = (int) ((Math.random() * 24) - 12);
                    if (random.nextInt(10) == 10) {
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(zombie);
                    }
                    if (random.nextInt(10) == 9) {
                        zombie.setPos(x + randomPos, surfaceY, z + randomPos);
                        zombie.setItemSlot(EquipmentSlot.HEAD, leatherHelmet);
                        zombie.setItemSlot(EquipmentSlot.CHEST, leatherChestplate);
                        zombie.setItemSlot(EquipmentSlot.LEGS, leatherLeggings);
                        zombie.setItemSlot(EquipmentSlot.FEET, leatherBoots);
                        zombie.setItemSlot(EquipmentSlot.MAINHAND, woodSword);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(zombie);
                    }
                    if (random.nextInt(10) == 8) {
                        zombie.setPos(x + randomPos, surfaceY, z + randomPos);
                        zombie.setItemSlot(EquipmentSlot.HEAD, ironHelmet);
                        zombie.setItemSlot(EquipmentSlot.CHEST, ironChestplate);
                        zombie.setItemSlot(EquipmentSlot.LEGS, ironLeggings);
                        zombie.setItemSlot(EquipmentSlot.FEET, ironBoots);
                        zombie.setItemSlot(EquipmentSlot.MAINHAND, ironSword);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(zombie);
                    }
                    if (random.nextInt(10) == 7) {
                        zombie.setPos(x + randomPos, surfaceY, z + randomPos);
                        zombie.setItemSlot(EquipmentSlot.HEAD, diamondHelmet);
                        zombie.setItemSlot(EquipmentSlot.CHEST, diamondChestplate);
                        zombie.setItemSlot(EquipmentSlot.LEGS, diamondLeggings);
                        zombie.setItemSlot(EquipmentSlot.FEET, diamondBoots);
                        zombie.setItemSlot(EquipmentSlot.MAINHAND, diamondSword);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(zombie);
                    }
                    if (random.nextInt(10) == 6) {
                        zombie.setPos(x + randomPos, surfaceY, z + randomPos);
                        zombie.setItemSlot(EquipmentSlot.HEAD, netheriteHelmet);
                        zombie.setItemSlot(EquipmentSlot.CHEST, netheriteChestplate);
                        zombie.setItemSlot(EquipmentSlot.LEGS, netheriteLeggings);
                        zombie.setItemSlot(EquipmentSlot.FEET, netheriteBoots);
                        zombie.setItemSlot(EquipmentSlot.MAINHAND, netheriteSword);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(zombie);
                    }
                    if (random.nextInt(20) == 5) {
                        skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                        skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(skeleton);
                    }
                    if (random.nextInt(20) == 4) {
                        skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                        skeleton.setItemSlot(EquipmentSlot.HEAD, leatherHelmet);
                        skeleton.setItemSlot(EquipmentSlot.CHEST, leatherChestplate);
                        skeleton.setItemSlot(EquipmentSlot.LEGS, leatherLeggings);
                        skeleton.setItemSlot(EquipmentSlot.FEET, leatherBoots);
                        enchantedBow.enchant(Enchantments.POWER_ARROWS, 1);
                        skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(skeleton);
                    }
                    if (random.nextInt(20) == 3) {
                        skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                        skeleton.setItemSlot(EquipmentSlot.HEAD, ironHelmet);
                        skeleton.setItemSlot(EquipmentSlot.CHEST, ironChestplate);
                        skeleton.setItemSlot(EquipmentSlot.LEGS, ironLeggings);
                        skeleton.setItemSlot(EquipmentSlot.FEET, ironBoots);
                        enchantedBow.enchant(Enchantments.POWER_ARROWS, 2);
                        skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(skeleton);
                    }
                    if (random.nextInt(20) == 2) {
                        skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                        skeleton.setItemSlot(EquipmentSlot.HEAD, diamondHelmet);
                        skeleton.setItemSlot(EquipmentSlot.CHEST, diamondChestplate);
                        skeleton.setItemSlot(EquipmentSlot.LEGS, diamondLeggings);
                        skeleton.setItemSlot(EquipmentSlot.FEET, diamondBoots);
                        enchantedBow.enchant(Enchantments.POWER_ARROWS, 3);
                        skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(skeleton);
                    }
                    if (random.nextInt(20) == 1) {
                        skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                        skeleton.setItemSlot(EquipmentSlot.HEAD, netheriteHelmet);
                        skeleton.setItemSlot(EquipmentSlot.CHEST, netheriteChestplate);
                        skeleton.setItemSlot(EquipmentSlot.LEGS, netheriteLeggings);
                        skeleton.setItemSlot(EquipmentSlot.FEET, netheriteBoots);
                        enchantedBow.enchant(Enchantments.POWER_ARROWS, 4);
                        skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                            if (BeyonderUtil.currentPathwayMatchesNoException(pPlayer, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(pPlayer) <= 6) {
                                if (entity != null) {
                                    zombie.setTarget(entity);
                                }
                            }
                        }
                        pPlayer.level().addFreshEntity(skeleton);
                    }
                    zombie.setDropChance(EquipmentSlot.HEAD, 0.0F);
                    zombie.setDropChance(EquipmentSlot.CHEST, 0.0F);
                    zombie.setDropChance(EquipmentSlot.LEGS, 0.0F);
                    zombie.setDropChance(EquipmentSlot.FEET, 0.0F);
                    skeleton.setDropChance(EquipmentSlot.HEAD, 0.0F);
                    skeleton.setDropChance(EquipmentSlot.CHEST, 0.0F);
                    skeleton.setDropChance(EquipmentSlot.LEGS, 0.0F);
                    skeleton.setDropChance(EquipmentSlot.FEET, 0.0F);
                    tag.putInt("calamityUndeadArmyCounter", tag.getInt("calamityUndeadArmyCounter") - 1);
                }
            }
        }
    }

    public static void calamityExplosion(LivingEntity livingEntity) {
        CompoundTag tag = livingEntity.getPersistentData();
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            int x = tag.getInt("calamityExplosionOccurrence");
            if (x >= 1 && livingEntity.tickCount % 20 == 0 && !livingEntity.level().isClientSide()) {
                int explosionX = tag.getInt("calamityExplosionX");
                int explosionY = tag.getInt("calamityExplosionY");
                int explosionZ = tag.getInt("calamityExplosionZ");
                int subtractX = explosionX - (int) livingEntity.getX();
                int subtractY = explosionY - (int) livingEntity.getY();
                int subtractZ = explosionZ - (int) livingEntity.getZ();
                tag.putInt("calamityExplosionOccurrence", x - 1);
                for (LivingEntity entity : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(15))) {
                    if (BeyonderUtil.currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.MONSTER.get()) && BeyonderUtil.getSequence(livingEntity) <= 3) {
                        entity.getPersistentData().putInt("calamityExplosionImmunity", 2);
                    }
                }
            }
            if (x == 1) {
                int explosionX = tag.getInt("calamityExplosionX");
                int explosionY = tag.getInt("calamityExplosionY");
                int explosionZ = tag.getInt("calamityExplosionZ");
                int data = CalamityEnhancementData.getInstance(serverLevel).getCalamityEnhancement();
                livingEntity.level().playSound(null, explosionX, explosionY, explosionZ, SoundEvents.GENERIC_EXPLODE, SoundSource.AMBIENT, 5.0F, 5.0F);
                Explosion explosion = new Explosion(livingEntity.level(), null, explosionX, explosionY, explosionZ, 10.0F + (data * 3), true, Explosion.BlockInteraction.DESTROY);
                explosion.explode();
                explosion.finalizeExplosion(true);
                tag.putInt("calamityExplosionOccurrence", 0);
            }
        }
    }
}
