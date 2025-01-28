package net.swimmingtuna.lotm.networking.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.swimmingtuna.lotm.util.ClientData.ClientAbilityCooldownData;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SyncAbilityCooldownsS2C {
    private final Map<String, Integer> cooldowns;

    public SyncAbilityCooldownsS2C(Map<String, Integer> cooldowns) {
        this.cooldowns = cooldowns;
    }

    public static void encode(SyncAbilityCooldownsS2C packet, FriendlyByteBuf buffer) {
        buffer.writeInt(packet.cooldowns.size());
        packet.cooldowns.forEach((combination, cooldown) -> {
            buffer.writeUtf(combination);
            buffer.writeInt(cooldown);
        });
    }

    public static SyncAbilityCooldownsS2C decode(FriendlyByteBuf buffer) {
        Map<String, Integer> cooldowns = new HashMap<>();
        int size = buffer.readInt();

        for (int i = 0; i < size; i++) {
            String combination = buffer.readUtf();
            int cooldown = buffer.readInt();
            cooldowns.put(combination, cooldown);
        }

        return new SyncAbilityCooldownsS2C(cooldowns);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientAbilityCooldownData.clearAbilities();
            cooldowns.forEach(ClientAbilityCooldownData::setAbilityCooldown);
        });
        context.setPacketHandled(true);
    }
}
