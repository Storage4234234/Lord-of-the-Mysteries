package net.swimmingtuna.lotm.events;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.client.AbilityOverlay;
import net.swimmingtuna.lotm.client.SpiritualityBarOverlay;
import net.swimmingtuna.lotm.item.SealedArtifacts.DeathKnell;
import net.swimmingtuna.lotm.util.ClientData.ClientShouldntRenderInvisibilityData;


@Mod.EventBusSubscriber(modid = LOTM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onRegisterOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "spirituality_overlay", SpiritualityBarOverlay.INSTANCE);
        //event.registerAbove(VanillaGuiOverlay.PLAYER_HEALTH.id(), "lotm_health_overlay", HealthBarOverlay.INSTANCE);
        event.registerAboveAll("ability_overlay", AbilityOverlay.INSTANCE);
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void zoomEvent(ViewportEvent.ComputeFov event) {
        Player player = event.getRenderer().getMinecraft().player;
        if (player != null && player.getMainHandItem().getItem() instanceof DeathKnell && player.isShiftKeyDown()) {
            event.setFOV(event.getFOV() * (1 - 0.5f));
        }
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
        if (ClientShouldntRenderInvisibilityData.getShouldntRender() && entity.getUUID().equals(ClientShouldntRenderInvisibilityData.getLivingUUID())) {
            event.setCanceled(true);
        }
        //    if (ClientShouldntRenderSpiritWorldData.getShouldntRender(entity.getUUID())) {
        //        event.setCanceled(true);
        //    }
    }
}