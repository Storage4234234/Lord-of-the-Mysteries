package net.swimmingtuna.lotm.util;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
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
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
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
import net.minecraft.world.entity.player.Abilities;
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
import net.swimmingtuna.lotm.beyonder.*;
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
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.TravelDoor;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.TravelDoorWaypoint;
import net.swimmingtuna.lotm.item.BeyonderAbilities.BeyonderAbilityUser;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.*;
import net.swimmingtuna.lotm.item.OtherItems.SwordOfSilver;
import net.swimmingtuna.lotm.item.OtherItems.SwordOfTwilight;
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
import java.util.function.Predicate;

import static net.swimmingtuna.lotm.commands.BeyonderRecipeCommand.executeRecipeCommand;
import static net.swimmingtuna.lotm.init.DamageTypeInit.MENTAL_DAMAGE;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.CycleOfFate.removeCycleEffect;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems.DreamIntoReality.stopFlying;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems.PsychologicalInvisibility.removePsychologicalInvisibilityEffect;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.TwilightFreeze.removeTwilightFreezeEffect;

public class BeyonderUtil {

    private static final Map<UUID, SimpleAbilityItem> pendingAbilityCopies = new HashMap<>();

    public static Projectile getProjectiles(LivingEntity livingEntity) {
        if (livingEntity.level().isClientSide()) {
            return null;
        }
        Level level = livingEntity.level();
        AABB boundingBox = livingEntity.getBoundingBox().inflate(50);
        Predicate<Projectile> projectilePredicate = projectile -> {
            if (projectile.tickCount <= 6 || projectile.tickCount >= 100) {
                return false;
            }
            if (projectile.getOwner() != livingEntity) {
                return false;
            }

            CompoundTag tag = projectile.getPersistentData();
            return tag.getInt("windDodgeProjectilesCounter") == 0;
        };
        return level.getEntitiesOfClass(Projectile.class, boundingBox, projectilePredicate).stream()
                .findFirst()
                .orElse(null);
    }

    public static void projectileEvent(LivingEntity living) {
        //PROJECTILE EVENT
        Projectile projectile = BeyonderUtil.getProjectiles(living);
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
                    if (currentPathwayAndSequenceMatches(living, BeyonderClassInit.SAILOR.get(), 0)) {
                        livingEntity.hurt(livingEntity.damageSources().lightningBolt(), 40);
                    }
                }
            }
        }
        LivingEntity target = BeyonderUtil.getTarget(projectile, 75, 0);
        if (target != null) {
            if (BeyonderUtil.currentPathwayAndSequenceMatches(living, BeyonderClassInit.SAILOR.get(), 8) && living.getPersistentData().getBoolean("sailorProjectileMovement")) {
                double dx = target.getX() - projectile.getX();
                double dy = target.getY() - projectile.getY();
                double dz = target.getZ() - projectile.getZ();
                double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double speed = 1.2;
                projectile.setDeltaMovement((dx / length) * speed, (dy / length) * speed, (dz / length) * speed);
                projectile.hurtMarked = true;
            }
            //APPRENTICE BOUNCE SHOT ARROWS
            if (BeyonderUtil.currentPathwayAndSequenceMatches(living, BeyonderClassInit.APPRENTICE.get(), 8) && living.getPersistentData().getBoolean("ApprenticeBounceProjectileMovement")) {
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
            if (BeyonderUtil.currentPathwayAndSequenceMatches(living, BeyonderClassInit.SAILOR.get(), 8) && living.getPersistentData().getBoolean("sailorProjectileMovement")) {
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
            if (BeyonderUtil.currentPathwayAndSequenceMatches(living, BeyonderClassInit.APPRENTICE.get(), 8) && living.getPersistentData().getBoolean("monsterProjectileControl")) {
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
            if (entity != owner && !entity.level().isClientSide() && (owner instanceof LivingEntity living && !BeyonderUtil.areAllies(living, entity))) {
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

    public static List<Item> getAbilities(LivingEntity livingEntity) {
        List<Item> abilityNames = new ArrayList<>();
        if (livingEntity.level().isClientSide()) {
            return abilityNames;
        }
        int sequence = getSequence(livingEntity);
        if (currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.SPECTATOR.get())) {
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
                abilityNames.add(ItemInit.PROPHECY.get());
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
                abilityNames.add(ItemInit.ENVISION_WEATHER.get());
            }
        }

        if (currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.SAILOR.get())) {
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
        if (currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.MONSTER.get())) {
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
        }
        if (currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.WARRIOR.get())) {
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
        if (currentPathwayMatchesNoException(livingEntity, BeyonderClassInit.APPRENTICE.get())) {
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
        return abilityNames;
    }

    private static final Map<ChatFormatting, Integer> COLOR_MAP = new HashMap<>();
    private static final Map<String, BeyonderClass> NAME_TO_BEYONDER = new HashMap<>();

    static {
        COLOR_MAP.put(ChatFormatting.BLACK, 0x000000);
        COLOR_MAP.put(ChatFormatting.DARK_BLUE, 0x0000AA);
        COLOR_MAP.put(ChatFormatting.DARK_GREEN, 0x00AA00);
        COLOR_MAP.put(ChatFormatting.DARK_AQUA, 0x00AAAA);
        COLOR_MAP.put(ChatFormatting.DARK_RED, 0xAA0000);
        COLOR_MAP.put(ChatFormatting.DARK_PURPLE, 0xAA00AA);
        COLOR_MAP.put(ChatFormatting.GOLD, 0xFFAA00);
        COLOR_MAP.put(ChatFormatting.GRAY, 0xAAAAAA);
        COLOR_MAP.put(ChatFormatting.DARK_GRAY, 0x555555);
        COLOR_MAP.put(ChatFormatting.BLUE, 0x5555FF);
        COLOR_MAP.put(ChatFormatting.GREEN, 0x55FF55);
        COLOR_MAP.put(ChatFormatting.AQUA, 0x55FFFF);
        COLOR_MAP.put(ChatFormatting.RED, 0xFF5555);
        COLOR_MAP.put(ChatFormatting.LIGHT_PURPLE, 0xFF55FF);
        COLOR_MAP.put(ChatFormatting.YELLOW, 0xFFFF55);
        COLOR_MAP.put(ChatFormatting.WHITE, 0xFFFFFF);
        NAME_TO_BEYONDER.put("Apothecary", new ApothecaryClass());
        NAME_TO_BEYONDER.put("Apprentice", new ApprenticeClass());
        NAME_TO_BEYONDER.put("Arbiter", new ArbiterClass());
        NAME_TO_BEYONDER.put("Assassin", new AssassinClass());
        NAME_TO_BEYONDER.put("Bard", new BardClass());
        NAME_TO_BEYONDER.put("Corpse Collector", new CorpseCollectorClass());
        NAME_TO_BEYONDER.put("Criminal", new CriminalClass());
        NAME_TO_BEYONDER.put("Hunter", new HunterClass());
        NAME_TO_BEYONDER.put("Lawyer", new LawyerClass());
        NAME_TO_BEYONDER.put("Marauder", new MarauderClass());
        NAME_TO_BEYONDER.put("Monster", new MonsterClass());
        NAME_TO_BEYONDER.put("Mystery Pryer", new MysteryPryerClass());
        NAME_TO_BEYONDER.put("Planter", new PlanterClass());
        NAME_TO_BEYONDER.put("Prisoner", new PrisonerClass());
        NAME_TO_BEYONDER.put("Reader", new ReaderClass());
        NAME_TO_BEYONDER.put("Sailor", new SailorClass());
        NAME_TO_BEYONDER.put("Savant", new SavantClass());
        NAME_TO_BEYONDER.put("Secret Supplicant", new SecretsSupplicantClass());
        NAME_TO_BEYONDER.put("Seer", new SeerClass());
        NAME_TO_BEYONDER.put("Sleepless", new SleeplessClass());
        NAME_TO_BEYONDER.put("Spectator", new SpectatorClass());
        NAME_TO_BEYONDER.put("Warrior", new WarriorClass());
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
        boolean hasEntityInteraction = false;
        boolean hasBlockInteraction = false;
        boolean hasGeneralAbility = false;

        try {
            Method entityMethod = ability.getClass().getMethod("useAbilityOnEntity", ItemStack.class, LivingEntity.class, LivingEntity.class, InteractionHand.class);
            hasEntityInteraction = !entityMethod.getDeclaringClass().equals(Ability.class);
        } catch (NoSuchMethodException ignored) {
        }

        try {
            Method blockMethod = ability.getClass().getMethod("useAbilityOnBlock", UseOnContext.class);
            hasBlockInteraction = !blockMethod.equals(Ability.class.getDeclaredMethod("useAbilityOnBlock", UseOnContext.class));
        } catch (NoSuchMethodException ignored) {
        }

        try {
            Method generalMethod = ability.getClass().getDeclaredMethod("useAbility", Level.class, LivingEntity.class, InteractionHand.class);
            hasGeneralAbility = !generalMethod.equals(Ability.class.getDeclaredMethod("useAbility", Level.class, LivingEntity.class, InteractionHand.class));
        } catch (NoSuchMethodException ignored) {
        }
        if (hasEntityInteraction) {
            Vec3 eyePosition = player.getEyePosition();
            Vec3 lookVector = player.getLookAngle();
            Vec3 reachVector = eyePosition.add(lookVector.scale(entityReach));
            AABB searchBox = player.getBoundingBox().inflate(entityReach);
            EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player.level(), player, eyePosition, reachVector, searchBox,
                    entity -> !entity.isSpectator() && entity.isPickable(), 0.1f);

            if (entityHit != null && entityHit.getEntity() instanceof LivingEntity livingEntity) {
                if (player.level().isEmptyBlock(livingEntity.blockPosition().above())) {
                    InteractionResult result = ability.useAbilityOnEntity(player.getItemInHand(hand), player, livingEntity, hand);
                    if (result != InteractionResult.PASS) {
                        successfulUse = true;
                    }
                }
            } else {
                List<LivingEntity> possibleTargets = player.level().getEntitiesOfClass(LivingEntity.class, searchBox,
                        entity -> !entity.isSpectator() && entity.isPickable() && entity != player);

                LivingEntity bestTarget = null;
                double bestDotProduct = 0.9915;
                for (LivingEntity target : possibleTargets) {
                    Vec3 toEntity = target.getEyePosition().subtract(eyePosition).normalize();
                    double dotProduct = toEntity.dot(lookVector);
                    if (dotProduct > bestDotProduct) {
                        BlockHitResult hitResult = player.level().clip(new ClipContext(
                                eyePosition, target.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

                        if (hitResult.getType() == HitResult.Type.MISS && player.level().isEmptyBlock(target.blockPosition().above())) {
                            bestTarget = target;
                            bestDotProduct = dotProduct;
                        }
                    }
                }
                if (bestTarget != null) {
                    InteractionResult result = ability.useAbilityOnEntity(player.getItemInHand(hand), player, bestTarget, hand);
                    if (result != InteractionResult.PASS) {
                        successfulUse = true;
                    }
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

        if ((hasEntityInteraction || hasBlockInteraction) && !hasGeneralAbility) {
            if (successfulUse) {

                player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true);
            } else {
                player.displayClientMessage(Component.literal("Missed: " + itemName).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD), true);
            }
        } else if (!hasEntityInteraction && !hasBlockInteraction) {
            ability.useAbility(player.level(), player, hand);
            player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true);
        } else if (successfulUse) {
            player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true);
        } else {
            ability.useAbility(player.level(), player, hand);
            player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true);
        }
    }

    private static boolean tryTargetedAbility(Player player, Ability ability, InteractionHand hand, String itemName) {
        double entityReach = ability.getEntityReach();
        double blockReach = ability.getBlockReach();

        // Try entity targeting first
        Vec3 eyePosition = player.getEyePosition();
        Vec3 lookVector = player.getLookAngle();
        Vec3 entityReachVector = eyePosition.add(lookVector.x * entityReach, lookVector.y * entityReach, lookVector.z * entityReach);
        AABB searchBox = player.getBoundingBox().inflate(entityReach);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player.level(), player, eyePosition, entityReachVector, searchBox,
                entity -> !entity.isSpectator() && entity.isPickable(), 0.0f);

        if (entityHit != null && entityHit.getEntity() instanceof LivingEntity livingEntity) {
            InteractionResult result = ability.useAbilityOnEntity(player.getItemInHand(hand), player, livingEntity, hand);
            if (result == InteractionResult.SUCCESS) {
                player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true);
                return true;
            }
        }

        // If entity targeting failed, try block targeting
        Vec3 blockReachVector = eyePosition.add(lookVector.x * blockReach, lookVector.y * blockReach, lookVector.z * blockReach);
        BlockHitResult blockHit = player.level().clip(new ClipContext(
                eyePosition,
                blockReachVector,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                player
        ));

        if (blockHit.getType() != HitResult.Type.MISS) {
            UseOnContext context = new UseOnContext(player.level(), player, hand, player.getItemInHand(hand), blockHit);
            InteractionResult result = ability.useAbilityOnBlock(context);
            if (result == InteractionResult.SUCCESS) {
                player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true);
                return true;
            }
        }

        return false;
    }

    public static Style getStyle(LivingEntity livingEntity) {
        if (livingEntity instanceof Player player) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            if (holder.getCurrentClass() != null) {
                return Style.EMPTY.withBold(true).withColor(holder.getCurrentClass().getColorFormatting());
            }
            return Style.EMPTY;
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
        boolean isMultiplayer = Minecraft.getInstance().hasSingleplayerServer() && !Minecraft.getInstance().getSingleplayerServer().isPublished();

        if (ClientLeftclickCooldownData.getCooldown() > 0) {
            return;
        }
        LOTMNetworkHandler.sendToServer(new RequestCooldownSetC2S());
        if (!heldItem.isEmpty()) {
            if (heldItem.getItem() instanceof DawnWeaponry) {
                LOTMNetworkHandler.sendToServer(new DawnWeaponryLeftClickC2S());
            } else if (heldItem.getItem() instanceof TravelDoor) {
                LOTMNetworkHandler.sendToServer(new TravelDoorC2S());
            } else if (heldItem.getItem() instanceof SwordOfTwilight) {
                LOTMNetworkHandler.sendToServer(new SwordOfTwilightC2S());
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
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.ENVISION_KINGDOM.get())));

            }  else if (heldItem.getItem() instanceof EnvisionKingdom) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.ENVISION_BARRIER.get())));
            } else if (heldItem.getItem() instanceof MeteorShower) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.METEOR_NO_LEVEL_SHOWER.get())));

            } else if (heldItem.getItem() instanceof MeteorNoLevelShower) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.METEOR_SHOWER.get())));

            } else if (heldItem.getItem() instanceof Prophecy) {
                LOTMNetworkHandler.sendToServer(new ProphesizeLeftClickC2S());
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
            } else if (heldItem.getItem() instanceof ProbabilityManipulationImpulse) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.PROBABILITYFORTUNE.get())));
            } else if (heldItem.getItem() instanceof DivineHandRight) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.DIVINEHANDLEFT.get())));
            } else if (heldItem.getItem() instanceof DivineHandLeft) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.DIVINEHANDRIGHT.get())));
            } else if (heldItem.getItem() instanceof BeamOfGlory) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.AURAOFGLORY.get())));
            } else if (heldItem.getItem() instanceof AuraOfGlory) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.BEAMOFGLORY.get())));
            } else if (heldItem.getItem() instanceof AuraOfTwilight) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.TWILIGHTFREEZE.get())));
            } else if (heldItem.getItem() instanceof TwilightFreeze) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.TWILIGHTACCELERATE.get())));
            } else if (heldItem.getItem() instanceof TwilightAccelerate) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.TWILIGHTLIGHT.get())));
            } else if (heldItem.getItem() instanceof TwilightLight) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.GLOBEOFTWILIGHT.get())));
            } else if (heldItem.getItem() instanceof GlobeOfTwilight) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.BEAMOFTWILIGHT.get())));
            } else if (heldItem.getItem() instanceof BeamOfTwilight) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.AURAOFTWILIGHT.get())));
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
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.ENVISION_KINGDOM.get())));
                heldItem.shrink(1);
            }  else if (heldItem.getItem() instanceof EnvisionKingdom) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.ENVISION_BARRIER.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof MeteorShower) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.METEOR_NO_LEVEL_SHOWER.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof MeteorNoLevelShower) {
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.METEOR_SHOWER.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof Prophecy) {
                LOTMNetworkHandler.sendToServer(new ProphesizeLeftClickC2S());
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

            } else if (heldItem.getItem() instanceof DivineHandRight) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.DIVINEHANDLEFT.get())));
            } else if (heldItem.getItem() instanceof DivineHandLeft) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.DIVINEHANDRIGHT.get())));
            } else if (heldItem.getItem() instanceof BeamOfGlory) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.AURAOFGLORY.get())));
            } else if (heldItem.getItem() instanceof AuraOfGlory) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.BEAMOFGLORY.get())));
            } else if (heldItem.getItem() instanceof AuraOfTwilight) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.TWILIGHTFREEZE.get())));
            } else if (heldItem.getItem() instanceof TwilightFreeze) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.TWILIGHTACCELERATE.get())));
            } else if (heldItem.getItem() instanceof TwilightAccelerate) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.TWILIGHTLIGHT.get())));
            } else if (heldItem.getItem() instanceof TwilightLight) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.GLOBEOFTWILIGHT.get())));
            } else if (heldItem.getItem() instanceof GlobeOfTwilight) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.BEAMOFTWILIGHT.get())));
            } else if (heldItem.getItem() instanceof BeamOfTwilight) {
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.AURAOFTWILIGHT.get())));
            } else if (heldItem.getItem() instanceof TravelDoor) {
                LOTMNetworkHandler.sendToServer(new TravelDoorC2S());
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

    public static boolean isBeyonderCapable(LivingEntity living) { //marked
        return living instanceof Player || living instanceof PlayerMobEntity;
    }

    public static @Nullable BeyonderClass getPathway(LivingEntity living) { //marked
        if (living instanceof Player player) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            return holder.getCurrentClass();
        } else if (living instanceof PlayerMobEntity playerMobEntity) {
            return playerMobEntity.getCurrentPathway();
        }
        return null;
    }

    public static void setPathway(LivingEntity living, BeyonderClass pathway) {
        if (living instanceof Player player) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            holder.setPathway(pathway);
        } else if (living instanceof PlayerMobEntity playerMobEntity) {
            playerMobEntity.setPathway(pathway);
        }
    }

    public static int getSequence(LivingEntity living) { //marked
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

    public static int getCooldownsForAbility(LivingEntity livingEntity, Item ability) {
        int cooldown = 0;
        for (Item item : getAbilities(livingEntity)) {
            if (item == ability && item instanceof SimpleAbilityItem simpleAbilityItem) {
                if (livingEntity instanceof Player player) {
                    int currentCooldown = (int) player.getCooldowns().getCooldownPercent(item, 0);
                    cooldown = simpleAbilityItem.getCooldown() * (100 - currentCooldown) / 100;
                }
            }
        }
        return cooldown;
    }

    public static Map<Item, Float> getDamage(LivingEntity livingEntity) {
        Map<Item, Float> damageMap = new HashMap<>();
        Level level = livingEntity.level();
        int enhancement = 1;
        if (level instanceof ServerLevel serverLevel) {
            enhancement = CalamityEnhancementData.getInstance(serverLevel).getCalamityEnhancement();
        }
        double dreamIntoReality;
        if (livingEntity instanceof Player) {
            dreamIntoReality = Objects.requireNonNull(livingEntity.getAttribute(ModAttributes.DIR.get())).getBaseValue();
        } else {
            dreamIntoReality = 1;
        }
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
        damageMap.put(ItemInit.PROPHECY.get(), applyAbilityStrengthened((float) (8.0f + (dreamIntoReality * 2) - (sequence * 4)) / abilityWeakness, abilityStrengthened));
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
        damageMap.put(ItemInit.SPEAROFDAWN.get(), applyAbilityStrengthened((10.0f - (sequence)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.DAWNWEAPONRY.get(), applyAbilityStrengthened((150.0f - (sequence * 15)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SILVERARMORY.get(), applyAbilityStrengthened((150.0f - (sequence * 15)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.LIGHTOFDAWN.get(), applyAbilityStrengthened((100.0f - (sequence * 10)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.AURAOFGLORY.get(), applyAbilityStrengthened((60.0f - (sequence * 10)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TWILIGHTMANIFESTATION.get(), applyAbilityStrengthened((200.0f - (sequence * 100)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TWILIGHTFREEZE.get(), applyAbilityStrengthened((600.0f - (sequence * 100)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TWILIGHTACCELERATE.get(), applyAbilityStrengthened((1800.0f - (sequence * 300)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.AURAOFTWILIGHT.get(), applyAbilityStrengthened((100.0f - (sequence * 20)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MERCURYLIQUEFICATION.get(), applyAbilityStrengthened((15.0f - (sequence * 2)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.DIVINEHANDRIGHT.get(), applyAbilityStrengthened((10.0f - (sequence * 4)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.DIVINEHANDLEFT.get(), applyAbilityStrengthened((10.0f - (sequence * 4)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.SILVERRAPIER.get(), applyAbilityStrengthened((40.0f - (sequence * 6)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.MERCURYCAGE.get(), applyAbilityStrengthened((500.0f - (sequence * 80)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.GLOBEOFTWILIGHT.get(), applyAbilityStrengthened((20.0f - (sequence * 8)) / abilityWeakness, abilityStrengthened));
        damageMap.put(ItemInit.TWILIGHTLIGHT.get(), applyAbilityStrengthened((300.0f - (sequence * 60)) / abilityWeakness, abilityStrengthened));


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

    public static void registerAllRecipes(CommandContext<CommandSourceStack> server) {
        // Monster Potions
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_9_potion ingredients 2 bossominium:flower_of_genesis bossominium:redstone_hard_drive minecraft:rotten_flesh alexscaves:charred_remnant bosses_of_mass_destruction:soul_star");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_8_potion ingredients 2 legendary_monsters:frozen_rune legendary_monsters:crystal_of_sandstorm mutantmonsters:hulk_hammer alexscaves:sweet_tooth minecraft:netherite_scrap");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_7_potion ingredients 2 alexscaves:pure_darkness bossominium:soul_eye arphex:giant_spinneret macabre:mortis_essence bossominium:dead_charm");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_6_potion ingredients 2 cataclysm:monstrous_horn illageandspillage:spellbound_book bosses_of_mass_destruction:void_thorn illageandspillage:bag_of_horrors minecraft:nether_star");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_5_potion ingredients 2 soulsweapons:chaos_crown cataclysm:witherite_ingot animatedmobsmod:ender_spectre arphex:crusher_claw alexscaves:immortal_embryo");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_4_potion ingredients 2 macabre:gargamaw_heart cataclysm:ignitium_ingot eeeabsmobs:guardian_core arphex:void_geode_shard iceandfire:dragon_skull_fire");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_3_potion ingredients 1 minecraft:nether_star iceandfire:dragon_skull_ice arphex:abyssal_crystal");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_2_potion ingredients 1 terramity:giant_sniffers_hoof soulsweapons:lord_soul_day_stalker soulsweapons:lord_soul_night_prowler minecraft:iron_ingot");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:monster_1_potion ingredients 1 terramity:music_sheet_of_the_omnipotent_ultra_sniffer minecraft:netherite_block");

        // Sailor Potions
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_9_potion ingredients 2 bossominium:rusted_trident mowziesmobs:sol_visage aquamirae:fin arphex:roach_nymph arphex:fly_appendage");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_8_potion ingredients 2 bossominium:mossy_stone_tablet alexsmobs:warped_muscle iceandfire:sea_serpent_fang minecraft:prismarine_shard mutantmonsters:endersoul_hand");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_7_potion ingredients 2 eeeabsmobs:heart_of_pagan mowziesmobs:ice_crystal aquamirae:abyssal_amethyst arphex:mantis_machete arphex:necrotic_fang");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_6_potion ingredients 2 illageandspillage:spellbound_book cataclysm:monstrous_horn arphex:oversized_stinger minecraft:white_banner bosses_of_mass_destruction:void_thorn");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_5_potion ingredients 2 aquamirae:frozen_key soulsweapons:essence_of_eventide soulsweapons:darkin_blade alexscaves:immortal_embryo arphex:void_geode_shard");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_4_potion ingredients 2 macabre:baal_heart alexscaves:tectonic_shard cataclysm:abyssal_egg terramity:belt_of_the_gnome_king iceandfire:dragon_skull_ice");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_3_potion ingredients 1 soulsweapons:essence_of_luminescence iceandfire:dragon_skull_lightning arphex:void_geode");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_2_potion ingredients 1 terramity:angel_feather soulsweapons:lord_soul_day_stalker soulsweapons:lord_soul_night_prowler minecraft:lightning_rod");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:sailor_1_potion ingredients 1 terramity:music_sheet_of_the_omnipotent_ultra_sniffer minecraft:diamond_block");

        // Spectator Potions
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_9_potion ingredients 2 bossominium:golden_shard bossominium:forest_core born_in_chaos_v1:nightmare_claw macabre:eye arphex:venomous_appendage");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_8_potion ingredients 2 alexscaves:heavy_bone bossominium:the_golden_eye born_in_chaos_v1:seedof_chaos born_in_chaos_v1:spider_mandible arphex:mangled_spider_flesh");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_7_potion ingredients 2 bossominium:pure_pearl deeperdarker:soul_crystal arphex:mantis_machete mutantmonsters:endersoul_hand legendary_monsters:withered_bone");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_6_potion ingredients 2 awakened_bosses:herobrine_nugget bossominium:ancient_scrap born_in_chaos_v1:lifestealer_bone arphex:abyssal_shard bossominium:all_seeing_crystal");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_5_potion ingredients 2 soulsweapons:essence_of_eventide soulsweapons:lord_soul_rose aquamirae:frozen_key cataclysm:witherite_ingot animatedmobsmod:ender_spectre");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_4_potion ingredients 2 macabre:gomoria_heart cataclysm:ignitium_ingot iceandfire:dragon_skull_lightning arphex:void_geode_shard sleepy_hollows:spectral_essence");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_3_potion ingredients 1 born_in_chaos_v1:lord_pumpkinheads_lamp iceandfire:dragon_skull_fire arphex:fire_opal");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_2_potion ingredients 1 terramity:fortunes_favor soulsweapons:lord_soul_day_stalker soulsweapons:lord_soul_night_prowler minecraft:spyglass");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:spectator_1_potion ingredients 1 terramity:music_sheet_of_the_omnipotent_ultra_sniffer minecraft:emerald_block");

        // Warrior Potions
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_9_potion ingredients 2 mowziesmobs:sol_visage zoniex:deathly_onyx mowziesmobs:wrought_axe macabre:rattails deeperdarker:sculk_bone");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_8_potion ingredients 2 aether:silver_dungeon_key bossominium:the_golden_eye terramity:spiteful_soul mutantmonsters:hulk_hammer macabre:blindbaloon_item");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_7_potion ingredients 2 aether:gold_dungeon_key bosses_of_mass_destruction:blazing_eye macabre:mortis_essence arphex:mangled_fly_flesh bosses_of_mass_destruction:obsidian_heart");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_6_potion ingredients 2 awakened_bosses:herobrine_nugget macabre:rootofinfestation iceandfire:hydra_fang born_in_chaos_v1:soul_cutlass minecraft:white_banner");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_5_potion ingredients 2 soulsweapons:lord_soul_rose soulsweapons:chaos_crown soulsweapons:essence_of_eventide cataclysm:witherite_ingot cataclysm:gauntlet_of_guard");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_4_potion ingredients 2 alexscaves:tectonic_shard macabre:valamon_heart iceandfire:dragon_skull_lightning eeeabsmobs:guardian_core terramity:belt_of_the_gnome_king");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_3_potion ingredients 1 terramity:perish_staff iceandfire:dragon_skull_fire arphex:abyssal_crystal");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_2_potion ingredients 1 terramity:fortunes_favor soulsweapons:lord_soul_day_stalker soulsweapons:lord_soul_night_prowler minecraft:clock");
        executeRecipeCommand(server, "/beyonderrecipe add lotm:warrior_1_potion ingredients 1 terramity:music_sheet_of_the_omnipotent_ultra_sniffer minecraft:gold_block");
    }

    public static void registerAbilities(Player player, MinecraftServer server) {
        int sequence = BeyonderUtil.getSequence(player);
        BeyonderClass beyonderClass = getPathway(player);
        boolean isSpectator = currentPathwayMatchesNoException(player, BeyonderClassInit.SPECTATOR.get());
        boolean isSailor = currentPathwayMatchesNoException(player, BeyonderClassInit.SAILOR.get());
        boolean isMonster = currentPathwayMatchesNoException(player, BeyonderClassInit.MONSTER.get());
        boolean isApprentice = currentPathwayMatchesNoException(player, BeyonderClassInit.APPRENTICE.get());
        boolean isWarrior = currentPathwayMatchesNoException(player, BeyonderClassInit.WARRIOR.get());
        if (isWarrior) {
            if (sequence == 9) {
                player.sendSystemMessage(Component.literal("No abilities to register"));
            } else if (sequence == 8) {
                player.sendSystemMessage(Component.literal("No abilities to register"));
            } else if (sequence == 7) {
                player.sendSystemMessage(Component.literal("No abilities to register"));
            } else if (sequence == 6) {
                executeCommand(server, "/abilityput LLLLL lotm:gignatification");
                executeCommand(server, "/abilityput LLLLR lotm:lightofdawn");
                executeCommand(server, "/abilityput RRRRR lotm:dawnarmory");
            } else if (sequence == 5) {
                executeCommand(server, "/abilityput LLLLL lotm:gignatification");
                executeCommand(server, "/abilityput LLLLR lotm:lightofdawn");
                executeCommand(server, "/abilityput RRRRL lotm:dawnarmory");
                executeCommand(server, "/abilityput RRLLL lotm:enabledisableprotection");
            } else if (sequence == 4) {
                executeCommand(server, "/abilityput LLLLL lotm:gignatification");
                executeCommand(server, "/abilityput LLLLR lotm:lightofdawn");
                executeCommand(server, "/abilityput RRRRL lotm:dawnarmory");
                executeCommand(server, "/abilityput RRLLL lotm:enabledisableprotection");
                executeCommand(server, "/abilityput LRLRL lotm:eyeofdemonhunting");
                executeCommand(server, "/abilityput LRRLR lotm:warriordangersense");
            } else if (sequence == 3) {
                executeCommand(server, "/abilityput LLLLL lotm:gignatification");
                executeCommand(server, "/abilityput LLLLR lotm:lightofdawn");
                executeCommand(server, "/abilityput RRRRL lotm:dawnarmory");
                executeCommand(server, "/abilityput RRLLL lotm:enabledisableprotection");
                executeCommand(server, "/abilityput LRLRL lotm:eyeofdemonhunting");
                executeCommand(server, "/abilityput LRRLR lotm:warriordangersense");
                executeCommand(server, "/abilityput RRRRR lotm:mercuryliquefication");
                executeCommand(server, "/abilityput LRLLR lotm:silverswordmanifestation");
                executeCommand(server, "/abilityput RRRLL lotm:silverrapier");
                executeCommand(server, "/abilityput RLRRL lotm:silverarmory");
                executeCommand(server, "/abilityput LLRRR lotm:lightconcealment");
            } else if (sequence == 2) {
                executeCommand(server, "/abilityput LLLLL lotm:gignatification");
                executeCommand(server, "/abilityput LLLLR lotm:lightofdawn");
                executeCommand(server, "/abilityput RRRRL lotm:dawnarmory");
                executeCommand(server, "/abilityput RRLLL lotm:enabledisableprotection");
                executeCommand(server, "/abilityput LRLRL lotm:eyeofdemonhunting");
                executeCommand(server, "/abilityput LRRLR lotm:warriordangersense");
                executeCommand(server, "/abilityput RRRRR lotm:mercuryliquefication");
                executeCommand(server, "/abilityput LRLLR lotm:silverswordmanifestation");
                executeCommand(server, "/abilityput RRRLL lotm:silverrapier");
                executeCommand(server, "/abilityput RLRRL lotm:silverarmory");
                executeCommand(server, "/abilityput LLRRR lotm:lightconcealment");
                executeCommand(server, "/abilityput RLLLL lotm:beamofglory");
                executeCommand(server, "/abilityput LRLLL lotm:auraofglory");
                executeCommand(server, "/abilityput RLRLL lotm:twilightsword");
                executeCommand(server, "/abilityput RLRLR lotm:mercurycage");
            } else if (sequence == 1) {
                executeCommand(server, "/abilityput LLLLL lotm:gignatification");
                executeCommand(server, "/abilityput LLLLR lotm:lightofdawn");
                executeCommand(server, "/abilityput RRRRL lotm:dawnarmory");
                executeCommand(server, "/abilityput RRLLL lotm:enabledisableprotection");
                executeCommand(server, "/abilityput LRLRL lotm:eyeofdemonhunting");
                executeCommand(server, "/abilityput LRRLR lotm:warriordangersense");
                executeCommand(server, "/abilityput RRRRR lotm:mercuryliquefication");
                executeCommand(server, "/abilityput LRLLR lotm:silverswordmanifestation");
                executeCommand(server, "/abilityput RRRLL lotm:silverrapier");
                executeCommand(server, "/abilityput RLRRL lotm:silverarmory");
                executeCommand(server, "/abilityput LLRRR lotm:lightconcealment");
                executeCommand(server, "/abilityput RLLLL lotm:beamofglory");
                executeCommand(server, "/abilityput LRLLL lotm:auraofglory");
                executeCommand(server, "/abilityput RLRLL lotm:twilightsword");
                executeCommand(server, "/abilityput RLRLR lotm:mercurycage");
                executeCommand(server, "/abilityput LRRLL lotm:divinehandright");
                executeCommand(server, "/abilityput RLLRR lotm:divinehandleft");
                executeCommand(server, "/abilityput LLRLR lotm:twilightmanifestation");
            } else if (sequence == 0) {
                executeCommand(server, "/abilityput LLLLL lotm:gignatification");
                executeCommand(server, "/abilityput LLLLR lotm:lightofdawn");
                executeCommand(server, "/abilityput RRRRL lotm:dawnarmory");
                executeCommand(server, "/abilityput RRLLL lotm:enabledisableprotection");
                executeCommand(server, "/abilityput LRLRL lotm:eyeofdemonhunting");
                executeCommand(server, "/abilityput LRRLR lotm:warriordangersense");
                executeCommand(server, "/abilityput RRRRR lotm:mercuryliquefication");
                executeCommand(server, "/abilityput LRLLR lotm:silverswordmanifestation");
                executeCommand(server, "/abilityput RRRLL lotm:silverrapier");
                executeCommand(server, "/abilityput RLRRL lotm:silverarmory");
                executeCommand(server, "/abilityput LLRRR lotm:lightconcealment");
                executeCommand(server, "/abilityput RLLLL lotm:beamoftwilight");
                executeCommand(server, "/abilityput LRLLL lotm:auraoftwilight");
                executeCommand(server, "/abilityput RLRLL lotm:twilightsword");
                executeCommand(server, "/abilityput RLRLR lotm:mercurycage");
                executeCommand(server, "/abilityput LRRLL lotm:divinehandright");
                executeCommand(server, "/abilityput RLLRR lotm:divinehandleft");
                executeCommand(server, "/abilityput LLRLR lotm:twilightmanifestation");

                executeCommand(server, "/abilityput RLRRR lotm:twilightfreeze");
                executeCommand(server, "/abilityput RLLLL lotm:twilightlight");
                executeCommand(server, "/abilityput RRLRR lotm:twilightaccelerate");
                executeCommand(server, "/abilityput LRRRR lotm:globeoftwilight");
            }
        } else if (isSpectator) {
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
            }
        } else if (isMonster) {
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


    public static int getMentalStrength(LivingEntity livingEntity) { //marked
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

    public static boolean areAllies(LivingEntity livingEntity, LivingEntity ally) {
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            PlayerAllyData allyData = serverLevel.getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
            return allyData.areAllies(livingEntity.getUUID(), ally.getUUID());
        }
        return false;
    }

    public static List<LivingEntity> getAllies(LivingEntity livingEntity) {
        List<LivingEntity> allyEntities = new ArrayList<>();
        if (livingEntity.level() instanceof ServerLevel serverLevel) {
            PlayerAllyData allyData = serverLevel.getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
            Set<UUID> allyUUIDs = allyData.getAllies(livingEntity.getUUID());
            for (UUID allyUUID : allyUUIDs) {
                LivingEntity ally = getEntityFromUUID(serverLevel, allyUUID);
                if (ally != null) {
                    allyEntities.add(ally);
                }
            }
        }
        return allyEntities;
    }

    public static void makeAlly(LivingEntity user, LivingEntity allyToBe) {
        if (user.level() instanceof ServerLevel serverLevel) {
            PlayerAllyData allyData = serverLevel.getDataStorage().computeIfAbsent(PlayerAllyData::load, PlayerAllyData::create, "player_allies");
            allyData.addAlly(user.getUUID(), allyToBe.getUUID());
        }
    }

    public static void useSpirituality(LivingEntity living, int spirituality) { //marked
        if (!living.level().isClientSide()) {
            if (living instanceof Player player) {
                BeyonderHolderAttacher.getHolderUnwrap(player).useSpirituality(spirituality);
            } else if (living instanceof PlayerMobEntity playerMobEntity) {
                playerMobEntity.useSpirituality(spirituality);
            }
        }
    }


    public static void addSpirituality(LivingEntity living, int spirituality) { //marked
        if (!living.level().isClientSide()) {
            if (living instanceof Player player) {
                BeyonderHolderAttacher.getHolderUnwrap(player).setSpirituality(getSpirituality(living) + spirituality);
            } else if (living instanceof PlayerMobEntity playerMobEntity) {
                playerMobEntity.setSpirituality(getSpirituality(playerMobEntity) + spirituality);
            }
        }
    }

    public static int getSpirituality(LivingEntity living) { //marked
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

    public static int getMaxSpirituality(LivingEntity living) { //marked
        if (!living.level().isClientSide()) {
            if (living instanceof Player player) {
                return (int) BeyonderHolderAttacher.getHolderUnwrap(player).getMaxSpirituality();
            } else if (living instanceof PlayerMobEntity playerMobEntity) {
                return playerMobEntity.getMaxSpirituality();
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

    public static ChatFormatting ageStyle(LivingEntity livingEntity) {
        ChatFormatting style = ChatFormatting.YELLOW;
        if (!livingEntity.level().isClientSide()) {
            int age = livingEntity.getPersistentData().getInt("age");
            int sequence = getSequence(livingEntity);
            int maxAge = 20;
            if (sequence == 9) {
                maxAge = 40;
            } else if (sequence == 8) {
                maxAge = 80;
            } else if (sequence == 7) {
                maxAge = 150;
            } else if (sequence == 6) {
                maxAge = 230;
            } else if (sequence == 5) {
                maxAge = 330;
            } else if (sequence == 4) {
                maxAge = 550;
            } else if (sequence == 3) {
                maxAge = 700;
            } else if (sequence == 2) {
                maxAge = 1000;
            } else if (sequence == 1) {
                maxAge = 1600;
            } else if (sequence == 0) {
                maxAge = 3000;
            }
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
            if (tenPercent) {
                style = ChatFormatting.YELLOW;
            }
            if (twentyPercent) {
                style = ChatFormatting.YELLOW;
            }
            if (thirtyPercent) {
                style = ChatFormatting.YELLOW;
            }
            if (fortyPercent) {
                style = ChatFormatting.RED;
            }
            if (fiftyPercent) {
                style = ChatFormatting.RED;
            }
            if (sixtyPercent) {
                style = ChatFormatting.RED;
            }
            if (seventyPercent) {
                style = ChatFormatting.DARK_RED;
            }
            if (eightyPercent) {
                style = ChatFormatting.DARK_RED;
            }
            if (ninetyPercent) {
                style = ChatFormatting.DARK_RED;
            }
            if (oneHundredPercent) {
                style = ChatFormatting.DARK_RED;
            }
        }
        return style;
    }



    public static ChatFormatting corruptionStyle(LivingEntity livingEntity) {
        ChatFormatting style = ChatFormatting.YELLOW;
        if (!livingEntity.level().isClientSide()) {
            int corruption = livingEntity.getPersistentData().getInt("corruption");
            int maxCorruption = 100;
            boolean tenPercent = corruption >= maxCorruption * 0.1;
            boolean twentyPercent = corruption >= maxCorruption * 0.2;
            boolean thirtyPercent = corruption >= maxCorruption * 0.3;
            boolean fortyPercent = corruption >= maxCorruption * 0.4;
            boolean fiftyPercent = corruption >= maxCorruption * 0.5;
            boolean sixtyPercent = corruption >= maxCorruption * 0.6;
            boolean seventyPercent = corruption >= maxCorruption * 0.7;
            boolean eightyPercent = corruption >= maxCorruption * 0.8;
            boolean ninetyPercent = corruption >= maxCorruption * 0.9;
            boolean oneHundredPercent = corruption >= maxCorruption;
            if (tenPercent) {
                style = ChatFormatting.YELLOW;
            }
            if (twentyPercent) {
                style = ChatFormatting.YELLOW;
            }
            if (thirtyPercent) {
                style = ChatFormatting.YELLOW;
            }
            if (fortyPercent) {
                style = ChatFormatting.RED;
            }
            if (fiftyPercent) {
                style = ChatFormatting.RED;
            }
            if (sixtyPercent) {
                style = ChatFormatting.RED;
            }
            if (seventyPercent) {
                style = ChatFormatting.DARK_RED;
            }
            if (eightyPercent) {
                style = ChatFormatting.DARK_RED;
            }
            if (ninetyPercent) {
                style = ChatFormatting.DARK_RED;
            }
            if (oneHundredPercent) {
                style = ChatFormatting.DARK_RED;
            }
        }
        return style;
    }

    public static void ageHandlerTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int age = tag.getInt("age");
        int sequence = getSequence(livingEntity);
        int maxAge = 20;
        if (sequence == 9) {
            maxAge = 40;
        } else if (sequence == 8) {
            maxAge = 80;
        } else if (sequence == 7) {
            maxAge = 150;
        } else if (sequence == 6) {
            maxAge = 230;
        } else if (sequence == 5) {
            maxAge = 330;
        } else if (sequence == 4) {
            maxAge = 550;
        } else if (sequence == 3) {
            maxAge = 700;
        } else if (sequence == 2) {
            maxAge = 1000;
        } else if (sequence == 1) {
            maxAge = 1600;
        } else if (sequence == 0) {
            maxAge = 3000;
        }
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
                if (livingEntity.tickCount % 20 == 0) {
                    tag.putInt("age", Math.max(0, age - (10 - sequence)));
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
            if (sequence <= 2 && sequence != -1 && livingEntity instanceof Player player) {
                player.getFoodData().setFoodLevel(20);
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
        if (currentPathwayAndSequenceMatchesNoException(scribe, BeyonderClassInit.APPRENTICE.get(), 6)) {
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
        return getSequence(livingEntity) != -1 && getPathway(livingEntity) != null;
    }

    public static void resetPathway(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity instanceof Player player) {
                BeyonderHolderAttacher.getHolderUnwrap(player).removePathway();
            } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
                playerMobEntity.setPathway(null);
                playerMobEntity.setSequence(-1);
            }
        }
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

    public static void copyAbilities(Level level, LivingEntity living, SimpleAbilityItem ability) { //marked
        if (living instanceof Player player) {
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

    public static void useCopiedAbility(LivingEntity living, Item ability) { //marked
        if (living instanceof Player player) {
            if (currentPathwayAndSequenceMatchesNoException(player, BeyonderClassInit.APPRENTICE.get(), 6)) {
                player.getCapability(CapabilityScribeAbilities.SCRIBE_CAPABILITY, null).ifPresent(storage -> {
                    storage.useScribeAbility(ability);
                });
            }
        }
    }

    public static boolean checkAbilityIsCopied(LivingEntity living, Item ability) { //marked
        if (living instanceof Player player) {
            int sequence = BeyonderUtil.getSequence(player);
            if (currentPathwayAndSequenceMatchesNoException(living, BeyonderClassInit.APOTHECARY.get(), 6))
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

    public static void removePathway(LivingEntity living) {
        if (living instanceof Player player) {
            BeyonderHolderAttacher.getHolderUnwrap(player).removePathway();
        } else {
            setPathwayAndSequence(living, null, -1);
        }
        ScaleTypes.BASE.getScaleData(living).setScale(1);
        if (living instanceof Player player) {
            Abilities playerAbilities = player.getAbilities();
            playerAbilities.setFlyingSpeed(0.05F);
            playerAbilities.setWalkingSpeed(0.1F);
            player.onUpdateAbilities();
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
            }
        }
        BeyonderUtil.removeTags(living);
    }


    public static void setScale(Entity entity, float scale) {
        if (!entity.level().isClientSide()) {
            ScaleTypes.BASE.getScaleData(entity).setScale(scale);
        }
    }

    public static float getScale(Entity entity) {
        float scale = 1;
        if (!entity.level().isClientSide()) {
            scale = ScaleTypes.BASE.getScaleData(entity).getScale();
        }
        return scale;
    }

    public static void setTargetScale(Entity entity, float scale) {
        if (!entity.level().isClientSide()) {
            ScaleTypes.BASE.getScaleData(entity).setTargetScale(scale);
        }
    }

    public static float getRandomInRange(float range) {
        float random = (float) Math.random();
        return (random * 2 * range) - range;
    }

    public static void destroyBlocksInSphere(Entity entity, BlockPos hitPos, double radius, float damage) {
        for (BlockPos pos : BlockPos.betweenClosed(
                hitPos.offset((int) -radius, (int) -radius, (int) -radius),
                hitPos.offset((int) radius, (int) radius, (int) radius))) {
            if (pos.distSqr(hitPos) <= radius * radius) {
                if (entity.level().getBlockState(pos).getDestroySpeed(entity.level(), pos) >= 0) {
                    entity.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
                }
            }
        }
        List<Entity> entities = entity.level().getEntities(entity, new AABB(hitPos.offset((int) -radius, (int) -radius, (int) -radius), hitPos.offset((int) radius, (int) radius, (int) radius)));
        for (Entity pEntity : entities) {
            if (pEntity instanceof LivingEntity livingEntity) {
                livingEntity.hurt(BeyonderUtil.genericSource(entity), damage);
            }
        }
    }

    public static int chatFormatingToInt(ChatFormatting color) {
        return COLOR_MAP.getOrDefault(color, 0xFFFFFF);
    }

    public static String getPathwayName(BeyonderClass pathway) {
        return pathway.sequenceNames().get(9);
    }

    public static BeyonderClass getPathwayByName(String name) {
        return NAME_TO_BEYONDER.getOrDefault(name, new SeerClass());
    }

    public static void useAvailableAbilityAsMob(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide() && getPathway(livingEntity) != null && livingEntity instanceof Mob mob) {
            List<Item> abilities = getAbilities(livingEntity);
            if (abilities.isEmpty()) {
                return;
            }
            LivingEntity target = mob.getTarget();
            Random random = new Random();
            Item randomItem = abilities.get(random.nextInt(abilities.size()));
            if (randomItem instanceof SimpleAbilityItem simpleAbilityItem) {
                if (livingEntity.hasEffect(ModEffects.STUN.get())) {
                    return;
                }
                if (!SimpleAbilityItem.checkAll(livingEntity, simpleAbilityItem.getRequiredPathway(),
                        simpleAbilityItem.getRequiredSequence(), simpleAbilityItem.getRequiredSpirituality(), false)) {
                    mob.setItemInHand(InteractionHand.MAIN_HAND, simpleAbilityItem.getDefaultInstance());
                }
                double entityReach = simpleAbilityItem.getEntityReach();
                double blockReach = simpleAbilityItem.getBlockReach();
                boolean successfulUse = false;
                boolean hasEntityInteraction = false;
                boolean hasBlockInteraction = false;
                boolean hasGeneralAbility = false;

                try {
                    Method entityMethod = simpleAbilityItem.getClass().getDeclaredMethod("useAbilityOnEntity", ItemStack.class, LivingEntity.class, LivingEntity.class, InteractionHand.class);
                    hasEntityInteraction = !entityMethod.equals(Ability.class.getDeclaredMethod("useAbilityOnEntity", ItemStack.class, LivingEntity.class, LivingEntity.class, InteractionHand.class));
                } catch (NoSuchMethodException ignored) {
                }
                try {
                    Method blockMethod = simpleAbilityItem.getClass().getDeclaredMethod("useAbilityOnBlock", UseOnContext.class);
                    hasBlockInteraction = !blockMethod.equals(Ability.class.getDeclaredMethod("useAbilityOnBlock", UseOnContext.class));
                } catch (NoSuchMethodException ignored) {
                }

                try {
                    Method generalMethod = simpleAbilityItem.getClass().getDeclaredMethod("useAbility", Level.class, LivingEntity.class, InteractionHand.class);
                    hasGeneralAbility = !generalMethod.equals(Ability.class.getDeclaredMethod("useAbility", Level.class, LivingEntity.class, InteractionHand.class));
                } catch (NoSuchMethodException ignored) {
                }
                if (hasEntityInteraction) {
                    if (target != null && target.distanceTo(mob) <= entityReach) {
                        InteractionResult result = simpleAbilityItem.useAbilityOnEntity(livingEntity.getItemInHand(InteractionHand.MAIN_HAND), mob, target, InteractionHand.MAIN_HAND);
                        if (result != InteractionResult.PASS) {
                            successfulUse = true;
                        }
                    }
                }

                if (!successfulUse && hasBlockInteraction) {
                    Vec3 eyePosition = livingEntity.getEyePosition();
                    Vec3 lookVector = livingEntity.getLookAngle();
                    Vec3 reachVector = eyePosition.add(lookVector.x * blockReach, lookVector.y * blockReach, lookVector.z * blockReach);
                    BlockHitResult blockHit = livingEntity.level().clip(new ClipContext(eyePosition, reachVector,
                            ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, livingEntity));
                    if (blockHit.getType() != HitResult.Type.MISS) {
                        UseOnContext context = new UseOnContext(
                                livingEntity.level(),
                                null,
                                InteractionHand.MAIN_HAND,
                                livingEntity.getItemInHand(InteractionHand.MAIN_HAND),
                                blockHit
                        );
                        InteractionResult result = simpleAbilityItem.useOn(context);
                        if (result != InteractionResult.PASS) {
                            successfulUse = true;
                        }
                    }
                }

                String itemName = simpleAbilityItem.getDescription().getString();
                if ((hasEntityInteraction || hasBlockInteraction) && !hasGeneralAbility) {
                    if (successfulUse) {
                    } else {
                    }
                } else if (!hasEntityInteraction && !hasBlockInteraction) {
                    simpleAbilityItem.useAbility(mob.level(), mob, InteractionHand.MAIN_HAND);
                } else if (successfulUse) {
                } else {
                    simpleAbilityItem.useAbility(mob.level(), mob, InteractionHand.MAIN_HAND);
                }
            }
        }
    }

    public static void removeTags(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            CompoundTag tag = livingEntity.getPersistentData();
            for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(500))) {
                if (living.getPersistentData().contains("divineHandUUID")) {
                    if (living.getPersistentData().getUUID("divineHandUUID").equals(livingEntity.getUUID())) {
                        living.getPersistentData().putInt("divineHandGuarding", 0);
                    }
                }
            }
            tag.putInt("apprenticeBounceHitArrows", 0);
            tag.putBoolean("ApprenticeBounceProjectileMovement", false);
            tag.putInt("invisibleHandCounter", 0);
            tag.putDouble("invisibleHandDistance", 0);
            tag.putInt("travelBlinkDistance", 0);
            TravelDoorWaypoint.clearAllWaypoints(livingEntity);
            tag.putBoolean("monsterAuraOfChaos", false);
            tag.putBoolean("monsterChaosWalkerCombat", false);
            tag.putInt("monsterCyclePotionEffectsCount", 0);
            tag.putInt("monsterCycleOfFateUser", 0);
            removeCycleEffect(livingEntity);
            removeTwilightFreezeEffect(livingEntity);
            tag.putBoolean("monsterRipple", false);
            tag.putInt("monsterReincarnationCounter", 0);
            tag.putInt("age", 0);
            ProbabilityManipulationWipe.wipeProbablility(tag);
            tag.putInt("calamityIncarnationInMeteor", 0);
            tag.putInt("calamityIncarnationInTornado", 0);
            tag.putInt("calamityIncarnationInLightning", 0);
            tag.putInt("calamityIncarnationInPlague", 0);
            tag.putInt("monsterCalamityImmunity", 0);
            tag.putBoolean("monsterDangerSense", false);
            tag.putBoolean("monsterCalamityAttraction", false);
            tag.putInt("probabilityManipulationInfiniteFortune", 0);
            tag.putInt("probabilityManipulationInfiniteMisfortune", 0);
            tag.putInt("monsterRebootPotionEffectsCount", 0);
            tag.putInt("monsterRebootAge", 0);
            tag.putInt("monsterRebootLuck", 0);
            tag.putInt("monsterRebootMisfortune", 0);
            tag.putInt("monsterRebootSanity", 0);
            tag.putInt("monsterRebootCorruption", 0);
            tag.putInt("monsterRebootHealth", 20);
            tag.putInt("monsterRebootSpirituality", 0);
            tag.putBoolean("auraOfGlory", false);
            tag.putBoolean("auraOfTwilight", false);
            tag.remove("dawnStoredArmorData");
            tag.putBoolean("warriorProtection", false);
            tag.putInt("monsterMisfortuneManipulationGravity", 0);
            tag.putBoolean("warriorProtection", false);
            tag.putBoolean("demonHuntingEye", false);
            tag.putBoolean("warriorShouldDestroyBlock", false);
            tag.putInt("globeOfTwilight", 0);
            tag.putInt("warriorLightConcealment", 0);
            tag.putInt("lightOfDawnCounter", 0);
            tag.putInt("corruption", 0);
            tag.putBoolean("mercuryLiquefication", false);
            tag.putInt("silverRapierSummoning", 0);
            tag.putInt("twilightAgeAccelerate", 0);
            tag.putInt("twilightAgeAccelerateEnemy", 0);
            tag.putInt("twilightLight", 0);
            tag.putInt("twilightManifestation", 0);
            tag.putBoolean("warriorDangerSense", false);
            stopFlying(livingEntity);
            tag.putInt("BarrierRadius", 0);
            tag.putInt("waitMakeLifeTimer", 0);
            tag.putInt("BlinkDistance", 0);
            tag.remove("manipulateMovementX");
            tag.remove("manipulateMovementY");
            tag.remove("manipulateMovementZ");
            tag.putBoolean("manipulateMovementBoolean", false);
            removePsychologicalInvisibilityEffect(livingEntity);
            tag.putInt("sailorAcidicRain", 0);
            tag.putInt("calamityIncarnationTornado", 0);
            tag.putInt("calamityIncarnationTsunami", 0);
            tag.putInt("sailorEarthquake", 0);
            tag.putBoolean("SailorLightning", false);
            tag.putInt("sailorExtremeColdness", 0);
            tag.putInt("sailorHurricane", 0);
            tag.putInt("tyrantMentionedInChat", 0);
            tag.putInt("sailorLightningStorm1", 0);
            tag.putInt("sailorLightningStorm", 0);
            tag.putInt("matterAccelerationBlockTimer", 0);
            tag.putInt("tyrantSelfAcceleration", 0);
            tag.putInt("ragingBlows", 0);
            tag.putBoolean("torrentialDownpour", false);
            tag.putBoolean("sailorProjectileMovement", false);
            tag.putInt("sirenSongHarm", 0);
            tag.putInt("sirenSongWeaken", 0);
            tag.putInt("sirenSongStun", 0);
            tag.putInt("sirenSongStrengthen", 0);
            tag.putInt("sailorLightningStar", 0);
            tag.putInt("inStormSeal", 0);
            tag.putInt("sailorTsunami", 0);
            tag.putInt("sailorTsunamiSeal", 0);
            tag.putInt("sailorSphere", 0);
            tag.putInt("mercuryLiqueficationTrapped", 0);
            tag.putInt("sailorSeal", 0);
            tag.putBoolean("sailorFlight1", false);
            tag.putInt("sailorFlight", 0);
            tag.putInt("sailorFlightDamageCancel", 0);
            tag.putInt("luckStoneDamageImmunity", 0);
            tag.putInt("luckStoneDamage", 0);
            tag.putInt("luckMeteorDamage", 0);
            tag.putInt("calamityMeteorImmunity", 0);
            tag.putInt("luckLightningMCDamage", 0);
            tag.putInt("luckMCLightningImmunity", 0);
            tag.putInt("calamityExplosionOccurrence", 0);
            tag.putInt("luckLightningLOTMDamage", 0);
            tag.putInt("calamityLightningBoltMonsterResistance", 0);
            tag.putInt("calamityLightningStormResistance", 0);
            tag.putInt("luckTornadoResistance", 0);
            tag.putInt("luckTornadoImmunity", 0);
            tag.putInt("calamityLOTMLightningImmunity", 0);
            tag.putInt("calamityLightningStormImmunity", 0);
        }
    }
}