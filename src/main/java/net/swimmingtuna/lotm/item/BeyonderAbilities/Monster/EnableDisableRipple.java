package net.swimmingtuna.lotm.item.BeyonderAbilities.Monster;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Vex;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.LightningEntity;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.entity.StoneEntity;
import net.swimmingtuna.lotm.entity.TornadoEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import net.swimmingtuna.lotm.world.worlddata.CalamityEnhancementData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.swimmingtuna.lotm.util.BeyonderUtil.getAbilities;

public class EnableDisableRipple extends SimpleAbilityItem {
    public EnableDisableRipple(Properties properties) {
        super(properties, BeyonderClassInit.MONSTER, 3, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        enableOrDisableDangerSense(player);
        return InteractionResult.SUCCESS;
    }

    public static void enableOrDisableDangerSense(Player player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            boolean ripple = tag.getBoolean("monsterRipple");
            tag.putBoolean("monsterRipple", !ripple);
            player.displayClientMessage(Component.literal("Ripple of Chaos " + (ripple ? "Off" : "On")).withStyle(ChatFormatting.BOLD, ChatFormatting.GRAY), true);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, enables or disables your Ripple of Misfortune. If enabled, when you get hit, all entities around you will face a calamity or misfortunate effect."));
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

    public static void rippleOfMisfortune(Player player) { //ADD CHECKS FOR NEARBY MONSTERS AT SEQ 6 AND 3
        if (!player.level().isClientSide() && player.getPersistentData().getBoolean("monsterRipple")) {
            Level level = player.level();
            int enhancement = 1;
            if (level instanceof ServerLevel serverLevel) {
                enhancement = CalamityEnhancementData.getInstance(serverLevel).getCalamityEnhancement();
            }
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            if (holder.getSpirituality() >= 150) {
                holder.useSpirituality(150);
            }
            if (holder.getSpirituality() <= 150) {
                player.getPersistentData().putBoolean("monsterRipple", false);
                player.sendSystemMessage(Component.literal("Ripple of Misfortune turned off due to not enough spirituality").withStyle(ChatFormatting.RED));
            }
            for (LivingEntity livingEntity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate((int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENABLEDISABLERIPPLE.get())))) {
                Random random = new Random();
                if (livingEntity != player && !BeyonderUtil.isAllyOf(player, livingEntity) && livingEntity.getMaxHealth() >= 15) {
                    int randomInt = random.nextInt(14);
                    if (randomInt == 0) {
                        livingEntity.hurt(livingEntity.damageSources().generic(), livingEntity.getMaxHealth() / (10 - enhancement));
                    }
                    if (randomInt == 1) {
                        BlockPos hitPos = livingEntity.blockPosition();
                        double radius = 10 - (holder.getCurrentSequence() * 2);
                        for (BlockPos pos : BlockPos.betweenClosed(
                                hitPos.offset((int) -radius, (int) -radius, (int) -radius),
                                hitPos.offset((int) radius, (int) radius, (int) radius))) {
                            if (pos.distSqr(hitPos) <= radius * radius) {
                                if (livingEntity.level().getBlockState(pos).getDestroySpeed(livingEntity.level(), pos) >= 0) {
                                    livingEntity.level().setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                                }
                            }
                        }
                        List<Entity> entities = livingEntity.level().getEntities(livingEntity,
                                new AABB(hitPos.offset((int) -radius, (int) -radius, (int) -radius),
                                        hitPos.offset((int) radius, (int) radius, (int) radius)));
                        for (Entity entity : entities) {
                            if (entity instanceof LivingEntity explosionHitEntity) {
                                if (explosionHitEntity instanceof Player player1 && BeyonderHolderAttacher.getHolderUnwrap(player1).currentClassMatches(BeyonderClassInit.MONSTER)) {
                                    BeyonderHolder holder1 = BeyonderHolderAttacher.getHolderUnwrap(player1);
                                    int sequence = holder1.getCurrentSequence();
                                    if (sequence <= 5 && sequence > 3) {
                                        player1.hurt(BeyonderUtil.genericSource(player), 10 + (enhancement * 3));
                                    } else if (sequence <= 3) {
                                        return;
                                    }
                                } else {
                                    explosionHitEntity.hurt(BeyonderUtil.genericSource(player), 10 + (enhancement * 3));
                                }
                            }
                        }
                    }
                    if (randomInt == 2) {
                        LightningBolt lightningBolt = new LightningBolt(EntityType.LIGHTNING_BOLT, player.level());
                        lightningBolt.setDamage(30 - (holder.getCurrentSequence() * 5));
                        lightningBolt.setPos(livingEntity.getOnPos().getCenter());
                        if (player instanceof ServerPlayer serverPlayer) {
                            lightningBolt.setCause(serverPlayer);
                        }
                        for (int i = 0; i < enhancement; i++) {
                            player.level().addFreshEntity(lightningBolt);
                        }
                    }
                    if (randomInt == 3) {
                        TornadoEntity tornadoEntity = new TornadoEntity(EntityInit.TORNADO_ENTITY.get(), player.level());
                        tornadoEntity.setTornadoLifecount(100);
                        tornadoEntity.setOwner(player);
                        tornadoEntity.setTornadoPickup(true);
                        tornadoEntity.setTornadoRadius(30 - (holder.getCurrentSequence() * 6) + (enhancement * 5));
                        tornadoEntity.setTornadoHeight(50 - (holder.getCurrentSequence() * 8) + (enhancement * 8));
                        tornadoEntity.teleportTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
                        player.level().addFreshEntity(tornadoEntity);
                        for (LivingEntity otherEntities : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(60))) {
                            if (otherEntities instanceof Player player1 && BeyonderHolderAttacher.getHolderUnwrap(player1).currentClassMatches(BeyonderClassInit.MONSTER)) {
                                BeyonderHolder holder1 = BeyonderHolderAttacher.getHolderUnwrap(player1);
                                int sequence = holder1.getCurrentSequence();
                                if (sequence <= 5 && sequence > 3) {
                                    player1.getPersistentData().putInt("luckTornadoResistance", 6);
                                } else if (sequence <= 3) {
                                    player1.getPersistentData().putInt("luckTornadoImmunity", 6);
                                }
                            }
                        }
                    }
                    if (randomInt == 4) {
                        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(40 - (holder.getCurrentSequence() * 10) + (enhancement * 10)))) {
                            if (entity != player) {
                                if (entity instanceof Player player1 && BeyonderHolderAttacher.getHolderUnwrap(player1).currentClassMatches(BeyonderClassInit.MONSTER)) {
                                    BeyonderHolder holder1 = BeyonderHolderAttacher.getHolderUnwrap(player1);
                                    int sequence = holder1.getCurrentSequence();
                                    if (sequence <= 5 && sequence > 3) {
                                        entity.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(), 30 - (holder.getCurrentSequence() * 6), 1, false, false));
                                        entity.setTicksFrozen(60 - (holder.getCurrentSequence() * 12));
                                    } else if (sequence <= 3) {
                                        return;
                                    }
                                }
                                entity.addEffect(new MobEffectInstance(ModEffects.PARALYSIS.get(), 60 - (holder.getCurrentSequence() * 12), 1, false, false));
                                entity.setTicksFrozen(60 - (holder.getCurrentSequence() * 12));

                            }
                        }
                    }
                    if (randomInt == 5) {
                        StoneEntity stoneEntity = new StoneEntity(EntityInit.STONE_ENTITY.get(), level);
                        stoneEntity.teleportTo(livingEntity.getX() + (Math.random() * 10) - 5, livingEntity.getY() + (Math.random() * 10) - 5, livingEntity.getZ() + (Math.random() * 10) - 5);
                        stoneEntity.setStoneXRot((int) (Math.random() * 10) - 5);
                        stoneEntity.setStoneYRot((int) (Math.random() * 10) - 5);
                        stoneEntity.setDeltaMovement(0, -2, 0);
                        for (int i = 0; i < enhancement; i++) {
                            if (holder.getCurrentSequence() >= 2) {
                                player.level().addFreshEntity(stoneEntity);
                                player.level().addFreshEntity(stoneEntity);
                                player.level().addFreshEntity(stoneEntity);
                                player.level().addFreshEntity(stoneEntity);
                                player.level().addFreshEntity(stoneEntity);
                                player.level().addFreshEntity(stoneEntity);
                                player.level().addFreshEntity(stoneEntity);
                            } else {
                                player.level().addFreshEntity(stoneEntity);
                                player.level().addFreshEntity(stoneEntity);
                                player.level().addFreshEntity(stoneEntity);
                                player.level().addFreshEntity(stoneEntity);
                                player.level().addFreshEntity(stoneEntity);
                                player.level().addFreshEntity(stoneEntity);
                                player.level().addFreshEntity(stoneEntity);
                                player.level().addFreshEntity(stoneEntity);
                                player.level().addFreshEntity(stoneEntity);
                                player.level().addFreshEntity(stoneEntity);
                                player.level().addFreshEntity(stoneEntity);
                            }
                        }
                    }
                    if (randomInt == 6) {
                        if (livingEntity instanceof ServerPlayer serverPlayer && serverPlayer.getAbilities().mayfly) {
                            serverPlayer.setDeltaMovement(livingEntity.getDeltaMovement().x, -6 - enhancement, livingEntity.getDeltaMovement().z);
                        } else {
                            livingEntity.setDeltaMovement(livingEntity.getDeltaMovement().x, 6 + enhancement, livingEntity.getDeltaMovement().z);
                        }
                    }
                    if (randomInt == 7) {
                        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(25 - (holder.getCurrentSequence() * 5) + (enhancement * 5)))) {
                            if (entity instanceof Player pPlayer) {
                                BeyonderHolder holder1 = BeyonderHolderAttacher.getHolderUnwrap(pPlayer);
                                if (holder1.currentClassMatches(BeyonderClassInit.MONSTER)) {
                                    if (holder1.getCurrentSequence() <= 3) {
                                        return;
                                    } else if (holder1.getCurrentSequence() <= 6) {
                                        pPlayer.hurt(pPlayer.damageSources().lava(), 9);
                                        pPlayer.setSecondsOnFire(4 + (enhancement * 2));
                                    }
                                }
                            } else entity.hurt(entity.damageSources().lava(), 12);
                            entity.setSecondsOnFire(6 + (enhancement * 3));
                        }
                    }
                    if (randomInt == 8) {
                        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(25 - (holder.getCurrentSequence() * 5) + (enhancement * 5)))) {
                            CompoundTag tag = entity.getPersistentData();
                            if (entity instanceof Player pPlayer) {
                                BeyonderHolder holder1 = BeyonderHolderAttacher.getHolderUnwrap(pPlayer);
                                double corruptionAmount = pPlayer.getPersistentData().getDouble("misfortune");
                                if (holder1.getCurrentSequence() == 3) {
                                    tag.putDouble("corruption", corruptionAmount + 10 + (enhancement * 3));
                                } else if (holder1.getCurrentSequence() <= 2) {
                                    return;
                                } else {
                                    tag.putDouble("corruption", corruptionAmount + 30 + (enhancement * 5));
                                }
                            } else if (entity instanceof PlayerMobEntity pPlayer) {
                                double corruptionAmount = pPlayer.getPersistentData().getDouble("misfortune");
                                if (pPlayer.getCurrentSequence() == 3) {
                                    tag.putDouble("corruption", corruptionAmount + 10 + (enhancement * 3));
                                } else if (pPlayer.getCurrentSequence() <= 2) {
                                    return;
                                } else {
                                    tag.putDouble("corruption", corruptionAmount + 30 + (enhancement * 5));
                                }
                            } else {
                                entity.addEffect(new MobEffectInstance(MobEffects.WITHER, 120, 3 + enhancement, false, false));
                                entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 120, 3 + enhancement, false, false));
                            }
                        }
                    }
                    if (randomInt == 9) {
                        LightningEntity lightning = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), livingEntity.level());
                        lightning.setSpeed(5.0f);
                        lightning.setTargetEntity(livingEntity);
                        lightning.setDamage(12);
                        lightning.setMaxLength(120);
                        lightning.setNewStartPos(new Vec3(livingEntity.getX(), livingEntity.getY() + 80, livingEntity.getZ()));
                        lightning.setDeltaMovement(0, -3, 0);
                        lightning.setNoUp(true);
                        if (holder.getCurrentSequence() == 3) {
                            player.level().addFreshEntity(lightning);
                            if (enhancement >= 2) {
                                player.level().addFreshEntity(lightning);
                            }
                        }
                        if (holder.getCurrentSequence() <= 2 && holder.getCurrentSequence() >= 1) {
                            player.level().addFreshEntity(lightning);
                            player.level().addFreshEntity(lightning);
                            player.level().addFreshEntity(lightning);
                            if (enhancement >= 2) {
                                player.level().addFreshEntity(lightning);
                                player.level().addFreshEntity(lightning);
                            }
                        }
                        if (holder.getCurrentSequence() == 0) {
                            player.level().addFreshEntity(lightning);
                            player.level().addFreshEntity(lightning);
                            player.level().addFreshEntity(lightning);
                            player.level().addFreshEntity(lightning);
                            player.level().addFreshEntity(lightning);
                            if (enhancement >= 2) {
                                player.level().addFreshEntity(lightning);
                                player.level().addFreshEntity(lightning);
                                player.level().addFreshEntity(lightning);
                            }
                        }
                    }
                    if (randomInt == 10) {
                        livingEntity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200 - (holder.getCurrentSequence() * 30) + (enhancement * 30), 1, false, false));
                    }
                    if (randomInt == 11) {
                        if (livingEntity instanceof Player pPlayer) {
                            List<SimpleAbilityItem> validAbilities = new ArrayList<>();

                            // First collect all valid abilities
                            for (Item item : getAbilities(pPlayer)) {
                                if (item instanceof SimpleAbilityItem simpleAbilityItem) {  // Changed to SimpleAbilityItem instead of Ability
                                    boolean hasEntityInteraction = false;
                                    try {
                                        Method entityMethod = item.getClass().getDeclaredMethod("useAbilityOnEntity", ItemStack.class, Player.class, LivingEntity.class, InteractionHand.class);
                                        hasEntityInteraction = !entityMethod.equals(SimpleAbilityItem.class.getDeclaredMethod("useAbilityOnEntity", ItemStack.class, Player.class, LivingEntity.class, InteractionHand.class));
                                        if (hasEntityInteraction) {
                                            validAbilities.add(simpleAbilityItem);
                                        }
                                    } catch (NoSuchMethodException ignored) {
                                    }
                                }
                            }

                            // Then use one random ability outside the loop
                            if (!validAbilities.isEmpty()) {
                                int randomIndex = player.getRandom().nextInt(validAbilities.size());
                                SimpleAbilityItem selectedAbility = validAbilities.get(randomIndex);
                                ItemStack stack = selectedAbility.getDefaultInstance();
                                selectedAbility.useAbilityOnEntity(stack, pPlayer, pPlayer, InteractionHand.MAIN_HAND);
                            }
                        } else {
                            livingEntity.hurt(BeyonderUtil.mentalSource(level, player, livingEntity), 10);
                        }
                    }
                    if (randomInt == 12) {

                        Vex vex = new Vex(EntityType.VEX, level);
                        vex.setTarget(livingEntity);
                        vex.setPos(player.getX(), player.getY(), player.getZ());
                        vex.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 300, 2, false, false));
                        vex.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 300, 4 - holder.getCurrentSequence(), false, false));
                        vex.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 2, false, false));
                        for (int i = 0; i < enhancement; i++) {
                            if (holder.getCurrentSequence() == 3) {
                                player.level().addFreshEntity(vex);
                            }
                            if (holder.getCurrentSequence() <= 2 && holder.getCurrentSequence() >= 1) {
                                player.level().addFreshEntity(vex);
                                player.level().addFreshEntity(vex);
                                player.level().addFreshEntity(vex);
                            }
                            if (holder.getCurrentSequence() == 0) {
                                player.level().addFreshEntity(vex);
                                player.level().addFreshEntity(vex);
                                player.level().addFreshEntity(vex);
                                player.level().addFreshEntity(vex);
                                player.level().addFreshEntity(vex);
                            }
                        }
                    } else if (randomInt == 13) {
                        if (livingEntity instanceof Player itemPlayer) {
                            for (Item item : getAbilities(itemPlayer)) {
                                if (item instanceof SimpleAbilityItem simpleAbilityItem) {
                                    int currentCooldown = (int) player.getCooldowns().getCooldownPercent(item, 0);
                                    int cooldownToSet = simpleAbilityItem.getCooldown() * (100 - currentCooldown) + (enhancement * 10);
                                    if (currentCooldown < cooldownToSet) {
                                        player.getCooldowns().addCooldown(item, cooldownToSet);
                                    }
                                }
                            }
                        }
                    } else if (randomInt == 0) {
                        boolean healthCheck = player.getHealth() >= livingEntity.getHealth();
                        if (healthCheck) {
                            double x = player.getX() - livingEntity.getX();
                            double y = Math.min(5, player.getY() - livingEntity.getY());
                            double z = player.getZ() - livingEntity.getZ();
                            livingEntity.setDeltaMovement(x * 0.3, y * 0.3, z * 0.3);
                        } else {
                            double x = livingEntity.getX() - player.getX();
                            double y = livingEntity.getY() - player.getY();
                            double z = livingEntity.getZ() - player.getZ();
                            double magnitude = Math.sqrt(x * x + y * y + z * z);
                            livingEntity.setDeltaMovement(x / magnitude * 8, y / magnitude * 8, z / magnitude * 8);
                        }
                    }
                }
            }
        }
    }

}