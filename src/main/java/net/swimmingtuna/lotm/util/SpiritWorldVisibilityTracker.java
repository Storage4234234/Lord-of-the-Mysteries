package net.swimmingtuna.lotm.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SyncShouldntRenderSpiritWorldPacketS2C;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SpiritWorldVisibilityTracker {
    private static final Map<UUID, Map<UUID, Boolean>> lastSentStates = new HashMap<>();
    private static final int UPDATE_INTERVAL = 10;
    private static int tickCounter = 0;

    public static void sendSpiritWorldPackets(LivingEntity livingEntity) {
        tickCounter++;
        if (tickCounter < UPDATE_INTERVAL) {
            return;
        }
        tickCounter = 0;
        CompoundTag tag = livingEntity.getPersistentData();
        boolean isInSpiritWorld = tag.getBoolean("inSpiritWorld");
        UUID entityId = livingEntity.getUUID();
        lastSentStates.computeIfAbsent(entityId, k -> new HashMap<>());
        for (Player player : livingEntity.level().players()) {
            if (player instanceof ServerPlayer serverPlayer) {
                boolean playerInSpiritWorld = serverPlayer.getPersistentData().getBoolean("inSpiritWorld");
                boolean shouldBeVisible = isInSpiritWorld == playerInSpiritWorld;
                UUID playerId = player.getUUID();
                Map<UUID, Boolean> entityStates = lastSentStates.get(entityId);
                Boolean lastState = entityStates.get(playerId);
                boolean shouldntRender = !shouldBeVisible;
                if (lastState == null || lastState != shouldntRender) {
                    LOTMNetworkHandler.sendToPlayer(new SyncShouldntRenderSpiritWorldPacketS2C(shouldntRender, entityId), serverPlayer);
                    entityStates.put(playerId, shouldntRender);
                }
            }
        }
    }

    public static void removeEntity(UUID entityId) {
        lastSentStates.remove(entityId);
    }

    public static void removePlayer(UUID playerId) {
        for (Map<UUID, Boolean> states : lastSentStates.values()) {
            states.remove(playerId);
        }
    }
}
