package net.swimmingtuna.lotm.util.ClientData;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientShouldntRenderSpiritWorldData {
    private static final Map<UUID, Boolean> entityVisibilityMap = new ConcurrentHashMap<>();

    public static void setShouldntRender(boolean value, UUID uuid) {
        entityVisibilityMap.put(uuid, value);
    }

    public static boolean getShouldntRender(UUID uuid) {
        return entityVisibilityMap.getOrDefault(uuid, false);
    }

    public static void removeEntity(UUID uuid) {
        entityVisibilityMap.remove(uuid);
    }

    public static void clear() {
        entityVisibilityMap.clear();
    }
}