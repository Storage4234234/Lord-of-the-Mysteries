package net.swimmingtuna.lotm.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.client.AbilityOverlay;
import net.swimmingtuna.lotm.client.SpiritualityBarOverlay;
import net.swimmingtuna.lotm.util.ClientShouldntRenderData;


@Mod.EventBusSubscriber(modid = LOTM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "spirituality_overlay", SpiritualityBarOverlay.INSTANCE);
        //event.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), "lotm_health_overlay", HealthBarOverlay.INSTANCE);
        event.registerAboveAll("ability_overlay", AbilityOverlay.INSTANCE);
    }

    //@SubscribeEvent
    //public static void renderCustomHealth(RenderGuiOverlayEvent.Pre event) {
    //    if (VanillaGuiOverlay.PLAYER_HEALTH.type() == event.getOverlay()) {
    //        event.setCanceled(true);
    //    }
    //}

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void livingRender(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (ClientShouldntRenderData.getShouldntRender()) {
            event.setCanceled(true);
        }
    }
}
