package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientData.ClientAntiConcealmentData;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncAntiConcealmentPacketS2C {
    private final boolean shouldAntiConceal;
    private final UUID playerUUID;

    public SyncAntiConcealmentPacketS2C(boolean shouldAntiConceal, UUID playerUUID) {
        this.shouldAntiConceal = shouldAntiConceal;
        this.playerUUID = playerUUID;
    }

    public SyncAntiConcealmentPacketS2C(FriendlyByteBuf buf) {
        this.shouldAntiConceal = buf.readBoolean();
        this.playerUUID = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.shouldAntiConceal);
        buf.writeUUID(this.playerUUID);
    }

    public static void handle(SyncAntiConcealmentPacketS2C msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientAntiConcealmentData.setAntiConcealment(msg.shouldAntiConceal, msg.playerUUID);
        });
        context.setPacketHandled(true);
    }
}
