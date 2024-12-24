package net.swimmingtuna.lotm.events.ability_events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.init.BeyonderClassInit;

public class WindManipulation {

    protected static void windManipulationGuide(CompoundTag playerPersistentData, BeyonderHolder holder, Player player) {
        //WIND MANIPULATION GLIDE
        int regularFlight = playerPersistentData.getInt("sailorFlight");
        boolean enhancedFlight = playerPersistentData.getBoolean("sailorFlight1");
        if (
                holder.currentClassMatches(BeyonderClassInit.SAILOR) &&
                        holder.getCurrentSequence() <= 7 &&
                        player.isShiftKeyDown() &&
                        player.getDeltaMovement().y() < 0 &&
                        !player.getAbilities().instabuild &&
                        !enhancedFlight &&
                        regularFlight == 0
        ) {
            Vec3 movement = player.getDeltaMovement();
            double deltaX = Math.cos(Math.toRadians(player.getYRot() + 90)) * 0.06;
            double deltaZ = Math.sin(Math.toRadians(player.getYRot() + 90)) * 0.06;
            player.setDeltaMovement(movement.x + deltaX, -0.05, movement.z + deltaZ);
            player.resetFallDistance();
            player.hurtMarked = true;
        }
    }
}
