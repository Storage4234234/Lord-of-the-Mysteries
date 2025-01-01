package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientData.ClientShouldntRenderSpiritWorldData;

import java.util.UUID;
import java.util.function.Supplier;

public class SyncShouldntRenderSpiritWorldPacketS2C {
    private final boolean shouldntRender;
    private final UUID entityUUID;

    public SyncShouldntRenderSpiritWorldPacketS2C(boolean shouldntRender, UUID entityUUID) {
        this.shouldntRender = shouldntRender;
        this.entityUUID = entityUUID;
    }

    public SyncShouldntRenderSpiritWorldPacketS2C(FriendlyByteBuf buf) {
        this.shouldntRender = buf.readBoolean();
        this.entityUUID = buf.readUUID();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.shouldntRender);
        buf.writeUUID(this.entityUUID);
    }

    public static void handle(SyncShouldntRenderSpiritWorldPacketS2C msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientShouldntRenderSpiritWorldData.setShouldntRender(msg.shouldntRender, msg.entityUUID);
        });
        context.setPacketHandled(true);
    }
}
