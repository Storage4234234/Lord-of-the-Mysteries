package net.swimmingtuna.lotm.util.ScribeRecording;

import net.minecraft.world.item.Item;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem.scribeAbilitiesStorage;

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
        if(hasScribedAbility(ability)){
            scribedAbilities.replace(ability, scribedAbilities.get(ability) + 1);
        }else{
            scribedAbilities.put(ability, 1);
        }
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

    @Override
    public int getRemainUses(Item ability) {
        if(hasScribedAbility(ability)){
            return scribedAbilities.get(ability);
        }
        return 0;
    }

    @Override
    public int getScribedAbilitiesCount(){
        return scribedAbilities.values().stream().mapToInt(Integer::intValue).sum();
    }
}
