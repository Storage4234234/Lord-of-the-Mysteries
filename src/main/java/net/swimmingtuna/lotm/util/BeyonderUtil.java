package net.swimmingtuna.lotm.util;

import net.minecraft.ChatFormatting;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.commands.AbilityRegisterCommand;
import net.swimmingtuna.lotm.entity.LowSequenceDoorEntity;
import net.swimmingtuna.lotm.entity.MidSequenceDoorEntity;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Ability;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.InvisibleHand;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.ScribeAbilities;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.TravelDoorWaypoint;
import net.swimmingtuna.lotm.item.BeyonderAbilities.BeyonderAbilityUser;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.DawnWeaponry;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.Gigantification;
import net.swimmingtuna.lotm.item.OtherItems.SwordOfSilver;
import net.swimmingtuna.lotm.item.SealedArtifacts.DeathKnell;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.*;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import net.swimmingtuna.lotm.util.AllyInformation.PlayerAllyData;
import net.swimmingtuna.lotm.util.ClientData.ClientLeftclickCooldownData;
import net.swimmingtuna.lotm.util.ScribeRecording.CapabilityScribeAbilities;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import net.swimmingtuna.lotm.world.worlddata.CalamityEnhancementData;
import org.jetbrains.annotations.Nullable;
import virtuoel.pehkui.api.ScaleTypes;

import java.lang.reflect.Method;
import java.util.*;

import static net.swimmingtuna.lotm.init.DamageTypeInit.MENTAL_DAMAGE;

public class BeyonderUtil {

    private static final Map<UUID, SimpleAbilityItem> pendingAbilityCopies = new HashMap<>();

    public static Projectile getProjectiles(LivingEntity livingEntity) {
        if (livingEntity.level().isClientSide()) {
            return null;
        }
        List<Projectile> projectiles = livingEntity.level().getEntitiesOfClass(Projectile.class, livingEntity.getBoundingBox().inflate(50));
        for (Projectile projectile : projectiles) {
            CompoundTag tag = projectile.getPersistentData();
            int x = tag.getInt("windDodgeProjectilesCounter");
            if (x == 0) {
                if (projectile.getOwner() == livingEntity && projectile.tickCount > 6 && projectile.tickCount < 100) {
                    return projectile;
                }
            }
        }
        return null;
    }

    public static void projectileEvent(Player player, BeyonderHolder holder) {
        //PROJECTILE EVENT
        Projectile projectile = BeyonderUtil.getProjectiles(player);
        if (projectile == null) return;


        //MATTER ACCELERATION ENTITIES
        if (projectile.getPersistentData().getInt("matterAccelerationEntities") >= 10) {
            double movementX = Math.abs(projectile.getDeltaMovement().x());
            double movementY = Math.abs(projectile.getDeltaMovement().y());
            double movementZ = Math.abs(projectile.getDeltaMovement().z());
            if (movementX >= 6 || movementY >= 6 || movementZ >= 6) {
                BlockPos entityPos = projectile.blockPosition();
                for (int x = -2; x <= 2; x++) {
                    for (int y = -2; y <= 2; y++) {
                        for (int z = -2; z <= 2; z++) {
                            BlockPos pos = entityPos.offset(x, y, z);
                            projectile.level().setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        }
                    }
                }
                for (LivingEntity livingEntity : projectile.level().getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(5))) {
                    if (currentPathwayAndSequenceMatches(player, BeyonderClassInit.SAILOR.get(), 0)) {
                        livingEntity.hurt(livingEntity.damageSources().lightningBolt(), 40);
                    }
                }
            }
        }
        LivingEntity target = BeyonderUtil.getTarget(projectile, 75, 0);
        if (target != null) {
            if (BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.SAILOR.get(), 8) && player.getPersistentData().getBoolean("sailorProjectileMovement")) {
                double dx = target.getX() - projectile.getX();
                double dy = target.getY() - projectile.getY();
                double dz = target.getZ() - projectile.getZ();
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double speed = 1.2;
                projectile.setDeltaMovement((dx / length) * speed, (dy / length) * speed, (dz / length) * speed);
                projectile.hurtMarked = true;
            }
            //APPRENTICE BOUNCE SHOT ARROWS
            if (BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.APPRENTICE.get(), 8) && player.getPersistentData().getBoolean("ApprenticeBounceProjectileMovement")) {
                double dx = target.getX() - projectile.getX();
                double dy = target.getY() - projectile.getY();
                double dz = target.getZ() - projectile.getZ();
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double speed = 1.2;
                projectile.setDeltaMovement((dx / length) * speed, (dy / length) * speed, (dz / length) * speed);
                projectile.hurtMarked = true;
            }
        }


        //SAILOR PASSIVE CHECK FROM HERE
        if (target != null) {
            if (BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.SAILOR.get(), 8) && player.getPersistentData().getBoolean("sailorProjectileMovement")) {
                double dx = target.getX() - projectile.getX();
                double dy = target.getY() - projectile.getY();
                double dz = target.getZ() - projectile.getZ();
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double speed = 1.2;
                projectile.setDeltaMovement((dx / length) * speed, (dy / length) * speed, (dz / length) * speed);
                projectile.hurtMarked = true;
            }
        }

        //MONSTER CALCULATION PASSIVE
        if (target != null) {
            if (BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.APPRENTICE.get(), 8) && player.getPersistentData().getBoolean("monsterProjectileControl")) {
                double dx = target.getX() - projectile.getX();
                double dy = target.getY() - projectile.getY();
                double dz = target.getZ() - projectile.getZ();
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double speed = 1.2;
                projectile.setDeltaMovement((dx / length) * speed, (dy / length) * speed, (dz / length) * speed);
                projectile.hurtMarked = true;
            }
        }
    }


    public static LivingEntity getTarget(Projectile projectile, double maxValue, double minValue) {
        Entity owner = null;
        if (projectile.getOwner() != null) {
            if (projectile.getOwner() instanceof LivingEntity) {
                owner = projectile.getOwner();
            }
        }
        LivingEntity closestEntity = null;
        double closestDistance = Double.MAX_VALUE;
        Vec3 projectilePos = projectile.position();
        List<LivingEntity> nearbyEntities = projectile.level().getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(maxValue));
        for (LivingEntity entity : nearbyEntities) {
            if (entity != owner && !entity.level().isClientSide() && (owner instanceof LivingEntity living && !BeyonderUtil.isAllyOf(living, entity))) {
                double distance = entity.distanceToSqr(projectilePos);
                if (distance < maxValue && distance > minValue && distance < closestDistance) {
                    closestDistance = distance;
                    closestEntity = entity;
                }
            }
        }
        return closestEntity;
    }

    public static Projectile getLivingEntitiesProjectile(LivingEntity player) {
        if (player.level().isClientSide()) {
            return null;
        }
        List<Projectile> projectiles = player.level().getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(30));
        for (Projectile projectile : projectiles) {
            if (projectile.getOwner() == player && projectile.tickCount > 8 && projectile.tickCount < 50) {
                return projectile;
            }
        }
        return null;
    }

    public static DamageSource genericSource(Entity entity) {
        Level level = entity.level();
        Holder<DamageType> damageTypeHolder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.GENERIC);
        return new DamageSource(damageTypeHolder, entity, entity, entity.getOnPos().getCenter());
    }

    public static DamageSource magicSource(Entity entity) {
        Level level = entity.level();
        Holder<DamageType> damageTypeHolder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MAGIC);
        return new DamageSource(damageTypeHolder, entity, entity, entity.getOnPos().getCenter());
    }

    public static DamageSource explosionSource(Entity entity) {
        Level level = entity.level();
        Holder<DamageType> damageTypeHolder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.EXPLOSION);
        return new DamageSource(damageTypeHolder, entity, entity, entity.getOnPos().getCenter());
    }

    public static DamageSource fallSource(Entity entity) {
        Level level = entity.level();
        Holder<DamageType> damageTypeHolder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.FALL);
        return new DamageSource(damageTypeHolder, entity, entity, entity.getOnPos().getCenter());
    }

    public static DamageSource lightningSource(Entity entity) {
        Level level = entity.level();
        Holder<DamageType> damageTypeHolder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.LIGHTNING_BOLT);
        return new DamageSource(damageTypeHolder, entity, entity, entity.getOnPos().getCenter());
    }

    public static DamageSource mentalSource(Level level, LivingEntity attacker, LivingEntity target) {
        final Registry<DamageType> registry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        final Holder.Reference<DamageType> damage = registry.getHolderOrThrow(MENTAL_DAMAGE);
        return new MentalDamageSource(damage, attacker, target);
    }

    public static boolean applyMentalDamage(LivingEntity attacker, LivingEntity target, float baseAmount) {
        Level level = attacker.level();
        Holder<DamageType> damageTypeHolder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(MENTAL_DAMAGE);
        MentalDamageSource damageSource = new MentalDamageSource(damageTypeHolder, attacker, target);
        float calculatedDamage = damageSource.calculateDamage(baseAmount);
        return target.hurt(damageSource, calculatedDamage);
    }


    public static StructurePlaceSettings getStructurePlaceSettings(BlockPos pos) {
        BoundingBox boundingBox = new BoundingBox(
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                pos.getX() + 160,
                pos.getY() + 97,
                pos.getZ() + 265
        );
        StructurePlaceSettings settings = new StructurePlaceSettings();
        settings.setRotation(Rotation.NONE);
        settings.setMirror(Mirror.NONE);
        settings.setRotationPivot(pos);
        settings.setBoundingBox(boundingBox);
        return settings;
    }

    public static List<Item> getAbilities(Player player) {
        List<Item> abilityNames = new ArrayList<>();
        if (player.level().isClientSide()) {
            return abilityNames;
        }
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        int sequence = holder.getSequence();
        if (currentPathwayMatchesNoException(player, BeyonderClassInit.SPECTATOR.get())) {
            if (sequence <= 8) {
                abilityNames.add(ItemInit.MIND_READING.get());
            }
            if (sequence <= 7) {
                abilityNames.add(ItemInit.AWE.get());
                abilityNames.add(ItemInit.FRENZY.get());
                abilityNames.add(ItemInit.PLACATE.get());
            }
            if (sequence <= 6) {
                abilityNames.add(ItemInit.PSYCHOLOGICAL_INVISIBILITY.get());
                abilityNames.add(ItemInit.BATTLE_HYPNOTISM.get());
            }
            if (sequence <= 5) {
                abilityNames.add(ItemInit.GUIDANCE.get());
                abilityNames.add(ItemInit.ALTERATION.get());
                abilityNames.add(ItemInit.NIGHTMARE.get());
                abilityNames.add(ItemInit.DREAM_WALKING.get());
            }
            if (sequence <= 4) {
                abilityNames.remove(ItemInit.FRENZY.get());
                abilityNames.add(ItemInit.APPLY_MANIPULATION.get());
                abilityNames.add(ItemInit.MANIPULATE_MOVEMENT.get());
                abilityNames.add(ItemInit.MANIPULATE_FONDNESS.get());
                abilityNames.add(ItemInit.MANIPULATE_EMOTION.get());
                abilityNames.add(ItemInit.MENTAL_PLAGUE.get());
                abilityNames.add(ItemInit.MIND_STORM.get());
                abilityNames.add(ItemInit.DRAGON_BREATH.get());
            }
            if (sequence <= 3) {
                abilityNames.add(ItemInit.CONSCIOUSNESS_STROLL.get());
                abilityNames.add(ItemInit.PLAGUE_STORM.get());
                abilityNames.add(ItemInit.DREAM_WEAVING.get());
            }
            if (sequence <= 2) {
                abilityNames.add(ItemInit.DISCERN.get());
                abilityNames.add(ItemInit.DREAM_INTO_REALITY.get());
            }
            if (sequence <= 1) {
                abilityNames.add(ItemInit.PROPHESIZE_DEMISE.get());
                abilityNames.add(ItemInit.PROPHESIZE_TELEPORT_PLAYER.get());
                abilityNames.add(ItemInit.PROPHESIZE_TELEPORT_BLOCK.get());
                abilityNames.add(ItemInit.METEOR_SHOWER.get());
                abilityNames.add(ItemInit.METEOR_NO_LEVEL_SHOWER.get());
            }
            if (sequence <= 0) {
                abilityNames.add(ItemInit.ENVISION_BARRIER.get());
                abilityNames.add(ItemInit.ENVISION_DEATH.get());
                abilityNames.add(ItemInit.ENVISION_HEALTH.get());
                abilityNames.add(ItemInit.ENVISION_KINGDOM.get());
                abilityNames.add(ItemInit.ENVISION_LIFE.get());
                abilityNames.add(ItemInit.ENVISION_LOCATION.get());
                abilityNames.add(ItemInit.ENVISION_LOCATION_BLINK.get());
                abilityNames.add(ItemInit.ENVISION_WEATHER.get());
            }
        }

        if (currentPathwayMatchesNoException(player, BeyonderClassInit.SAILOR.get())) {
            if (sequence <= 8) {
                abilityNames.add(ItemInit.RAGING_BLOWS.get());
            }
            if (sequence <= 7) {
                abilityNames.add(ItemInit.ENABLE_OR_DISABLE_LIGHTNING.get());
                abilityNames.add(ItemInit.AQUEOUS_LIGHT_PUSH.get());
                abilityNames.add(ItemInit.AQUEOUS_LIGHT_PULL.get());
                abilityNames.add(ItemInit.AQUEOUS_LIGHT_DROWN.get());
                abilityNames.add(ItemInit.SAILORPROJECTILECTONROL.get());
            }
            if (sequence <= 6) {
                abilityNames.add(ItemInit.WIND_MANIPULATION_BLADE.get());
                abilityNames.add(ItemInit.WIND_MANIPULATION_FLIGHT.get());
                abilityNames.add(ItemInit.WIND_MANIPULATION_SENSE.get());
            }
            if (sequence <= 5) {
                abilityNames.add(ItemInit.SAILOR_LIGHTNING.get());
                abilityNames.add(ItemInit.SIREN_SONG_HARM.get());
                abilityNames.add(ItemInit.SIREN_SONG_STRENGTHEN.get());
                abilityNames.add(ItemInit.SIREN_SONG_WEAKEN.get());
                abilityNames.add(ItemInit.SIREN_SONG_STUN.get());
                abilityNames.add(ItemInit.ACIDIC_RAIN.get());
                abilityNames.add(ItemInit.WATER_SPHERE.get());
            }
            if (sequence <= 4) {
                abilityNames.add(ItemInit.TSUNAMI.get());
                abilityNames.add(ItemInit.TSUNAMI_SEAL.get());
                abilityNames.add(ItemInit.HURRICANE.get());
                abilityNames.add(ItemInit.TORNADO.get());
                abilityNames.add(ItemInit.EARTHQUAKE.get());
                abilityNames.add(ItemInit.ROAR.get());
            }
            if (sequence <= 3) {
                abilityNames.add(ItemInit.AQUATIC_LIFE_MANIPULATION.get());
                abilityNames.add(ItemInit.LIGHTNING_STORM.get());
                abilityNames.add(ItemInit.LIGHTNING_BRANCH.get());
                abilityNames.add(ItemInit.SONIC_BOOM.get());
                abilityNames.add(ItemInit.THUNDER_CLAP.get());
            }
            if (sequence <= 2) {
                abilityNames.add(ItemInit.RAIN_EYES.get());
                abilityNames.add(ItemInit.VOLCANIC_ERUPTION.get());
                abilityNames.add(ItemInit.EXTREME_COLDNESS.get());
                abilityNames.add(ItemInit.LIGHTNING_BALL.get());
            }
            if (sequence <= 1) {
                abilityNames.add(ItemInit.LIGHTNING_BALL_ABSORB.get());
                abilityNames.add(ItemInit.SAILOR_LIGHTNING_TRAVEL.get());
                abilityNames.add(ItemInit.STAR_OF_LIGHTNING.get());
                abilityNames.add(ItemInit.LIGHTNING_REDIRECTION.get());
            }
            if (sequence <= 0) {
                abilityNames.add(ItemInit.STORM_SEAL.get());
                abilityNames.add(ItemInit.WATER_COLUMN.get());
                abilityNames.add(ItemInit.MATTER_ACCELERATION_SELF.get());
                abilityNames.add(ItemInit.MATTER_ACCELERATION_BLOCKS.get());
                abilityNames.add(ItemInit.MATTER_ACCELERATION_ENTITIES.get());
                abilityNames.add(ItemInit.TYRANNY.get());
            }
        }
        if (currentPathwayMatchesNoException(player, BeyonderClassInit.MONSTER.get())) {
            if (sequence <= 9) {
                abilityNames.add(ItemInit.SPIRITVISION.get());
                abilityNames.add(ItemInit.MONSTERDANGERSENSE.get());
            }
            if (sequence <= 8) {
                abilityNames.add(ItemInit.MONSTERPROJECTILECONTROL.get());

            }
            if (sequence <= 7) {
                abilityNames.add(ItemInit.LUCKPERCEPTION.get());

            }
            if (sequence <= 6) {
                abilityNames.add(ItemInit.PSYCHESTORM.get());
            }
            if (sequence <= 5) {
                abilityNames.add(ItemInit.LUCK_MANIPULATION.get());
                abilityNames.add(ItemInit.LUCKDEPRIVATION.get());
                abilityNames.add(ItemInit.LUCKGIFTING.get());
                abilityNames.add(ItemInit.MISFORTUNEBESTOWAL.get());
                abilityNames.add(ItemInit.LUCKFUTURETELLING.get());
            }
            if (sequence <= 4) {
                abilityNames.add(ItemInit.DECAYDOMAIN.get());
                abilityNames.add(ItemInit.PROVIDENCEDOMAIN.get());
                abilityNames.add(ItemInit.LUCKCHANNELING.get());
                abilityNames.add(ItemInit.LUCKDENIAL.get());
                abilityNames.add(ItemInit.MISFORTUNEMANIPULATION.get());
                abilityNames.add(ItemInit.MONSTERCALAMITYATTRACTION.get());
            }
            if (sequence <= 3) {
                abilityNames.add(ItemInit.CALAMITYINCARNATION.get());
                abilityNames.add(ItemInit.ENABLEDISABLERIPPLE.get());
                abilityNames.add(ItemInit.AURAOFCHAOS.get());
                abilityNames.add(ItemInit.CHAOSWALKERCOMBAT.get());
                abilityNames.add(ItemInit.MISFORTUNEREDIRECTION.get());
                abilityNames.add(ItemInit.MONSTERDOMAINTELEPORATION.get());
            }
            if (sequence <= 2) {
                abilityNames.add(ItemInit.WHISPEROFCORRUPTION.get());
                abilityNames.add(ItemInit.FORTUNEAPPROPIATION.get());
                abilityNames.add(ItemInit.FALSEPROPHECY.get());
                abilityNames.add(ItemInit.MISFORTUNEIMPLOSION.get());
            }
            if (sequence <= 1) {
                abilityNames.add(ItemInit.MONSTERREBOOT.get());
                abilityNames.add(ItemInit.FATEREINCARNATION.get());
                abilityNames.add(ItemInit.CYCLEOFFATE.get());
                abilityNames.add(ItemInit.CHAOSAMPLIFICATION.get());
                abilityNames.add(ItemInit.FATEDCONNECTION.get());
                abilityNames.add(ItemInit.REBOOTSELF.get());
            }
            if (sequence <= 0) {
                abilityNames.add(ItemInit.PROBABILITYMISFORTUNEINCREASE.get());
                abilityNames.add(ItemInit.PROBABILITYFORTUNEINCREASE.get());
                abilityNames.add(ItemInit.PROBABILITYFORTUNE.get());
                abilityNames.add(ItemInit.PROBABILITYMISFORTUNE.get());
                abilityNames.add(ItemInit.PROBABILITYWIPE.get());
                abilityNames.add(ItemInit.PROBABILITYEFFECT.get());
                abilityNames.add(ItemInit.PROBABILITYINFINITEFORTUNE.get());
                abilityNames.add(ItemInit.PROBABILITYINFINITEMISFORTUNE.get());
            }
            if (currentPathwayMatchesNoException(player, BeyonderClassInit.WARRIOR.get())) {
                if (sequence <= 6) {
                    abilityNames.add(ItemInit.GIGANTIFICATION.get());
                    abilityNames.add(ItemInit.LIGHTOFDAWN.get());
                    abilityNames.add(ItemInit.DAWNARMORY.get());
                    abilityNames.add(ItemInit.DAWNWEAPONRY.get());
                }
                if (sequence <= 5) {
                    abilityNames.add(ItemInit.ENABLEDISABLEPROTECTION.get());
                }
                if (sequence <= 4) {
                    abilityNames.add(ItemInit.EYEOFDEMONHUNTING.get());
                    abilityNames.add(ItemInit.WARRIORDANGERSENSE.get());
                }
                if (sequence <= 3) {
                    abilityNames.add(ItemInit.MERCURYLIQUEFICATION.get());
                    abilityNames.add(ItemInit.SILVERSWORDMANIFESTATION.get());
                    abilityNames.add(ItemInit.SILVERRAPIER.get());
                    abilityNames.add(ItemInit.SILVERARMORY.get());
                    abilityNames.add(ItemInit.LIGHTCONCEALMENT.get());
                }
                if (sequence <= 2) {
                    abilityNames.add(ItemInit.BEAMOFGLORY.get());
                    abilityNames.add(ItemInit.AURAOFGLORY.get());
                    abilityNames.add(ItemInit.TWILIGHTSWORD.get());
                    abilityNames.add(ItemInit.MERCURYCAGE.get());
                }
                if (sequence <= 1) {
                    abilityNames.add(ItemInit.DIVINEHANDLEFT.get());
                    abilityNames.add(ItemInit.DIVINEHANDRIGHT.get());
                    abilityNames.add(ItemInit.TWILIGHTMANIFESTATION.get());
                }
                if (sequence <= 0) {
                    abilityNames.add(ItemInit.AURAOFTWILIGHT.get());
                    abilityNames.add(ItemInit.TWILIGHTFREEZE.get());
                    abilityNames.add(ItemInit.TWILIGHTACCELERATE.get());
                    abilityNames.add(ItemInit.GLOBEOFTWILIGHT.get());
                    abilityNames.add(ItemInit.BEAMOFTWILIGHT.get());
                    abilityNames.add(ItemInit.TWILIGHTLIGHT.get());
                }
            }
            if (currentPathwayMatchesNoException(player, BeyonderClassInit.APPRENTICE.get())) {
                if (sequence <= 9) {
                    abilityNames.add(ItemInit.CREATEDOOR.get());
                }
                if (sequence <= 8) {
                    abilityNames.add(ItemInit.TRICKBURN.get());
                    abilityNames.add(ItemInit.TRICKBOUNCE.get());
                    abilityNames.add(ItemInit.TRICKFREEZE.get());
                    abilityNames.add(ItemInit.TRICKTUMBLE.get());
                    abilityNames.add(ItemInit.TRICKWINDPULL.get());
                    abilityNames.add(ItemInit.TRICKWINDPUSH.get());
                }
                if (sequence <= 6) {
                    abilityNames.add(ItemInit.RECORDSCRIBE.get());
                }
                if (sequence <= 5) {
                    abilityNames.add(ItemInit.TRAVELDOOR.get());
                    abilityNames.add(ItemInit.TRAVELDOORHOME.get());
                    abilityNames.add(ItemInit.INVISIBLEHAND.get());
                }

            }
        }
        return abilityNames;
    }

    private static String getItemName(Item item) {
        return I18n.get(item.getDescriptionId()).toLowerCase();
    }

    private static final String REGISTERED_ABILITIES_KEY = "RegisteredAbilities";

    public static void useAbilityByNumber(Player player, int abilityNumber, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return;
        }
        if (player.hasEffect(ModEffects.STUN.get())) {
            player.sendSystemMessage(Component.literal("You are stunned and unable to use abilities for another " +
                            (int) Objects.requireNonNull(player.getEffect(ModEffects.STUN.get())).getDuration() / 20 + " seconds.")
                    .withStyle(ChatFormatting.RED));
            return;
        }

        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(REGISTERED_ABILITIES_KEY, Tag.TAG_COMPOUND)) {
            player.sendSystemMessage(Component.literal("No registered abilities found."));
            return;
        }

        CompoundTag registeredAbilities = persistentData.getCompound(REGISTERED_ABILITIES_KEY);
        if (!registeredAbilities.contains(String.valueOf(abilityNumber), Tag.TAG_STRING)) {
            player.sendSystemMessage(Component.literal("Ability " + abilityNumber + " not found."));
            return;
        }

        ResourceLocation resourceLocation = new ResourceLocation(registeredAbilities.getString(String.valueOf(abilityNumber)));
        Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
        if (item == null) {
            player.sendSystemMessage(Component.literal("Item not found in registry for ability " + abilityNumber +
                    " with resource location: " + resourceLocation));
            return;
        }

        String itemName = item.getDescription().getString();
        if (!(item instanceof Ability ability)) {
            player.sendSystemMessage(Component.literal("Registered ability ").append(itemName)
                    .append(" for ability number " + abilityNumber + " is not an ability."));
            return;
        }

        if (player.getCooldowns().isOnCooldown(item)) {
            player.sendSystemMessage(Component.literal("Ability ").append(itemName).append(" is on cooldown!"));
            return;
        }

        double entityReach = ability.getEntityReach();
        double blockReach = ability.getBlockReach();
        boolean successfulUse = false;

        // Check for existence of methods
        boolean hasEntityInteraction = false;
        boolean hasBlockInteraction = false;
        boolean hasGeneralAbility = false;

        try {
            Method entityMethod = ability.getClass().getDeclaredMethod("useAbilityOnEntity",
                    ItemStack.class, Player.class, LivingEntity.class, InteractionHand.class);
            hasEntityInteraction = !entityMethod.equals(Ability.class.getDeclaredMethod("useAbilityOnEntity",
                    ItemStack.class, Player.class, LivingEntity.class, InteractionHand.class));
        } catch (NoSuchMethodException ignored) {
        }

        try {
            Method blockMethod = ability.getClass().getDeclaredMethod("useAbilityOnBlock", UseOnContext.class);
            hasBlockInteraction = !blockMethod.equals(Ability.class.getDeclaredMethod("useAbilityOnBlock", UseOnContext.class));
        } catch (NoSuchMethodException ignored) {
        }

        try {
            Method generalMethod = ability.getClass().getDeclaredMethod("useAbility", Level.class, Player.class, InteractionHand.class);
            hasGeneralAbility = !generalMethod.equals(Ability.class.getDeclaredMethod("useAbility", Level.class, Player.class, InteractionHand.class));
        } catch (NoSuchMethodException ignored) {
        }

        // Check for entity interaction
        if (hasEntityInteraction) {
            Vec3 eyePosition = player.getEyePosition();
            Vec3 lookVector = player.getLookAngle();
            Vec3 reachVector = eyePosition.add(lookVector.x * entityReach, lookVector.y * entityReach, lookVector.z * entityReach);
            AABB searchBox = player.getBoundingBox().inflate(entityReach);
            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(
                    player.level(),
                    player,
                    eyePosition,
                    reachVector,
                    searchBox,
                    entity -> !entity.isSpectator() && entity.isPickable(),
                    0.0f
            );

            if (entityHit != null && entityHit.getEntity() instanceof LivingEntity livingEntity) {
                InteractionResult result = ability.useAbilityOnEntity(player.getItemInHand(hand), player, livingEntity, hand);
                if (result != InteractionResult.PASS) {
                    successfulUse = true;
                }
            }
        }

        // Check for block interaction
        if (!successfulUse && hasBlockInteraction) {
            Vec3 eyePosition = player.getEyePosition();
            Vec3 lookVector = player.getLookAngle();
            Vec3 reachVector = eyePosition.add(lookVector.x * blockReach, lookVector.y * blockReach, lookVector.z * blockReach);

            BlockHitResult blockHit = player.level().clip(new ClipContext(
                    eyePosition,
                    reachVector,
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    player
            ));

            if (blockHit.getType() != HitResult.Type.MISS) {
                UseOnContext context = new UseOnContext(player.level(), player, hand, player.getItemInHand(hand), blockHit);
                InteractionResult result = ability.useAbilityOnBlock(context);
                if (result != InteractionResult.PASS) {
                    successfulUse = true;
                }
            }
        }

        // Handle different cases
        if ((hasEntityInteraction || hasBlockInteraction) && !hasGeneralAbility) {
            if (successfulUse) {
                player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true);
            } else {
                player.displayClientMessage(Component.literal("Missed: " + itemName).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD), true);
            }
        } else if (!hasEntityInteraction && !hasBlockInteraction) {
            // If it's just a general ability with no targeting
            ability.useAbility(player.level(), player, hand);
            player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true);
        } else if (successfulUse) {
            // If it has both targeted and general abilities, and the targeted one succeeded
            player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true);
        } else {
            // If it has both targeted and general abilities, but the targeted one missed
            ability.useAbility(player.level(), player, hand);
            player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true);
        }
    }

    public static Style getStyle(Player player) {
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        if (holder.getCurrentClass() != null) {
            return Style.EMPTY.withBold(true).withColor(holder.getCurrentClass().getColorFormatting());
        }
        return Style.EMPTY;
    }

    public static void mentalDamage(Player source, Player hurtEntity, int damage) { //can make it so that with useOn, sets shiftKeyDown to true for player
        BeyonderHolder sourceHolder = BeyonderHolderAttacher.getHolderUnwrap(source);
        BeyonderHolder hurtHolder = BeyonderHolderAttacher.getHolderUnwrap(hurtEntity);
        float x = Math.min(damage, damage * (hurtHolder.getMentalStrength() / sourceHolder.getMentalStrength()));
        hurtEntity.hurt(hurtEntity.damageSources().magic(), x);
    }

    public static float mentalInt(Player source, Player hurtEntity, int mentalInt) {
        BeyonderHolder sourceHolder = BeyonderHolderAttacher.getHolderUnwrap(source);
        BeyonderHolder hurtHolder = BeyonderHolderAttacher.getHolderUnwrap(hurtEntity);
        float x = Math.min(mentalInt, mentalInt * (hurtHolder.getMentalStrength() / sourceHolder.getMentalStrength()));
        return x;
    }

    public static void abilityCooldownsServerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (!player.level().isClientSide()) {
            if (player instanceof ServerPlayer serverPlayer) {
                Map<String, Integer> cooldowns = new HashMap<>();
                CompoundTag tag = serverPlayer.getPersistentData();
                if (tag.contains(AbilityRegisterCommand.REGISTERED_ABILITIES_KEY, Tag.TAG_COMPOUND)) {
                    CompoundTag registeredAbilities = tag.getCompound(AbilityRegisterCommand.REGISTERED_ABILITIES_KEY);
                    for (String combinationNumber : registeredAbilities.getAllKeys()) {
                        String abilityResourceLocationString = registeredAbilities.getString(combinationNumber);
                        ResourceLocation resourceLocation = new ResourceLocation(abilityResourceLocationString);
                        Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
                        if (item instanceof SimpleAbilityItem simpleAbilityItem && player.getCooldowns().isOnCooldown(item)) {
                            float cooldownPercent = player.getCooldowns().getCooldownPercent(item, 0.0F);
                            if (cooldownPercent > 0) {
                                int totalCooldown = simpleAbilityItem.getCooldown();
                                int remainingCooldown = (int) (totalCooldown * cooldownPercent);
                                if (remainingCooldown > 0) {
                                    String combination = AbilityRegisterCommand.findCombinationForNumber(Integer.parseInt(combinationNumber));
                                    if (!combination.isEmpty()) {
                                        cooldowns.put(combination, remainingCooldown);
                                    }
                                }
                            }
                        }
                    }
                }

                if (!cooldowns.isEmpty()) {
                    SyncAbilityCooldownsS2C syncPacket = new SyncAbilityCooldownsS2C(cooldowns);
                    LOTMNetworkHandler.sendToPlayer(syncPacket, serverPlayer);
                }
            }
        }
    }


    public static int getCoordinateAtLeastAway(int centerCoord, int minDistance, int maxDistance) {
        Random random = new Random();
        int offset = random.nextInt(maxDistance - minDistance + 1) + minDistance;
        return random.nextBoolean() ? centerCoord + offset : centerCoord - offset;
    }

    public static void setCooldown(ServerPlayer player, int cooldown) {
        player.getPersistentData().putInt("leftClickCooldown", cooldown);
        LOTMNetworkHandler.sendToPlayer(new SyncLeftClickCooldownS2C(cooldown), player);
    }

    public static int getCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt("leftClickCooldown");
    }

    public static void leftClickEmpty(Player pPlayer) {
        Style style = BeyonderUtil.getStyle(pPlayer);
        ItemStack heldItem = pPlayer.getMainHandItem();
        int activeSlot = pPlayer.getInventory().selected;
        if (ClientLeftclickCooldownData.getCooldown() > 0) {
            return;
        }
        LOTMNetworkHandler.sendToServer(new RequestCooldownSetC2S());
        if (!heldItem.isEmpty()) {
            if (heldItem.getItem() instanceof DawnWeaponry) {
                LOTMNetworkHandler.sendToServer(new DawnWeaponryLeftClickC2S());
            } else if (heldItem.getItem() instanceof Gigantification) {
                LOTMNetworkHandler.sendToServer(new GigantificationC2S());
            } else if (heldItem.getItem() instanceof SwordOfSilver) {
                LOTMNetworkHandler.sendToServer(new SwordOfSilverC2S());
            } else if (heldItem.getItem() instanceof MonsterDomainTeleporation) {
                LOTMNetworkHandler.sendToServer(new MonsterLeftClickC2S());
            } else if (heldItem.getItem() instanceof BeyonderAbilityUser) {
                LOTMNetworkHandler.sendToServer(new LeftClickC2S()); //DIFFERENT FOR LEFT CLICK BLOCK
            } else if (heldItem.getItem() instanceof AqueousLightPush) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.AQUEOUS_LIGHT_PULL.get())));
            } else if (heldItem.getItem() instanceof AqueousLightPull) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.AQUEOUS_LIGHT_DROWN.get())));

            } else if (heldItem.getItem() instanceof AqueousLightDrown) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.AQUEOUS_LIGHT_PUSH.get())));

            } else if (heldItem.getItem() instanceof Hurricane) {
                LOTMNetworkHandler.sendToServer(new LeftClickC2S());

            } else if (heldItem.getItem() instanceof LightningStorm) {
                LOTMNetworkHandler.sendToServer(new LeftClickC2S());

            } else if (heldItem.getItem() instanceof MatterAccelerationBlocks) {
                LOTMNetworkHandler.sendToServer(new MatterAccelerationBlockC2S());

            } else if (heldItem.getItem() instanceof MatterAccelerationEntities) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.MATTER_ACCELERATION_SELF.get())));

            } else if (heldItem.getItem() instanceof MatterAccelerationSelf) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.MATTER_ACCELERATION_BLOCKS.get())));

            } else if (heldItem.getItem() instanceof WindManipulationBlade) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.WIND_MANIPULATION_FLIGHT.get())));

            } else if (heldItem.getItem() instanceof WindManipulationFlight) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.WIND_MANIPULATION_SENSE.get())));

            } else if (heldItem.getItem() instanceof WindManipulationSense) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.WIND_MANIPULATION_BLADE.get())));

            } else if (heldItem.getItem() instanceof ApplyManipulation) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.MANIPULATE_EMOTION.get())));

            } else if (heldItem.getItem() instanceof ManipulateEmotion) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.MANIPULATE_MOVEMENT.get())));

            } else if (heldItem.getItem() instanceof ManipulateMovement) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.MANIPULATE_FONDNESS.get())));

            } else if (heldItem.getItem() instanceof ManipulateFondness) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.APPLY_MANIPULATION.get())));

            } else if (heldItem.getItem() instanceof EnvisionBarrier) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.ENVISION_DEATH.get())));

            } else if (heldItem.getItem() instanceof EnvisionDeath) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.ENVISION_HEALTH.get())));

            } else if (heldItem.getItem() instanceof EnvisionHealth) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.ENVISION_LIFE.get())));

            } else if (heldItem.getItem() instanceof EnvisionLife) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.ENVISION_WEATHER.get())));

            } else if (heldItem.getItem() instanceof EnvisionWeather) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.ENVISION_LOCATION.get())));

            } else if (heldItem.getItem() instanceof EnvisionLocation) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.ENVISION_LOCATION_BLINK.get())));

            } else if (heldItem.getItem() instanceof EnvisionLocationBlink) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.ENVISION_KINGDOM.get())));

            } else if (heldItem.getItem() instanceof EnvisionKingdom) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.ENVISION_BARRIER.get())));

            } else if (heldItem.getItem() instanceof MeteorShower) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.METEOR_NO_LEVEL_SHOWER.get())));

            } else if (heldItem.getItem() instanceof MeteorNoLevelShower) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.METEOR_SHOWER.get())));

            } else if (heldItem.getItem() instanceof ProphesizeDemise) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROPHESIZE_TELEPORT_BLOCK.get())));

            } else if (heldItem.getItem() instanceof ProphesizeTeleportBlock) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROPHESIZE_TELEPORT_PLAYER.get())));

            } else if (heldItem.getItem() instanceof ProphesizeTeleportPlayer) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROPHESIZE_DEMISE.get())));
            } else if (heldItem.getItem() instanceof SirenSongHarm) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.SIREN_SONG_STRENGTHEN.get())));

            } else if (heldItem.getItem() instanceof SirenSongStrengthen) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.SIREN_SONG_STUN.get())));

            } else if (heldItem.getItem() instanceof SirenSongStun) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.SIREN_SONG_WEAKEN.get())));
            } else if (heldItem.getItem() instanceof SirenSongWeaken) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.SIREN_SONG_HARM.get())));
            } else if (heldItem.getItem() instanceof ProbabilityManipulationFortune) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYMISFORTUNE.get())));
            } else if (heldItem.getItem() instanceof ProbabilityManipulationMisfortune) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYINFINITEFORTUNE.get())));
            } else if (heldItem.getItem() instanceof ProbabilityManipulationInfiniteFortune) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYINFINITEMISFORTUNE.get())));
            } else if (heldItem.getItem() instanceof ProbabilityManipulationInfiniteMisfortune) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYFORTUNEINCREASE.get())));
            } else if (heldItem.getItem() instanceof ProbabilityManipulationWorldFortune) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYMISFORTUNEINCREASE.get())));
            } else if (heldItem.getItem() instanceof ProbabilityManipulationWorldMisfortune) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYWIPE.get())));
            } else if (heldItem.getItem() instanceof ProbabilityManipulationWipe) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYEFFECT.get())));
            } else if (heldItem.getItem() instanceof ProbabilityManipulationImpulse) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYFORTUNE.get())));
            } else if (heldItem.getItem() instanceof LuckManipulation) {
                LOTMNetworkHandler.sendToServer(new LuckManipulationLeftClickC2S());
            } else if (heldItem.getItem() instanceof MisfortuneManipulation) {
                LOTMNetworkHandler.sendToServer(new MisfortuneManipulationLeftClickC2S());
            } else if (heldItem.getItem() instanceof MonsterCalamityIncarnation) {
                LOTMNetworkHandler.sendToServer(new MonsterCalamityIncarnationLeftClickC2S());
            } else if (heldItem.getItem() instanceof FalseProphecy) {
                LOTMNetworkHandler.sendToServer(new FalseProphecyLeftClickC2S());
            } else if (heldItem.getItem() instanceof ChaosAmplification) {
                LOTMNetworkHandler.sendToServer(new CalamityEnhancementLeftClickC2S());
            } else if (heldItem.getItem() instanceof DeathKnell) {
                LOTMNetworkHandler.sendToServer(new DeathKnellLeftClickC2S());
            } else if (heldItem.getItem() instanceof DomainOfProvidence || heldItem.getItem() instanceof DomainOfDecay || heldItem.getItem() instanceof MonsterDomainTeleporation) {
                LOTMNetworkHandler.sendToServer(new MonsterDomainLeftClickC2S());
            } else if (heldItem.getItem() instanceof InvisibleHand) {
                LOTMNetworkHandler.sendToServer(new ToggleDistanceC2S());
            } else if (heldItem.getItem() instanceof TravelDoorWaypoint) {
                LOTMNetworkHandler.sendToServer(new TravelerWaypointC2S());
            } else if (heldItem.getItem() instanceof ScribeAbilities) {
                LOTMNetworkHandler.sendToServer(new ScribeCopyAbilityC2S());
            }
        }
    }

    public static void leftClickBlock(Player pPlayer) {
        Style style = BeyonderUtil.getStyle(pPlayer);
        ItemStack heldItem = pPlayer.getMainHandItem();
        int activeSlot = pPlayer.getInventory().selected;
        if (ClientLeftclickCooldownData.getCooldown() > 0) {
            return;
        }
        if (FMLEnvironment.dist == Dist.CLIENT) {
            LOTMNetworkHandler.sendToServer(new RequestCooldownSetC2S());
        }
        if (!heldItem.isEmpty()) {
            if (heldItem.getItem() instanceof MonsterDomainTeleporation) {
                LOTMNetworkHandler.sendToServer(new MonsterLeftClickC2S());
            }
            if (heldItem.getItem() instanceof AqueousLightPush) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.AQUEOUS_LIGHT_PULL.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof AqueousLightPull) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.AQUEOUS_LIGHT_DROWN.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof AqueousLightDrown) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.AQUEOUS_LIGHT_PUSH.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof MatterAccelerationBlocks) {
                MatterAccelerationBlocks.leftClick(pPlayer);
            } else if (heldItem.getItem() instanceof MatterAccelerationEntities) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.MATTER_ACCELERATION_SELF.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof MatterAccelerationSelf) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.MATTER_ACCELERATION_BLOCKS.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof WindManipulationBlade) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.WIND_MANIPULATION_FLIGHT.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof WindManipulationFlight) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.WIND_MANIPULATION_SENSE.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof WindManipulationSense) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.WIND_MANIPULATION_BLADE.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof ApplyManipulation) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.MANIPULATE_EMOTION.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof ManipulateEmotion) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.MANIPULATE_MOVEMENT.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof ManipulateMovement) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.MANIPULATE_FONDNESS.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof ManipulateFondness) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.APPLY_MANIPULATION.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof EnvisionBarrier) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.ENVISION_DEATH.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof EnvisionDeath) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.ENVISION_HEALTH.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof EnvisionHealth) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.ENVISION_LIFE.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof EnvisionLife) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.ENVISION_WEATHER.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof EnvisionWeather) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.ENVISION_LOCATION.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof EnvisionLocation) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.ENVISION_LOCATION_BLINK.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof EnvisionLocationBlink) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.ENVISION_KINGDOM.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof EnvisionKingdom) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.ENVISION_BARRIER.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof MeteorShower) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.METEOR_NO_LEVEL_SHOWER.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof MeteorNoLevelShower) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.METEOR_SHOWER.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof ProphesizeDemise) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.PROPHESIZE_TELEPORT_BLOCK.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof ProphesizeTeleportBlock) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.PROPHESIZE_TELEPORT_PLAYER.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof ProphesizeTeleportPlayer) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.PROPHESIZE_DEMISE.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof LuckManipulation) {
                LOTMNetworkHandler.sendToServer(new LuckManipulationLeftClickC2S());
            } else if (heldItem.getItem() instanceof Gigantification) {
                LOTMNetworkHandler.sendToServer(new GigantificationC2S());
            } else if (heldItem.getItem() instanceof MisfortuneManipulation) {
                LOTMNetworkHandler.sendToServer(new MisfortuneManipulationLeftClickC2S());
            } else if (heldItem.getItem() instanceof MonsterCalamityIncarnation) {
                LOTMNetworkHandler.sendToServer(new MonsterCalamityIncarnationLeftClickC2S());
            } else if (heldItem.getItem() instanceof FalseProphecy) {
                LOTMNetworkHandler.sendToServer(new FalseProphecyLeftClickC2S());
            } else if (heldItem.getItem() instanceof ChaosAmplification) {
                LOTMNetworkHandler.sendToServer(new CalamityEnhancementLeftClickC2S());
            } else if (heldItem.getItem() instanceof DeathKnell) {
                LOTMNetworkHandler.sendToServer(new DeathKnellLeftClickC2S());
            } else if (heldItem.getItem() instanceof TravelDoorWaypoint) {
                LOTMNetworkHandler.sendToServer(new TravelerWaypointC2S());
            } else if (heldItem.getItem() instanceof ProbabilityManipulationFortune) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYMISFORTUNE.get())));
            } else if (heldItem.getItem() instanceof ProbabilityManipulationMisfortune) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYINFINITEFORTUNE.get())));
            } else if (heldItem.getItem() instanceof ProbabilityManipulationInfiniteFortune) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYINFINITEMISFORTUNE.get())));
            } else if (heldItem.getItem() instanceof ProbabilityManipulationInfiniteMisfortune) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYFORTUNEINCREASE.get())));
            } else if (heldItem.getItem() instanceof ProbabilityManipulationWorldFortune) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYMISFORTUNEINCREASE.get())));
            } else if (heldItem.getItem() instanceof ProbabilityManipulationWorldMisfortune) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYWIPE.get())));
            } else if (heldItem.getItem() instanceof ProbabilityManipulationWipe) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYEFFECT.get())));
            } else if (heldItem.getItem() instanceof ProbabilityManipulationImpulse) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYFORTUNE.get())));
            } else if (heldItem.getItem() instanceof InvisibleHand) {
                LOTMNetworkHandler.sendToServer(new ToggleDistanceC2S());
            } else if (heldItem.getItem() instanceof ScribeAbilities) {
                LOTMNetworkHandler.sendToServer(new ScribeCopyAbilityC2S());
            }
        }
    }

    public static void spawnParticlesInSphere(ServerLevel level, double x, double y, double z, int maxRadius, int maxParticles, float xSpeed, float ySpeed, float zSpeed, ParticleOptions particle) {
        for (int i = 0; i < maxParticles; i++) {
            double dx = level.random.nextGaussian() * maxRadius;
            double dy = level.random.nextGaussian() * 2;
            double dz = level.random.nextGaussian() * maxRadius;
            double distance = Math.sqrt(dx * dx + dz * dz);
            if (distance < maxRadius) {
                double density = 1.0 - (distance / maxRadius);
                if (level.random.nextDouble() < density) {
                    level.sendParticles(particle, x + dx, y + dy, z + dz, 0, xSpeed, ySpeed, zSpeed, 1);
                }
            }
        }
    }

    public static void applyMobEffect(LivingEntity pPlayer, MobEffect mobEffect, int duration, int amplifier, boolean ambient, boolean visible) {
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

    public static boolean isBeyonderCapable(LivingEntity living) {
        return living instanceof Player || living instanceof PlayerMobEntity;
    }

    public static @Nullable BeyonderClass getPathway(LivingEntity living) {
        if (living instanceof Player player) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            return holder.getCurrentClass();
        } else if (living instanceof PlayerMobEntity playerMobEntity) {
            return (BeyonderClass) playerMobEntity.getCurrentPathway();
        }
        return null;
    }

    public static int getSequence(LivingEntity living) {
        if (living instanceof Player player) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            return holder.getSequence();
        } else if (living instanceof PlayerMobEntity playerMobEntity) {
            return playerMobEntity.getCurrentSequence();
        } else {
            float maxHp = living.getMaxHealth();
            if (maxHp <= 20) {
                return 9;
            } else if (maxHp <= 35) {
                return 8;
            } else if (maxHp <= 70) {
                return 7;
            } else if (maxHp <= 120) {
                return 6;
            } else if (maxHp <= 190) {
                return 5;
            } else if (maxHp <= 300) {
                return 4;
            } else if (maxHp <= 450) {
                return 3;
            } else if (maxHp <= 700) {
                return 2;
            } else if (maxHp <= 999) {
                return 1;
            } else if (maxHp >= 1000) {
                return 0;
            } else {
                return -1;
            }
        }
    }

    public static Map<Item, Float> getDamage(LivingEntity livingEntity) {
        Map<Item, Float> damageMap = new HashMap<>();
        Level level = livingEntity.level();
        int enhancement = 1;
        if (level instanceof ServerLevel serverLevel) {
            enhancement = CalamityEnhancementData.getInstance(serverLevel).getCalamityEnhancement();
        }
        double dreamIntoReality = Objects.requireNonNull(livingEntity.getAttribute(ModAttributes.DIR.get())).getBaseValue();
        float abilityStrengthened = 1;
        if (livingEntity.getPersistentData().getInt("abilityStrengthened") >= 1) {
            abilityStrengthened = 2;
        }
        int sequence = 0;
        int abilityWeakness = 1;
        if (livingEntity.hasEffect(ModEffects.ABILITY_WEAKNESS.get())) {
            abilityWeakness = Math.max(1, (livingEntity.getEffect(ModEffects.ABILITY_WEAKNESS.get())).getAmplifier());
        }
        if (livingEntity instanceof Player player) {
            sequence = BeyonderHolderAttacher.getHolderUnwrap(player).getSequence();
        } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
            sequence = playerMobEntity.getCurrentSequence();
        }
        //SAILOR
        damageMap.put(ItemInit.ACIDIC_RAIN.get(), applyAbilityStrengthened((50.0f - (sequence * 7)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.AQUATIC_LIFE_MANIPULATION.get(), applyAbilityStrengthened((50.0f - (sequence * 5)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.AQUEOUS_LIGHT_PUSH.get(), applyAbilityStrengthened((8.0f - sequence) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.AQUEOUS_LIGHT_PULL.get(), applyAbilityStrengthened((8.0f - sequence) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.AQUEOUS_LIGHT_DROWN.get(), applyAbilityStrengthened((8.0f - sequence) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.CALAMITY_INCARNATION_TORNADO.get(), applyAbilityStrengthened((300.0f - (50 * sequence)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.CALAMITY_INCARNATION_TSUNAMI.get(), applyAbilityStrengthened((200.0f - (30 * sequence)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.EARTHQUAKE.get(), applyAbilityStrengthened((75.0f - (sequence * 6)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENABLE_OR_DISABLE_LIGHTNING.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.EXTREME_COLDNESS.get(), applyAbilityStrengthened((150.0f - (sequence * 20)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.HURRICANE.get(), applyAbilityStrengthened((600.0f - (sequence * 100)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LIGHTNING_BALL.get(), applyAbilityStrengthened((10.0f + (10 - sequence * 3)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LIGHTNING_BALL_ABSORB.get(), applyAbilityStrengthened((10.0f + (10 - sequence * 3)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LIGHTNING_BRANCH.get(), applyAbilityStrengthened((30.0f - (sequence * 3)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LIGHTNING_REDIRECTION.get(), applyAbilityStrengthened((200.0f - (sequence * 25)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LIGHTNING_STORM.get(), applyAbilityStrengthened((500.0f - (sequence * 80)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MATTER_ACCELERATION_BLOCKS.get(), applyAbilityStrengthened((10.0f - sequence) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MATTER_ACCELERATION_ENTITIES.get(), applyAbilityStrengthened((300.0f - (sequence * 80)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MATTER_ACCELERATION_SELF.get(), applyAbilityStrengthened((120.0f - (sequence * 30)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.RAGING_BLOWS.get(), applyAbilityStrengthened((10.0f - (sequence)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.RAIN_EYES.get(), applyAbilityStrengthened((500.0f - (sequence * 50)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ROAR.get(), applyAbilityStrengthened((10.0f - sequence) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SAILOR_LIGHTNING.get(), applyAbilityStrengthened((20.0f - (2 * sequence)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SAILOR_LIGHTNING_TRAVEL.get(), applyAbilityStrengthened((400.0f - (sequence * 150)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SAILORPROJECTILECTONROL.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SIREN_SONG_HARM.get(), applyAbilityStrengthened((50.0f - (sequence * 6)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SIREN_SONG_WEAKEN.get(), applyAbilityStrengthened((50.0f - (sequence * 6)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SIREN_SONG_STRENGTHEN.get(), applyAbilityStrengthened((21.0f - sequence) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SIREN_SONG_STUN.get(), applyAbilityStrengthened((50.0f - (sequence * 6)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SONIC_BOOM.get(), applyAbilityStrengthened((40.0f - (sequence * 5)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.STAR_OF_LIGHTNING.get(), applyAbilityStrengthened((125.0f - sequence * 20) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.STORM_SEAL.get(), applyAbilityStrengthened((3.0f - sequence) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.THUNDER_CLAP.get(), applyAbilityStrengthened((300.0f - (sequence * 50)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TORNADO.get(), applyAbilityStrengthened((150.0f - (sequence * 30)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TSUNAMI.get(), applyAbilityStrengthened((600.0f - (sequence * 80)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TSUNAMI_SEAL.get(), applyAbilityStrengthened((600.0f - (sequence * 80)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TYRANNY.get(), applyAbilityStrengthened((250.0f - (sequence * 80)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.VOLCANIC_ERUPTION.get(), applyAbilityStrengthened((120.0f - (sequence * 10)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.WATER_COLUMN.get(), applyAbilityStrengthened((200.0f - (sequence * 60)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.WATER_SPHERE.get(), applyAbilityStrengthened((200.0f - (sequence * 20)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.WIND_MANIPULATION_BLADE.get(), applyAbilityStrengthened((7.0f - sequence) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.WIND_MANIPULATION_FLIGHT.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.WIND_MANIPULATION_SENSE.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));

        // SPECTATOR
        damageMap.put(ItemInit.APPLY_MANIPULATION.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.AWE.get(), applyAbilityStrengthened((190.0f - (sequence * 15)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.BATTLE_HYPNOTISM.get(), applyAbilityStrengthened((400.0f - (sequence * 20)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.CONSCIOUSNESS_STROLL.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.DISCERN.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.DRAGON_BREATH.get(), applyAbilityStrengthened((float) ((60.0f * dreamIntoReality) - (sequence * 4)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.DREAM_INTO_REALITY.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.DREAM_WALKING.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.DREAM_WEAVING.get(), applyAbilityStrengthened((20.0f - (sequence * 3)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENVISION_BARRIER.get(), applyAbilityStrengthened((101.0f - (sequence * 20)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENVISION_DEATH.get(), applyAbilityStrengthened((float) ((40.0f + (dreamIntoReality * 5)) - (sequence * 10)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENVISION_HEALTH.get(), applyAbilityStrengthened((float) (0.66f - (sequence * 0.05) + (dreamIntoReality * 0.05f)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENVISION_KINGDOM.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.ENVISION_LIFE.get(), applyAbilityStrengthened((3.0f + (sequence)) * abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENVISION_LOCATION.get(), applyAbilityStrengthened((float) (500.0f / dreamIntoReality) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENVISION_LOCATION_BLINK.get(), applyAbilityStrengthened((float) (1000.0f - dreamIntoReality) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENVISION_WEATHER.get(), applyAbilityStrengthened((float) (500.0f / dreamIntoReality) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.FRENZY.get(), applyAbilityStrengthened((float) ((15.0f - sequence) * dreamIntoReality) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MANIPULATE_MOVEMENT.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MANIPULATE_EMOTION.get(), applyAbilityStrengthened((30.0f - (sequence * 3)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MANIPULATE_FONDNESS.get(), applyAbilityStrengthened((float) (600.0f * dreamIntoReality) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MENTAL_PLAGUE.get(), applyAbilityStrengthened((float) (200.0f / dreamIntoReality) * abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.METEOR_NO_LEVEL_SHOWER.get(), applyAbilityStrengthened((float) ((10.0f + dreamIntoReality * 2) - (4 * sequence)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.METEOR_SHOWER.get(), applyAbilityStrengthened((float) ((10.0f + dreamIntoReality * 2) - (4 * sequence)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MIND_READING.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.MIND_STORM.get(), applyAbilityStrengthened((30.0f - (sequence * 2)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.NIGHTMARE.get(), applyAbilityStrengthened((40.0f - (sequence * 2)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PLACATE.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.PLAGUE_STORM.get(), applyAbilityStrengthened((float) ((12.0f * dreamIntoReality) - (sequence * 1.5)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROPHESIZE_DEMISE.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.PROPHESIZE_TELEPORT_BLOCK.get(), applyAbilityStrengthened((float) ((500.0f * dreamIntoReality) - (sequence * 100)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROPHESIZE_TELEPORT_PLAYER.get(), applyAbilityStrengthened((float) ((500.0f * dreamIntoReality) - (sequence * 100)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PSYCHOLOGICAL_INVISIBILITY.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));

        // MONSTER
        damageMap.put(ItemInit.AURAOFCHAOS.get(), applyAbilityStrengthened((200.0f - (sequence * 50) + (enhancement * 50)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.CHAOSAMPLIFICATION.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.CHAOSWALKERCOMBAT.get(), applyAbilityStrengthened(((float) Math.max(50, 200 - (sequence * 35))) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.CYCLEOFFATE.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.DECAYDOMAIN.get(), applyAbilityStrengthened((250.0f - (sequence * 45)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROVIDENCEDOMAIN.get(), applyAbilityStrengthened((250.0f - (sequence * 45)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.ENABLEDISABLERIPPLE.get(), applyAbilityStrengthened((150.0f - (sequence * 20)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.FALSEPROPHECY.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.FATEDCONNECTION.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.FATEREINCARNATION.get(), applyAbilityStrengthened((0.0f), abilityStrengthened));
        damageMap.put(ItemInit.FORTUNEAPPROPIATION.get(), applyAbilityStrengthened((200.0f - (sequence * 40)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LUCKCHANNELING.get(), applyAbilityStrengthened((100.0f - (sequence * 25)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LUCKDENIAL.get(), applyAbilityStrengthened((1800.0f - (sequence * 150)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LUCKDEPRIVATION.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LUCKFUTURETELLING.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LUCKGIFTING.get(), applyAbilityStrengthened((101.0f - (sequence * 5)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LUCK_MANIPULATION.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LUCKPERCEPTION.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MISFORTUNEBESTOWAL.get(), applyAbilityStrengthened((60.0f - (sequence * 7) + (enhancement * 10)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MISFORTUNEIMPLOSION.get(), applyAbilityStrengthened((250.0f - (sequence * 100) + (enhancement * 50)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MISFORTUNEMANIPULATION.get(), applyAbilityStrengthened((15.0f - sequence * 2) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MISFORTUNEREDIRECTION.get(), applyAbilityStrengthened((300.0f - (sequence * 50)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.CALAMITYINCARNATION.get(), applyAbilityStrengthened((8.0f - sequence) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MONSTERDANGERSENSE.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MONSTERCALAMITYATTRACTION.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MONSTERDOMAINTELEPORATION.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MONSTERPROJECTILECONTROL.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MONSTERREBOOT.get(), applyAbilityStrengthened((30.0f - (sequence * 5)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROBABILITYFORTUNE.get(), applyAbilityStrengthened((200.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROBABILITYEFFECT.get(), applyAbilityStrengthened((200.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROBABILITYINFINITEFORTUNE.get(), applyAbilityStrengthened((2000.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROBABILITYINFINITEMISFORTUNE.get(), applyAbilityStrengthened((2000.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROBABILITYMISFORTUNE.get(), applyAbilityStrengthened((200.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROBABILITYWIPE.get(), applyAbilityStrengthened((200.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROBABILITYFORTUNEINCREASE.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PROBABILITYMISFORTUNEINCREASE.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.PSYCHESTORM.get(), applyAbilityStrengthened((30.0f - (sequence * 3)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.REBOOTSELF.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SPIRITVISION.get(), applyAbilityStrengthened((0.0f) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.WHISPEROFCORRUPTION.get(), applyAbilityStrengthened(((float) sequence) / abilityWeakness, abilityStrengthened));

        // WARRIOR
        damageMap.put(ItemInit.GIGANTIFICATION.get(), applyAbilityStrengthened((9.0f - sequence) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SWORDOFDAWN.get(), applyAbilityStrengthened((150.0f - (sequence * 15)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SWORDOFSILVER.get(), applyAbilityStrengthened((200.0f - (sequence * 20)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TWILIGHTSWORD.get(), applyAbilityStrengthened((400.0f - (sequence * 100)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.DAWNARMORY.get(), applyAbilityStrengthened((150.0f - (sequence * 15)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.DAWNWEAPONRY.get(), applyAbilityStrengthened((150.0f - (sequence * 15)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SILVERARMORY.get(), applyAbilityStrengthened((150.0f - (sequence * 15)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LIGHTOFDAWN.get(), applyAbilityStrengthened((100.0f - (sequence * 10)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.AURAOFGLORY.get(), applyAbilityStrengthened((15.0f - (sequence)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TWILIGHTMANIFESTATION.get(), applyAbilityStrengthened((200.0f - (sequence * 100)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TWILIGHTFREEZE.get(), applyAbilityStrengthened((600.0f - (sequence * 100)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TWILIGHTACCELERATE.get(), applyAbilityStrengthened((1800.0f - (sequence * 300)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.AURAOFTWILIGHT.get(), applyAbilityStrengthened((30.0f - (sequence * 2)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MERCURYLIQUEFICATION.get(), applyAbilityStrengthened((15.0f - (sequence * 2)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.DIVINEHANDRIGHT.get(), applyAbilityStrengthened((10.0f - (sequence * 4)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.DIVINEHANDLEFT.get(), applyAbilityStrengthened((10.0f - (sequence * 4)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SILVERRAPIER.get(), applyAbilityStrengthened((40.0f - (sequence * 6)) / abilityWeakness, abilityStrengthened));

        // APPRENTICE
        damageMap.put(ItemInit.CREATEDOOR.get(), applyAbilityStrengthened(0.0f, abilityStrengthened));
        damageMap.put(ItemInit.TRICKBOUNCE.get(), applyAbilityStrengthened((300.0f - (sequence * 25)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.RECORDSCRIBE.get(), applyAbilityStrengthened(0.0f, abilityStrengthened));
        damageMap.put(ItemInit.TRICKBURN.get(), applyAbilityStrengthened((8.0f - sequence) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TRAVELDOOR.get(), applyAbilityStrengthened(3.0f + abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TRAVELDOORHOME.get(), applyAbilityStrengthened(0.0f, abilityStrengthened));
        damageMap.put(ItemInit.TRICKTUMBLE.get(), applyAbilityStrengthened((float) (50 - (sequence * 5)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.INVISIBLEHAND.get(), applyAbilityStrengthened((float) (50 - (sequence * 8)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TRICKFREEZE.get(), applyAbilityStrengthened((float) (50 - (sequence * 6)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TRICKWINDPULL.get(), applyAbilityStrengthened((float) (100 - (sequence * 10)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TRICKWINDPUSH.get(), applyAbilityStrengthened((float) (100 - (sequence * 10)) / abilityWeakness, abilityStrengthened));
        return damageMap;
    }

    public static float applyAbilityStrengthened(float damage, float abilityStrengthened) {
        if (abilityStrengthened >= 1) {
            return damage * 1.5f; // Increase damage by 50%
        }
        return damage;
    }


    public static boolean isLivingEntityMoving(LivingEntity entity) {
        CompoundTag tag = entity.getPersistentData();
        updatePositions(entity, tag);
        double MOVEMENT_THRESHOLD = 0.0023;
        double prevX = tag.getDouble("prevX");
        double prevY = tag.getDouble("prevY");
        double prevZ = tag.getDouble("prevZ");
        double currentX = tag.getDouble("currentX");
        double currentY = tag.getDouble("currentY");
        double currentZ = tag.getDouble("currentZ");

        // Check if movement exceeds threshold in any direction
        return Math.abs(prevX - currentX) > MOVEMENT_THRESHOLD ||
                Math.abs(prevY - currentY) > MOVEMENT_THRESHOLD ||
                Math.abs(prevZ - currentZ) > MOVEMENT_THRESHOLD;
    }

    private static void updatePositions(Entity entity, CompoundTag tag) {
        int tickCounter = tag.getInt("tickCounter");

        if (tickCounter == 0) {
            // Store previous position
            tag.putDouble("prevX", entity.getX());
            tag.putDouble("prevY", entity.getY());
            tag.putDouble("prevZ", entity.getZ());
            tag.putInt("tickCounter", 1);
        } else {
            // Store current position
            tag.putDouble("currentX", entity.getX());
            tag.putDouble("currentY", entity.getY());
            tag.putDouble("currentZ", entity.getZ());
            tag.putInt("tickCounter", 0);
        }
    }

    public static void setTargetToHighestHP(Mob mob, int searchRange) {
        List<LivingEntity> nearbyEntities = mob.level().getEntitiesOfClass(LivingEntity.class, mob.getBoundingBox().inflate(searchRange), entity -> entity != mob && entity.isAlive() && mob.canAttack(entity));
        if (nearbyEntities.isEmpty()) {
            return;
        }
        nearbyEntities.stream().max(Comparator.comparingDouble(LivingEntity::getMaxHealth)).ifPresent(mob::setTarget);
    }

    public static void saveWorld() {
        // Get the Minecraft server instance
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            for (ServerLevel level : server.getAllLevels()) {
                level.save(null, true, false);
            }
            PlayerList playerList = server.getPlayerList();
            playerList.saveAll();
        }
    }

    public static void executeCommand(ServerLevel world, BlockPos pos, String command) {
        CommandSourceStack source = new CommandSourceStack(
                CommandSource.NULL,
                Vec3.atCenterOf(pos),
                Vec2.ZERO,
                world,
                4, // Permission level
                "", // Name
                Component.literal(""), // Display name
                world.getServer(),
                null // Entity
        );

        world.getServer().getCommands().performPrefixedCommand(source, command);
    }

    public static void executeCommand(MinecraftServer server, String command) {
        CommandSourceStack source = server.createCommandSourceStack();
        server.getCommands().performPrefixedCommand(source, command);
    }

    public static void registerAllRecipes(MinecraftServer server) {
        executeCommand(server, "/beyonderrecipe add lotm:monster_9_potion bossominium:flower_of_genesis bossominium:redstone_hard_drive minecraft:rotten_flesh alexscaves:charred_remnant samurai_dynasty:jorogumo_eye");
        executeCommand(server, "/beyonderrecipe add lotm:monster_8_potion legendary_monsters:crystal_of_sandstorm alexscaves:sweet_tooth minecraft:netherite_scrap mutantmonsters:hulk_hammer legendary_monsters:primal_ice_shard");
        executeCommand(server, "/beyonderrecipe add lotm:monster_7_potion alexscaves:pure_darkness bossominium:soul_eye born_in_chaos_v1:spiritual_dust bossominium:possesed_metal bossominium:dead_charm");
        executeCommand(server, "/beyonderrecipe add lotm:monster_6_potion nether_star alexsmobs:void_worm_eye kom:nectra_egg cataclysm:monstrous_horn illageandspillage:bag_of_horrors");
        executeCommand(server, "/beyonderrecipe add lotm:monster_5_potion soulsweapons:chaos_crown cataclysm:witherite_ingot alexscaves:uranium kom:anglospike");
        executeCommand(server, "/beyonderrecipe add lotm:monster_4_potion iceandfire:dragon_skull_fire eeeabsmobs:guardian_core cataclysm:ignitium_ingot");
        executeCommand(server, "/beyonderrecipe add lotm:monster_3_potion minecraft:nether_star iceandfire:dragon_skull_ice");
        executeCommand(server, "/beyonderrecipe add lotm:monster_2_potion terramity:giant_sniffers_hoof soulsweapons:lord_soul_day_stalker soulsweapons:lord_soul_night_prowler");
        executeCommand(server, "/beyonderrecipe add lotm:sailor_9_potion bossominium:rusted_trident mowziesmobs:sol_visage aquamirae:fin aether:victory_medal samurai_dynasty:oni_horn");
        executeCommand(server, "/beyonderrecipe add lotm:sailor_8_potion bossominium:mossy_stone_tablet iceandfire:sea_serpent_fang alexsmobs:warped_muscle prismarine_shard mutantmonsters:endersoul_hand");
        executeCommand(server, "/beyonderrecipe add lotm:sailor_7_potion eeeabsmobs:heart_of_pagan aquamirae:abyssal_amethyst bossominium:decayed_mushroom mowziesmobs:ice_crystal faded_conquest_2:eye_of_the_storm");
        executeCommand(server, "/beyonderrecipe add lotm:sailor_6_potion nether_star illageandspillage:spellbound_book minecraft:dragon_egg kom:caligan_saw minecraft:white_banner");
        executeCommand(server, "/beyonderrecipe add lotm:sailor_5_potion cataclysm:gauntlet_of_guard aquamirae:frozen_key soulsweapons:essence_of_eventide alexscaves:immortal_embryo");
        executeCommand(server, "/beyonderrecipe add lotm:sailor_4_potion iceandfire:dragon_skull_ice alexscaves:tectonic_shard cataclysm:abyssal_egg");
        executeCommand(server, "/beyonderrecipe add lotm:sailor_3_potion soulsweapons:essence_of_luminescence iceandfire:dragon_skull_lightning");
        executeCommand(server, "/beyonderrecipe add lotm:sailor_2_potion terramity:angel_feather soulsweapons:lord_soul_day_stalker soulsweapons:lord_soul_night_prowler");
        executeCommand(server, "/beyonderrecipe add lotm:spectator_9_potion bossominium:golden_shard bossominium:forest_core minecraft:ender_pearl iceandfire:witherbone born_in_chaos_v1:nightmare_claw");
        executeCommand(server, "/beyonderrecipe add lotm:spectator_8_potion minecraft:sandstone bossominium:the_golden_eye born_in_chaos_v1:seedof_chaos born_in_chaos_v1:spider_mandible alexscaves:heavy_bone");
        executeCommand(server, "/beyonderrecipe add lotm:spectator_7_potion bossominium:pure_pearl deeperdarker:soul_crystal mutantmonsters:endersoul_hand legendary_monsters:withered_bone aether:gold_dungeon_key");
        executeCommand(server, "/beyonderrecipe add lotm:spectator_6_potion illageandspillage:spellbound_book bossominium:ancient_scrap born_in_chaos_v1:lifestealer_bone born_in_chaos_v1:soul_cutlass");
        executeCommand(server, "/beyonderrecipe add lotm:spectator_5_potion cataclysm:witherite_ingot soulsweapons:lord_soul_rose kom:sigil_of_revival soulsweapons:darkin_blade");
        executeCommand(server, "/beyonderrecipe add lotm:spectator_4_potion iceandfire:dragon_skull_lightning sleepy_hollows:spectral_essence terramity:belt_of_the_gnome_king");
        executeCommand(server, "/beyonderrecipe add lotm:spectator_3_potion iceandfire:dragon_skull_fire born_in_chaos_v1:lord_pumpkinheads_lamp");
        executeCommand(server, "/beyonderrecipe add lotm:spectator_2_potion terramity:fortunes_favor soulsweapons:lord_soul_day_stalker soulsweapons:lord_soul_night_prowler");
    }

    public static void registerAbilities(Player player, MinecraftServer server) {
        int sequence = BeyonderUtil.getSequence(player);
        BeyonderClass beyonderClass = getPathway(player);
        boolean isSpectator = currentPathwayMatchesNoException(player, BeyonderClassInit.SPECTATOR.get());
        boolean isSailor = currentPathwayMatchesNoException(player, BeyonderClassInit.SAILOR.get());
        boolean isMonster = currentPathwayMatchesNoException(player, BeyonderClassInit.MONSTER.get());
        boolean isApprentice = currentPathwayMatchesNoException(player, BeyonderClassInit.APPRENTICE.get());
        boolean isWarrior = currentPathwayMatchesNoException(player, BeyonderClassInit.WARRIOR.get());
        System.out.println("Sequence: " + sequence);
        System.out.println("Pathway: " + beyonderClass);
        if (isSpectator) {
            player.sendSystemMessage(Component.literal("spectator"));
            if (sequence == 9) {
                player.sendSystemMessage(Component.literal("No abilities to register"));
            } else if (sequence >= 8) {
                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
            } else if (sequence == 7) {
                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
                executeCommand(server, "/abilityput LLLLL lotm:awe");
                executeCommand(server, "/abilityput LLLRL lotm:frenzy");
                executeCommand(server, "/abilityput RRRLR lotm:placate");
            } else if (sequence == 6) {
                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
                executeCommand(server, "/abilityput LLLLL lotm:awe");
                executeCommand(server, "/abilityput LLLRL lotm:frenzy");
                executeCommand(server, "/abilityput RRRLR lotm:placate");
                executeCommand(server, "/abilityput RRRLL lotm:psychologicalinvisibility");
            } else if (sequence == 5) {
                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
                executeCommand(server, "/abilityput LLLLL lotm:awe");
                executeCommand(server, "/abilityput LLLRL lotm:frenzy");
                executeCommand(server, "/abilityput RRRLR lotm:placate");
                executeCommand(server, "/abilityput RRRLL lotm:psychologicalinvisibility");
                executeCommand(server, "/abilityput RRRRR lotm:dreamwalking");
            } else if (sequence == 4) {
                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
                executeCommand(server, "/abilityput LLLLL lotm:awe");
                executeCommand(server, "/abilityput LLLRL lotm:frenzy");
                executeCommand(server, "/abilityput RRRLR lotm:placate");
                executeCommand(server, "/abilityput RRRLL lotm:psychologicalinvisibility");
                executeCommand(server, "/abilityput RRRRR lotm:dreamwalking");
                executeCommand(server, "/abilityput RRRRL lotm:dragonbreath");
                executeCommand(server, "/abilityput RLLLL lotm:mindstorm");
            } else if (sequence == 3) {
                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
                executeCommand(server, "/abilityput LLLLL lotm:awe");
                executeCommand(server, "/abilityput LLLRL lotm:frenzy");
                executeCommand(server, "/abilityput RRRLR lotm:placate");
                executeCommand(server, "/abilityput RRRLL lotm:psychologicalinvisibility");
                executeCommand(server, "/abilityput RRRRR lotm:dreamwalking");
                executeCommand(server, "/abilityput RRRRL lotm:dragonbreath");
                executeCommand(server, "/abilityput RLLLL lotm:plaguestorm");
            } else if (sequence == 2) {
                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
                executeCommand(server, "/abilityput LLLLL lotm:awe");
                executeCommand(server, "/abilityput LLLRL lotm:frenzy");
                executeCommand(server, "/abilityput RRRLR lotm:placate");
                executeCommand(server, "/abilityput RRRLL lotm:psychologicalinvisibility");
                executeCommand(server, "/abilityput RRRRR lotm:dreamwalking");
                executeCommand(server, "/abilityput RRRRL lotm:dragonbreath");
                executeCommand(server, "/abilityput RLLLL lotm:plaguestorm");
                executeCommand(server, "/abilityput RRLRR lotm:dreamintoreality");
                executeCommand(server, "/abilityput LLLLR lotm:discern");
            } else if (sequence == 1) {
                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
                executeCommand(server, "/abilityput LLLLL lotm:awe");
                executeCommand(server, "/abilityput LLLRL lotm:frenzy");
                executeCommand(server, "/abilityput RRRLR lotm:placate");
                executeCommand(server, "/abilityput RRRLL lotm:psychologicalinvisibility");
                executeCommand(server, "/abilityput RRRRR lotm:dreamwalking");
                executeCommand(server, "/abilityput RRRRL lotm:dragonbreath");
                executeCommand(server, "/abilityput RLLLL lotm:plaguestorm");
                executeCommand(server, "/abilityput RRLRR lotm:dreamintoreality");
                executeCommand(server, "/abilityput LLLLR lotm:discern");
                executeCommand(server, "/abilityput LLRRR lotm:prophesizeblock");
                executeCommand(server, "/abilityput LLRLR lotm:prophesizeplayer");
                executeCommand(server, "/abilityput LLRLL lotm:prophesizedemise");
                executeCommand(server, "/abilityput RRLLL lotm:meteorshower");
            } else if (sequence == 0) {
                executeCommand(server, "/abilityput LRRLL lotm:mindreading");
                executeCommand(server, "/abilityput LLLLL lotm:awe");
                executeCommand(server, "/abilityput LLLRL lotm:frenzy");
                executeCommand(server, "/abilityput RRRLR lotm:placate");
                executeCommand(server, "/abilityput RRRLL lotm:psychologicalinvisibility");
                executeCommand(server, "/abilityput RRRRR lotm:dreamwalking");
                executeCommand(server, "/abilityput RRRRL lotm:dragonbreath");
                executeCommand(server, "/abilityput RLLLL lotm:plaguestorm");
                executeCommand(server, "/abilityput RRLRR lotm:dreamintoreality");
                executeCommand(server, "/abilityput LLLLR lotm:discern");
                executeCommand(server, "/abilityput LLRRR lotm:prophesizeblock");
                executeCommand(server, "/abilityput LLRLR lotm:prophesizeplayer");
                executeCommand(server, "/abilityput LLRLL lotm:prophesizedemise");
                executeCommand(server, "/abilityput RRLLL lotm:meteorshower");
                executeCommand(server, "/abilityput RLRRR lotm:envisionhealth");
                executeCommand(server, "/abilityput RLLRR lotm:envisionbarrier");
                executeCommand(server, "/abilityput LLRRL lotm:envisionlocationblink");
            }
        } else if (isMonster) {
            player.sendSystemMessage(Component.literal("monster"));
            if (sequence == 9) {
                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
            } else if (sequence >= 8) {
                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
            } else if (sequence == 7) {
                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
                executeCommand(server, "/abilityput RRLLR lotm:luckperception");
            } else if (sequence == 6) {
                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
                executeCommand(server, "/abilityput RRLLR lotm:luckperception");
                executeCommand(server, "/abilityput LLLRR lotm:psychestorm");
            } else if (sequence == 5) {
                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
                executeCommand(server, "/abilityput RRLLR lotm:luckperception");
                executeCommand(server, "/abilityput LLLRR lotm:psychestorm");
                executeCommand(server, "/abilityput RRLLL lotm:luckfuturetelling");
                executeCommand(server, "/abilityput LLLLL lotm:misfortunebestowal");
            } else if (sequence == 4) {
                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
                executeCommand(server, "/abilityput RRLLR lotm:luckperception");
                executeCommand(server, "/abilityput LLLRR lotm:psychestorm");
                executeCommand(server, "/abilityput RRLLL lotm:luckfuturetelling");
                executeCommand(server, "/abilityput LLLLL lotm:misfortunebestowal");
                executeCommand(server, "/abilityput LRRLL lotm:providencedomain");
                executeCommand(server, "/abilityput LRLLL lotm:misfortunedomain");
            } else if (sequence == 3) {
                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
                executeCommand(server, "/abilityput RRLLR lotm:luckperception");
                executeCommand(server, "/abilityput LLLRR lotm:psychestorm");
                executeCommand(server, "/abilityput RRLLL lotm:luckfuturetelling");
                executeCommand(server, "/abilityput LLLLL lotm:misfortunebestowal");
                executeCommand(server, "/abilityput LRRLL lotm:providencedomain");
                executeCommand(server, "/abilityput LRLLL lotm:misfortunedomain");
                executeCommand(server, "/abilityput RRRRR lotm:auraofchaos");
                executeCommand(server, "/abilityput RRRRL lotm:chaoswalkercombat");
                executeCommand(server, "/abilityput LRRRR lotm:enabledisableripple");
            } else if (sequence == 2) {
                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
                executeCommand(server, "/abilityput RRLLR lotm:luckperception");
                executeCommand(server, "/abilityput LLLRR lotm:psychestorm");
                executeCommand(server, "/abilityput RRLLL lotm:luckfuturetelling");
                executeCommand(server, "/abilityput LLLLL lotm:misfortunebestowal");
                executeCommand(server, "/abilityput LRRLL lotm:providencedomain");
                executeCommand(server, "/abilityput LRLLL lotm:misfortunedomain");
                executeCommand(server, "/abilityput RRRRR lotm:auraofchaos");
                executeCommand(server, "/abilityput RRRLL lotm:chaoswalkercombat");
                executeCommand(server, "/abilityput LRRRR lotm:enabledisableripple");
                executeCommand(server, "/abilityput RRRRL lotm:whispersofcorruption");
                executeCommand(server, "/abilityput LRRRR lotm:misfortuneimplosion");
            } else if (sequence == 1) {
                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
                executeCommand(server, "/abilityput RRLLR lotm:luckperception");
                executeCommand(server, "/abilityput LLLRR lotm:psychestorm");
                executeCommand(server, "/abilityput RRLLL lotm:luckfuturetelling");
                executeCommand(server, "/abilityput LLLLL lotm:misfortunebestowal");
                executeCommand(server, "/abilityput LRRLL lotm:providencedomain");
                executeCommand(server, "/abilityput LRLLL lotm:misfortunedomain");
                executeCommand(server, "/abilityput RRRRR lotm:auraofchaos");
                executeCommand(server, "/abilityput RRRLL lotm:chaoswalkercombat");
                executeCommand(server, "/abilityput LRRRR lotm:enabledisableripple");
                executeCommand(server, "/abilityput RRRRL lotm:whispersofcorruption");
                executeCommand(server, "/abilityput LRRRR lotm:misfortuneimplosion");
                executeCommand(server, "/abilityput RLLLL lotm:rebootself");
                executeCommand(server, "/abilityput RRRLR lotm:cycleoffate");
                executeCommand(server, "/abilityput RRLRR lotm:fatereincarnation");
            } else if (sequence == 0) {
                executeCommand(server, "/abilityput RLRRL lotm:monsterdangersense");
                executeCommand(server, "/abilityput RRLLR lotm:luckperception");
                executeCommand(server, "/abilityput LLLRR lotm:psychestorm");
                executeCommand(server, "/abilityput RRLLL lotm:luckfuturetelling");
                executeCommand(server, "/abilityput LLLLL lotm:misfortunebestowal");
                executeCommand(server, "/abilityput LRRLL lotm:providencedomain");
                executeCommand(server, "/abilityput LRLLL lotm:misfortunedomain");
                executeCommand(server, "/abilityput RRRRR lotm:auraofchaos");
                executeCommand(server, "/abilityput RRRLL lotm:chaoswalkercombat");
                executeCommand(server, "/abilityput LRRRR lotm:enabledisableripple");
                executeCommand(server, "/abilityput RRRRL lotm:whispersofcorruption");
                executeCommand(server, "/abilityput LRRRR lotm:misfortuneimplosion");
                executeCommand(server, "/abilityput RLLLL lotm:rebootself");
                executeCommand(server, "/abilityput RRRLR lotm:cycleoffate");
                executeCommand(server, "/abilityput RRLRR lotm:fatereincarnation");
                executeCommand(server, "/abilityput LLLRL lotm:probabilityinfinitefortune");
                executeCommand(server, "/abilityput RLRRR lotm:probabilityinfinitemisfortune");
                executeCommand(server, "/abilityput LRLRL lotm:probabilityfortune");
                executeCommand(server, "/abilityput RLRLR lotm:probabilitymisfortune");
            }
        } else if (isSailor) {
            player.sendSystemMessage(Component.literal("sailor"));
            if (sequence == 9) {
                player.sendSystemMessage(Component.literal("No abilities to register"));
            } else if (sequence >= 8) {
                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
            } else if (sequence == 7) {
                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
            } else if (sequence == 6) {
                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
            } else if (sequence == 5) {
                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
                executeCommand(server, "/abilityput RRRRR lotm:sailorlightning");
                executeCommand(server, "/abilityput LLRRL lotm:acidicrain");
                executeCommand(server, "/abilityput LRRLL lotm:watersphere");
            } else if (sequence == 4) {
                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
                executeCommand(server, "/abilityput RRRRR lotm:sailorlightning");
                executeCommand(server, "/abilityput LLRRL lotm:acidicrain");
                executeCommand(server, "/abilityput LRRLL lotm:watersphere");
                executeCommand(server, "/abilityput LLLRR lotm:tornado");
                executeCommand(server, "/abilityput LLLLR lotm:roar");
                executeCommand(server, "/abilityput RRRLL lotm:earthquake");
            } else if (sequence == 3) {
                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
                executeCommand(server, "/abilityput RRRRR lotm:sailorlightning");
                executeCommand(server, "/abilityput LLRRL lotm:acidicrain");
                executeCommand(server, "/abilityput LRRLL lotm:watersphere");
                executeCommand(server, "/abilityput LLLRR lotm:tornado");
                executeCommand(server, "/abilityput LLLLR lotm:roar");
                executeCommand(server, "/abilityput RRRLL lotm:earthquake");
                executeCommand(server, "/abilityput LRRRR lotm:sonicboom");
                executeCommand(server, "/abilityput RRRRL lotm:lightningbranch");
                executeCommand(server, "/abilityput RRLLR lotm:thunderclap");
            } else if (sequence == 2) {
                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
                executeCommand(server, "/abilityput RRRRR lotm:sailorlightning");
                executeCommand(server, "/abilityput LLRRL lotm:acidicrain");
                executeCommand(server, "/abilityput LRRLL lotm:watersphere");
                executeCommand(server, "/abilityput LLLRR lotm:tornado");
                executeCommand(server, "/abilityput LLLLR lotm:roar");
                executeCommand(server, "/abilityput RRRLL lotm:earthquake");
                executeCommand(server, "/abilityput LRRRR lotm:sonicboom");
                executeCommand(server, "/abilityput RRRRL lotm:lightningbranch");
                executeCommand(server, "/abilityput RRLLR lotm:thunderclap");
                executeCommand(server, "/abilityput LRLRL lotm:lightningball");
                executeCommand(server, "/abilityput RRLLL lotm:extremecoldness");
                executeCommand(server, "/abilityput LRRLR lotm:raineyes");
                executeCommand(server, "/abilityput RLRRL lotm:volcaniceruption");
            } else if (sequence == 1) {

                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
                executeCommand(server, "/abilityput RRRRR lotm:sailorlightning");
                executeCommand(server, "/abilityput LLRRL lotm:acidicrain");
                executeCommand(server, "/abilityput LRRLL lotm:watersphere");
                executeCommand(server, "/abilityput LLLRR lotm:tornado");
                executeCommand(server, "/abilityput LLLLR lotm:roar");
                executeCommand(server, "/abilityput RRRLL lotm:earthquake");
                executeCommand(server, "/abilityput LRRRR lotm:sonicboom");
                executeCommand(server, "/abilityput RRRRL lotm:lightningbranch");
                executeCommand(server, "/abilityput RRLLR lotm:thunderclap");
                executeCommand(server, "/abilityput LRLRL lotm:lightningball");
                executeCommand(server, "/abilityput RRLLL lotm:extremecoldness");
                executeCommand(server, "/abilityput LRRLR lotm:raineyes");
                executeCommand(server, "/abilityput RLRRL lotm:volcaniceruption");
                executeCommand(server, "/abilityput LRLLL lotm:lightningballabsorb");
                executeCommand(server, "/abilityput RRRLR lotm:sailorlightningtravel");
                executeCommand(server, "/abilityput LLRRR lotm:staroflightning");
                executeCommand(server, "/abilityput LRLLR lotm:lightningredirection");

            } else if (sequence == 0) {
                executeCommand(server, "/abilityput LLLLL lotm:ragingblows");
                executeCommand(server, "/abilityput RRRRR lotm:sailorlightning");
                executeCommand(server, "/abilityput LLRRL lotm:acidicrain");
                executeCommand(server, "/abilityput LRRLL lotm:watersphere");
                executeCommand(server, "/abilityput LLLRR lotm:tornado");
                executeCommand(server, "/abilityput LLLLR lotm:roar");
                executeCommand(server, "/abilityput RRRLL lotm:earthquake");
                executeCommand(server, "/abilityput LRRRR lotm:sonicboom");
                executeCommand(server, "/abilityput RRRRL lotm:lightningbranch");
                executeCommand(server, "/abilityput RRLLR lotm:thunderclap");
                executeCommand(server, "/abilityput LRLRL lotm:lightningball");
                executeCommand(server, "/abilityput RRLLL lotm:extremecoldness");
                executeCommand(server, "/abilityput LRRLR lotm:raineyes");
                executeCommand(server, "/abilityput RLRRL lotm:volcaniceruption");
                executeCommand(server, "/abilityput LRLLL lotm:lightningballabsorb");
                executeCommand(server, "/abilityput RRRLR lotm:sailorlightningtravel");
                executeCommand(server, "/abilityput LLRRR lotm:staroflightning");
                executeCommand(server, "/abilityput LRLLR lotm:lightningredirection");
                executeCommand(server, "/abilityput RLLLL lotm:tyranny");
                executeCommand(server, "/abilityput RLLLR lotm:stormseal");
            }
        }
    }


    public static boolean isPhysicalDamage(DamageSource source) {
        return source.is(DamageTypes.FALL) || source.is(DamageTypes.CACTUS) || source.is(DamageTypes.FLY_INTO_WALL) || source.is(DamageTypes.GENERIC) || source.is(DamageTypes.FALLING_BLOCK) || source.is(DamageTypes.FALLING_ANVIL) || source.is(DamageTypes.FALLING_STALACTITE) || source.is(DamageTypes.STING) ||
                source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO) || source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.ARROW) || source.is(DamageTypes.TRIDENT) || source.is(DamageTypes.MOB_PROJECTILE) || source.is(DamageTypes.FIREBALL) || source.is(DamageTypes.UNATTRIBUTED_FIREBALL) ||
                source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.WITHER_SKULL) || source.is(DamageTypes.PLAYER_EXPLOSION) || source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.FIREWORKS) || source.is(DamageTypes.FREEZE) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LAVA) || source.is(DamageTypes.HOT_FLOOR) || source.is(DamageTypes.HOT_FLOOR);
    }

    public static boolean isSupernaturalDamage(DamageSource source) {
        return source.is(DamageTypes.MAGIC) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LIGHTNING_BOLT) || source.is(DamageTypes.DRAGON_BREATH) || source.is(DamageTypes.WITHER) || source.is(MENTAL_DAMAGE);
    }


    public static int getMentalStrength(LivingEntity livingEntity) {
        int mentalStrength = 10; // Default value
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity instanceof Player player) {
                BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                mentalStrength = Math.max(1, holder.getMentalStrength());
            } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
                mentalStrength = Math.max(1, playerMobEntity.getMentalStrength());
            } else {
                mentalStrength = Math.max(1, (int) (livingEntity.getMaxHealth() / 2));
            }
        }
        return mentalStrength;
    }

    public static boolean isPurifiable(LivingEntity livingEntity) {
        return livingEntity.getName().getString().toLowerCase().contains("skeleton") || livingEntity.getName().getString().toLowerCase().contains("demon") || livingEntity.getName().getString().toLowerCase().contains("ghost") || livingEntity.getName().getString().toLowerCase().contains("wraith") || livingEntity.getName().getString().toLowerCase().contains("zombie") || livingEntity.getName().getString().toLowerCase().contains("undead") || livingEntity.getPersistentData().getBoolean("isWraith");

    }

    public static boolean isAllyOf(LivingEntity livingEntity, LivingEntity ally) {
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            PlayerAllyData allyData = serverLevel.getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
            return allyData.areAllies(livingEntity.getUUID(), ally.getUUID());
        }
        return false;
    }

    public static void useSpirituality(LivingEntity living, int spirituality) {
        if (!living.level().isClientSide()) {
            if (living instanceof Player player) {
                BeyonderHolderAttacher.getHolderUnwrap(player).useSpirituality(spirituality);
            } else if (living instanceof PlayerMobEntity playerMobEntity) {
                playerMobEntity.useSpirituality(spirituality);
            }
        }
    }

    public static void addSpirituality(LivingEntity living, int spirituality) {
        if (!living.level().isClientSide()) {
            if (living instanceof Player player) {
                BeyonderHolderAttacher.getHolderUnwrap(player).setSpirituality(getSpirituality(living) + spirituality);
            } else if (living instanceof PlayerMobEntity playerMobEntity) {
                playerMobEntity.setSpirituality(getSpirituality(playerMobEntity) + spirituality);
            }
        }
    }

    public static int getSpirituality(LivingEntity living) {
        if (!living.level().isClientSide()) {
            if (living instanceof Player player) {
                return (int) BeyonderHolderAttacher.getHolderUnwrap(player).getSpirituality();
            } else if (living instanceof PlayerMobEntity playerMobEntity) {
                return playerMobEntity.getSpirituality();
            } else {
                return 0;
            }
        }
        return 0;
    }

    public static final Map<Item, Potion> EFFECT_INGREDIENTS = new HashMap<>() {{
        put(Items.SUGAR, Potions.SWIFTNESS);
        put(Items.RABBIT_FOOT, Potions.LEAPING);
        put(Items.GLISTERING_MELON_SLICE, Potions.HEALING);
        put(Items.SPIDER_EYE, Potions.POISON);
        put(Items.PUFFERFISH, Potions.WATER_BREATHING);
        put(Items.MAGMA_CREAM, Potions.FIRE_RESISTANCE);
        put(Items.GOLDEN_CARROT, Potions.NIGHT_VISION);
        put(Items.BLAZE_POWDER, Potions.STRENGTH);
        put(Items.GHAST_TEAR, Potions.REGENERATION);
        put(Items.TURTLE_HELMET, Potions.TURTLE_MASTER);
        put(Items.PHANTOM_MEMBRANE, Potions.SLOW_FALLING);
    }};

    public static Potion getPotionForIngredient(Item ingredient) {
        return EFFECT_INGREDIENTS.get(ingredient);
    }

    public static boolean isValidPotionIngredient(Item ingredient) {
        return EFFECT_INGREDIENTS.containsKey(ingredient);
    }

    public static void createSphereOfParticles(ServerLevel level, Vec3 center, ParticleOptions particleOptions, int particleCount, double travelDistance) {
        if (!(level instanceof ServerLevel)) {
            return;
        }

        double goldenRatio = (1 + Math.sqrt(5)) / 2;
        double angleIncrement = Math.PI * 2 * goldenRatio;

        double velocityScale = travelDistance / 2.2;

        for (int i = 0; i < particleCount; i++) {
            double t = (double) i / particleCount;
            double inclination = Math.acos(1 - 2 * t);
            double azimuth = angleIncrement * i;

            // Calculate direction vector
            double x = Math.sin(inclination) * Math.cos(azimuth);
            double y = Math.sin(inclination) * Math.sin(azimuth);
            double z = Math.cos(inclination);

            // Calculate velocities - these will determine the direction and speed
            double velocityX = x * velocityScale;
            double velocityY = y * velocityScale;
            double velocityZ = z * velocityScale;

            // The particles should start at the center position with zero offset
            level.sendParticles(
                    particleOptions,
                    center.x, // spawn X
                    center.y, // spawn Y
                    center.z, // spawn Z
                    1,  // count per particle location
                    velocityX, // x velocity
                    velocityY, // y velocity
                    velocityZ, // z velocity
                    1.0 // speed modifier - set to 1.0 to use our custom velocities
            );
        }
    }

    public static void createShphereParticlesPlayerCenter(LivingEntity livingEntity, ParticleOptions particle, int particleCount, double speed) {
        double x = livingEntity.getX();
        double y = livingEntity.getY();
        double z = livingEntity.getZ();

        // Calculate the step size for each particle in the cube
        double step = 2.0 / (particleCount - 1);

        for (int i = 0; i < particleCount; i++) {
            // Calculate the position within the cube
            double offsetX = -1.0 + i * step;
            double offsetY = -1.0 + i * step;
            double offsetZ = -1.0 + i * step;

            // Calculate the velocity components to expand the cube
            double velocityX = offsetX * speed;
            double velocityY = offsetY * speed;
            double velocityZ = offsetZ * speed;

            // Create the particle packet
            SendParticleS2C packet = new SendParticleS2C(
                    particle,
                    x + offsetX, y + offsetY, z + offsetZ,
                    velocityX, velocityY, velocityZ
            );

            // Send the packet (assuming you have a method to send it)
            LOTMNetworkHandler.sendToAllPlayers(packet);
        }
    }

    public static List<LivingEntity> checkEntitiesInLocation(LivingEntity livingEntity, float inflation, int X, int Y, int Z) {
        AABB box = new AABB(X - inflation, Y - inflation, Z - inflation, X + inflation, Y + inflation, Z + inflation);
        return livingEntity.level().getEntitiesOfClass(LivingEntity.class, box);
    }

    public static void ageHandlerTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int age = tag.getInt("age");
        int maxAge = (int) (livingEntity.getMaxHealth() * 5);
        boolean tenPercent = age >= maxAge * 0.1;
        boolean twentyPercent = age >= maxAge * 0.2;
        boolean thirtyPercent = age >= maxAge * 0.3;
        boolean fortyPercent = age >= maxAge * 0.4;
        boolean fiftyPercent = age >= maxAge * 0.5;
        boolean sixtyPercent = age >= maxAge * 0.6;
        boolean seventyPercent = age >= maxAge * 0.7;
        boolean eightyPercent = age >= maxAge * 0.8;
        boolean ninetyPercent = age >= maxAge * 0.9;
        boolean oneHundredPercent = age >= maxAge;
        int sequence = BeyonderUtil.getSequence(livingEntity);
        if (!livingEntity.level().isClientSide()) {
            int ageDecay = tag.getInt("ageDecay");
            if (ageDecay >= 1) {
                tag.putInt("ageDecay", ageDecay - 1);
                for (int i = 0; i <= 20; i++) {
                    double random = (Math.random() * 4) - 2;
                    double scaleAmount = Math.max(2, ScaleTypes.BASE.getScaleData(livingEntity).getScale() * Math.random() * 2);
                    double x = livingEntity.getX() + ((scaleAmount * Math.random()) - (scaleAmount * Math.random()));
                    double y = livingEntity.getY() + ((scaleAmount * Math.random()) - (scaleAmount * Math.random()));
                    double z = livingEntity.getZ() + ((scaleAmount * Math.random()) - (scaleAmount * Math.random()));
                    LOTMNetworkHandler.sendToAllPlayers(new SendParticleS2C(ParticleTypes.WHITE_ASH, x, y, z, random, random, random));
                }
                if (ageDecay == 1) {
                    for (int i = 0; i <= 150; i++) {
                        double random = (Math.random() * 4) - 2;
                        double scaleAmount = Math.max(2, ScaleTypes.BASE.getScaleData(livingEntity).getScale() * Math.random() * 2);
                        double x = livingEntity.getX() + ((scaleAmount * Math.random()) - (scaleAmount * Math.random()));
                        double y = livingEntity.getY() + ((scaleAmount * Math.random()) - (scaleAmount * Math.random()));
                        double z = livingEntity.getZ() + ((scaleAmount * Math.random()) - (scaleAmount * Math.random()));
                        LOTMNetworkHandler.sendToAllPlayers(new SendParticleS2C(ParticleTypes.WHITE_ASH, x, y, z, random, random, random));
                    }
                    tag.putInt("age", 0);
                    tag.putInt("ageDecay", 0);
                    livingEntity.kill();
                    livingEntity.sendSystemMessage(Component.literal("You died due to aging."));
                }
            }
            if (age >= 1) {
                if (livingEntity.tickCount % 1200 == 0) {
                    tag.putInt("age", Math.max(0, age - sequence));
                }
                if (oneHundredPercent) {
                    tag.putInt("age", 0);
                    tag.putInt("ageDecay", 100);
                }

                if (ninetyPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 20, 2, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 4, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WEAKNESS, 20, 4, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.DIG_SLOWDOWN, 20, 3, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WITHER, 20, 5, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.NOREGENERATION.get(), 20, 1, true, true);
                } else if (eightyPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 20, 2, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 3, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WEAKNESS, 20, 4, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.DIG_SLOWDOWN, 20, 3, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WITHER, 20, 4, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.NOREGENERATION.get(), 20, 1, true, true);
                } else if (seventyPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 3, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WEAKNESS, 20, 3, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.DIG_SLOWDOWN, 20, 2, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WITHER, 20, 3, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.NOREGENERATION.get(), 20, 1, true, true);
                } else if (sixtyPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.NOREGENERATION.get(), 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 3, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WEAKNESS, 20, 3, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.DIG_SLOWDOWN, 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WITHER, 20, 2, true, true);
                } else if (fiftyPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 2, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WEAKNESS, 20, 2, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.DIG_SLOWDOWN, 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WITHER, 20, 1, true, true);

                } else if (fortyPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WITHER, 20, 0, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 20, 0, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WEAKNESS, 20, 2, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.DIG_SLOWDOWN, 20, 0, true, true);
                } else if (thirtyPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 20, 0, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WEAKNESS, 20, 1, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.DIG_SLOWDOWN, 20, 0, true, true);
                } else if (twentyPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 0, true, true);
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.WEAKNESS, 20, 0, true, true);
                } else if (tenPercent) {
                    BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 20, 0, true, true);
                }
            }
        }
    }

    public static void ageHandlerHurt(LivingHurtEvent event) {
        if (!event.getEntity().level().isClientSide() && event.getEntity().getPersistentData().getInt("age") >= event.getEntity().getMaxHealth() * 5) {
            if (isPhysicalDamage(event.getSource())) {
                event.setAmount(event.getAmount() * 1.5f);
            }
        }
    }

    public static boolean scribeLookingAtYou(Player target, LivingEntity scribe) {
        double radius = 30.0;
        double angleThreshold = 45.0;
        if (currentPathwayAndSequenceMatchesNoException(scribe,BeyonderClassInit.APPRENTICE.get(), 6)) {
            Vec3 scribePos = scribe.position();
            Vec3 targetPos = target.position();
            double distanceQr = scribePos.distanceToSqr(targetPos);
            if (distanceQr > radius * radius) {
                return false;
            }
            Vec3 lookVec = scribe.getLookAngle().normalize();
            Vec3 toTargetVec = targetPos.subtract(scribePos).normalize();
            double dotProduct = lookVec.dot(toTargetVec);
            double angle = Math.toDegrees(Math.acos(dotProduct));

            return angle < angleThreshold;
        }
        return false;
    }

    public static boolean isBeyonder(LivingEntity livingEntity) {
        return getSequence(livingEntity) != -1;
    }

    public static boolean isSmeltable(ItemStack itemStack, Level world) {
        SimpleContainer container = new SimpleContainer(itemStack);
        return world.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, world).isPresent();
    }

    public static ItemStack getSmeltingResult(ItemStack itemStack, Level world) {
        SimpleContainer container = new SimpleContainer(itemStack);
        return world.getRecipeManager().getRecipeFor(RecipeType.SMELTING, container, world).map(recipe -> recipe.getResultItem(world.registryAccess())).orElse(ItemStack.EMPTY);
    }

    public static Boolean isEntityColliding(Entity entity, Level level, Double radius) {
        AABB entityBoundingBox = entity.getBoundingBox();
        for (Entity otherEntity : level.getEntitiesOfClass(Entity.class, entity.getBoundingBox().inflate(radius))) {
            if (entityBoundingBox.intersects(otherEntity.getBoundingBox())) {
                return true;
            }
        }
        return false;
    }

    public static Entity checkEntityCollision(Entity entity, Level level, Double radius) {
        AABB entityBoundingBox = entity.getBoundingBox();
        AABB searchArea = entityBoundingBox.inflate(radius);
        for (Entity otherEntity : level.getEntities(entity, searchArea, otherEntity -> true)) {
            if (entityBoundingBox.intersects(otherEntity.getBoundingBox())) {
                return otherEntity;
            }
        }
        return null;
    }

    public static void spawnDoorTeleportationOnly(Level level, BlockPos pos, double X, double Y, double Z, Entity canPassTrough, Direction direction, int life, LivingEntity user) {
        int sequence = getSequence(user);
        double x = pos.getX();
        double y = pos.getY();
        double z = pos.getZ();
        float YAW = 0F;
        if (direction == Direction.NORTH) {
            x = x + 0.5;
            z = z + 0.25;
            YAW = 0F;
        } else if (direction == Direction.WEST) {
            x = x + 0.25;
            z = z + 0.5;
            YAW = 90F;
        } else if (direction == Direction.SOUTH) {
            x = x + 0.5;
            z = z + 0.75;
            YAW = 180;
        } else if (direction == Direction.EAST) {
            x = x + 0.75;
            z = z + 0.5;
            YAW = 270F;
        }
        LowSequenceDoorEntity lowDoor = new LowSequenceDoorEntity(canPassTrough, level, X, Y, Z, YAW, life);
        MidSequenceDoorEntity midDoor = new MidSequenceDoorEntity(level, X, Y, Z, YAW, life);

        if (sequence >= 8) {
            lowDoor.setPos(x, y, z);
            level.addFreshEntity(lowDoor);
        } else if (sequence >= 5) {
            midDoor.setPos(x, y, z);
            level.addFreshEntity(midDoor);
        }

    }


    public static LivingEntity getEntityFromUUID(Level level, UUID uuid) {
        if (level instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(uuid);
            if (entity instanceof LivingEntity livingEntity) {
                return livingEntity;
            }
        }
        return null;
    }

    public static boolean currentPathwayMatches(LivingEntity livingEntity, BeyonderClass matchingPathway) {
        if (getPathway(livingEntity) == matchingPathway || (getPathway(livingEntity) == BeyonderClassInit.APPRENTICE.get() && getSequence(livingEntity) <= 6)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean currentPathwayMatchesNoException(LivingEntity livingEntity, BeyonderClass matchingPathway) {
        if (getPathway(livingEntity) == matchingPathway) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean currentPathwayAndSequenceMatches(LivingEntity livingEntity, BeyonderClass matchingPathway, int sequence) {
        if ((getPathway(livingEntity) == matchingPathway && getSequence(livingEntity) <= sequence) || (getPathway(livingEntity) == BeyonderClassInit.APPRENTICE.get() && getSequence(livingEntity) <= 6)) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean currentPathwayAndSequenceMatchesNoException(LivingEntity livingEntity, BeyonderClass matchingPathway, int sequence) {
        if (getPathway(livingEntity) == matchingPathway && getSequence(livingEntity) <= sequence) {
            return true;
        } else {
            return false;
        }
    }

    public static void setSpirituality(LivingEntity livingEntity, int spirituality) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity instanceof Player player) {
                BeyonderHolderAttacher.getHolderUnwrap(player).setSpirituality(spirituality);
            } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
                playerMobEntity.setSpirituality(spirituality);
            }
        }
    }

    public static boolean sequenceAbleCopy(LivingEntity entity) {
        int sequence = getSequence(entity);
        if (getPathway(entity) == BeyonderClassInit.APPRENTICE.get() && sequence <= 6) {
            return true;
        }
        return false;
    }

    public static boolean sequenceAbleCopy(BeyonderHolder holder) {
        int sequence = holder.getSequence();
        if (holder.currentClassMatches(BeyonderClassInit.APPRENTICE.get()) && sequence <= 6) {
            return true;
        }
        return false;
    }

    public static void copyAbilities(Level level, Player player, SimpleAbilityItem ability) {
        int playerSequence = getSequence(player);
        int abilitySequence = ability.getRequiredSequence();
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(50))) {
            if (BeyonderUtil.isBeyonderCapable(entity) && entity != player) {
                if (currentPathwayAndSequenceMatchesNoException(player, BeyonderClassInit.APPRENTICE.get(), 6)) {
                    if (entity instanceof Player scribe) {
                        if (BeyonderUtil.scribeLookingAtYou(player, scribe)) {
                            if (checkValidAbilityCopy(new ItemStack(ability))) {
                                if (player.getCapability(CapabilityScribeAbilities.SCRIBE_CAPABILITY, null).map(storage -> storage.getScribedAbilitiesCount()).orElse(0) < player.getPersistentData().getInt("maxScribedAbilities")) {
                                    if (copyAbilityTest(playerSequence, abilitySequence)) {
                                        if (!pendingAbilityCopies.containsKey(scribe.getUUID())) {
                                            pendingAbilityCopies.put(scribe.getUUID(), ability);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void confirmCopyAbility(Player player) {
        if (pendingAbilityCopies.containsKey(player.getUUID())) {
            if (!player.isShiftKeyDown()) {
                player.getPersistentData().putBoolean("acceptCopiedAbility", true);
            } else {
                player.getPersistentData().putBoolean("deleteCopiedAbility", true);
            }

        }
    }

    public static void copyAbilityTick(Player player) {
        Iterator<Map.Entry<UUID, SimpleAbilityItem>> iterator = pendingAbilityCopies.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, SimpleAbilityItem> entry = iterator.next();
            SimpleAbilityItem ability = entry.getValue();
            UUID uuid = entry.getKey();
            if (player.getUUID().equals(uuid)) {
                player.displayClientMessage(Component.literal("Trying to copy: ").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD).append(Component.literal(ability.getDefaultInstance().getHoverName().getString()).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD)), true);
                if (player.getPersistentData().getBoolean("acceptCopiedAbility")) {
                    player.getPersistentData().putBoolean("acceptCopiedAbility", false);
                    player.getCapability(CapabilityScribeAbilities.SCRIBE_CAPABILITY, null).ifPresent(storage -> {
                        storage.copyScribeAbility(ability);
                        player.displayClientMessage(Component.literal("Successfully copied: ").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD).append(Component.literal(ability.getDefaultInstance().getHoverName().getString()).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD)), true);
                    });
                    iterator.remove();
                } else if (player.getPersistentData().getBoolean("deleteCopiedAbility")) {
                    player.getPersistentData().putBoolean("deleteCopiedAbility", false);
                    player.displayClientMessage(Component.literal("You have given up on copying: ").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD).append(Component.literal(ability.getDefaultInstance().getHoverName().getString()).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD)), true);
                    iterator.remove();
                }
            }
        }
    }

    public static void useCopiedAbility(Player player, Item ability) {
        if (currentPathwayAndSequenceMatchesNoException(player, BeyonderClassInit.APPRENTICE.get(), 6)) {
            player.getCapability(CapabilityScribeAbilities.SCRIBE_CAPABILITY, null).ifPresent(storage -> {
                storage.useScribeAbility(ability);
            });
        }
    }

    public static boolean checkAbilityIsCopied(Player player, Item ability) {
        int sequence = BeyonderUtil.getSequence(player);
        if (BeyonderUtil.getPathway(player) == BeyonderClassInit.APPRENTICE.get()
                && sequence <= 6) {
            return player.getCapability(CapabilityScribeAbilities.SCRIBE_CAPABILITY, null)
                    .map(storage -> storage.hasScribedAbility(ability))
                    .orElse(false);
        }
        return false;
    }

    public static boolean copyAbilityTest(int copierSequence, int targetAbilitySequence) {
        double chance = 0.3 + (0.7 / 9) * (targetAbilitySequence - copierSequence);
        chance = Math.max(0.05, Math.min(chance, 1));

        return Math.random() < chance;
    }

    public static boolean checkValidAbilityCopy(ItemStack ability) {
        List<ItemStack> invalidAbilities = new ArrayList<>();
        invalidAbilities.add(new ItemStack(ItemInit.GIGANTIFICATION.get()));
        invalidAbilities.add(new ItemStack(ItemInit.FATEDCONNECTION.get()));

        for (ItemStack invalidAbility : invalidAbilities) {
            if (ItemStack.isSameItem(ability, invalidAbility)) {
                return false;
            }
        }
        return true;
    }

    public static void setPathway(LivingEntity livingEntity, BeyonderClass pathway) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity instanceof Player player) {
                BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                holder.setPathway(pathway);
            } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
                playerMobEntity.setPathway(pathway);
            } else {
                return;
            }
        }
    }

    public static void setSequence(LivingEntity livingEntity, int sequence) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity instanceof Player player) {
                BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                holder.setSequence(sequence);
            } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
                playerMobEntity.setSequence(sequence);
            } else {
                return;
            }
        }
    }

    public static void setPathwayAndSequence(LivingEntity livingEntity, BeyonderClass pathway, int sequence) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity instanceof Player player) {
                BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                holder.setSequence(sequence);
                holder.setPathway(pathway);
            } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
                playerMobEntity.setSequence(sequence);
                playerMobEntity.setPathway(pathway);
            } else {
                return;
            }
        }
    }

    public static void removePathway(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity instanceof Player player) {
                BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                holder.removePathway();
            } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
                playerMobEntity.setPathway(null);
                playerMobEntity.setSequence(-1);
            } else {
                return;
            }
        }
    }

    public static void setScale(Entity entity, float scale) {
        if (!entity.level().isClientSide()) {
            ScaleTypes.BASE.getScaleData(entity).setScale(scale);
        }
    }

    public static void setTargetScale(Entity entity, float scale) {
        if (!entity.level().isClientSide()) {
            ScaleTypes.BASE.getScaleData(entity).setTargetScale(scale);
        }
    }
}