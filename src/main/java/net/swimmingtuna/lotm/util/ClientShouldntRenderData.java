package net.swimmingtuna.lotm.util;

public class ClientShouldntRenderData {
    private static boolean shouldntRender;

    public static void setShouldntRender(boolean value) {
        shouldntRender = value;
    }

    public static boolean getShouldntRender() {
        return shouldntRender;
    }
}