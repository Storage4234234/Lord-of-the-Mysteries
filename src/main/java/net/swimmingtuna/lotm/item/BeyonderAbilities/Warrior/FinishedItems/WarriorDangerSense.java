package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.List;

public class WarriorDangerSense extends SimpleAbilityItem {


    public WarriorDangerSense(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 9, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        startGigantification(player);
        return InteractionResult.SUCCESS;
    }

    public static void startGigantification(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(livingEntity);
            scaleData.setTargetScale(3.0f);
        }
    }

    public static void warriorDangerSense(LivingEntity livingEntity) {
        boolean warriorDangerSense = livingEntity.getPersistentData().getBoolean("warriorDangerSense");
        if (!warriorDangerSense) {
            return;
        }
        double radius = 200 - (BeyonderUtil.getSequence(livingEntity) * 20);
        for (Player otherPlayer : livingEntity.level().getEntitiesOfClass(Player.class, livingEntity.getBoundingBox().inflate(radius))) {
            if (otherPlayer == livingEntity || BeyonderUtil.isAllyOf(livingEntity, otherPlayer)) {
                continue;
            }
            if (otherPlayer.getMainHandItem().getItem() instanceof SimpleAbilityItem || otherPlayer.getMainHandItem().getItem() instanceof ProjectileWeaponItem || otherPlayer.getMainHandItem().getItem() instanceof SwordItem || otherPlayer.getMainHandItem().getItem() instanceof AxeItem) { //also add for sealed artifacts
                Vec3 directionToPlayer = otherPlayer.position().subtract(livingEntity.position()).normalize();
                Vec3 lookAngle = livingEntity.getLookAngle();
                double horizontalAngle = Math.atan2(directionToPlayer.x, directionToPlayer.z) - Math.atan2(lookAngle.x, lookAngle.z);

                String horizontalDirection;
                if (Math.abs(horizontalAngle) < Math.PI / 4) {
                    horizontalDirection = "in front of";
                } else if (horizontalAngle < -Math.PI * 3 / 4 || horizontalAngle > Math.PI * 3 / 4) {
                    horizontalDirection = "behind";
                } else if (horizontalAngle < 0) {
                    horizontalDirection = "to the right of";
                } else {
                    horizontalDirection = "to the left of";
                }

                String verticalDirection;
                if (directionToPlayer.y > 0.2) {
                    verticalDirection = "above";
                } else if (directionToPlayer.y < -0.2) {
                    verticalDirection = "below";
                } else {
                    verticalDirection = "at the same level as";
                }

                String message = otherPlayer.getName().getString() + " is " + horizontalDirection + " and " + verticalDirection + " you.";
                if (livingEntity.tickCount % 200 == 0) {
                    livingEntity.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.BOLD, ChatFormatting.RED));
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, disable or enable your danger sense. If enabled,you will be aware of all entities around you holding a weapon."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("0").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("WARRIOR_ABILITY", ChatFormatting.YELLOW);
    }


}

