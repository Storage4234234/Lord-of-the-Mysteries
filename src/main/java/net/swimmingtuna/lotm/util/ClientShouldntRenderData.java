package net.swimmingtuna.lotm.util;

import java.util.UUID;

public class ClientShouldntRenderData {
    private static boolean shouldntRender;
    private static UUID livingUUID;  // Add this to store player ID

    public static void setShouldntRender(boolean value, UUID uuid) {
        shouldntRender = value;
        livingUUID = uuid;
    }

    public static boolean getShouldntRender() {
        return shouldntRender;
    }

    public static UUID getLivingUUID() {
        return livingUUID;
    }
}