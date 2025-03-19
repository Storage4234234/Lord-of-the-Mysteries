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
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.SilverLightEntity;
import net.swimmingtuna.lotm.entity.SpearOfDawnEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class SpearOfDawn extends SwordItem {


    public SpearOfDawn(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }


    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide()) {
            if (livingEntity.tickCount % 20 == 0 && !livingEntity.level().isClientSide()) {
                if (!BeyonderUtil.currentPathwayAndSequenceMatches(livingEntity, BeyonderClassInit.WARRIOR.get(), 6)) {
                    removeItemFromSlot(livingEntity, stack);
                } else {
                    if (BeyonderUtil.getSpirituality(livingEntity) >= 25) {
                        BeyonderUtil.useSpirituality(livingEntity, 25);
                    } else {
                        removeItemFromSlot(livingEntity, stack);
                    }
                }
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    private void removeItemFromSlot(LivingEntity entity, ItemStack stack) {
        if (entity.getItemBySlot(EquipmentSlot.MAINHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        } else if (entity.getItemBySlot(EquipmentSlot.OFFHAND) == stack) {
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
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int pTimeCharged) {
        if (!level.isClientSide) {
            SpearOfDawnEntity silverLight = new SpearOfDawnEntity(EntityInit.SPEAR_OF_DAWN_ENTITY.get(), level);
            Vec3 lookVec = livingEntity.getLookAngle().normalize().scale(5);
            silverLight.setDeltaMovement(lookVec);
            silverLight.setOwner(livingEntity);
            silverLight.hurtMarked = true;
            silverLight.teleportTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
            BeyonderUtil.setScale(silverLight, BeyonderUtil.getDamage(livingEntity).get(ItemInit.SPEAROFDAWN.get()));
            livingEntity.level().addFreshEntity(silverLight);
            removeItemFromSlot(livingEntity, stack);
        }
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.SPEAR;
    }

    private float getPowerForTime(int pCharge) {
        float f = (float) pCharge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Can be used as a melee weapon, or hold down right click to throw it at a rapid speed."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("25 per second.").withStyle(ChatFormatting.YELLOW)));
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("DAWN_ITEM", ChatFormatting.YELLOW);
    }


}
