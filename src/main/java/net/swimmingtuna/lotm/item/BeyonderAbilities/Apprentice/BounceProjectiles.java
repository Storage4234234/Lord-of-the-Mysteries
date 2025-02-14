package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class BounceProjectiles extends SimpleAbilityItem {


    public BounceProjectiles(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 8, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        if (!player.isShiftKeyDown()) {
            enableOrDisableArrows(player, player);
        } else {
            enableOrDisableArrowsShot(player);
        }
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    public static void enableOrDisableArrows(LivingEntity player, LivingEntity target) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            int damage = (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.TRICKBOUNCE.get());
            tag.putInt("apprenticeBounceHitArrows", damage);
            if (player instanceof Player pPlayer) {
                pPlayer.displayClientMessage(Component.literal("Projectiles of similar size or less will bounce off of " + target.getName().getString() + " for " + ((int) (damage / 20)) + " seconds").withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.BOLD), true);
            }
        }
    }

    public static void decrementBounceArrows(LivingEntity livingEntity) {
        if (livingEntity.getPersistentData().getInt("apprenticeBounceHitArrows") >= 1) {
            livingEntity.getPersistentData().putInt("apprenticeBounceHitArrows", livingEntity.getPersistentData().getInt("apprenticeBounceHitArrows") - 1);
        }
    }

    public static void enableOrDisableArrowsShot(LivingEntity player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            boolean shootProjectile = tag.getBoolean("ApprenticeBounceProjectileMovement");
            tag.putBoolean("ApprenticeBounceProjectileMovement", !shootProjectile);
            if (player instanceof Player pPlayer) {
                pPlayer.displayClientMessage(Component.literal("Bounce shot arrows turned " + (shootProjectile ? "off" : "on")).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.BOLD), true);
            }
        }
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, Player player, LivingEntity interactionTarget, InteractionHand hand) {
        if(!player.level().isClientSide && !interactionTarget.level().isClientSide){
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            enableOrDisableArrows(player, interactionTarget);
            addCooldown(player);
            useSpirituality(player);
        }
        return InteractionResult.SUCCESS;
    }



    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, disable or enable the ability to make all projectiles that would hit you or the target entity bounce off. If your shift key is down while using this item, you arrows will bounce towards their targets."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("0").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICES_ABILITY", ChatFormatting.BLUE);
    }
}