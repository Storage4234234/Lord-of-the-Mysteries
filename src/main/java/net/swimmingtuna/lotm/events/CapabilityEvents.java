package net.swimmingtuna.lotm.events;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.util.ScribeRecording.ScribeAbilityStorageProvider;

@Mod.EventBusSubscriber(modid = LOTM.MOD_ID)
public class CapabilityEvents {

    @SubscribeEvent
    public static void attachCapabilitiesOnEntity(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof LivingEntity) {
            event.addCapability(new ResourceLocation("lotm", "scribe_ability"), new ScribeAbilityStorageProvider());
        }
    }
}
