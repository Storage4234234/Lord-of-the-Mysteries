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
import net.swimmingtuna.lotm.entity.DivineHandLeftEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class DivineHandLeft extends SimpleAbilityItem {


    public DivineHandLeft(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 1, 1000, 500);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        divineHandLeft(player);
        return InteractionResult.SUCCESS;
    }

    public static void divineHandLeft(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            DivineHandLeftEntity divineHandLeft = new DivineHandLeftEntity(EntityInit.DIVINE_HAND_LEFT_ENTITY.get(), livingEntity.level());
            divineHandLeft.setDeltaMovement(livingEntity.getLookAngle().scale(3));
            BeyonderUtil.setScale(divineHandLeft, BeyonderUtil.getDamage(livingEntity).get(ItemInit.DIVINEHANDLEFT.get()));
            Vec3 scale = livingEntity.getLookAngle().scale(5.0f);
            divineHandLeft.teleportTo(livingEntity.getX() + scale.x, livingEntity.getY(), livingEntity.getZ() + scale.z);
            divineHandLeft.hurtMarked = true;
            divineHandLeft.setOwner(livingEntity);
            divineHandLeft.setYaw(livingEntity.getYRot());
            divineHandLeft.setPitch(livingEntity.getXRot());
            livingEntity.level().addFreshEntity(divineHandLeft);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, conjure the left hand of god, causing all blocks around it to be decayed, all entites to be aged, and pressed down heavily for some time."));
        tooltipComponents.add(Component.literal("Left Click for Divine Hand Right"));
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
        if (target != null) {
            return 70;
        }
        return 0;
    }
}

