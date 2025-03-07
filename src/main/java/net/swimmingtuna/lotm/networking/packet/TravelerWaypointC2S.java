package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.BeyonderUtil;

import java.util.function.Supplier;

public class TravelerWaypointC2S {
    public TravelerWaypointC2S() {

    }

    public TravelerWaypointC2S(FriendlyByteBuf buf) {

    }

    public void toByte(FriendlyByteBuf buf) {

    }

    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        ServerPlayer player = context.getSender();
        context.enqueueWork(() -> {
            if (player == null) return;
            CompoundTag tag = player.getPersistentData();
            int currentWaypoint = tag.getInt("doorWaypoint");
            int maxWaypoints = 20 - (BeyonderUtil.getSequence(player) * 3);
            int newWaypoint = currentWaypoint + 1;
            if (newWaypoint > maxWaypoints) {
                newWaypoint = 1;
            }
            tag.putInt("doorWaypoint", newWaypoint);
            player.displayClientMessage(Component.literal("Waypoint " + newWaypoint).withStyle(BeyonderUtil.getStyle(player)), true);
        });
        return true;
    }
}
