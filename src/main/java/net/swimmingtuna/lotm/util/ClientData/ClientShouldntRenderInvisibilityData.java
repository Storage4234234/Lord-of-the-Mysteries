package net.swimmingtuna.lotm.util.ClientData;

import java.util.UUID;

public class ClientShouldntRenderInvisibilityData {
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