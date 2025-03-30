package net.swimmingtuna.lotm.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.client.AbilityOverlay;
import net.swimmingtuna.lotm.client.SpiritualityBarOverlay;
import net.swimmingtuna.lotm.item.SealedArtifacts.DeathKnell;
import net.swimmingtuna.lotm.util.ClientData.ClientAbilityCooldownData;
import net.swimmingtuna.lotm.util.ClientData.ClientShouldntRenderInvisibilityData;
import net.swimmingtuna.lotm.util.ClientData.ClientShouldntRenderTransformData;

import java.util.HashMap;
import java.util.Map;


@Mod.EventBusSubscriber(modid = LOTM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onPlayerRender(RenderPlayerEvent.Pre event) {
        if (ClientShouldntRenderTransformData.getInstance().isTransformed()) {
            Mob mob = ClientShouldntRenderTransformData.getInstance().getCachedMob();
            event.setCanceled(true);
            Player player = event.getEntity();
            float partialTick = event.getPartialTick();
            double x = player.xo + (player.getX() - player.xo) * partialTick;
            double y = player.yo + (player.getY() - player.yo) * partialTick;
            double z = player.zo + (player.getZ() - player.zo) * partialTick;
            float yRot = lerpAngle(partialTick, player.yRotO, player.getYRot());
            float xRot = lerpAngle(partialTick, player.xRotO, player.getXRot());
            mob.setPos(x, y, z);
            mob.setYRot(yRot);
            mob.setXRot(xRot);
            mob.yHeadRot = player.yHeadRot;
            mob.yBodyRot = player.yBodyRot;

            EntityRenderDispatcher dispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
            dispatcher.render(mob, 0, 0, 0, 0, event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
        }
    }

    private static float lerpAngle(float partialTick, float prev, float current) {
        return prev + (current - prev) * partialTick;
    }

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


    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void clientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Map<String, Integer> currentCooldowns = new HashMap<>(ClientAbilityCooldownData.getCooldowns());
            for (Map.Entry<String, Integer> entry : currentCooldowns.entrySet()) {
                int newValue = entry.getValue() - 1;
                ClientAbilityCooldownData.setAbilityCooldown(entry.getKey(), newValue);
            }
        }
    }

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void livingRender(RenderLivingEvent.Pre<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (ClientShouldntRenderInvisibilityData.getShouldntRender() && entity.getUUID().equals(ClientShouldntRenderInvisibilityData.getLivingUUID())) {
            event.setCanceled(true);
            if (event.getRenderer().shadowRadius == 1.0f) {
                event.getRenderer().shadowRadius = 0.0f;
            }
        } else if (event.getRenderer().shadowRadius == 0.0f) {
            event.getRenderer().shadowRadius = 1.0f;
        }
    }


}