package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SendDustParticleS2C;
import net.swimmingtuna.lotm.networking.packet.SendParticleS2C;
import net.swimmingtuna.lotm.networking.packet.SyncAntiConcealmentPacketS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EyeOfDemonHunting extends SimpleAbilityItem {


    public EyeOfDemonHunting(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 4, 0, 20);
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
            if (tag.getBoolean("demonHuntingEye")) {
                demonHuntingTracking(event);
                if (entity instanceof ServerPlayer serverPlayer) {
                    for (LivingEntity living : serverPlayer.level().getEntitiesOfClass(LivingEntity.class, serverPlayer.getBoundingBox().inflate(80))) {
                        if (!serverPlayer.level().isClientSide() && living.tickCount % 20 == 0) {
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
                }
            }
            if (entity.tickCount % 10 == 0 && BeyonderUtil.getSequence(entity) <= 4 && (BeyonderUtil.getPathway(entity) == BeyonderClassInit.WARRIOR.get() || BeyonderUtil.sequenceAbleCopy(entity))) {
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
                            BeyonderUtil.applyMobEffect(livingEntity, ModEffects.ABILITY_WEAKNESS.get(), 100, 1, true, true);
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

    public static void demonHunterAntiConcealment(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            BeyonderClass pathway = BeyonderUtil.getPathway(entity);
            if (pathway != null && entity.tickCount % 200 == 0) {
                if ((pathway == BeyonderClassInit.WARRIOR.get() || BeyonderUtil.sequenceAbleCopy(entity)) && BeyonderUtil.getSequence(entity) <= 4) {
                    LOTMNetworkHandler.sendToAllPlayers(new SyncAntiConcealmentPacketS2C(true, entity.getUUID()));
                } else {
                    LOTMNetworkHandler.sendToAllPlayers(new SyncAntiConcealmentPacketS2C(false, entity.getUUID()));
                }
            }
        }
    }

    public static void demonHunterAlchemy(PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        ItemStack heldItem = event.getItemStack();
        Level level = event.getLevel();
        if (!level.isClientSide()) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            if ((holder.currentClassMatches(BeyonderClassInit.WARRIOR) || BeyonderUtil.sequenceAbleCopy(holder)) && holder.getCurrentSequence() <= 4) {
                if (player.isShiftKeyDown()) {
                    if (isValidPotionIngredient(heldItem.getItem())) {
                        Potion potion = getPotionForIngredient(heldItem.getItem());
                        ItemStack brewedPotion = PotionUtils.setPotion(new ItemStack(Items.POTION), potion);
                        heldItem.shrink(1);
                        if (!player.getInventory().add(brewedPotion)) {
                            player.drop(brewedPotion, false);
                        }
                    }
                }
            }
        }
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

    public static void demonHuntingTracking(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        CompoundTag tag = entity.getPersistentData();
        if (!entity.level().isClientSide() && entity instanceof ServerPlayer player && entity.tickCount % 5 == 0) {
            if (tag.getBoolean("demonHuntingEye")) {
                for (LivingEntity living : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(100))) {
                    double x = living.getX();
                    double y = living.getY();
                    double z = living.getZ();
                    if (BeyonderUtil.getPathway(living) != BeyonderClassInit.WARRIOR.get() && BeyonderUtil.getSequence(living) > 4) {
                        if (living instanceof Player || living instanceof PlayerMobEntity) {
                            LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.PLAYER_TRAIL_PARTICLE.get(), x, y, z, 0, 0, 0), player);
                        } else if (living instanceof Monster) {
                            LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleInit.MOB_TRAIL_PARTICLE.get(), x, y, z, 0, 0, 0), player);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, disable or enable your eye of demon hunting. If enabled, you will see trails of players/mobs, gain strengths dependent on the weakness of your opponent's pathway, and be able to see the health of entities around you."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("0").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}

