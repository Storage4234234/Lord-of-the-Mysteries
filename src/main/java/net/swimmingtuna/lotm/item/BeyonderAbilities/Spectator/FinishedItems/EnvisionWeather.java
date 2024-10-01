package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
@Mod.EventBusSubscriber(modid = LOTM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EnvisionWeather extends Item {

    public EnvisionWeather(Properties properties) {
        super(properties);
    }
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("While holding this item, say either Clear, Rain, or Thunder, to change the weather at your disposal\n" +
                "Spirituality Used: 500\n" +
                "Cooldown: 0 seconds").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @SubscribeEvent
    public static void onChatMessage(ServerChatEvent event) {

        Level level = event.getPlayer().serverLevel();
        Player player = event.getPlayer();
        AttributeInstance dreamIntoReality = player.getAttribute(ModAttributes.DIR.get());
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        Style style = BeyonderUtil.getStyle(player);
        if (!player.level().isClientSide() && player.getMainHandItem().getItem() instanceof EnvisionWeather) {
            if (!holder.currentClassMatches(BeyonderClassInit.SPECTATOR)) {
                player.displayClientMessage(Component.literal("You are not of the Spectator pathway").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
            }
            if (holder.getSpirituality() < (int) 500 / dreamIntoReality.getValue()) {
                player.displayClientMessage(Component.literal("You need " + ((int) 500 / dreamIntoReality.getValue()) + " spirituality in order to use this").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
            }
            String message = event.getMessage().getString().toLowerCase();
            if (holder.currentClassMatches(BeyonderClassInit.SPECTATOR) && player.getMainHandItem().getItem() instanceof EnvisionWeather && holder.getCurrentSequence() == 0) {
                if (message.equals("clear") && holder.useSpirituality((int) (500 / dreamIntoReality.getValue()))) {
                    setWeatherClear(level);
                    event.getPlayer().displayClientMessage(Component.literal("Set Weather to Clear").withStyle(style), true);
                    holder.useSpirituality((int) (500 / dreamIntoReality.getValue()));
                    event.setCanceled(true);
                }
                if (message.equals("rain") && holder.useSpirituality((int) (500 / dreamIntoReality.getValue()))) {
                    event.getPlayer().displayClientMessage(Component.literal("Set Weather to Rain").withStyle(style), true);
                    setWeatherRain(level);
                    event.setCanceled(true);
                }
                if (message.equals("thunder") && holder.useSpirituality((int) (500 / dreamIntoReality.getValue()))) {
                    event.getPlayer().displayClientMessage(Component.literal("Set Weather to Thunder").withStyle(style), true);
                    setWeatherThunder(level);
                    event.setCanceled(true);
                }
            }
        }
        if (!player.level().isClientSide()) {
            String message = event.getMessage().getString().toLowerCase();
            for (Player otherPlayer : level.players()) {
                if (message.contains(otherPlayer.getName().getString().toLowerCase())) {
                    BeyonderHolder otherHolder = BeyonderHolderAttacher.getHolderUnwrap(otherPlayer);
                    if (otherHolder.currentClassMatches(BeyonderClassInit.SPECTATOR) && otherHolder.getCurrentSequence() <= 2 && !otherPlayer.level().isClientSide()) {
                        otherPlayer.sendSystemMessage(Component.literal(player.getName().getString() + " mentioned you in chat. Their coordinates are: " + (int) player.getX() + " ," + (int) player.getY() + " ," + (int) player.getZ()).withStyle(style));
                    }
                    if (otherHolder.currentClassMatches(BeyonderClassInit.SAILOR) && otherHolder.getCurrentSequence() <= 1 && !otherPlayer.level().isClientSide()) {
                        otherPlayer.getPersistentData().putInt("tyrantMentionedInChat", 200);
                        otherPlayer.sendSystemMessage(Component.literal(player.getName().getString() + " mentioned you in chat. Do you want to summon a lightning storm on them? Type Yes if so, you have 10 seconds").withStyle(style));
                        otherPlayer.getPersistentData().putInt("sailorStormVecX1", (int) player.getX());
                        otherPlayer.getPersistentData().putInt("sailorStormVecY1", (int) player.getY());
                        otherPlayer.getPersistentData().putInt("sailorStormVecZ1", (int) player.getZ());
                    }
                }
            }
            if (player.getPersistentData().getInt("tyrantMentionedInChat") >= 1 && message.contains("yes") && holder.getSpirituality() >= 1200) {
                holder.useSpirituality(1200);
                player.getPersistentData().putInt("sailorLightningStorm1", 300);
                event.setCanceled(true);
            }
        }
    }

    private static void setWeatherClear(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.setWeatherParameters(8000, 0, false, false);
        }
    }

    private static void setWeatherRain(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.setWeatherParameters(40, 8000, true, true);
        }
    }

    private static void setWeatherThunder(Level level) {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.setWeatherParameters(40, 8000, true, true);
        }
    }
}
