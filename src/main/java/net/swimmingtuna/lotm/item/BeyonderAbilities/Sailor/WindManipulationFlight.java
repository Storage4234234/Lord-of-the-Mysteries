package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class WindManipulationFlight extends SimpleAbilityItem {


    public WindManipulationFlight(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 7, 0, 10);
    }

    public static void windManipulationFlight1(Player player, CompoundTag playerPersistentData) {
        //WIND MANIPULATION FLIGHT
        Vec3 lookVector = player.getLookAngle();
        if (!playerPersistentData.getBoolean("sailorFlight1")) {
            return;
        }
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        if (!holder.useSpirituality(2)) {
            return;
        }
        flightSailor(player, playerPersistentData, lookVector);
    }

    public static void windManipulationFlight(LivingEntity livingEntity, CompoundTag tag) {
        //WIND MANIPULATION FLIGHT
        Vec3 lookVector = livingEntity.getLookAngle();
        if (!tag.getBoolean("sailorFlight1")) {
            return;
        }
        flightSailor(livingEntity, tag, lookVector);
    }

    private static void flightSailor(LivingEntity livingEntity, CompoundTag tag, Vec3 lookVector) {
        int flight = tag.getInt("sailorFlight");
        int flightCancel = tag.getInt("sailorFlightDamageCancel");
        if (flightCancel >= 1) {
            tag.putInt("sailorFlightDamageCancel", flightCancel + 1);
        }
        if (flightCancel >= 300) {
            tag.putInt("sailorFlightDamageCancel", 0);
        }
        if (flight >= 1) {
            tag.putInt("sailorFlight", flight + 1);
        }
        if (flight <= 60 && flight % 20 == 0) {
            livingEntity.setDeltaMovement(lookVector.x * 2, lookVector.y * 2, lookVector.z * 2);
            livingEntity.hurtMarked = true;
        }
        if (flight > 60) {
            tag.putInt("sailorFlight", 0);
        }
    }


    public static void windManipulationFlight(Player player, CompoundTag tag) {
        Vec3 lookVector = player.getLookAngle();
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        if (tag.getBoolean("sailorFlight1")) {
            if (holder.getSpirituality() >= 3) {
                holder.useSpirituality(3);
            } else {
                WindManipulationFlight.stopFlying(player);
            }
        }
        int flightCancel = tag.getInt("sailorFlightDamageCancel");
        if (flightCancel >= 1) {
            player.fallDistance = 0;
            tag.putInt("sailorFlightDamageCancel", flightCancel + 1);
            if (flightCancel >= 300) {
                tag.putInt("sailorFlightDamageCancel", 0);
            }
        }
        int flight = tag.getInt("sailorFlight");
        if (flight >= 1) {
            tag.putInt("sailorFlight", flight + 1);
            if (flight <= 45 && flight % 15 == 0) {
                player.setDeltaMovement(lookVector.x * 2, lookVector.y * 2, lookVector.z * 2);
                player.hurtMarked = true;
            }
            if (flight > 45) {
                tag.putInt("sailorFlight", 0);
            }
        }
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        if (holder.getCurrentSequence() <= 4) {
            toggleFlying(player);
        } else {
            useSpirituality(player,40);
            flightRegular(player);
        }
        CompoundTag tag = player.getPersistentData();
        boolean sailorFlight1 = tag.getBoolean("sailorFlight1");
        if (!player.isCreative() && !sailorFlight1) {
            addCooldown(player);
        }
        return InteractionResult.SUCCESS;
    }

    public static void flightRegular(Player player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            tag.putInt("sailorFlight", 1);
            tag.putInt("sailorFlightDamageCancel", 1);
        }
    }

    public static void startFlying(Player player) {
        if (!player.level().isClientSide()) {

            player.getPersistentData().putBoolean("sailorFlight1", true);
            Abilities playerAbilities = player.getAbilities();
            if (!playerAbilities.instabuild) {
                playerAbilities.mayfly = true;
                playerAbilities.flying = true;
                playerAbilities.setFlyingSpeed(0.1F);
            }
            player.onUpdateAbilities();
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
            }
        }
    }
    public static void toggleFlying(Player player) {
        if (!player.level().isClientSide()) {
            boolean canFly = player.getPersistentData().getBoolean("sailorFlight1");
            if (canFly) {
                stopFlying(player);
            } else {
                startFlying(player);
            }
        }
    }

    public static void stopFlying(Player player) {
        if (!player.level().isClientSide()) {
            player.getPersistentData().putBoolean("sailorFlight1", false);
            Abilities playerAbilities = player.getAbilities();
            if (!player.isCreative() && !player.isSpectator()) {
                playerAbilities.mayfly = false;
                playerAbilities.flying = false;
            }
            playerAbilities.setFlyingSpeed(0.05F);
            player.onUpdateAbilities();
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.literal("Upon use, uses the wind to burst forward in the direction the player is looking three times or allow the user to fly, depending on the sequence"));
        tooltipComponents.add(Component.literal("Activation Cost: ").append(Component.literal("None").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("40 per second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("after disabling, 0.5 seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, isAdvanced);
    }
}
