package net.swimmingtuna.lotm.util;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.swimmingtuna.lotm.LOTM;

import java.util.LinkedList;
import java.util.Queue;

@Mod.EventBusSubscriber(modid = LOTM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class TemporaryMessageManager {
    private static class TimedMessage {
        final String message;
        int remainingTicks;

        TimedMessage(String message, int ticks) {
            this.message = message;
            this.remainingTicks = ticks;
        }
    }

    private final Queue<TimedMessage> messageQueue = new LinkedList<>();
    private TimedMessage currentMessage = null;

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        Player player = Minecraft.getInstance().player;
        if (event.phase != TickEvent.Phase.END || player == null) {
            return;
        }
        if (currentMessage != null) {
            currentMessage.remainingTicks--;
            if (currentMessage.remainingTicks <= 0) {
                currentMessage = null;
                if (!messageQueue.isEmpty()) {
                    currentMessage = messageQueue.poll();
                    displayMessage(player, currentMessage.message);
                } else {
                    displayMessage(player,"");
                }
            }
        } else if (!messageQueue.isEmpty()) {
            currentMessage = messageQueue.poll();
            displayMessage(player,currentMessage.message);
        }
    }

    private void displayMessage(Player player,String message) {
        player.displayClientMessage(Component.literal(message), true);
    }

    public void showTemporaryMessage(Player player, String message, int ticks) {
        TimedMessage timedMessage = new TimedMessage(message, ticks);
        if (currentMessage == null) {
            currentMessage = timedMessage;
            displayMessage(player, message);
        } else {
            messageQueue.offer(timedMessage);
        }
    }
}
