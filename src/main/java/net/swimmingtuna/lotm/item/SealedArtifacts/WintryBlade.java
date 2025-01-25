package net.swimmingtuna.lotm.item.SealedArtifacts;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class WintryBlade extends SwordItem {


    public WintryBlade(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof LivingEntity player && !level.isClientSide()) {
            if (player.tickCount % 20 == 0) {
                if (player.getMainHandItem().getItem() instanceof WintryBlade) {
                    for (LivingEntity livingEntity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(20))) {
                        if (livingEntity != player) {
                            CompoundTag tag = livingEntity.getPersistentData();
                            if (livingEntity.getPersistentData().getInt("wintryBladeOthers") <= 1000) {
                                tag.putInt("wintryBladeOthers", tag.getInt("wintryBladeOthers") + 1);
                            }
                        }
                    }
                }
            }
            if (player.tickCount % 20 == 0) {
                CompoundTag tag = player.getPersistentData();
                if (player.getPersistentData().getInt("wintryBladeSelf") <= 100) {
                    tag.putInt("wintryBladeSelf", tag.getInt("wintryBladeSelf") + 1);
                }
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        if (pTarget.getPersistentData().getInt("wintryBladeOthers") <= 1000) {
            pTarget.getPersistentData().putInt("wintryBladeOthers", pTarget.getPersistentData().getInt("wintryBladeOthers") + 25);
        }
        return super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    public static void wintryBladeTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int x = tag.getInt("wintryBladeOthers");
        int y = tag.getInt("wintryBladeSelf");
        if (!livingEntity.level().isClientSide()) {
            if (livingEntity.tickCount % 10 == 0) {
                if (livingEntity.isOnFire()) {
                    if (x >= 1) {
                        tag.putInt("wintryBladeOthers", x - 1);
                    }
                    if (y >= 1) {
                        tag.putInt("wintryBladeSelf", y - 1);
                    }
                }
            }
            if (livingEntity.tickCount % 100 == 0) {
                if (x >= 1) {
                    tag.putInt("wintryBladeOthers", x - 1);
                }
                if (y >= 1) {
                    tag.putInt("wintryBladeSelf", y - 1);
                }
                if (x >= 1) {
                    if (x <= 10) {
                        BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 200, 0, true, true);
                    } else if (x <= 20) {
                        BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 200, 1, true, true);
                    } else if (x <= 30) {
                        BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 200, 2, true, true);
                        BeyonderUtil.applyMobEffect(livingEntity, MobEffects.CONFUSION, 200, 0, true, true);
                    } else if (x <= 40) {
                        BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 200, 3, true, true);
                        BeyonderUtil.applyMobEffect(livingEntity, MobEffects.CONFUSION, 200, 0, true, true);
                        livingEntity.setTicksFrozen(200);
                    } else if (x <= 50) {
                        BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 200, 4, true, true);
                        BeyonderUtil.applyMobEffect(livingEntity, MobEffects.CONFUSION, 200, 0, true, true);
                        livingEntity.setTicksFrozen(200);
                    } else {
                        livingEntity.setTicksFrozen(200);
                        BeyonderUtil.applyMobEffect(livingEntity, ModEffects.STUN.get(), 200, 0, true, true);
                    }
                }
                if (y >= 1) {
                    if (y <= 125) {
                        BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 200, 0, true, true);
                    } else if (y <= 250) {
                        BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 200, 1, true, true);
                    } else if (y <= 375) {
                        BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 200, 2, true, true);
                    } else if (y <= 500) {
                        BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 200, 3, true, true);
                        livingEntity.setTicksFrozen(200);
                    } else if (y <= 625) {
                        BeyonderUtil.applyMobEffect(livingEntity, MobEffects.MOVEMENT_SLOWDOWN, 200, 4, true, true);
                        livingEntity.setTicksFrozen(200);
                    } else {
                        BeyonderUtil.applyMobEffect(livingEntity, ModEffects.STUN.get(), 999999999, 0, true, true);
                        livingEntity.setTicksFrozen(200);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level
            level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
//        tooltipComponents.add(Component.literal("A transparent, triangular blade that will slow down those hit on the first hit, heavily slow down and confuse them on the second, and stun them in place for a long time on the third. The amount of time can stack.").withStyle(ChatFormatting.DARK_GRAY).withStyle(ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("In addition, apart from the user, those around the user will slow down over time, having the effects of being hit happen gradually").withStyle(ChatFormatting.DARK_BLUE));
        tooltipComponents.add(Component.literal("Drawback: As long as Wintry Blade is in your inventory, you will slow down more and more, until you're frozen permanently. You can reverse the effects up until that point in both yourself and others affected by this blade by being on fire.").withStyle(ChatFormatting.GRAY));

        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }
}
