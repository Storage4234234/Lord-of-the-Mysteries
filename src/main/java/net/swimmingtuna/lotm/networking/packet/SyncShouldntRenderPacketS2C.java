package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientShouldntRenderData;

import java.util.function.Supplier;

public class SyncShouldntRenderPacketS2C {
    private final boolean shouldntRender;

    public SyncShouldntRenderPacketS2C(boolean shouldntRender) {
        this.shouldntRender = shouldntRender;
    }

    public SyncShouldntRenderPacketS2C(FriendlyByteBuf buf) {
        this.shouldntRender = buf.readBoolean();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeBoolean(this.shouldntRender);
    }

    public boolean getCurrentSequence() {
        return shouldntRender;
    }


    public static void handle(SyncShouldntRenderPacketS2C msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientShouldntRenderData.setShouldntRender(msg.shouldntRender);
        });
        context.setPacketHandled(true);
    }
}
