package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.events.ModEvents;

import java.util.function.Supplier;

public class RequestCooldownSetC2S {
    public RequestCooldownSetC2S() {
    }

    public RequestCooldownSetC2S(FriendlyByteBuf buf) {
    }

    public void toByte(FriendlyByteBuf buf) {
    }

    public static void handle(RequestCooldownSetC2S msg, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            // On the server
            ServerPlayer player = context.getSender();
            if (player != null) {
                ModEvents.setCooldown(player, 2);
            }
        });
        context.setPacketHandled(true);
    }
}
