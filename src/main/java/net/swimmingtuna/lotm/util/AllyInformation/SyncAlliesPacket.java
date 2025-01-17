package net.swimmingtuna.lotm.util.AllyInformation;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.*;
import java.util.function.Supplier;

public class SyncAlliesPacket {
    private final Map<UUID, Set<UUID>> playerAllies;

    public SyncAlliesPacket(Map<UUID, Set<UUID>> playerAllies) {
        this.playerAllies = playerAllies;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeInt(playerAllies.size());
        playerAllies.forEach((playerUUID, allies) -> {
            buf.writeUUID(playerUUID);
            buf.writeInt(allies.size());
            allies.forEach(buf::writeUUID);
        });
    }

    public static SyncAlliesPacket decode(FriendlyByteBuf buf) {
        Map<UUID, Set<UUID>> playerAllies = new HashMap<>();
        int playerCount = buf.readInt();

        for (int i = 0; i < playerCount; i++) {
            UUID playerUUID = buf.readUUID();
            int allyCount = buf.readInt();
            Set<UUID> allies = new HashSet<>();

            for (int j = 0; j < allyCount; j++) {
                allies.add(buf.readUUID());
            }

            playerAllies.put(playerUUID, allies);
        }

        return new SyncAlliesPacket(playerAllies);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Update client-side data
            ClientAllyData.setPlayerAllies(playerAllies);
        });
        ctx.get().setPacketHandled(true);
    }
}
