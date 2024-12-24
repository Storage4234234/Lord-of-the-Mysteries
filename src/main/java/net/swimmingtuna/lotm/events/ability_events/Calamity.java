package net.swimmingtuna.lotm.events.ability_events;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.LightningEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.util.TickEventUtil;

import java.util.Random;

public class Calamity {
    private Calamity(){}

    public static void calamityExplosion(Player pPlayer) {
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

    protected static void calamityLightningStorm(Player pPlayer) {
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
            }
            double random = (Math.random() * 60) - 30;
            lightningEntity.teleportTo(stormX + random, stormY + 60, stormZ + random);
            lightningEntity.setMaxLength(60);
            pPlayer.level().addFreshEntity(lightningEntity);
        }
    }

    protected static void calamityUndeadArmy(Player pPlayer) {
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
                for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                    BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(pPlayer);
                    if (holder.currentClassMatches(BeyonderClassInit.MONSTER) && holder.getCurrentSequence() <= 6) {
                        if (entity != null) {
                            zombie.setTarget(entity);
                        }
                    }
                }
                pPlayer.level().addFreshEntity(zombie);
            }
            if (random.nextInt(10) == 9) {
                giveItemsAndSetTarget(pPlayer, x, z, subtractX, subtractY, subtractZ, surfaceY, leatherHelmet, leatherChestplate, leatherLeggings, leatherBoots, woodSword, zombie, randomPos);
            }
            if (random.nextInt(10) == 8) {
                giveItemsAndSetTarget(pPlayer, x, z, subtractX, subtractY, subtractZ, surfaceY, ironHelmet, ironChestplate, ironLeggings, ironBoots, ironSword, zombie, randomPos);
            }
            if (random.nextInt(10) == 7) {
                giveItemsAndSetTarget(pPlayer, x, z, subtractX, subtractY, subtractZ, surfaceY, diamondHelmet, diamondChestplate, diamondLeggings, diamondBoots, diamondSword, zombie, randomPos);
            }
            if (random.nextInt(10) == 6) {
                giveItemsAndSetTarget(pPlayer, x, z, subtractX, subtractY, subtractZ, surfaceY, netheriteHelmet, netheriteChestplate, netheriteLeggings, netheriteBoots, netheriteSword, zombie, randomPos);
            }
            if (random.nextInt(20) == 5) {
                skeleton.setPos(x + randomPos, surfaceY, z + randomPos);
                skeleton.setItemSlot(EquipmentSlot.MAINHAND, enchantedBow);
                for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                    BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(pPlayer);
                    if (holder.currentClassMatches(BeyonderClassInit.MONSTER) && holder.getCurrentSequence() <= 6) {
                        if (entity != null) {
                            zombie.setTarget(entity);
                        }
                    }
                }
                pPlayer.level().addFreshEntity(skeleton);
            }
            if (random.nextInt(20) == 4) {
                TickEventUtil.giveSkeletonitems(x, z, surfaceY, leatherHelmet, leatherChestplate, leatherLeggings, leatherBoots, enchantedBow, skeleton, randomPos);
                for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                    BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(pPlayer);
                    if (holder.currentClassMatches(BeyonderClassInit.MONSTER) && holder.getCurrentSequence() <= 6) {
                        if (entity != null) {
                            zombie.setTarget(entity);
                        }
                    }
                }
                pPlayer.level().addFreshEntity(skeleton);
            }
            if (random.nextInt(20) == 3) {
                TickEventUtil.giveSkeletonitems(x, z, surfaceY, ironHelmet, ironChestplate, ironLeggings, ironBoots, enchantedBow, skeleton, randomPos);
                for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
                    BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(pPlayer);
                    if (holder.currentClassMatches(BeyonderClassInit.MONSTER) && holder.getCurrentSequence() <= 6) {
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
                    BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(pPlayer);
                    if (holder.currentClassMatches(BeyonderClassInit.MONSTER) && holder.getCurrentSequence() <= 6) {
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
                    BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(pPlayer);
                    if (holder.currentClassMatches(BeyonderClassInit.MONSTER) && holder.getCurrentSequence() <= 6) {
                        if (entity != null) {
                            zombie.setTarget(entity);
                        }
                    }
                }
                pPlayer.level().addFreshEntity(skeleton);
            }
            TickEventUtil.setDropchanceZombieAndSkeleton(tag, zombie, skeleton);
        }
    }

    private static void giveItemsAndSetTarget(Player pPlayer, int x, int z, int subtractX, int subtractY, int subtractZ, int surfaceY, ItemStack diamondHelmet, ItemStack diamondChestplate, ItemStack diamondLeggings, ItemStack diamondBoots, ItemStack diamondSword, Zombie zombie, int randomPos) {
        TickEventUtil.giveZombieItems(x, z, surfaceY, diamondHelmet, diamondChestplate, diamondLeggings, diamondBoots, diamondSword, zombie, randomPos);
        for (LivingEntity entity : pPlayer.level().getEntitiesOfClass(LivingEntity.class, pPlayer.getBoundingBox().move(subtractX, subtractY, subtractZ).inflate(20))) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(pPlayer);
            if (holder.currentClassMatches(BeyonderClassInit.MONSTER) && holder.getCurrentSequence() <= 6) {
                if (entity != null) {
                    zombie.setTarget(entity);
                }
            }
        }
        pPlayer.level().addFreshEntity(zombie);
    }
}
