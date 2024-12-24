package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.events.ability_events.AbilityEventsUtil;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class WindManipulationSense extends SimpleAbilityItem {


    public WindManipulationSense(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 7, 0, 0);
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

    public static void windManipulationSense(CompoundTag playerPersistentData, PlayerMobEntity player) {
        //WIND MANIPULATION SENSE
        boolean windManipulationSense = playerPersistentData.getBoolean("windManipulationSense");
        if (!windManipulationSense) {
            return;
        }
        if (player.useSpirituality(2)) return;
        double radius = 100 - (player.getCurrentSequence() * 10);
        for (LivingEntity otherEntity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius))) {
            if (otherEntity == player) {
                continue;
            }
            if (otherEntity.getMaxHealth() >= player.getMaxHealth()) {
                player.setTarget(otherEntity);
            }
        }
    }

    public static void windManipulationSense(CompoundTag playerPersistentData, BeyonderHolder holder, Player player) {
        //WIND MANIPULATION SENSE
        boolean windManipulationSense = playerPersistentData.getBoolean("windManipulationSense");
        if (!windManipulationSense) {
            return;
        }
        if (!holder.useSpirituality(2)) return;
        double radius = 100 - (holder.getCurrentSequence() * 10);
        for (Player otherPlayer : player.level().getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(radius))) {
            if (otherPlayer == player) {
                continue;
            }
            Vec3 directionToPlayer = otherPlayer.position().subtract(player.position()).normalize();
            Vec3 lookAngle = player.getLookAngle();
            double horizontalAngle = Math.atan2(directionToPlayer.x, directionToPlayer.z) - Math.atan2(lookAngle.x, lookAngle.z);

            String message = AbilityEventsUtil.getString(otherPlayer, horizontalAngle, directionToPlayer);
            if (player.tickCount % 200 == 0) {
                player.sendSystemMessage(Component.literal(message).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE));
            }
        }
    }

    public static void windManipulationSense(Player player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            boolean windManipulationSense = tag.getBoolean("windManipulationSense");
            tag.putBoolean("windManipulationSense", !windManipulationSense);
            player.displayClientMessage(Component.literal("Wind Sense Turned " + (windManipulationSense ? "Off" : "On")).withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
        }
    }
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.literal("Upon use, controls the surrounding wind to extend your senses, alerting you of players around you and where they are"));
        tooltipComponents.add(Component.literal("Activation Cost: ").append(Component.literal("None").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("40 per second").withStyle(ChatFormatting.YELLOW)));
        Component.literal("Cooldown: ").append(Component.literal("None").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}