package net.swimmingtuna.lotm.util;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.*;
import net.swimmingtuna.lotm.events.ability_events.ModEvents;
import net.swimmingtuna.lotm.events.custom_events.ModEventFactory;
import net.swimmingtuna.lotm.events.custom_events.ProjectileEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.SoundInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.*;
import net.swimmingtuna.lotm.util.effect.ModEffects;

import java.util.*;

import static net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.SirenSongStrengthen.isInsideSphere;

public class TickEventUtil {


    private static void lightningStorm(PlayerMobEntity player, CompoundTag playerPersistentData) {
        //LIGHTNING STORM
        double distance = player.getPersistentData().getDouble("sailorLightningStormDistance");
        if (distance > 300) {
            playerPersistentData.putDouble("sailorLightningStormDistance", 0);
        }
        int tyrantVer = playerPersistentData.getInt("sailorLightningStormTyrant");
        int sailorMentioned = playerPersistentData.getInt("tyrantMentionedInChat");
        int sailorLightningStorm1 = playerPersistentData.getInt("sailorLightningStorm1");
        int x1 = playerPersistentData.getInt("sailorStormVecX1");
        int y1 = playerPersistentData.getInt("sailorStormVecY1");
        int z1 = playerPersistentData.getInt("sailorStormVecZ1");
        if (sailorMentioned >= 1) {
            playerPersistentData.putInt("tyrantMentionedInChat", sailorMentioned - 1);
            if (sailorLightningStorm1 >= 1) {
                for (int i = 0; i < (tyrantVer >= 1 ? 8 : 4); i++) {
                    LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), player.level());
                    lightningEntity.setSpeed(10.0f);
                    lightningEntity.setDeltaMovement((Math.random() * 0.4) - 0.2, -4, (Math.random() * 0.4) - 0.2);
                    lightningEntity.setMaxLength(30);
                    lightningEntity.setOwner(player);
                    lightningEntity.setNoUp(true);
                    lightningEntity.teleportTo(x1 + ((Math.random() * 300) - (double) 300 / 2), y1 + 80, z1 + ((Math.random() * 300) - (double) 300 / 2));
                    player.level().addFreshEntity(lightningEntity);
                }
                if (tyrantVer >= 1) {
                    playerPersistentData.putInt("sailorLightningStormTyrant", tyrantVer - 1);
                }
                playerPersistentData.putInt("sailorLightningStorm1", sailorLightningStorm1 - 1);
            }
        }
        int sailorLightningStorm = playerPersistentData.getInt("sailorLightningStorm");
        int stormVec = playerPersistentData.getInt("sailorStormVec");
        double sailorStormVecX = playerPersistentData.getInt("sailorStormVecX");
        double sailorStormVecY = playerPersistentData.getInt("sailorStormVecY");
        double sailorStormVecZ = playerPersistentData.getInt("sailorStormVecZ");
        if (sailorLightningStorm >= 1) {
            for (int i = 0; i < 4; i++) {
                LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), player.level());
                lightningEntity.setSpeed(10.0f);
                lightningEntity.setDeltaMovement((Math.random() * 0.4) - 0.2, -4, (Math.random() * 0.4) - 0.2);
                lightningEntity.setMaxLength(30);
                lightningEntity.setOwner(player);
                lightningEntity.setNoUp(true);
                lightningEntity.teleportTo(sailorStormVecX + ((Math.random() * distance) - distance / 2), sailorStormVecY + 80, sailorStormVecZ + ((Math.random() * distance) - distance / 2));
                player.level().addFreshEntity(lightningEntity);
            }
            playerPersistentData.putInt("sailorLightningStorm", sailorLightningStorm - 1);
        }
        if (player.getCurrentPathway() == BeyonderClassInit.SAILOR && player.getCurrentSequence() <= 3) {
            playerPersistentData.putInt("sailorStormVec", 50);
            if (stormVec > 300) {
                playerPersistentData.putInt("sailorStormVec", 0);
            }
        }
    }

    private static void matterAccelerationSelf(PlayerMobEntity player) {
        //MATTER ACCELERATION SELF
        if (player.isSpectator()) return;
        int matterAccelerationDistance = player.getPersistentData().getInt("tyrantSelfAcceleration");
        int blinkDistance = player.getPersistentData().getInt("BlinkDistance");
        if (player.getTarget() != null) {
            int distance = (int) player.getTarget().distanceTo(player);
            player.getPersistentData().putInt("tyrantSelfAcceleration", distance + (distance / 10));
            player.getPersistentData().putInt("BlinkDistance", distance);
        }
        if (player.tickCount % 500 == 0) {
            if (player.getTarget() == null) {
                player.getPersistentData().putInt("BlinkDistance", 200);
            }
        }
        if (matterAccelerationDistance >= 1000) {
            player.getPersistentData().putInt("tyrantSelfAcceleration", 0);
        }
        if (blinkDistance > 200) {
            player.getPersistentData().putInt("BlinkDistance", 0);
        }
    }

    private static void acidicRain(PlayerMobEntity player, int sequence) {
        //ACIDIC RAIN
        int acidicRain = player.getPersistentData().getInt("sailorAcidicRain");
        if (acidicRain <= 0) {
            return;
        }
        AcidicRain.spawnAcidicRainParticlesPM(player);
        player.getPersistentData().putInt("sailorAcidicRain", acidicRain + 1);
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
    }

    public static List<BlockPos> getBlockPos(BlockPos playerPos) {
        int radius = 20;
        List<BlockPos> blocksInRadius = new ArrayList<>();

        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos newPos = playerPos.offset(x, y, z);
                    if (playerPos.distSqr(newPos) <= radius * radius) {
                        blocksInRadius.add(newPos);
                    }
                }
            }
        }
        return blocksInRadius;
    }


    private static void projectileEvent(PlayerMobEntity player) {
        //PROJECTILE EVENT
        Projectile projectile = BeyonderUtil.getLivingEntitiesProjectile(player);
        if (projectile == null) return;
        ProjectileEvent.ProjectileControlEvent projectileEvent = new ProjectileEvent.ProjectileControlEvent(projectile);
        ModEventFactory.onSailorShootProjectile(projectile);

        //MATTER ACCELERATION ENTITIES
        if (projectile.getPersistentData().getInt("matterAccelerationEntities") >= 10) {
            double movementX = Math.abs(projectile.getDeltaMovement().x());
            double movementY = Math.abs(projectile.getDeltaMovement().y());
            double movementZ = Math.abs(projectile.getDeltaMovement().z());
            if (movementX >= 6 || movementY >= 6 || movementZ >= 6) {
                ModEvents.removeBlockInRange(projectile);
                for (LivingEntity entity1 : projectile.level().getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(5))) {
                    if (entity1 instanceof Player playerEntity) {
                        if (player.getCurrentPathway() != BeyonderClassInit.SAILOR && player.getCurrentSequence() == 0) {
                            playerEntity.hurt(playerEntity.damageSources().lightningBolt(), 10);
                        }
                    } else {
                        entity1.hurt(entity1.damageSources().lightningBolt(), 10);
                    }
                }
            }
        }


        //SAILOR PASSIVE CHECK FROM HERE
        LivingEntity target = projectileEvent.getTarget(75, 0);
        if (target != null) {
            if (player.getCurrentPathway() == BeyonderClassInit.SAILOR && player.getCurrentSequence() <= 7 && player.getPersistentData().getBoolean("sailorProjectileMovement")) {
                projectileEvent.addMovement(projectile, (target.getX() - projectile.getX()) * 0.1, (target.getY() - projectile.getY()) * 0.1, (target.getZ() - projectile.getZ()) * 0.1);
                projectile.hurtMarked = true;
            }
        }

        //MONSTER CALCULATION PASSIVE
        if (target != null) {
            if (player.getCurrentPathway() == BeyonderClassInit.MONSTER && player.getCurrentSequence() <= 8 && player.getPersistentData().getBoolean("monsterProjectileControl")) {
                projectileEvent.addMovement(projectile, (target.getX() - projectile.getX()) * 0.1, (target.getY() - projectile.getY()) * 0.1, (target.getZ() - projectile.getZ()) * 0.1);
                projectile.hurtMarked = true;
            }
        }

    }

    private static void windManipulationGuide(CompoundTag playerPersistentData, PlayerMobEntity player) {
        //WIND MANIPULATION GLIDE
        int regularFlight = playerPersistentData.getInt("sailorFlight");
        boolean enhancedFlight = playerPersistentData.getBoolean("sailorFlight1");
        if (
                player.getCurrentPathway() == BeyonderClassInit.SAILOR &&
                        player.getCurrentSequence() <= 7 &&
                        player.fallDistance >= 10 &&
                        player.getDeltaMovement().y() < 0 &&
                        !enhancedFlight &&
                        regularFlight == 0
        ) {
            Vec3 movement = player.getDeltaMovement();
            double deltaX = Math.cos(Math.toRadians(player.getYRot() + 90)) * 0.06;
            double deltaZ = Math.sin(Math.toRadians(player.getYRot() + 90)) * 0.06;
            player.setDeltaMovement(movement.x + deltaX, -0.05, movement.z + deltaZ);
            player.resetFallDistance();
            player.hurtMarked = true;
        }
    }

    public static void removeArmor(LivingEntity player) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                ItemStack armorStack = player.getItemBySlot(slot);
                if (!armorStack.isEmpty()) {
                    player.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
        }
    }



    private static void calamityIncarnationTornado(CompoundTag playerPersistentData, LivingEntity player) {
        //CALAMITY INCARNATION TORNADO
        if (playerPersistentData.getInt("calamityIncarnationTornado") >= 1) {
            playerPersistentData.putInt("calamityIncarnationTornado", player.getPersistentData().getInt("calamityIncarnationTornado") - 1);
        }
    }

    protected static void calamityUndeadArmy(PlayerMobEntity pPlayer) {
        CompoundTag tag = pPlayer.getPersistentData();
        int x = tag.getInt("calamityUndeadArmyX");
        int y = tag.getInt("calamityUndeadArmyY");
        int z = tag.getInt("calamityUndeadArmyZ");
        int subtractX = (int) (x - pPlayer.getX());
        int subtractY = (int) (y - pPlayer.getY());
        int subtractZ = (int) (z - pPlayer.getZ());
        int surfaceY = pPlayer.level().getHeight(Heightmap.Types.WORLD_SURFACE, x, z) + 1;
        int undeadArmyCounter = tag.getInt("calamityUndeadArmyCounter");
        if (undeadArmyCounter >= 1) {
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
            if (random.nextInt(11) == 10) {
                setZombieTarget(pPlayer, subtractX, subtractY, subtractZ, zombie);
            }
            if (random.nextInt(10) == 9) {
                giveMobItems(pPlayer, x, z, subtractX, subtractY, subtractZ, surfaceY, leatherHelmet, leatherChestplate, leatherLeggings, leatherBoots, woodSword, zombie, randomPos);
            }
            if (random.nextInt(10) == 8) {
                giveMobItems(pPlayer, x, z, subtractX, subtractY, subtractZ, surfaceY, ironHelmet, ironChestplate, ironLeggings, ironBoots, ironSword, zombie, randomPos);
            }
            if (random.nextInt(10) == 7) {
                giveMobItems(pPlayer, x, z, subtractX, subtractY, subtractZ, surfaceY, diamondHelmet, diamondChestplate, diamondLeggings, diamondBoots, diamondSword, zombie, randomPos);
            }
            if (random.nextInt(10) == 6) {
                giveMobItems(pPlayer, x, z, subtractX, subtractY, subtractZ, surfaceY, netheriteHelmet, netheriteChestplate, netheriteLeggings, netheriteBoots, netheriteSword, zombie, randomPos);
            }
            if (random.nextInt(20) == 5) {
                skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                setTargetOfZombie(pPlayer, subtractX, subtractY, subtractZ, zombie, skeleton);
            }
            if (random.nextInt(20) == 4) {
                giveSkeletonitems(x, z, surfaceY, leatherHelmet, leatherChestplate, leatherLeggings, leatherBoots, enchantedBow, skeleton, randomPos);
                setTargetOfZombie(pPlayer, subtractX, subtractY, subtractZ, zombie, skeleton);
            }
            giveSkeletonItems(pPlayer, x, z, subtractX, subtractY, subtractZ, surfaceY, random, ironHelmet, ironChestplate, ironLeggings, ironBoots, enchantedBow, zombie, skeleton, randomPos, 3, 2);
            giveSkeletonItems(pPlayer, x, z, subtractX, subtractY, subtractZ, surfaceY, random, diamondHelmet, diamondChestplate, diamondLeggings, diamondBoots, enchantedBow, zombie, skeleton, randomPos, 2, 3);
            giveSkeletonItems(pPlayer, x, z, subtractX, subtractY, subtractZ, surfaceY, random, netheriteHelmet, netheriteChestplate, netheriteLeggings, netheriteBoots, enchantedBow, zombie, skeleton, randomPos, 1, 4);
            setDropchanceZombieAndSkeleton(tag, zombie, skeleton);
        }
    }

    public static void setDropchanceZombieAndSkeleton(CompoundTag tag, Zombie zombie, Skeleton skeleton) {
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

    private static void giveSkeletonItems(PlayerMobEntity pPlayer, int x, int z, int subtractX, int subtractY, int subtractZ, int surfaceY, Random random, ItemStack netheriteHelmet, ItemStack netheriteChestplate, ItemStack netheriteLeggings, ItemStack netheriteBoots, ItemStack enchantedBow, Zombie zombie, Skeleton skeleton, int randomPos, int i, int i2) {
        if (random.nextInt(20) == i) {
            setItemsSkeleton(x, z, surfaceY, netheriteHelmet, netheriteChestplate, netheriteLeggings, netheriteBoots, enchantedBow, skeleton, randomPos, i2);
            setTargetOfZombie(pPlayer, subtractX, subtractY, subtractZ, zombie, skeleton);
        }
    }

    private static void setTargetOfZombie(PlayerMobEntity pPlayer, int subtractX, int subtractY, int subtractZ, Zombie zombie, Skeleton skeleton) {
        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
            if (pPlayer.getCurrentPathway() == BeyonderClassInit.MONSTER && pPlayer.getCurrentSequence() <= 6) {
                if (entity != null) {
                    zombie.setTarget(entity);
                }
            }
        }
        pPlayer.level().addFreshEntity(skeleton);
    }

    public static void setItemsSkeleton(int x, int z, int surfaceY, ItemStack helmet, ItemStack chestplate, ItemStack leggings, ItemStack boots, ItemStack enchantedBow, Skeleton skeleton, int randomPos, int enchantLVL) {
        skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
        skeleton.setItemSlot(EquipmentSlot.HEAD, helmet);
        skeleton.setItemSlot(EquipmentSlot.CHEST, chestplate);
        skeleton.setItemSlot(EquipmentSlot.LEGS, leggings);
        skeleton.setItemSlot(EquipmentSlot.FEET, boots);
        enchantedBow.enchant(Enchantments.POWER_ARROWS, enchantLVL);
        skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
    }

    public static void giveSkeletonitems(int x, int z, int surfaceY, ItemStack leatherHelmet, ItemStack leatherChestplate, ItemStack leatherLeggings, ItemStack leatherBoots, ItemStack enchantedBow, Skeleton skeleton, int randomPos) {
        skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
        skeleton.setItemSlot(EquipmentSlot.HEAD, leatherHelmet);
        skeleton.setItemSlot(EquipmentSlot.CHEST, leatherChestplate);
        skeleton.setItemSlot(EquipmentSlot.LEGS, leatherLeggings);
        skeleton.setItemSlot(EquipmentSlot.FEET, leatherBoots);
        enchantedBow.enchant(Enchantments.POWER_ARROWS, 1);
        skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
    }

    private static void giveMobItems(PlayerMobEntity pPlayer, int x, int z, int subtractX, int subtractY, int subtractZ, int surfaceY, ItemStack leatherHelmet, ItemStack leatherChestplate, ItemStack leatherLeggings, ItemStack leatherBoots, ItemStack woodSword, Zombie zombie, int randomPos) {
        giveZombieItems(x, z, surfaceY, leatherHelmet, leatherChestplate, leatherLeggings, leatherBoots, woodSword, zombie, randomPos);
        setZombieTarget(pPlayer, subtractX, subtractY, subtractZ, zombie);
    }

    public static void giveZombieItems(int x, int z, int surfaceY, ItemStack leatherHelmet, ItemStack leatherChestplate, ItemStack leatherLeggings, ItemStack leatherBoots, ItemStack woodSword, Zombie zombie, int randomPos) {
        zombie.setPos(x + randomPos, surfaceY, z + randomPos);
        zombie.setItemSlot(EquipmentSlot.HEAD, leatherHelmet);
        zombie.setItemSlot(EquipmentSlot.CHEST, leatherChestplate);
        zombie.setItemSlot(EquipmentSlot.LEGS, leatherLeggings);
        zombie.setItemSlot(EquipmentSlot.FEET, leatherBoots);
        zombie.setItemSlot(EquipmentSlot.MAINHAND, woodSword);
    }

    private static void setZombieTarget(PlayerMobEntity pPlayer, int subtractX, int subtractY, int subtractZ, Zombie zombie) {
        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
            if (pPlayer.getCurrentPathway() == BeyonderClassInit.MONSTER && pPlayer.getCurrentSequence() <= 6) {
                if (entity != null) {
                    zombie.setTarget(entity);
                }
            }
        }
        pPlayer.level().addFreshEntity(zombie);
    }

    private static void calamityLightningStorm(LivingEntity pPlayer) {
        CompoundTag tag = pPlayer.getPersistentData();
        int stormCounter = tag.getInt("calamityLightningStormSummon");
        if (stormCounter >= 1) {
            LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), pPlayer.level());
            tag.putInt("calamityLightningStormSummon", stormCounter - 1);
            lightningEntity.setSpeed(6);
            lightningEntity.setNoUp(true);
            lightningEntity.setDeltaMovement((Math.random() * 0.4) - 0.2, -4, (Math.random() * 0.4) - 0.2);
            int stormX = tag.getInt("calamityLightningStormX");
            int stormY = tag.getInt("calamityLightningStormY");
            int stormZ = tag.getInt("calamityLightningStormZ");
            int subtractX = (int) (stormX - pPlayer.getX());
            int subtractY = (int) (stormY - pPlayer.getY());
            int subtractZ = (int) (stormZ - pPlayer.getZ());
            for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(40))) {
                if (entity instanceof Player player) {
                    BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                    if (holder.currentClassMatches(BeyonderClassInit.MONSTER) && holder.getCurrentSequence() <= 3) {
                        player.getPersistentData().putInt("calamityLightningStormImmunity", 20);
                    }
                }
                if (entity instanceof PlayerMobEntity mobEntity) {
                    if (mobEntity.getCurrentPathway() == BeyonderClassInit.MONSTER && mobEntity.getCurrentSequence() <= 3) {
                        mobEntity.getPersistentData().putInt("calamityLightningStormImmunity", 20);
                    }
                }
            }
            double random = (Math.random() * 60) - 30;
            lightningEntity.teleportTo(stormX + random, stormY + 60, stormZ + random);
            lightningEntity.setMaxLength(60);
            pPlayer.level().addFreshEntity(lightningEntity);
        }
    }

    private static void calamityExplosion(LivingEntity pPlayer) {
        CompoundTag tag = pPlayer.getPersistentData();
        int x = tag.getInt("calamityExplosionOccurrence");
        if (x >= 1 && pPlayer.tickCount % 20 == 0 && !pPlayer.level().isClientSide()) {
            pPlayer.sendSystemMessage(Component.literal("working"));
            int explosionX = tag.getInt("calamityExplosionX");
            int explosionY = tag.getInt("calamityExplosionY");
            int explosionZ = tag.getInt("calamityExplosionZ");
            int subtractX = explosionX - (int) pPlayer.getX();
            int subtractY = explosionY - (int) pPlayer.getY();
            int subtractZ = explosionZ - (int) pPlayer.getZ();
            tag.putInt("calamityExplosionOccurrence", x - 1);
            for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(15))) {
                if (entity instanceof Player player) {
                    BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                    if (holder.currentClassMatches(BeyonderClassInit.MONSTER) && holder.getCurrentSequence() <= 3) {
                        player.getPersistentData().putInt("calamityExplosionImmunity", 2);
                    }
                }
                if (entity instanceof PlayerMobEntity mobEntity) {
                    if (mobEntity.getCurrentPathway() == BeyonderClassInit.MONSTER && mobEntity.getCurrentSequence() <= 3) {
                        mobEntity.getPersistentData().putInt("calamityExplosionImmunity", 2);
                    }
                }
            }
        }
        if (x == 1) {
            int explosionX = tag.getInt("calamityExplosionX");
            int explosionY = tag.getInt("calamityExplosionY");
            int explosionZ = tag.getInt("calamityExplosionZ");
            pPlayer.level().playSound(null, explosionX, explosionY, explosionZ, SoundEvents.GENERIC_EXPLODE, SoundSource.AMBIENT, 5.0F, 5.0F);
            Explosion explosion = new Explosion(pPlayer.level(), null, explosionX, explosionY, explosionZ, 10.0F, true, Explosion.BlockInteraction.DESTROY);
            explosion.explode();
            explosion.finalizeExplosion(true);
            tag.putInt("calamityExplosionOccurrence", 0);
        }
    }

    private static void decrementMonsterAttackEvent(LivingEntity pPlayer) {
        if (pPlayer.getPersistentData().getInt("attackedMonster") >= 1) {
            pPlayer.getPersistentData().putInt("attackedMonster", pPlayer.getPersistentData().getInt("attackedMonster") - 1);
        }
    }


    protected static void rainEyes(LivingEntity player) {
        //RAIN EYES
        if (!player.level().isRaining()) {
            return;
        }
        if (player.getPersistentData().getBoolean("torrentialDownpour") && player.tickCount % 200 == 0) {
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(500))) {
                if (entity != player && entity instanceof Player otherPlayer && otherPlayer.isInWaterOrRain()) {
                    player.sendSystemMessage(Component.literal(otherPlayer.getName().getString() + "'s location is " + otherPlayer.getX() + ", " + otherPlayer.getY() + ", " + otherPlayer.getZ()).withStyle(ChatFormatting.BOLD));
                }
            }
        }
    }

    private static void sirenSongs(CompoundTag playerPersistentData, PlayerMobEntity player, int sequence) {
        //SIREN SONGS
        int sirenSongHarm = playerPersistentData.getInt("sirenSongHarm");
        int sirenSongWeaken = playerPersistentData.getInt("sirenSongWeaken");
        int sirenSongStun = playerPersistentData.getInt("sirenSongStun");
        int sirenSongStrengthen = playerPersistentData.getInt("sirenSongStrengthen");
        if (player.getCurrentPathway() != BeyonderClassInit.SAILOR || player.getCurrentSequence() > 5) {
            return;
        }
        if (sirenSongHarm % 20 == 0 && sirenSongHarm != 0) {
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(50 - (sequence * 6)))) {
                if (entity != player) {
                    entity.hurt(entity.damageSources().magic(), 10 - sequence);
                }
            }
        }
        SoundEvent harmSoundEvent = switch (sirenSongHarm) {
            case 400 -> SoundInit.SIREN_SONG_HARM_1.get();
            case 380 -> SoundInit.SIREN_SONG_HARM_2.get();
            case 360 -> SoundInit.SIREN_SONG_HARM_3.get();
            case 340 -> SoundInit.SIREN_SONG_HARM_4.get();
            case 320 -> SoundInit.SIREN_SONG_HARM_5.get();
            case 300 -> SoundInit.SIREN_SONG_HARM_6.get();
            case 280 -> SoundInit.SIREN_SONG_HARM_7.get();
            case 260 -> SoundInit.SIREN_SONG_HARM_8.get();
            case 240 -> SoundInit.SIREN_SONG_HARM_9.get();
            case 220 -> SoundInit.SIREN_SONG_HARM_10.get();
            case 200 -> SoundInit.SIREN_SONG_HARM_11.get();
            case 180 -> SoundInit.SIREN_SONG_HARM_12.get();
            case 160 -> SoundInit.SIREN_SONG_HARM_13.get();
            case 140 -> SoundInit.SIREN_SONG_HARM_14.get();
            case 120 -> SoundInit.SIREN_SONG_HARM_15.get();
            case 100 -> SoundInit.SIREN_SONG_HARM_16.get();
            case 80 -> SoundInit.SIREN_SONG_HARM_17.get();
            case 60 -> SoundInit.SIREN_SONG_HARM_18.get();
            case 40 -> SoundInit.SIREN_SONG_HARM_19.get();
            case 20 -> SoundInit.SIREN_SONG_HARM_20.get();
            default -> null;
        };
        if (harmSoundEvent != null) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), harmSoundEvent, SoundSource.NEUTRAL, 6f, 1f);
        }

        if (sirenSongHarm >= 1) {
            playerPersistentData.putInt("sirenSongHarm", sirenSongHarm - 1);
        }

        if (sirenSongWeaken % 20 == 0 && sirenSongWeaken != 0) { //make it for 380,360,430 etc.
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(50 - (sequence * 6)))) {
                if (entity != player) {
                    entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 19, 2, false, false));
                    entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 19, 2, false, false));
                }
            }
        }

        SoundEvent weakenSoundEvent = switch (sirenSongWeaken) {
            case 400 -> SoundInit.SIREN_SONG_WEAKEN_1.get();
            case 380 -> SoundInit.SIREN_SONG_WEAKEN_2.get();
            case 360 -> SoundInit.SIREN_SONG_WEAKEN_3.get();
            case 340 -> SoundInit.SIREN_SONG_WEAKEN_4.get();
            case 320 -> SoundInit.SIREN_SONG_WEAKEN_5.get();
            case 300 -> SoundInit.SIREN_SONG_WEAKEN_6.get();
            case 280 -> SoundInit.SIREN_SONG_WEAKEN_7.get();
            case 260 -> SoundInit.SIREN_SONG_WEAKEN_8.get();
            case 240 -> SoundInit.SIREN_SONG_WEAKEN_9.get();
            case 220 -> SoundInit.SIREN_SONG_WEAKEN_10.get();
            case 200 -> SoundInit.SIREN_SONG_WEAKEN_11.get();
            case 180 -> SoundInit.SIREN_SONG_WEAKEN_12.get();
            case 160 -> SoundInit.SIREN_SONG_WEAKEN_13.get();
            case 140 -> SoundInit.SIREN_SONG_WEAKEN_14.get();
            case 120 -> SoundInit.SIREN_SONG_WEAKEN_15.get();
            case 100 -> SoundInit.SIREN_SONG_WEAKEN_16.get();
            case 80 -> SoundInit.SIREN_SONG_WEAKEN_17.get();
            case 60 -> SoundInit.SIREN_SONG_WEAKEN_18.get();
            case 40 -> SoundInit.SIREN_SONG_WEAKEN_19.get();
            case 20 -> SoundInit.SIREN_SONG_WEAKEN_20.get();
            default -> null;
        };
        if (weakenSoundEvent != null) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), weakenSoundEvent, SoundSource.NEUTRAL, 6f, 1f);
        }

        if (sirenSongWeaken >= 1) {
            playerPersistentData.putInt("sirenSongWeaken", sirenSongWeaken - 1);
        }

        if (sirenSongStun % 20 == 0 && sirenSongStun != 0) {
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(50 - (sequence * 6)))) {
                if (entity != player) {
                    entity.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(), 19 - (sequence * 2), 2, false, false));
                }
            }
        }
        SoundEvent stunSoundEvent = switch (sirenSongStun) {
            case 400 -> SoundInit.SIREN_SONG_STUN_1.get();
            case 380 -> SoundInit.SIREN_SONG_STUN_2.get();
            case 360 -> SoundInit.SIREN_SONG_STUN_3.get();
            case 340 -> SoundInit.SIREN_SONG_STUN_4.get();
            case 320 -> SoundInit.SIREN_SONG_STUN_5.get();
            case 300 -> SoundInit.SIREN_SONG_STUN_6.get();
            case 280 -> SoundInit.SIREN_SONG_STUN_7.get();
            case 260 -> SoundInit.SIREN_SONG_STUN_8.get();
            case 240 -> SoundInit.SIREN_SONG_STUN_9.get();
            case 220 -> SoundInit.SIREN_SONG_STUN_10.get();
            case 200 -> SoundInit.SIREN_SONG_STUN_11.get();
            case 180 -> SoundInit.SIREN_SONG_STUN_12.get();
            case 160 -> SoundInit.SIREN_SONG_STUN_13.get();
            case 140 -> SoundInit.SIREN_SONG_STUN_14.get();
            case 120 -> SoundInit.SIREN_SONG_STUN_15.get();
            case 100 -> SoundInit.SIREN_SONG_STUN_16.get();
            case 80 -> SoundInit.SIREN_SONG_STUN_17.get();
            case 60 -> SoundInit.SIREN_SONG_STUN_18.get();
            case 40 -> SoundInit.SIREN_SONG_STUN_19.get();
            case 20 -> SoundInit.SIREN_SONG_STUN_20.get();
            default -> null;
        };

        if (stunSoundEvent != null) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), stunSoundEvent, SoundSource.NEUTRAL, 6f, 1f);
        }

        if (sirenSongStun >= 1) {
            playerPersistentData.putInt("sirenSongStun", sirenSongStun - 1);
        }
        if (sirenSongStrengthen % 20 == 0 && sirenSongStrengthen != 0) {
            if (player.hasEffect(MobEffects.DAMAGE_BOOST)) {
                int strengthAmp = player.getEffect(MobEffects.DAMAGE_BOOST).getAmplifier();
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 19, strengthAmp + 2));
            } else if (!player.hasEffect(MobEffects.DAMAGE_BOOST)) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 19, 2));
            }
            if (player.hasEffect(MobEffects.REGENERATION)) {
                int regenAmp = player.getEffect(MobEffects.REGENERATION).getAmplifier();
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 19, regenAmp + 2));
            } else if (!player.hasEffect(MobEffects.REGENERATION)) {
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 19, 2));
            }
        }
        SoundEvent strengthenSoundEvent = switch (sirenSongStrengthen) {
            case 400 -> SoundInit.SIREN_SONG_STRENGTHEN_1.get();
            case 380 -> SoundInit.SIREN_SONG_STRENGTHEN_2.get();
            case 360 -> SoundInit.SIREN_SONG_STRENGTHEN_3.get();
            case 340 -> SoundInit.SIREN_SONG_STRENGTHEN_4.get();
            case 320 -> SoundInit.SIREN_SONG_STRENGTHEN_5.get();
            case 300 -> SoundInit.SIREN_SONG_STRENGTHEN_6.get();
            case 280 -> SoundInit.SIREN_SONG_STRENGTHEN_7.get();
            case 260 -> SoundInit.SIREN_SONG_STRENGTHEN_8.get();
            case 240 -> SoundInit.SIREN_SONG_STRENGTHEN_9.get();
            case 220 -> SoundInit.SIREN_SONG_STRENGTHEN_10.get();
            case 200 -> SoundInit.SIREN_SONG_STRENGTHEN_11.get();
            case 180 -> SoundInit.SIREN_SONG_STRENGTHEN_12.get();
            case 160 -> SoundInit.SIREN_SONG_STRENGTHEN_13.get();
            case 140 -> SoundInit.SIREN_SONG_STRENGTHEN_14.get();
            case 120 -> SoundInit.SIREN_SONG_STRENGTHEN_15.get();
            case 100 -> SoundInit.SIREN_SONG_STRENGTHEN_16.get();
            case 80 -> SoundInit.SIREN_SONG_STRENGTHEN_17.get();
            case 60 -> SoundInit.SIREN_SONG_STRENGTHEN_18.get();
            case 40 -> SoundInit.SIREN_SONG_STRENGTHEN_19.get();
            case 20 -> SoundInit.SIREN_SONG_STRENGTHEN_20.get();
            default -> null;
        };

        if (strengthenSoundEvent != null) {
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), strengthenSoundEvent, SoundSource.NEUTRAL, 6f, 1f);
        }

        if (sirenSongStrengthen >= 1) {
            playerPersistentData.putInt("sirenSongStrengthen", sirenSongStrengthen - 1);
        }
    }

    private static void starOfLightning(LivingEntity livingEntity, CompoundTag tag) {
        //STAR OF LIGHTNING
        int sailorLightningStar = tag.getInt("sailorLightningStar");
        if (sailorLightningStar >= 2) {
            StarOfLightning.summonLightningParticles(livingEntity);
            tag.putInt("sailorLightningStar", sailorLightningStar - 1);
        }
        if (sailorLightningStar == 1) {
            tag.putInt("sailorLightningStar", 0);
            for (int i = 0; i < 500; i++) {
                LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), livingEntity.level());
                lightningEntity.setSpeed(50);
                double sailorStarX = (Math.random() * 2 - 1);
                double sailorStarY = (Math.random() * 2 - 1); // You might want different random values for y and z
                double sailorStarZ = (Math.random() * 2 - 1);
                lightningEntity.setDeltaMovement(sailorStarX, sailorStarY, sailorStarZ);
                lightningEntity.setMaxLength(10);
                lightningEntity.setOwner(livingEntity);
                lightningEntity.teleportTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                livingEntity.level().addFreshEntity(lightningEntity);
            }
        }
    }

    private static void sirenSongsParticles(PlayerMobEntity player) {
        int sequence = player.getCurrentSequence();
        CompoundTag playerPersistentData = player.getPersistentData();
        int harmCounter = 50 - (sequence * 6);
        int sirenSongWeaken = playerPersistentData.getInt("sirenSongWeaken");
        int sirenSongStrengthen = playerPersistentData.getInt("sirenSongStrengthen");
        int sirenSongHarm = playerPersistentData.getInt("sirenSongHarm");
        int sirenSongStun = playerPersistentData.getInt("sirenSongStun");
        if (sirenSongStrengthen >= 1 || sirenSongWeaken >= 1 || sirenSongStun >= 1 || sirenSongHarm >= 1) {
            SirenSongStrengthen.spawnParticlesInSphere(player, harmCounter);
        }
    }

    public static void spawnParticlesInSphere(LivingEntity livingEntity, int radius) {
        Level level = livingEntity.level();
        Random random = new Random();
        if (level instanceof ServerLevel serverLevel) {

            for (int i = 0; i < 20; i++) { // Adjust the number of particles as needed
                double x = livingEntity.getX() + (random.nextDouble() * 2 - 1) * radius;
                double y = livingEntity.getY() + (random.nextDouble() * 2 - 1) * radius;
                double z = livingEntity.getZ() + (random.nextDouble() * 2 - 1) * radius;
                if (isInsideSphere(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), x, y, z, radius)) {
                    double noteValue = random.nextInt(25) / 24.0;
                    serverLevel.sendParticles(ParticleTypes.NOTE, x, y, z, 0, noteValue, 0, 0, 0);
                }
            }
        }
    }


    private static void waterSphereCheck(LivingEntity livingEntity, ServerLevel level) {
        //WATER SPHERE CHECK
        if (livingEntity.getPersistentData().getInt("sailorSphere") >= 5) {
            for (Entity entity : livingEntity.level().getEntitiesOfClass(Entity.class, livingEntity.getBoundingBox().inflate(4))) {
                if (!(entity instanceof LivingEntity) && !(entity instanceof MeteorEntity) && !(entity instanceof MeteorNoLevelEntity)) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
            BlockPos livingEntityPos = livingEntity.blockPosition();
            double radius = 3.0;
            double minRemovalRadius = 4.0;
            double maxRemovalRadius = 7.0;

            // Create a sphere of water around the livingEntity
            for (int sphereX = (int) -radius; sphereX <= radius; sphereX++) {
                for (int sphereY = (int) -radius; sphereY <= radius; sphereY++) {
                    for (int sphereZ = (int) -radius; sphereZ <= radius; sphereZ++) {
                        double sphereDistance = Math.sqrt(sphereX * sphereX + sphereY * sphereY + sphereZ * sphereZ);
                        if (!(sphereDistance <= radius)) {
                            continue;
                        }
                        BlockPos blockPos = livingEntityPos.offset(sphereX, sphereY, sphereZ);
                        if (level.getBlockState(blockPos).isAir() && !level.getBlockState(blockPos).is(Blocks.WATER)) {
                            level.setBlock(blockPos, Blocks.WATER.defaultBlockState(), 3);
                        }
                    }
                }
            }
            for (int sphereX = (int) -maxRemovalRadius; sphereX <= maxRemovalRadius; sphereX++) {
                for (int sphereY = (int) -maxRemovalRadius; sphereY <= maxRemovalRadius; sphereY++) {
                    for (int sphereZ = (int) -maxRemovalRadius; sphereZ <= maxRemovalRadius; sphereZ++) {
                        double sphereDistance = Math.sqrt(sphereX * sphereX + sphereY * sphereY + sphereZ * sphereZ);
                        if (!(sphereDistance <= maxRemovalRadius) || !(sphereDistance >= minRemovalRadius)) {
                            continue;
                        }
                        BlockPos blockPos = livingEntityPos.offset(sphereX, sphereY, sphereZ);
                        if (level.getBlockState(blockPos).getBlock() == Blocks.WATER) {
                            level.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
        if (livingEntity.getPersistentData().getInt("sailorSphere") >= 1 && livingEntity.getPersistentData().getInt("sailorSphere") <= 4) {
            livingEntity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 100, false, false));
            for (int sphereX = -6; sphereX <= 6; sphereX++) {
                for (int sphereY = -6; sphereY <= 6; sphereY++) {
                    for (int sphereZ = -6; sphereZ <= 6; sphereZ++) {
                        double sphereDistance = Math.sqrt(sphereX * sphereX + sphereY * sphereY + sphereZ * sphereZ);
                        if (!(sphereDistance <= 6)) {
                            continue;
                        }
                        BlockPos blockPos = livingEntity.getOnPos().offset(sphereX, sphereY, sphereZ);
                        if (livingEntity.level().getBlockState(blockPos).getBlock() == Blocks.WATER) {
                            livingEntity.level().setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
        if (livingEntity.getPersistentData().getInt("sailorSphere") >= 1) {
            livingEntity.getPersistentData().putInt("sailorSphere", livingEntity.getPersistentData().getInt("sailorSphere") - 1);
        }
    }
}
