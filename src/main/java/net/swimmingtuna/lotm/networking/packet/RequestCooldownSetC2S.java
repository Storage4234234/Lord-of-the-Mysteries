package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

import static net.swimmingtuna.lotm.util.BeyonderUtil.setCooldown;

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
            ServerPlayer player = context.getSender();
            if (player != null) {
                if (player.getServer() != null) {
                    boolean isIntegratedServer = player.getServer().isSingleplayer();
                    int cooldownValue = isIntegratedServer ? 2 : 1;
                    setCooldown(player, cooldownValue);
                    setCooldown(player, 2);
                } else {
                    setCooldown(player, 2);
                }
            }
        });
        context.setPacketHandled(true);
    }
}
