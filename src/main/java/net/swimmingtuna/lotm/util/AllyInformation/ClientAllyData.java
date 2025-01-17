package net.swimmingtuna.lotm.util.AllyInformation;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class ClientAllyData {
    private static Map<UUID, Set<UUID>> playerAllies = new HashMap<>();

    public static void setPlayerAllies(Map<UUID, Set<UUID>> newAllies) {
        playerAllies = new HashMap<>(newAllies);
    }

    public static boolean areAllies(UUID player1, UUID player2) {
        return playerAllies.getOrDefault(player1, new HashSet<>()).contains(player2);
    }

    public static Set<UUID> getAllies(UUID player) {
        return new HashSet<>(playerAllies.getOrDefault(player, new HashSet<>()));
    }
}
