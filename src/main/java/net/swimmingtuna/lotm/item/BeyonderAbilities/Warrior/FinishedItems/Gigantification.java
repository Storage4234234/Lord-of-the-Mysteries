package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.MisfortuneManipulation;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.List;

public class Gigantification extends SimpleAbilityItem {


    public Gigantification(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        gigantification(player);
        return InteractionResult.SUCCESS;
    }

    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof Player player) {
            if (player.tickCount % 2 == 0 && !level.isClientSide()) {
                int sequence = BeyonderUtil.getSequence(player);
                if (sequence <= 4) {
                    boolean destroyBlocks = entity.getPersistentData().getBoolean("warriorShouldDestroyBlock");
                    if (player.getMainHandItem().getItem() instanceof Gigantification) {
                        player.displayClientMessage(Component.literal("Block Destroying: " + (destroyBlocks ? "On" : "Off")).withStyle(ChatFormatting.YELLOW).withStyle(ChatFormatting.BOLD), true);
                    }
                }
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    public static void gigantification(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            CompoundTag tag = livingEntity.getPersistentData();
            boolean isGiant = tag.getBoolean("warriorGiant");
            boolean isHoGGiant = tag.getBoolean("handOfGodGiant");
            boolean isTwilightGiant = tag.getBoolean("twilightGiant");
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(livingEntity);
            int sequence = BeyonderUtil.getSequence(livingEntity);
            float scaleToSet = BeyonderUtil.getDamage(livingEntity).get(ItemInit.GIGANTIFICATION.get());
            if (isGiant || isHoGGiant || isTwilightGiant) {
                scaleData.setTargetScale(1.0f);
                tag.putBoolean("warriorGiant", false);
                tag.putBoolean("handOfGodGiant", false);
                tag.putBoolean("twilightGiant", false);
            } else if (sequence <= 6 && sequence >= 2) {
                tag.putBoolean("warriorGiant", true);
                tag.putBoolean("handOfGodGiant", false);
                tag.putBoolean("twilightGiant", false);
                scaleData.setTargetScale(scaleToSet);
            } else if (sequence == 1) {
                tag.putBoolean("handOfGodGiant", true);
                tag.putBoolean("warriorGiant", false);
                tag.putBoolean("twilightGiant", false);
                scaleData.setTargetScale(scaleToSet * 2);
            } else if (sequence == 0) {
                tag.putBoolean("handOfGodGiant", false);
                tag.putBoolean("warriorGiant", false);
                tag.putBoolean("twilightGiant", true);
                scaleData.setTargetScale(scaleToSet * 3);
            }
        }
    }

    public static void gigantificationDestroyBlocks(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide() && entity.isShiftKeyDown() && entity.tickCount % 20 == 0) {
            CompoundTag tag = entity.getPersistentData();
            boolean isGiant = tag.getBoolean("warriorGiant");
            boolean isHoGGiant = tag.getBoolean("handOfGodGiant");
            boolean isTwilightGiant = tag.getBoolean("twilightGiant");
            boolean destroyBlocks = tag.getBoolean("warriorShouldDestroyBlock");
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(entity);
            float scale = scaleData.getScale();
            if (destroyBlocks && (isGiant || isHoGGiant || isTwilightGiant)) {
                int radius = (int) (scale + 2);
                if (isHoGGiant) {
                    radius *= 2;
                } else if (isTwilightGiant) {
                    radius *= 4;
                }
                BlockPos playerPos = entity.blockPosition();
                Level level = entity.level();
                float obsidianStrength = 1200.0F;
                for (int x = -radius; x <= radius; x++) {
                    for (int y = -radius; y <= radius; y++) {
                        for (int z = -radius; z <= radius; z++) {
                            BlockPos pos = playerPos.offset(x, y, z);
                            if (pos.distSqr(playerPos) <= radius * radius) {
                                BlockState state = level.getBlockState(pos);
                                if (!state.isAir() && state.getBlock().getExplosionResistance() < obsidianStrength) {
                                    level.destroyBlock(pos, false);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public static void gigantificationScale(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        if (!livingEntity.level().isClientSide()) {
            boolean isGiant = tag.getBoolean("warriorGiant");
            boolean isHoGGiant = tag.getBoolean("handOfGodGiant");
            boolean isTwilightGiant = tag.getBoolean("twilightGiant");
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(livingEntity);
            float scale = 1.0f;
            int sequence = BeyonderUtil.getSequence(livingEntity);
            if (sequence == 6) {
                scale = 1.3f;
            } else if (sequence == 5) {
                scale = 1.5f;
            } else if (sequence == 4) {
                scale = 1.5f;
            } else if (sequence == 3) {
                scale = 2.0f;
            } else if (sequence == 2) {
                scale = 2.3f;
            } else if (sequence == 1) {
                scale = 2.3f;
            } else if (sequence == 0) {
                scale = 2.5f;
            }
            BeyonderClass pathway = BeyonderUtil.getPathway(livingEntity);
            if (pathway == BeyonderClassInit.WARRIOR.get()) {
                float scaleToSet = BeyonderUtil.getDamage(livingEntity).get(ItemInit.GIGANTIFICATION.get());
                if (!isGiant && !isHoGGiant && !isTwilightGiant) {
                    scaleData.setTargetScale(scale);
                } else if (isGiant && !isHoGGiant && !isTwilightGiant) {
                    scaleData.setTargetScale(scaleToSet);
                } else if (!isGiant && isHoGGiant && !isTwilightGiant) {
                    scaleData.setTargetScale(scaleToSet * 2);
                } else if (!isGiant && !isHoGGiant && isTwilightGiant) {
                    scaleData.setTargetScale(scaleToSet * 3);
                }
                if (isTwilightGiant) {
                    tag.putBoolean("handOfGodGiant", false);
                    tag.putBoolean("warriorGiant", false);
                } else if (isHoGGiant) {
                    tag.putBoolean("warriorGiant", false);
                    tag.putBoolean("twilightGiant", false);
                } else if (isGiant) {
                    tag.putBoolean("twilightGiant", false);
                    tag.putBoolean("handOfGodGiant", false);

                }
            }
        }
    }
    public static void warriorGiant(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide() && livingEntity.tickCount % 20 == 0) {
            boolean isGiant = livingEntity.getPersistentData().getBoolean("warriorGiant");
            boolean isHoGGiant = livingEntity.getPersistentData().getBoolean("handOfGodGiant");
            boolean isTwilightGiant = livingEntity.getPersistentData().getBoolean("twilightGiant");
            if (isGiant || isHoGGiant || isTwilightGiant) {
                int amp = 0;
                if (livingEntity.hasEffect(MobEffects.DAMAGE_RESISTANCE)) {
                    amp = livingEntity.getEffect(MobEffects.DAMAGE_RESISTANCE).getAmplifier();
                }
                BeyonderUtil.applyMobEffect(livingEntity, MobEffects.DAMAGE_RESISTANCE, 40, amp + 1, true, true);
            }
        }
    }
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, transform into a giant, in this form, you take less damage, have small amounts negated. In addition, if your sequence is less than four, you can break the blocks around you by shifting."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("0").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

}

