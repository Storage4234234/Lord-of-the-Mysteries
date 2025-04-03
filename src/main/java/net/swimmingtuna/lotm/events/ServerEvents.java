package net.swimmingtuna.lotm.events;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.TravelDoor;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems.ConsciousnessStroll;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems.EnvisionLife;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems.EnvisionLocation;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems.EnvisionWeather;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import net.swimmingtuna.lotm.util.BeyonderUtil;

import static net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.TravelDoor.coordsTravel;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems.EnvisionLife.spawnMob;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems.EnvisionLocation.isThreeIntegers;
import static net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems.EnvisionWeather.*;

@Mod.EventBusSubscriber(modid = LOTM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ServerEvents {


    @SubscribeEvent
    public static void onChatMessage(ServerChatEvent event) {
        Level level = event.getPlayer().serverLevel();
        ServerPlayer player = event.getPlayer();
        AttributeInstance dreamIntoReality = player.getAttribute(ModAttributes.DIR.get());
        Style style = BeyonderUtil.getStyle(player);
        if (!player.level().isClientSide() && player.getMainHandItem().getItem() instanceof EnvisionWeather) {
            if (BeyonderUtil.currentPathwayMatches(player, BeyonderClassInit.SPECTATOR.get())) {
                player.displayClientMessage(Component.literal("You are not of the Spectator pathway").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
            }
            if (BeyonderUtil.getSpirituality(player) < (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_WEATHER.get())) {
                player.displayClientMessage(Component.literal("You need " + ((int) 500 / dreamIntoReality.getValue()) + " spirituality in order to use this").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
            }
            String message = event.getMessage().getString().toLowerCase();
            if (BeyonderUtil.currentPathwayMatches(player, BeyonderClassInit.SPECTATOR.get()) && player.getMainHandItem().getItem() instanceof EnvisionWeather) {
                if (message.equals("clear") && BeyonderUtil.getSpirituality(player) > (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_WEATHER.get())) {
                    setWeatherClear(level);
                    event.getPlayer().displayClientMessage(Component.literal("Set Weather to Clear").withStyle(style), true);
                    BeyonderUtil.useSpirituality(player, (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_WEATHER.get()));
                    event.setCanceled(true);
                }
                if (message.equals("rain") && BeyonderUtil.getSpirituality(player) > (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_WEATHER.get())) {
                    event.getPlayer().displayClientMessage(Component.literal("Set Weather to Rain").withStyle(style), true);
                    setWeatherRain(level);
                    BeyonderUtil.useSpirituality(player, (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_WEATHER.get()));
                    event.setCanceled(true);
                }
                if (message.equals("thunder") && BeyonderUtil.getSpirituality(player) > (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_WEATHER.get())) {
                    event.getPlayer().displayClientMessage(Component.literal("Set Weather to Thunder").withStyle(style), true);
                    setWeatherThunder(level);
                    BeyonderUtil.useSpirituality(player, (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_WEATHER.get()));
                    event.setCanceled(true);
                }
            }
        }
        if (!player.level().isClientSide()) {
            String message = event.getMessage().getString().toLowerCase();
            for (Player otherPlayer : level.players()) {
                if (message.contains(otherPlayer.getName().getString().toLowerCase())) {
                    BeyonderHolder otherHolder = BeyonderHolderAttacher.getHolderUnwrap(otherPlayer);
                    if (otherHolder.currentClassMatches(BeyonderClassInit.SPECTATOR) && otherHolder.getSequence() <= 1 && !otherPlayer.level().isClientSide()) {
                        otherPlayer.sendSystemMessage(Component.literal(player.getName().getString() + " mentioned you in chat. Their coordinates are: " + (int) player.getX() + " ," + (int) player.getY() + " ," + (int) player.getZ()).withStyle(style));
                    }
                    if (otherHolder.currentClassMatches(BeyonderClassInit.SAILOR) && otherHolder.getSequence() <= 1 && !otherPlayer.level().isClientSide()) {
                        otherPlayer.getPersistentData().putInt("tyrantMentionedInChat", 200);
                        otherPlayer.sendSystemMessage(Component.literal(player.getName().getString() + " mentioned you in chat. Do you want to summon a lightning storm on them? Type Yes if so, you have 10 seconds").withStyle(style));
                        otherPlayer.getPersistentData().putInt("sailorStormVecX1", (int) player.getX());
                        otherPlayer.getPersistentData().putInt("sailorStormVecY1", (int) player.getY());
                        otherPlayer.getPersistentData().putInt("sailorStormVecZ1", (int) player.getZ());
                    }
                }
            }
            if (player.getPersistentData().getInt("tyrantMentionedInChat") >= 1 && message.toLowerCase().contains("yes")) {
                if (BeyonderUtil.getSpirituality(player) >= 800) {
                    BeyonderUtil.useSpirituality(player,800);
                    player.getPersistentData().putInt("sailorLightningStorm1", 300);
                    player.getPersistentData().putInt("sailorStormVecX1", (int) player.getX());
                    player.getPersistentData().putInt("sailorStormVecY1", (int) player.getY());
                    player.getPersistentData().putInt("sailorStormVecZ1", (int) player.getZ());
                    event.setCanceled(true);
                } else {
                    player.sendSystemMessage(Component.literal("Not enough spirituality").withStyle(BeyonderUtil.getStyle(player)));
                }
            }
        }
        if (!player.level().isClientSide()) {
            String message = event.getMessage().getString().toLowerCase();
            if (player.getMainHandItem().getItem() instanceof ProbabilityManipulationWorldFortune && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.MONSTER.get(), 0)) {
                for (Player onlinePlayer : player.level().players()) {
                    if (message.equals(onlinePlayer.getName().getString().toLowerCase())) {
                        ProbabilityManipulationFortune.giveFortuneEvents(onlinePlayer);
                        BeyonderUtil.useSpirituality(player, 500);
                    }
                }
            }
            if (player.getMainHandItem().getItem() instanceof ProbabilityManipulationWorldFortune && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.MONSTER.get(), 0)) {
                for (Player onlinePlayer : player.level().players()) {
                    if (message.equals(onlinePlayer.getName().getString().toLowerCase())) {
                        ProbabilityManipulationMisfortune.giveMisfortuneEvents(onlinePlayer);
                        BeyonderUtil.useSpirituality(player, 500);
                    }
                }
            }
            if (player.getMainHandItem().getItem() instanceof ProbabilityManipulationInfiniteMisfortune && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.MONSTER.get(), 0)) {
                for (Player onlinePlayer : player.level().players()) {
                    if (message.equals(onlinePlayer.getName().getString().toLowerCase())) {
                        ProbabilityManipulationInfiniteFortune.giveInfiniteFortune(onlinePlayer);
                        BeyonderUtil.useSpirituality(player, 2000);
                    }
                }
            }
            if (player.getMainHandItem().getItem() instanceof ProbabilityManipulationInfiniteFortune && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.MONSTER.get(), 0)) {
                for (Player onlinePlayer : player.level().players()) {
                    if (message.equals(onlinePlayer.getName().getString().toLowerCase())) {
                        ProbabilityManipulationInfiniteMisfortune.giveInfiniteMisfortune(onlinePlayer);
                        BeyonderUtil.useSpirituality(player, 2000);
                    }
                }
            }
        }
        if (!player.level().isClientSide() && player.getMainHandItem().getItem() instanceof ConsciousnessStroll && !player.getCooldowns().isOnCooldown(ItemInit.CONSCIOUSNESS_STROLL.get()) && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.SPECTATOR.get(), 3)) {
            String message = event.getMessage().getString();
            for (ServerPlayer onlinePlayer : player.getServer().getPlayerList().getPlayers()) {
                if (message.equalsIgnoreCase(onlinePlayer.getName().getString())) {
                    if (!BeyonderUtil.currentPathwayMatches(player, BeyonderClassInit.SPECTATOR.get())) {
                        player.displayClientMessage(Component.literal("You are not of the Spectator pathway").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
                    } else if (BeyonderUtil.getSpirituality(player) < 300) {
                        player.displayClientMessage(Component.literal("You need 300 spirituality in order to use this").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
                    } else {
                        player.getPersistentData().putInt("consciousnessStrollActivated", 60);
                        PlayerMobEntity playerMobEntity = new PlayerMobEntity(EntityInit.PLAYER_MOB_ENTITY.get(), player.level());
                        AttributeInstance playerMaxHealth = player.getAttribute(Attributes.MAX_HEALTH);
                        AttributeInstance playerMobMaxHealth = playerMobEntity.getAttribute(Attributes.MAX_HEALTH);
                        playerMobMaxHealth.setBaseValue(playerMaxHealth.getValue());
                        playerMobEntity.setHealth(player.getHealth());
                        playerMobEntity.teleportTo(player.getX(), player.getY(), player.getZ());
                        playerMobEntity.setOwner(player);
                        playerMobEntity.getPersistentData().putInt("CSlifetime", 60);
                        playerMobEntity.setUsername(player.getName().getString());
                        for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(50))) {
                            if (mob.getTarget() == player) {
                                mob.setTarget(playerMobEntity);
                            }
                        }
                        player.level().addFreshEntity(playerMobEntity);
                        player.getCooldowns().addCooldown(ItemInit.CONSCIOUSNESS_STROLL.get(), 400);
                        player.getPersistentData().putInt("consciousnessStrollActivatedX", (int) player.getX());
                        player.getPersistentData().putInt("consciousnessStrollActivatedY", (int) player.getY());
                        player.getPersistentData().putInt("consciousnessStrollActivatedZ", (int) player.getZ());
                        player.setGameMode(GameType.SPECTATOR);
                        BeyonderUtil.useSpirituality(player, 300);
                        player.teleportTo(onlinePlayer.getX(), onlinePlayer.getY(), onlinePlayer.getZ());
                        event.setCanceled(true);
                    }
                }
            }
        }
        if (player.getMainHandItem().getItem() instanceof EnvisionLife && !player.level().isClientSide() && !player.getCooldowns().isOnCooldown(ItemInit.ENVISION_LIFE.get())) {
            if (!BeyonderUtil.currentPathwayMatches(player, BeyonderClassInit.SPECTATOR.get())) {
                player.displayClientMessage(Component.literal("You are not of the Spectator pathway").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
            }

            if (BeyonderUtil.getSpirituality(player) < 1500) {
                player.displayClientMessage(Component.literal("You need " + (int) (1500 / dreamIntoReality.getValue()) + " spirituality in order to use this").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
            }
        }
        if (!player.level().isClientSide() && player.getMainHandItem().getItem() instanceof EnvisionLife && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.SPECTATOR.get(), 0)) {
            String message = event.getMessage().getString().toLowerCase();
            spawnMob(player, message);
            BeyonderUtil.useSpirituality(player, 1500 / (int) dreamIntoReality.getValue());
            event.setCanceled(true);
        }
        String message = event.getMessage().getString();
        if (!player.level().isClientSide() && player.getMainHandItem().getItem() instanceof EnvisionLocation &&  BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.SPECTATOR.get(), 0)) {
            if (!BeyonderUtil.currentPathwayMatches(player, BeyonderClassInit.SPECTATOR.get())) {
                player.displayClientMessage(Component.literal("You are not of the Spectator pathway").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
                event.setCanceled(true);
                return;
            }
            if (BeyonderUtil.getSpirituality(player) < BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_LOCATION.get())) {
                player.displayClientMessage(Component.literal("You need " + (int)(500 / dreamIntoReality.getValue()) + " spirituality in order to use this").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
                event.setCanceled(true);
                return;
            }
            if (isThreeIntegers(message)) {
                String[] coordinates = message.replace(",", " ").trim().split("\\s+");
                int x = Integer.parseInt(coordinates[0]);
                int y = Integer.parseInt(coordinates[1]);
                int z = Integer.parseInt(coordinates[2]);

                player.teleportTo(x, y, z);
                event.getPlayer().displayClientMessage(Component.literal("Teleported to " + x + ", " + y + ", " + z).withStyle(BeyonderUtil.getStyle(player)), true);
                BeyonderUtil.useSpirituality(player, (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_LOCATION.get()));
                event.setCanceled(true);
                return;
            }
            Player targetPlayer = null;
            for (Player serverPlayer : level.players()) {
                if (serverPlayer.getName().getString().toLowerCase().equals(message.toLowerCase())) {
                    targetPlayer = serverPlayer;
                    break;
                }
            }
            if (targetPlayer != null) {
                int x = (int)targetPlayer.getX();
                int y = (int)targetPlayer.getY();
                int z = (int)targetPlayer.getZ();
                player.teleportTo(x, y, z);
                BeyonderUtil.useSpirituality(player, (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_LOCATION.get()));
                event.getPlayer().displayClientMessage(Component.literal("Teleported to " + targetPlayer.getName().getString()).withStyle(BeyonderUtil.getStyle(player)), true);
            } else {
                event.getPlayer().displayClientMessage(Component.literal("Invalid coordinates or player name: " + message).withStyle(BeyonderUtil.getStyle(player)), true);
            }
            event.setCanceled(true);
        }
        if (!player.level().isClientSide && player.getMainHandItem().getItem() instanceof TravelDoor && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.APPRENTICE.get(), 5)) {
            if (!BeyonderUtil.currentPathwayMatches(player, BeyonderClassInit.APPRENTICE.get())) {
                player.displayClientMessage(Component.literal("You are not of the Apprentice pathway").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
                event.setCanceled(true);
                return;
            }
            if (BeyonderUtil.getSpirituality(player) < 300) {
                player.displayClientMessage(Component.literal("You need 300 spirituality in order to use this").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
                event.setCanceled(true);
                return;
            }
            if (coordsTravel(message)) {
                String[] coordinates = message.replace(",", " ").trim().split("\\s+");
                int x = Integer.parseInt(coordinates[0]);
                int y = Integer.parseInt(coordinates[1]);
                int z = Integer.parseInt(coordinates[2]);

                player.teleportTo(x, y, z);
                event.getPlayer().displayClientMessage(Component.literal("Teleported to " + x + ", " + y + ", " + z).withStyle(BeyonderUtil.getStyle(player)), true);
                BeyonderUtil.useSpirituality(player,300);
                event.setCanceled(true);
                return;
            }
            Player targetPlayer = null;
            for (Player serverPlayer : level.players()) {
                if (serverPlayer.getName().getString().toLowerCase().equals(message.toLowerCase())) {
                    targetPlayer = serverPlayer;
                    break;
                }
            }
            if (targetPlayer != null) {
                if(BeyonderUtil.areAllies(targetPlayer, event.getPlayer())){
                    int x = (int)targetPlayer.getX();
                    int y = (int)targetPlayer.getY();
                    int z = (int)targetPlayer.getZ();
                    player.teleportTo(x, y, z);
                    BeyonderUtil.useSpirituality(player,300);
                    event.getPlayer().displayClientMessage(Component.literal("Teleported to " + targetPlayer.getName().getString()).withStyle(BeyonderUtil.getStyle(player)), true);
                }else{
                    event.getPlayer().displayClientMessage(Component.literal("Player is not your ally").withStyle(BeyonderUtil.getStyle(player)), true);
                }
            } else {
                event.getPlayer().displayClientMessage(Component.literal("Invalid coordinates or player name: " + message).withStyle(BeyonderUtil.getStyle(player)), true);
            }
            event.setCanceled(true);
        }
    }
}
