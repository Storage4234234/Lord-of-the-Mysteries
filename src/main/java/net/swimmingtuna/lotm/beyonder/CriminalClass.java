package net.swimmingtuna.lotm.beyonder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.init.ItemInit;

import java.util.List;

public class CriminalClass implements BeyonderClass {
    @Override
    public List<String> sequenceNames() {
        return List.of(
                "Abyss",
                "Filthy Monarch",
                "Bloody Archduke",
                "Blatherer",
                "Demon",
                "Devil",
                "Serial Killer",
                "Unwinged Angel",
                "Criminal"
        );
    }

    @Override
    public List<Integer> spiritualityLevels() {
        return List.of(10000, 5000, 3000, 1800, 1200, 700, 450, 300, 175, 125);
    }

    @Override
    public List<Integer> mentalStrength() {
        return List.of(480, 335, 230, 187, 163, 120, 88, 72, 47, 35);
    }

    @Override
    public List<Integer> spiritualityRegen() {
        return List.of(34, 22, 16, 12, 10, 8, 6, 5, 3, 2);
    }

    @Override
    public List<Double> maxHealth() {
        return List.of(350.0, 250.0, 186.0, 136.0, 96.0, 66.0, 54.0, 48.0, 28.0, 22.0);
    }

    @Override
    public void tick(Player player, int sequenceLevel) {
        if (player.level().getGameTime() % 50 == 0) {
            if (sequenceLevel == 9) {
            }
            if (sequenceLevel == 8) {
            }
            if (sequenceLevel == 7) {
            }
            if (sequenceLevel == 6) {
            }
            if (sequenceLevel == 5) {

            }
            if (sequenceLevel == 4) {
            }
            if (sequenceLevel == 3) {
            }
            if (sequenceLevel == 2) {
            }
            if (sequenceLevel == 1) {

            }
            if (sequenceLevel == 0) {

            }
        }
    }

    @Override
    public Multimap<Integer, Item> getItems() {
        HashMultimap<Integer, Item> items = HashMultimap.create();
        items.put(0, ItemInit.PLACATE.get());
        return items;
    }

    @Override
    public ChatFormatting getColorFormatting() {
        return ChatFormatting.GRAY;
    }


}
