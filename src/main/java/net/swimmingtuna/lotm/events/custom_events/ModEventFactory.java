package net.swimmingtuna.lotm.events.custom_events;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.ModLoader;
import net.minecraftforge.fml.event.IModBusEvent;

public class ModEventFactory {
    public static <T extends Event & IModBusEvent> void fireModEvent(T event) {
        ModLoader.get().postEvent(event);
    }

    public static void onSailorShootProjectile(Projectile projectile) {
        ProjectileEvent.ProjectileControlEvent projectileEvent = new ProjectileEvent.ProjectileControlEvent(projectile);
        MinecraftForge.EVENT_BUS.post(projectileEvent);
    }
}
