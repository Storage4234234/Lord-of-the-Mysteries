package net.swimmingtuna.lotm.util.ClientData;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ClientAgeDecayData {
    private static final Map<UUID, Integer> ageDecayValues = new HashMap<>();

    public static void setAgeDecay(int value, UUID uuid) {
        if (value <= 0) {
            ageDecayValues.remove(uuid);  // Clean up when decay is done
        } else {
            ageDecayValues.put(uuid, value);
        }
    }

    public static int getAgeDecayAmount(UUID uuid) {
        return ageDecayValues.getOrDefault(uuid, 0);
    }
}