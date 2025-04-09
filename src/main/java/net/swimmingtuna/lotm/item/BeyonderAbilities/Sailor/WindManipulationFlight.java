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
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class WindManipulationFlight extends SimpleAbilityItem {


    public WindManipulationFlight(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 6, 0, 100);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        if (BeyonderUtil.getSequence(player) <= 4 && player instanceof Player) {
            toggleFlying(player);
        } else {
            useSpirituality(player,40);
            flightRegular(player);
        }
        CompoundTag tag = player.getPersistentData();
        boolean sailorFlight1 = tag.getBoolean("sailorFlight1");
        if ( !sailorFlight1) {
            addCooldown(player);
        }
        return InteractionResult.SUCCESS;
    }

    public static void flightRegular(LivingEntity player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            tag.putInt("sailorFlight", 1);
            tag.putInt("sailorFlightDamageCancel", 1);
        }
    }

    public static void startFlying(LivingEntity player) { //marked
        if (!player.level().isClientSide() && player instanceof Player pPlayer) {
            player.getPersistentData().putBoolean("sailorFlight1", true);
            Abilities playerAbilities = pPlayer.getAbilities();
            if (!playerAbilities.instabuild) {
                playerAbilities.mayfly = true;
                playerAbilities.setFlyingSpeed(0.1F);
            }
            pPlayer.onUpdateAbilities();
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
            }
        }
    }
    public static void toggleFlying(LivingEntity player) {
        if (!player.level().isClientSide()) {
            boolean canFly = player.getPersistentData().getBoolean("sailorFlight1");
            if (canFly) {
                player.sendSystemMessage(Component.literal("Wind Manipulation (Flight) turned off").withStyle(ChatFormatting.RED));
                stopFlying(player);
            } else {
                player.sendSystemMessage(Component.literal("Wind Manipulation (Flight) turned on").withStyle(ChatFormatting.GREEN));
                startFlying(player);
            }
        }
    }

    public static void stopFlying(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide() && livingEntity instanceof Player player) { //marked
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

    public static void windManipulationGuide(LivingEntity livingEntity) {
        //WIND MANIPULATION GLIDE
        CompoundTag tag = livingEntity.getPersistentData();
        boolean enhancedFlight = tag.getBoolean("sailorFlight1");
        if (BeyonderUtil.currentPathwayAndSequenceMatches(livingEntity, BeyonderClassInit.SAILOR.get(), 6) && livingEntity.isShiftKeyDown() && livingEntity.fallDistance >= 3 && !(livingEntity instanceof Player player && player.getAbilities().instabuild) && !enhancedFlight) {
            Vec3 movement = livingEntity.getDeltaMovement();
            double deltaX = Math.cos(Math.toRadians(livingEntity.getYRot() + 90)) * 0.06;
            double deltaZ = Math.sin(Math.toRadians(livingEntity.getYRot() + 90)) * 0.06;
            livingEntity.setDeltaMovement(movement.x + deltaX, -0.05, movement.z + deltaZ);
            livingEntity.fallDistance = 5;
            livingEntity.hurtMarked = true;
        }
    }

    public static void windManipulationFlight(LivingEntity livingEntity) {
        Vec3 lookVector = livingEntity.getLookAngle();
        CompoundTag tag = livingEntity.getPersistentData();
        if (tag.getBoolean("sailorFlight1")) {
            if (livingEntity instanceof Player player && player.getAbilities().flying) {
                BeyonderUtil.useSpirituality(player, 3);
            }
        }
        int flightCancel = tag.getInt("sailorFlightDamageCancel");
        if (flightCancel >= 1) {
            livingEntity.fallDistance = 0;
            tag.putInt("sailorFlightDamageCancel", flightCancel + 1);
            if (flightCancel >= 300) {
                tag.putInt("sailorFlightDamageCancel", 0);
            }
        }
        int flight = tag.getInt("sailorFlight");
        if (flight >= 1) {
            tag.putInt("sailorFlight", flight + 1);
            if (flight <= 45 && flight % 15 == 0) {
                livingEntity.setDeltaMovement(lookVector.x * 2, lookVector.y * 2, lookVector.z * 2);
                livingEntity.hurtMarked = true;
            }
            if (flight > 45) {
                tag.putInt("sailorFlight", 0);
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        tooltipComponents.add(Component.literal("Upon use, uses the wind to burst forward in the direction the player is looking three times or allow the user to fly, depending on the sequence"));
        tooltipComponents.add(Component.literal("Left Click for Wind Manipulation (Sense)"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("40 / 40 Per Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("5 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, isAdvanced);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null && target.distanceTo(livingEntity) >= 10) {
            return 50;
        }
        return 0;
    }

}
