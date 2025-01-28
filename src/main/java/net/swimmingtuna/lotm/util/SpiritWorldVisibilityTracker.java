package net.swimmingtuna.lotm.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.BatchedSpiritWorldUpdatePacketS2C;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpiritWorldVisibilityTracker {
    private static final Map<UUID, Map<UUID, Boolean>> lastSentStates = new HashMap<>();
    private static final int UPDATE_INTERVAL = 20; // 1 second (20 ticks)
    private static int tickCounter = 0;

    private static final Map<UUID, Map<UUID, Boolean>> pendingUpdates = new HashMap<>();

    public static void sendSpiritWorldPackets(LivingEntity livingEntity) {
        tickCounter++;
        if (tickCounter < UPDATE_INTERVAL) {
            queueUpdate(livingEntity);
            return;
        }

        if (tickCounter >= UPDATE_INTERVAL) {
            processQueuedUpdates(livingEntity.level());
            tickCounter = 0;
        }
    }

    private static void queueUpdate(LivingEntity entity) {
        CompoundTag tag = entity.getPersistentData();
        boolean isInSpiritWorld = tag.getBoolean("inSpiritWorld");
        UUID entityId = entity.getUUID();

        lastSentStates.computeIfAbsent(entityId, k -> new HashMap<>());

        for (Player player : entity.level().players()) {
            if (!(player instanceof ServerPlayer serverPlayer)) continue;

            boolean playerInSpiritWorld = serverPlayer.getPersistentData().getBoolean("inSpiritWorld");
            boolean shouldBeVisible = isInSpiritWorld == playerInSpiritWorld;
            UUID playerId = player.getUUID();

            Map<UUID, Boolean> entityStates = lastSentStates.get(entityId);
            Boolean lastState = entityStates.get(playerId);
            boolean shouldntRender = !shouldBeVisible;

            if (lastState == null || lastState != shouldntRender) {
                pendingUpdates
                        .computeIfAbsent(playerId, k -> new HashMap<>())
                        .put(entityId, shouldntRender);
            }
        }
    }

    private static void processQueuedUpdates(Level level) {
        for (Map.Entry<UUID, Map<UUID, Boolean>> entry : pendingUpdates.entrySet()) {
            UUID playerId = entry.getKey();
            Map<UUID, Boolean> updates = entry.getValue();

            Player player = level.getPlayerByUUID(playerId);
            if (player instanceof ServerPlayer serverPlayer) {
                LOTMNetworkHandler.sendToPlayer(new BatchedSpiritWorldUpdatePacketS2C(new HashMap<>(updates)), serverPlayer);
                updates.forEach((entityId, state) -> {lastSentStates.computeIfAbsent(entityId, k -> new HashMap<>()).put(playerId, state);
                });
            }
        }

        pendingUpdates.clear();
    }

    public static void removeEntity(UUID entityId) {
        lastSentStates.remove(entityId);
        pendingUpdates.values().forEach(updates -> updates.remove(entityId));
    }

    public static void removePlayer(UUID playerId) {
        lastSentStates.values().forEach(states -> states.remove(playerId));
        pendingUpdates.remove(playerId);
    }
}
