package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientData.ClientShouldntRenderSpiritWorldData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class BatchedSpiritWorldUpdatePacketS2C {
    private final Map<UUID, Boolean> updates;

    public BatchedSpiritWorldUpdatePacketS2C(Map<UUID, Boolean> updates) {
        this.updates = updates;
    }

    public BatchedSpiritWorldUpdatePacketS2C(FriendlyByteBuf buf) {
        this.updates = new HashMap<>();
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            updates.put(buf.readUUID(), buf.readBoolean());
        }
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(updates.size());
        updates.forEach((uuid, state) -> {
            buf.writeUUID(uuid);
            buf.writeBoolean(state);
        });
    }

    public static void handle(BatchedSpiritWorldUpdatePacketS2C msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            msg.updates.forEach((uuid, state) -> ClientShouldntRenderSpiritWorldData.setShouldntRender(state, uuid));
        });
        context.setPacketHandled(true);
    }
}