package net.swimmingtuna.lotm.util.AllyInformation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.network.PacketDistributor;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;

import java.util.*;

public class PlayerAllyData extends SavedData {
    private final Map<UUID, Set<UUID>> playerAllies = new HashMap<>();

    public static PlayerAllyData create() {
        return new PlayerAllyData();
    }

    public void addAlly(UUID player, UUID ally) {
        playerAllies.computeIfAbsent(player, k -> new HashSet<>()).add(ally);
        setDirty();
        syncToClients();
    }

    public void removeAlly(UUID player, UUID ally) {
        if (playerAllies.containsKey(player)) {
            playerAllies.get(player).remove(ally);
            setDirty();
            syncToClients();
        }
    }

    public Set<UUID> getAllies(UUID player) {
        return playerAllies.getOrDefault(player, new HashSet<>());
    }

    public boolean areAllies(UUID living1, UUID living2) {
        return getAllies(living1).contains(living2);
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag alliesTag = new CompoundTag();

        playerAllies.forEach((playerUUID, allies) -> {
            ListTag allyList = new ListTag();
            allies.forEach(allyUUID -> {
                CompoundTag allyTag = new CompoundTag();
                allyTag.putUUID("UUID", allyUUID);
                allyList.add(allyTag);
            });
            alliesTag.put(playerUUID.toString(), allyList);
        });

        tag.put("PlayerAllies", alliesTag);
        return tag;
    }

    public static PlayerAllyData load(CompoundTag tag) {
        PlayerAllyData data = create();

        if (tag.contains("PlayerAllies", Tag.TAG_COMPOUND)) {
            CompoundTag alliesTag = tag.getCompound("PlayerAllies");

            alliesTag.getAllKeys().forEach(playerUUIDString -> {
                UUID playerUUID = UUID.fromString(playerUUIDString);
                ListTag allyList = alliesTag.getList(playerUUIDString, Tag.TAG_COMPOUND);

                Set<UUID> allies = new HashSet<>();
                allyList.forEach(allyTag -> {
                    UUID allyUUID = ((CompoundTag) allyTag).getUUID("UUID");
                    allies.add(allyUUID);
                });

                data.playerAllies.put(playerUUID, allies);
            });
        }
        return data;
    }

    private void syncToClients() {
       LOTMNetworkHandler.sendToAllPlayers(new SyncAlliesPacket(playerAllies));
    }
}
