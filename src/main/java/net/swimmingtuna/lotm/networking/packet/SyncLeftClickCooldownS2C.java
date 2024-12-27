package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientLeftclickCooldownData;

import java.util.function.Supplier;

public class SyncLeftClickCooldownS2C {
    private final int cooldown;

    public SyncLeftClickCooldownS2C(int cooldown) {
        this.cooldown = cooldown;
    }

    public SyncLeftClickCooldownS2C(FriendlyByteBuf buf) {
        this.cooldown = buf.readInt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(this.cooldown);
    }

    public int getCurrentCooldown() {
        return cooldown;
    }


    public static void handle(SyncLeftClickCooldownS2C msg, Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientLeftclickCooldownData.setCooldown(msg.getCurrentCooldown());
        });
        context.setPacketHandled(true);
    }
}
