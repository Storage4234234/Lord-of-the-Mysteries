package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.entity.DivineHandRightEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class DivineHandRight extends SimpleAbilityItem {


    public DivineHandRight(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 1, 100, 500);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        divineHandRight(player);
        return InteractionResult.SUCCESS;
    }

    public static void divineHandRight(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            DivineHandRightEntity divineHandRight = new DivineHandRightEntity(EntityInit.DIVINE_HAND_RIGHT_ENTITY.get(), livingEntity.level());
            divineHandRight.setDeltaMovement(livingEntity.getLookAngle().scale(3));
            BeyonderUtil.setScale(divineHandRight, BeyonderUtil.getDamage(livingEntity).get(ItemInit.DIVINEHANDLEFT.get()));
            Vec3 scale = livingEntity.getLookAngle().scale(5.0f);
            divineHandRight.teleportTo(livingEntity.getX() + scale.x, livingEntity.getY(), livingEntity.getZ() + scale.z);
            divineHandRight.hurtMarked = true;
            divineHandRight.setOwner(livingEntity);
            divineHandRight.setYaw(livingEntity.getYRot());
            divineHandRight.setPitch(livingEntity.getXRot());
            livingEntity.level().addFreshEntity(divineHandRight);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, conjure the right hand of god, causing all blocks around it to be decayed, all allies hit to have their damage transfered to you for a minute, their luck increased, negative harmful effects removed, and corruption lowered."));
        tooltipComponents.add(Component.literal("Left Click for Divine Hand Left"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("1000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("25 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("WARRIOR_ABILITY", ChatFormatting.YELLOW);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        return 0;
    }
}

