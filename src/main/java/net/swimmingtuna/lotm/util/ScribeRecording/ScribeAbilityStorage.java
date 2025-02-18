package net.swimmingtuna.lotm.util.ScribeRecording;

import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem.scribeAbilitiesStorage;
import net.minecraft.world.item.Item;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;

import java.util.HashMap;
import java.util.Map;

public class ScribeAbilityStorage implements scribeAbilitiesStorage {
    private final Map<Item, Integer> scribedAbilities = new HashMap<>();


    @Override
    public Map<Item, Integer> getScribedAbilities() {
        return scribedAbilities;
    }

    @Override
    public boolean hasScribedAbility(Item ability){
        return scribedAbilities.containsKey(ability);
    }

    @Override
    public void copyScribeAbility(Item ability) {
        scribedAbilities.put(ability, scribedAbilities.getOrDefault(ability, 0) + 1);
    }

    @Override
    public void useScribeAbility(Item ability) {
        if(hasScribedAbility(ability)) {
            int count = scribedAbilities.get(ability);
            if(count > 1){
                scribedAbilities.put(ability, count - 1);
            }else{
                scribedAbilities.remove(ability);
            }
        }
    }
}
