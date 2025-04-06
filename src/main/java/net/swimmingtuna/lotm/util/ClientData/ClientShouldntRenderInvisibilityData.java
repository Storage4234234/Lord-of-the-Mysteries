package net.swimmingtuna.lotm.util.ClientData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientShouldntRenderInvisibilityData {
    private static final Map<UUID, Boolean> invisibilityStates = new HashMap<>();

    public static void setShouldntRender(boolean value, UUID uuid) {
        if (value) {
            invisibilityStates.put(uuid, true);
        } else {
            invisibilityStates.remove(uuid);
        }
    }

    public static boolean getShouldntRender(UUID uuid) {
        return invisibilityStates.getOrDefault(uuid, false);
    }

    public static void clearAll() {
        invisibilityStates.clear();
    }
}