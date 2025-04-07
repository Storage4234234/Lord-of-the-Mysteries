package net.swimmingtuna.lotm.events;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.beyonder.SpectatorClass;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice.TravelDoor;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Monster.*;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems.*;
import net.swimmingtuna.lotm.item.OtherItems.TestItem;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import net.swimmingtuna.lotm.util.BeyonderUtil;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

import static net.swimmingtuna.lotm.beyonder.SpectatorClass.EVENT_TO_TAG;
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
                if (message.contains(otherPlayer.getName().getString().toLowerCase()) && !event.isCanceled()) {
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
        ItemStack heldItem = player.getMainHandItem();
        if (!heldItem.isEmpty() && heldItem.getItem() instanceof Prophecy) {
            Matcher matcher = SpectatorClass.PROPHECY_PATTERN.matcher(message);
            if (matcher.matches()) {
                String targetPlayerName = matcher.group(1);
                String eventDescription = matcher.group(2);
                int timeValue = Integer.parseInt(matcher.group(3));
                String timeUnit = matcher.group(4).toLowerCase();
                int ticksPerSecond = 20;
                int ticks;
                if (timeUnit.startsWith("second")) {
                    ticks = timeValue * ticksPerSecond;
                } else {
                    ticks = timeValue * 60 * ticksPerSecond;
                }
                String tagKey = null;
                for (Map.Entry<String, String> entry : EVENT_TO_TAG.entrySet()) {
                    if (eventDescription.equals(entry.getKey())) {
                        tagKey = entry.getValue();
                        break;
                    }
                }

                if (tagKey != null) {
                    Optional<ServerPlayer> targetPlayer = level.getServer().getPlayerList().getPlayers().stream().filter(p -> p.getName().getString().equals(targetPlayerName)).findFirst();
                    if (targetPlayer.isPresent()) {
                        CompoundTag tag = targetPlayer.get().getPersistentData();
                        tag.putInt(tagKey, ticks);
                        player.sendSystemMessage(Component.literal("Prophecy has been set for " + targetPlayerName));
                    } else {
                        player.sendSystemMessage(Component.literal("Could not find player: " + targetPlayerName));
                    }
                } else {
                    for (String description : EVENT_TO_TAG.keySet()) {
                        player.sendSystemMessage(Component.literal("• " + description).withStyle(ChatFormatting.YELLOW));
                    }
                    player.sendSystemMessage(Component.literal("Unknown prophecy. Known prophecy types are above").withStyle(ChatFormatting.RED));

                }
            } else {
                player.sendSystemMessage(Component.literal("Prophecy written incorrectly. Should be put in the format of (Player) will (event) in (number) (minutes/seconds).").withStyle(ChatFormatting.RED));
            }
            event.setCanceled(true);
        }
    }
}
