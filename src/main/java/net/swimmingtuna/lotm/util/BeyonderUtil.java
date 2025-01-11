package net.swimmingtuna.lotm.util;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.phys.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.server.ServerLifecycleHooks;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Ability;
import net.swimmingtuna.lotm.item.BeyonderAbilities.BeyonderAbilityUser;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems.*;
import net.swimmingtuna.lotm.item.SealedArtifacts.DeathKnell;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.*;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import net.swimmingtuna.lotm.util.ClientData.ClientLeftclickCooldownData;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import net.swimmingtuna.lotm.world.worlddata.CalamityEnhancementData;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.*;

import static net.swimmingtuna.lotm.init.DamageTypeInit.MENTAL_DAMAGE;

public class BeyonderUtil {

    public static Projectile getProjectiles(Player player) {
        if (player.level().isClientSide()) {
            return null;
        }
        List<Projectile> projectiles = player.level().getEntitiesOfClass(Projectile.class, player.getBoundingBox().inflate(50));
        for (Projectile projectile : projectiles) {
            if (projectile.getOwner() == player && projectile.tickCount > 6 && projectile.tickCount < 100) {
                return projectile;
            }
        }
        return null;
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
            if (entity != owner && !entity.level().isClientSide()) {
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
        int sequence = holder.getCurrentSequence();
        if (holder.currentClassMatches(BeyonderClassInit.SPECTATOR)) {
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
                abilityNames.add(ItemInit.DREAM_WALKING.get());
                abilityNames.add(ItemInit.NIGHTMARE.get());
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

        if (holder.currentClassMatches(BeyonderClassInit.SAILOR)) {
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
                abilityNames.add(ItemInit.WIND_MANIPULATION_CUSHION.get());
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
        if (holder.currentClassMatches(BeyonderClassInit.MONSTER)) {
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
            player.sendSystemMessage(Component.literal("Item not found in registry for ability " + abilityNumber + " with resource location: " + resourceLocation));
            return;
        }
        String itemName = item.getDescription().getString();
        if (!(item instanceof Ability ability)) {
            player.sendSystemMessage(Component.literal("Registered ability ").append(itemName).append(" for ability number " + abilityNumber + " is not an ability."));
            return;
        }

        if (player.getCooldowns().isOnCooldown(item)) {
            player.sendSystemMessage(Component.literal("Ability ").append(itemName).append(" is on cooldown!"));
            return;
        }
        double entityReach = ability.getEntityReach();
        double blockReach = ability.getBlockReach();

        boolean hasEntityInteraction = false;
        try {
            Method entityMethod = ability.getClass().getDeclaredMethod("useAbilityOnEntity", ItemStack.class, Player.class, LivingEntity.class, InteractionHand.class);
            hasEntityInteraction = !entityMethod.equals(Ability.class.getDeclaredMethod("useAbilityOnEntity", ItemStack.class, Player.class, LivingEntity.class, InteractionHand.class));
        } catch (NoSuchMethodException ignored) {
        }

        boolean hasBlockInteraction = false;
        try {
            Method blockMethod = ability.getClass().getDeclaredMethod("useAbilityOnBlock", UseOnContext.class);
            hasBlockInteraction = !blockMethod.equals(Ability.class.getDeclaredMethod("useAbilityOnBlock", UseOnContext.class));
        } catch (NoSuchMethodException ignored) {
        }
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
                player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true); // Display ability name

                if (result != InteractionResult.PASS) {
                    return;
                }
            }
        }
        if (hasBlockInteraction) {
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
                player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true); // Display ability name
                if (result != InteractionResult.PASS) {
                    return;
                }
            }
        }
        player.displayClientMessage(Component.literal("Used: " + itemName).withStyle(getStyle(player)), true); // Display ability name
        ability.useAbility(player.level(), player, hand);

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

    public static void leftClickEmpty(Player pPlayer) {
        Style style = BeyonderUtil.getStyle(pPlayer);
        ItemStack heldItem = pPlayer.getMainHandItem();
        int activeSlot = pPlayer.getInventory().selected;
        if (ClientLeftclickCooldownData.getCooldown() > 0) {
            return;
        }
        LOTMNetworkHandler.sendToServer(new RequestCooldownSetC2S());
        if (!heldItem.isEmpty()) {
            if (heldItem.getItem() instanceof MonsterDomainTeleporation) {
                LOTMNetworkHandler.sendToServer(new MonsterLeftClickC2S());
            }
            if (heldItem.getItem() instanceof BeyonderAbilityUser) {
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
                LOTMNetworkHandler.sendToServer(new UpdateItemInHandC2S(activeSlot, new ItemStack(ItemInit.WIND_MANIPULATION_CUSHION.get())));

            } else if (heldItem.getItem() instanceof WindManipulationCushion) {
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
                pPlayer.getInventory().setItem(activeSlot, new ItemStack((ItemInit.WIND_MANIPULATION_CUSHION.get())));
                heldItem.shrink(1);
            } else if (heldItem.getItem() instanceof WindManipulationCushion) {
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
            return holder.getCurrentSequence();
        } else if (living instanceof PlayerMobEntity playerMobEntity) {
            return playerMobEntity.getCurrentSequence();
        }
        return -1;
    }

    public static Map<Item, Float> getDamage(LivingEntity livingEntity) {
        Map<Item, Float> damageMap = new HashMap<>();
        int enhancement = CalamityEnhancementData.getInstance((ServerLevel) livingEntity.level()).getCalamityEnhancement();
        double dreamIntoReality = Objects.requireNonNull(livingEntity.getAttribute(ModAttributes.DIR.get())).getBaseValue();
        int sequence = 0;
        int abilityWeakness = 1;
        if (livingEntity.hasEffect(ModEffects.TWILIGHT.get())) {
            abilityWeakness = Math.max(1,Objects.requireNonNull(livingEntity.getEffect(ModEffects.TWILIGHT.get())).getAmplifier());
        }
        if (livingEntity instanceof Player player) {
            sequence = BeyonderHolderAttacher.getHolderUnwrap(player).getCurrentSequence();
        } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
            sequence = playerMobEntity.getCurrentSequence();
        }
        //SAILOR
        damageMap.put(ItemInit.ACIDIC_RAIN.get(), 50.0f - (sequence * 7));
        damageMap.put(ItemInit.AQUATIC_LIFE_MANIPULATION.get(), 50.0f - (sequence * 5));
        damageMap.put(ItemInit.AQUEOUS_LIGHT_PUSH.get(), 8.0f - sequence);
        damageMap.put(ItemInit.AQUEOUS_LIGHT_PULL.get(), 8.0f - sequence);
        damageMap.put(ItemInit.AQUEOUS_LIGHT_DROWN.get(), 8.0f - sequence);
        damageMap.put(ItemInit.CALAMITY_INCARNATION_TORNADO.get(), 300.0f - (50 * sequence));
        damageMap.put(ItemInit.CALAMITY_INCARNATION_TSUNAMI.get(), 200.0f - (30 * sequence));
        damageMap.put(ItemInit.EARTHQUAKE.get(), 75.0f - (sequence * 6));
        damageMap.put(ItemInit.ENABLE_OR_DISABLE_LIGHTNING.get(), 0.0f);
        damageMap.put(ItemInit.EXTREME_COLDNESS.get(), 150.0f - (sequence * 20));
        damageMap.put(ItemInit.HURRICANE.get(), 600.0f - (sequence * 100));
        damageMap.put(ItemInit.LIGHTNING_BALL.get(), 10.0f + (10 - sequence * 3));
        damageMap.put(ItemInit.LIGHTNING_BALL_ABSORB.get(), 10.0f + (10 - sequence * 3));
        damageMap.put(ItemInit.LIGHTNING_BRANCH.get(), 30.0f - (sequence * 3));
        damageMap.put(ItemInit.LIGHTNING_REDIRECTION.get(), 200.0f - (sequence * 25));
        damageMap.put(ItemInit.LIGHTNING_STORM.get(), 500.0f - (sequence * 80));
        damageMap.put(ItemInit.MATTER_ACCELERATION_BLOCKS.get(), 10.0f - sequence);
        damageMap.put(ItemInit.MATTER_ACCELERATION_ENTITIES.get(), 300.0f - (sequence * 80));
        damageMap.put(ItemInit.MATTER_ACCELERATION_SELF.get(), 120.0f - (sequence * 30));
        damageMap.put(ItemInit.RAGING_BLOWS.get(), 10.0f - (sequence));
        damageMap.put(ItemInit.RAIN_EYES.get(), 500.0f - (sequence * 50));
        damageMap.put(ItemInit.ROAR.get(), 10.0f - sequence);
        damageMap.put(ItemInit.SAILOR_LIGHTNING.get(), 20.0f - ( 2 * sequence));
        damageMap.put(ItemInit.SAILOR_LIGHTNING_TRAVEL.get(), 400.0f - (sequence * 150));
        damageMap.put(ItemInit.SAILORPROJECTILECTONROL.get(), 0.0f);
        damageMap.put(ItemInit.SIREN_SONG_HARM.get(), 50.0f - (sequence * 6));
        damageMap.put(ItemInit.SIREN_SONG_WEAKEN.get(), 50.0f - (sequence * 6));
        damageMap.put(ItemInit.SIREN_SONG_STRENGTHEN.get(), 21.0f - sequence);
        damageMap.put(ItemInit.SIREN_SONG_STUN.get(), 50.0f - (sequence * 6));
        damageMap.put(ItemInit.SONIC_BOOM.get(), 40.0f - (sequence * 5));
        damageMap.put(ItemInit.STAR_OF_LIGHTNING.get(), 125.0f - sequence * 20);
        damageMap.put(ItemInit.STORM_SEAL.get(), 3.0f - sequence);
        damageMap.put(ItemInit.THUNDER_CLAP.get(), 300.0f - (sequence * 50));
        damageMap.put(ItemInit.TORNADO.get(), 150.0f - (sequence * 30));
        damageMap.put(ItemInit.TSUNAMI.get(), 600.0f - (sequence * 80));
        damageMap.put(ItemInit.TSUNAMI_SEAL.get(), 600.0f - (sequence * 80));
        damageMap.put(ItemInit.TYRANNY.get(), 250.0f - (sequence * 80));
        damageMap.put(ItemInit.VOLCANIC_ERUPTION.get(), 120.0f - (sequence * 10));
        damageMap.put(ItemInit.WATER_COLUMN.get(), 200.0f - (sequence * 60));
        damageMap.put(ItemInit.WATER_SPHERE.get(), 200.0f - (sequence * 20));
        damageMap.put(ItemInit.WIND_MANIPULATION_BLADE.get(), 7.0f - sequence);
        damageMap.put(ItemInit.WIND_MANIPULATION_CUSHION.get(), 0.0f);
        damageMap.put(ItemInit.WIND_MANIPULATION_FLIGHT.get(), 0.0f);
        damageMap.put(ItemInit.WIND_MANIPULATION_SENSE.get(), 0.0f);

        //SPECTATOR
        damageMap.put(ItemInit.APPLY_MANIPULATION.get(), 0.0f);
        damageMap.put(ItemInit.AWE.get(), 190.0f - (sequence * 15));
        damageMap.put(ItemInit.BATTLE_HYPNOTISM.get(), 400.0f - (sequence * 20));
        damageMap.put(ItemInit.CONSCIOUSNESS_STROLL.get(), 0.0f);
        damageMap.put(ItemInit.DISCERN.get(),  0.0f);
        damageMap.put(ItemInit.DRAGON_BREATH.get(), (float) (60.0f * dreamIntoReality) - (sequence * 4));
        damageMap.put(ItemInit.DREAM_INTO_REALITY.get(), 0.0f);
        damageMap.put(ItemInit.DREAM_WALKING.get(), 0.0f);
        damageMap.put(ItemInit.DREAM_WEAVING.get(), 20.0f - (sequence * 3));
        damageMap.put(ItemInit.ENVISION_BARRIER.get(), 101.0f - (sequence * 20));
        damageMap.put(ItemInit.ENVISION_DEATH.get(), (float) (40.0f + (dreamIntoReality * 5)) - (sequence * 10));
        damageMap.put(ItemInit.ENVISION_HEALTH.get(), (float) (0.66f - (sequence * 0.05) + (dreamIntoReality * 0.05f)));
        damageMap.put(ItemInit.ENVISION_KINGDOM.get(), 0.0f);
        damageMap.put(ItemInit.ENVISION_LIFE.get(), (3.0f + (sequence)));
        damageMap.put(ItemInit.ENVISION_LOCATION.get(), (float) (500.0f / dreamIntoReality));
        damageMap.put(ItemInit.ENVISION_LOCATION_BLINK.get(), (float) (1000.0f - dreamIntoReality));
        damageMap.put(ItemInit.ENVISION_WEATHER.get(), (float) (500.0f / dreamIntoReality));
        damageMap.put(ItemInit.FRENZY.get(), (float) ((15.0f - sequence) * dreamIntoReality));
        damageMap.put(ItemInit.MANIPULATE_MOVEMENT.get(), 0.0f);
        damageMap.put(ItemInit.MANIPULATE_EMOTION.get(), 50.0f - (sequence * 5));
        damageMap.put(ItemInit.MANIPULATE_FONDNESS.get(), (float) (600.0f * dreamIntoReality));
        damageMap.put(ItemInit.MENTAL_PLAGUE.get(), (float) (200.0f / dreamIntoReality));
        damageMap.put(ItemInit.METEOR_NO_LEVEL_SHOWER.get(), (float) (10.0f + dreamIntoReality * 2) - (4 * sequence));
        damageMap.put(ItemInit.METEOR_SHOWER.get(),  (float) (10.0f + dreamIntoReality * 2) - (4 * sequence));
        damageMap.put(ItemInit.MIND_READING.get(), 0.0f);
        damageMap.put(ItemInit.MIND_STORM.get(), 30.0f - (sequence * 2));
        damageMap.put(ItemInit.NIGHTMARE.get(), 40.0f - (sequence * 2));
        damageMap.put(ItemInit.PLACATE.get(), 0.0f);
        damageMap.put(ItemInit.PLAGUE_STORM.get(), (float) ((float) (12.0f * dreamIntoReality) - (sequence * 1.5)));
        damageMap.put(ItemInit.PROPHESIZE_DEMISE.get(), 0.0f);
        damageMap.put(ItemInit.PROPHESIZE_TELEPORT_BLOCK.get(), (float) ((500.0f * dreamIntoReality) - (sequence * 100)));
        damageMap.put(ItemInit.PROPHESIZE_TELEPORT_PLAYER.get(), (float) ((500.0f * dreamIntoReality) - (sequence * 100)));
        damageMap.put(ItemInit.PSYCHOLOGICAL_INVISIBILITY.get(), 0.0f);

        //MONSTER
        damageMap.put(ItemInit.AURAOFCHAOS.get(), 200.0f - (sequence * 50) + (enhancement * 50));
        damageMap.put(ItemInit.CHAOSAMPLIFICATION.get(), 0.0f);
        damageMap.put(ItemInit.CHAOSWALKERCOMBAT.get(), (float) Math.max(50, 200 - (sequence * 35)));
        damageMap.put(ItemInit.CYCLEOFFATE.get(), 0.0f);
        damageMap.put(ItemInit.DECAYDOMAIN.get(),  250.0f - (sequence * 45));
        damageMap.put(ItemInit.PROVIDENCEDOMAIN.get(), 250.0f - (sequence * 45));
        damageMap.put(ItemInit.ENABLEDISABLERIPPLE.get(), 150.0f - (sequence * 20));
        damageMap.put(ItemInit.FALSEPROPHECY.get(), 0.0f);
        damageMap.put(ItemInit.FATEDCONNECTION.get(), 0.0f);
        damageMap.put(ItemInit.FATEREINCARNATION.get(), 0.0f);
        damageMap.put(ItemInit.FORTUNEAPPROPIATION.get(), 200.0f - (sequence * 40));
        damageMap.put(ItemInit.LUCKCHANNELING.get(), 100.0f - (sequence * 25));
        damageMap.put(ItemInit.LUCKDENIAL.get(), 1800.0f - (sequence * 150));
        damageMap.put(ItemInit.LUCKDEPRIVATION.get(), 0.0f);
        damageMap.put(ItemInit.LUCKFUTURETELLING.get(), 0.0f);
        damageMap.put(ItemInit.LUCKGIFTING.get(),  101.0f - (sequence * 5));
        damageMap.put(ItemInit.LUCK_MANIPULATION.get(), 0.0f);
        damageMap.put(ItemInit.LUCKPERCEPTION.get(), 0.0f);
        damageMap.put(ItemInit.MISFORTUNEBESTOWAL.get(),  60.0f - (sequence * 7) + (enhancement * 10));
        damageMap.put(ItemInit.MISFORTUNEIMPLOSION.get(),  250.0f - (sequence * 100) + (enhancement * 50));
        damageMap.put(ItemInit.MISFORTUNEMANIPULATION.get(), 15.0f - sequence * 2);
        damageMap.put(ItemInit.MISFORTUNEREDIRECTION.get(), 300.0f - (sequence * 50));
        damageMap.put(ItemInit.CALAMITYINCARNATION.get(), 8.0f - sequence);
        damageMap.put(ItemInit.MONSTERDANGERSENSE.get(), 0.0f);
        damageMap.put(ItemInit.MONSTERCALAMITYATTRACTION.get(),  0.0f);
        damageMap.put(ItemInit.MONSTERDOMAINTELEPORATION.get(), 0.0f);
        damageMap.put(ItemInit.MONSTERPROJECTILECONTROL.get(), 0.0f);
        damageMap.put(ItemInit.MONSTERREBOOT.get(), 30.0f - (sequence * 5));
        damageMap.put(ItemInit.PROBABILITYFORTUNE.get(), 200.0f);
        damageMap.put(ItemInit.PROBABILITYEFFECT.get(), 200.0f);
        damageMap.put(ItemInit.PROBABILITYINFINITEFORTUNE.get(), 2000.0f);
        damageMap.put(ItemInit.PROBABILITYINFINITEMISFORTUNE.get(),2000.0f);
        damageMap.put(ItemInit.PROBABILITYMISFORTUNE.get(), 200.0f);
        damageMap.put(ItemInit.PROBABILITYWIPE.get(), 200.0f);
        damageMap.put(ItemInit.PROBABILITYFORTUNEINCREASE.get(), 0.0f);
        damageMap.put(ItemInit.PROBABILITYMISFORTUNEINCREASE.get(), 0.0f);
        damageMap.put(ItemInit.PSYCHESTORM.get(), 30.0f - (sequence * 3));
        damageMap.put(ItemInit.REBOOTSELF.get(), 0.0f);
        damageMap.put(ItemInit.SPIRITVISION.get(), 0.0f);
        damageMap.put(ItemInit.WHISPEROFCORRUPTION.get(), (float) sequence);
        return damageMap;
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
        System.out.println("mental strength is " + mentalStrength);
        return mentalStrength;
    }


}