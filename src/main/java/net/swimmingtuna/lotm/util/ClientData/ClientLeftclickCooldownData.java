package net.swimmingtuna.lotm.util.ClientData;

public class ClientLeftclickCooldownData {
    private static int cooldown;

    public static void setCooldown(int cooldownToSet) {
        cooldown = cooldownToSet;
    }

    public static int getCooldown() {
        return cooldown;
    }
    public static void decrementCooldown() {
        if (cooldown >= 1) {
            cooldown--;
        }
    }
}
