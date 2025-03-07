package net.swimmingtuna.lotm.util.ScribeRecording;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;

public class CapabilityScribeAbilities {
    public static final Capability<SimpleAbilityItem.scribeAbilitiesStorage> SCRIBE_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>(){});
}
