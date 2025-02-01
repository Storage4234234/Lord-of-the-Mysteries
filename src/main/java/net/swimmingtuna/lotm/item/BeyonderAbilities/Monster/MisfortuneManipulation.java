package net.swimmingtuna.lotm.item.BeyonderAbilities.Monster;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.LightningEntity;
import net.swimmingtuna.lotm.entity.MeteorEntity;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.entity.TornadoEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import net.swimmingtuna.lotm.world.worlddata.CalamityEnhancementData;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MisfortuneManipulation extends SimpleAbilityItem {
    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    public MisfortuneManipulation(Properties properties) {
        super(properties, BeyonderClassInit.MONSTER, 4, 300, 170, 100, 100);
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            useSpirituality(player);
            addCooldown(player);
            manipulateMisfortune(interactionTarget, player);
        }
        return InteractionResult.SUCCESS;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.lazyAttributeMap.get();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    private Multimap<Attribute, AttributeModifier> createAttributeMap() {
        ImmutableMultimap.Builder<Attribute, AttributeModifier> attributeBuilder = ImmutableMultimap.builder();
        attributeBuilder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND));
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 100, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 100, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, cause calamity to befall the target in any way you wish."));
        tooltipComponents.add(Component.literal("Left click to cycle"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("300").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("8.5 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    private static void manipulateMisfortune(LivingEntity interactionTarget, Player player) {
        if (!player.level().isClientSide() && !interactionTarget.level().isClientSide()) {
            CompoundTag tag = interactionTarget.getPersistentData();
            CompoundTag playerTag = player.getPersistentData();
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            int misfortuneManipulation = playerTag.getInt("misfortuneManipulationItem");
            if (misfortuneManipulation == 1) {
                if (holder.getCurrentSequence() > 2) {
                    summonMeteor(interactionTarget, player);
                } else if (holder.getCurrentSequence() == 2 || holder.getCurrentSequence() == 1) {
                    summonMeteor(interactionTarget, player);
                    summonMeteor(interactionTarget, player);
                } else if (holder.getCurrentSequence() == 0) {
                    summonMeteor(interactionTarget, player);
                    summonMeteor(interactionTarget, player);
                    summonMeteor(interactionTarget, player);
                    summonMeteor(interactionTarget, player);
                    summonMeteor(interactionTarget, player);
                }
            }
            if (misfortuneManipulation == 2) {
                TornadoEntity tornadoEntity = new TornadoEntity(EntityInit.TORNADO_ENTITY.get(), player.level());
                tornadoEntity.setTornadoLifecount(300 - (holder.getCurrentSequence() * 50));
                tornadoEntity.setOwner(player);
                tornadoEntity.setTornadoPickup(true);
                tornadoEntity.setTornadoRadius((int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MISFORTUNEMANIPULATION.get()) * 4);
                tornadoEntity.setTornadoHeight((int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MISFORTUNEMANIPULATION.get()) * 8);
                tornadoEntity.teleportTo(interactionTarget.getX(), interactionTarget.getY(), interactionTarget.getZ());
                player.level().addFreshEntity(tornadoEntity);
            } else if (misfortuneManipulation == 3) {
                interactionTarget.getPersistentData().putInt("sailorLightningStorm2", (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MISFORTUNEMANIPULATION.get()) * 15);
                interactionTarget.getPersistentData().putInt("sailorStormVecX2", (int) interactionTarget.getX());
                interactionTarget.getPersistentData().putInt("sailorStormVecY2", (int) interactionTarget.getY());
                interactionTarget.getPersistentData().putInt("sailorStormVecZ2", (int) interactionTarget.getZ());
            } else if (misfortuneManipulation == 4) {
                LightningEntity lightning = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), interactionTarget.level());
                lightning.setSpeed(5.0f);
                lightning.setDamage((int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MISFORTUNEMANIPULATION.get()));
                lightning.setTargetEntity(interactionTarget);
                lightning.setMaxLength(200);
                lightning.setOwner(player);
                lightning.teleportTo(interactionTarget.getX(), interactionTarget.getY() + 80, interactionTarget.getZ());
                lightning.setNewStartPos(new Vec3(interactionTarget.getX(), interactionTarget.getY() + 80, interactionTarget.getZ()));
                lightning.setDeltaMovement(0, -3, 0);
                lightning.setNoUp(true);
                player.level().addFreshEntity(lightning);
            } else if (misfortuneManipulation == 5) {
                for (Mob mob : interactionTarget.level().getEntitiesOfClass(Mob.class, interactionTarget.getBoundingBox().inflate(BeyonderUtil.getDamage(player).get(ItemInit.MISFORTUNEMANIPULATION.get()) * 4))) {
                    if (mob.getTarget() != interactionTarget) {
                        mob.setTarget(interactionTarget);
                        mob.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2, false, false));
                    }
                }
            } else if (misfortuneManipulation == 6) {
                tag.putInt("luckDoubleDamage", tag.getInt("luckDoubleDamage") + (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MISFORTUNEMANIPULATION.get()) / 3);
            } else if (misfortuneManipulation == 7) {
                Random random = new Random();
                List<EquipmentSlot> armorSlots = Arrays.asList(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET);
                List<EquipmentSlot> equippedArmor = armorSlots.stream()
                        .filter(slot -> !interactionTarget.getItemBySlot(slot).isEmpty())
                        .toList();
                if (!equippedArmor.isEmpty()) {
                    EquipmentSlot randomArmorSlot = equippedArmor.get(random.nextInt(equippedArmor.size()));
                    ItemStack armorPiece = interactionTarget.getItemBySlot(randomArmorSlot);
                    interactionTarget.spawnAtLocation(armorPiece);
                    interactionTarget.setItemSlot(randomArmorSlot, ItemStack.EMPTY);
                }
            } else if (misfortuneManipulation == 8) {
                tag.putInt("luckIgnoreAbility", tag.getInt("luckIgnoreAbility") + 1);
            } else if (misfortuneManipulation == 9) {
                BeyonderUtil.applyMobEffect(interactionTarget, MobEffects.POISON, (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MISFORTUNEMANIPULATION.get()) * 20, 4, true, true);
            } else if (misfortuneManipulation == 10) {
                tag.putInt("monsterMisfortuneManipulationGravity", (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MISFORTUNEMANIPULATION.get()) * 20);
            } else if (misfortuneManipulation == 11) {
                for (PlayerMobEntity playerMobEntity : interactionTarget.level().getEntitiesOfClass(PlayerMobEntity.class, interactionTarget.getBoundingBox().inflate(BeyonderUtil.getDamage(player).get(ItemInit.MISFORTUNEMANIPULATION.get()) * 20))) {
                    playerMobEntity.setTarget(interactionTarget);
                }
            } else if (misfortuneManipulation == 12) {
                tag.putInt("abilitySelfTarget", (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.MISFORTUNEMANIPULATION.get()) / 3);
            }
        }

    }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof Player player) {
            if (player.tickCount % 2 == 0 && !level.isClientSide()) {
                if (player.getMainHandItem().getItem() instanceof MisfortuneManipulation) {
                    player.displayClientMessage(Component.literal("Current Misfortune Manipulation is: " + misfortuneManipulationString(player)).withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GRAY), true);
                }
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    public static String misfortuneManipulationString(Player pPlayer) {
        CompoundTag tag = pPlayer.getPersistentData();
        int luckManipulation = tag.getInt("misfortuneManipulationItem");
        if (luckManipulation == 1) {
            return "Meteor";
        } else if (luckManipulation == 2) {
            return "Tornado";
        } else if (luckManipulation == 3) {
            return "Lightning Storm";
        } else if (luckManipulation == 4) {
            return "Lightning Bolt";
        } else if (luckManipulation == 5) {
            return "Attract Mobs";
        } else if (luckManipulation == 6) {
            return "Double next damage";
        } else if (luckManipulation == 7) {
            return "Unequip Armor";
        } else if (luckManipulation == 8) {
            return "Next Ability Use Failed";
        } else if (luckManipulation == 9) {
            return "Poison";
        } else if (luckManipulation == 10) {
            return "Gravity Press";
        } else if (luckManipulation == 11) {
            return "Rogue Beyonders will target them";
        } else if (luckManipulation == 12) {
            return "Next 5 Targeted Abilities will target the user";
        }
        return "None";
    }

    public static void summonMeteor(LivingEntity entity, LivingEntity player) {
        if (!entity.level().isClientSide()) {
            int x = (int) entity.getX();
            int y = (int) entity.getY();
            int z = (int) entity.getZ();
            Vec3 targetPos = new Vec3(x, y, z);
            Level level = player.level();
            int enhancement = 1;
            if (level instanceof ServerLevel serverLevel) {
                enhancement = CalamityEnhancementData.getInstance(serverLevel).getCalamityEnhancement();
            }
            MeteorEntity meteor = new MeteorEntity(EntityInit.METEOR_ENTITY.get(), entity.level());
            meteor.teleportTo(x + (Math.random() * 100) - 50, y + 150 + (Math.random() * 100) - 50, z + (Math.random() * 100) - 50);
            meteor.noPhysics = true;
            meteor.setOwner(player);
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(meteor);
            scaleData.setScale(3 + (enhancement));
            scaleData.markForSync(true);
            Vec3 randomizedTargetPos = targetPos.add((Math.random() * 20 - 10), (Math.random() * 20 - 10), (Math.random() * 20 - 10));
            double speed = 4.0;
            Vec3 directionToTarget = randomizedTargetPos.subtract(meteor.position()).normalize();
            meteor.setDeltaMovement(directionToTarget.scale(speed));
            entity.level().addFreshEntity(meteor);
        }
    }

    public static void livingTickMisfortuneManipulation(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        if (!livingEntity.level().isClientSide()) {
            int gravity = tag.getInt("monsterMisfortuneManipulationGravity");
            if (gravity >= 1) {
                tag.putInt("monsterMisfortuneManipulationGravity", gravity - 1);
                livingEntity.getDeltaMovement().add(0, -2, 0);
            }
        }
    }

    public static void livingUseAbilityMisfortuneManipulation(LivingEntity livingEntity) {
        CompoundTag tag = livingEntity.getPersistentData();
        if (!livingEntity.level().isClientSide() && livingEntity instanceof Player player) {
            int selfTarget = tag.getInt("abilitySelfTarget");
            if (selfTarget >= 1 && livingEntity.getMainHandItem().getItem() instanceof SimpleAbilityItem simpleAbilityItem) {
                boolean hasEntityInteraction = false;
                try {
                    Method entityMethod = simpleAbilityItem.getClass().getDeclaredMethod("useAbilityOnEntity", ItemStack.class, Player.class, LivingEntity.class, InteractionHand.class);
                    hasEntityInteraction = !entityMethod.equals(SimpleAbilityItem.class.getDeclaredMethod("useAbilityOnEntity", ItemStack.class, Player.class, LivingEntity.class, InteractionHand.class));

                } catch (NoSuchMethodException ignored) {
                }
                if (hasEntityInteraction) {
                    ItemStack stack = simpleAbilityItem.getDefaultInstance();
                    simpleAbilityItem.useAbilityOnEntity(stack, player, livingEntity, InteractionHand.MAIN_HAND);
                    tag.putInt("abilitySelfTarget", selfTarget - 1);
                }
            }
        }
    }

    public static void livingLightningStorm(LivingEntity livingEntity) {
        //MISFORTUNE MANIPULATION
        if (livingEntity.tickCount % 5 == 0) {
            CompoundTag tag = livingEntity.getPersistentData();
            int sailorLightningStorm2 = tag.getInt("sailorLightningStorm2");
            int x1 = livingEntity.getPersistentData().getInt("sailorStormVecX2");
            int y1 = livingEntity.getPersistentData().getInt("sailorStormVecY2");
            int z1 = livingEntity.getPersistentData().getInt("sailorStormVecZ2");
            if (sailorLightningStorm2 >= 1) {
                Random random = new Random();
                tag.putInt("sailorLightningStorm2", sailorLightningStorm2 - 1);
                LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), livingEntity.level());
                lightningEntity.setSpeed(7.0f);
                lightningEntity.setDamage(8);
                lightningEntity.setMaxLength(30);
                lightningEntity.setNoUp(true);
                if (random.nextInt(15) == 1) {
                    lightningEntity.teleportTo(livingEntity.getX(), lightningEntity.getY() + 50, lightningEntity.getZ());
                    lightningEntity.setTargetPos(livingEntity.getOnPos().getCenter());
                    lightningEntity.setDeltaMovement(0, -3, 0);
                } else {
                    lightningEntity.setDeltaMovement((Math.random() * 0.4) - 0.2, -3, (Math.random() * 0.4) - 0.2);
                    lightningEntity.teleportTo(x1 + ((Math.random() * 150) - (double) 150 / 2), y1 + 130, z1 + ((Math.random() * 150) - (double) 150 / 2));
                }
                lightningEntity.level().addFreshEntity(lightningEntity);
            }
        }
    }


    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("MONSTER_ABILITY", ChatFormatting.GRAY);
    }
}