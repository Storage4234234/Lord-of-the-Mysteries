package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

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
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class WindManipulationSense extends SimpleAbilityItem {


    public WindManipulationSense(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        windManipulationSense(player);
        return InteractionResult.SUCCESS;
    }

    public static void windManipulationSense(Player player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            boolean windManipulationSense = tag.getBoolean("windManipulationSense");
            tag.putBoolean("windManipulationSense", !windManipulationSense);
            player.displayClientMessage(Component.literal("Wind Sense Turned " + (windManipulationSense ? "Off" : "On")).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
            int sequence = BeyonderUtil.getSequence(player);
            double radius = 100 - (sequence * 10);
            for (Player otherPlayer : player.level().getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(radius))) {
                if (otherPlayer == player) {
                    continue;
                }
                Vec3 directionToPlayer = otherPlayer.position().subtract(player.position()).normalize();
                Vec3 lookAngle = player.getLookAngle();
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
                if (player.tickCount % 200 == 0) {
                    player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE));
                }
            }
        }
    }

    public static void windManipulationSense(LivingEntity livingEntity) {
        CompoundTag tag = livingEntity.getPersistentData();
        boolean windManipulationSense = tag.getBoolean("windManipulationSense");
        if (!windManipulationSense) {
            return;
        }
        if (BeyonderUtil.getSpirituality(livingEntity) <= 1) return;
        BeyonderUtil.useSpirituality(livingEntity, 1);
        double radius = 100 - (BeyonderUtil.getSequence(livingEntity) * 10);
        for (Player otherPlayer : livingEntity.level().getEntitiesOfClass(Player.class, livingEntity.getBoundingBox().inflate(radius))) {
            if (otherPlayer == livingEntity || BeyonderUtil.isAllyOf(livingEntity, otherPlayer)) {
                continue;
            }
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
            if (livingEntity.tickCount % 140 == 0) {
                livingEntity.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE));
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, summon constant winds around you. While active, you'll get the location of nearby entities"));
        tooltipComponents.add(Component.literal("Left Click for Wind Manipulation (Blade)"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("20 per second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }
}