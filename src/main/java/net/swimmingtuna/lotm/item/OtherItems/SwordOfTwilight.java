package net.swimmingtuna.lotm.item.OtherItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class SwordOfTwilight extends SwordItem {


    public SwordOfTwilight(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }


    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide()) {
            if (livingEntity.tickCount % 20 == 0 && !livingEntity.level().isClientSide()) {
                int sequence = BeyonderUtil.getSequence(livingEntity);
                if (livingEntity instanceof Player player) {
                    BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                    if (!holder.currentClassMatches(BeyonderClassInit.WARRIOR) || sequence >= 7) {
                        player.getInventory().setItem(itemSlot, ItemStack.EMPTY);
                    } else {
                        if (holder.getSpirituality() >= 25) {
                            holder.useSpirituality(25);
                        } else {
                            player.getInventory().setItem(itemSlot, ItemStack.EMPTY);
                        }
                    }
                } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
                    if (playerMobEntity.getCurrentPathway() != BeyonderClassInit.WARRIOR || playerMobEntity.getCurrentSequence() >= 7) {
                        removeItemFromSlot(livingEntity, stack);
                    } else {
                        if (playerMobEntity.getSpirituality() >= 25) {
                            playerMobEntity.useSpirituality(25);
                        } else {
                            removeItemFromSlot(livingEntity, stack);
                        }
                    }
                } else {
                    removeItemFromSlot(livingEntity, stack);
                }
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    private void removeItemFromSlot(LivingEntity entity, ItemStack stack) {
        if (entity.getItemBySlot(EquipmentSlot.MAINHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        else if (entity.getItemBySlot(EquipmentSlot.OFFHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        boolean canBePurified = pTarget.getName().getString().toLowerCase().contains("demon") || pTarget.getName().getString().toLowerCase().contains("ghost") || pTarget.getName().getString().toLowerCase().contains("wraith") || pTarget.getName().getString().toLowerCase().contains("zombie") || pTarget.getName().getString().toLowerCase().contains("undead") || pTarget.getPersistentData().getBoolean("isWraith");
        if (canBePurified) {
            pTarget.hurt(BeyonderUtil.magicSource(pAttacker), this.getDamage());
            pTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2, false, false));
            pTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, false, false));
        }
        return super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        if (!pPlayer.level().isClientSide()) {
            pPlayer.startUsingItem(pUsedHand);
        }
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity pLivingEntity, int pTimeCharged) {
        if (!(pLivingEntity instanceof Player player)) {
            return;
        }
        int i = this.getUseDuration(pStack) - pTimeCharged;
        float powerScale = getPowerForTime(i);
        if (!pLevel.isClientSide) {
            Arrow arrow = new Arrow(pLevel, player);
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, powerScale * 3.0F, 1.0F);

            arrow.setCritArrow(powerScale > 0.5F);
            arrow.setBaseDamage(arrow.getBaseDamage() * 2.0);

            // Spawn the arrow in the world
            pLevel.addFreshEntity(arrow);
        }
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000; // Same as bow
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.SPEAR; // Or BOW if you prefer that animation
    }

    private float getPowerForTime(int pCharge) {
        float f = (float)pCharge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("WORK IN PROGRESS").withStyle(ChatFormatting.RED));
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("DAWN_ITEM", ChatFormatting.YELLOW);
    }


}
