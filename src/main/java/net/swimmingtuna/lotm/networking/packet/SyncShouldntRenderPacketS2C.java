package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientShouldntRenderData;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncShouldntRenderPacketS2C {
    private final boolean shouldntRender;
    private final UUID playerUUID;

    public SyncShouldntRenderPacketS2C(boolean shouldntRender, UUID playerUUID) {
        this.shouldntRender = shouldntRender;
        this.playerUUID = playerUUID;
    }

    public SyncShouldntRenderPacketS2C(FriendlyByteBuf buf) {
        this.shouldntRender = buf.readBoolean();
        this.playerUUID = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.shouldntRender);
        buf.writeUUID(this.playerUUID);
    }

    public static void handle(SyncShouldntRenderPacketS2C msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientShouldntRenderData.setShouldntRender(msg.shouldntRender, msg.playerUUID);
        });
        context.setPacketHandled(true);
    }
}
