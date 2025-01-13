package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SyncShouldntRenderInvisibilityPacketS2C;
import net.swimmingtuna.lotm.util.ClientData.ClientShouldntRenderInvisibilityData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static net.swimmingtuna.lotm.events.ModEvents.lastSentInvisibilityStates;

public class PsychologicalInvisibility extends SimpleAbilityItem {

    public PsychologicalInvisibility(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 6, 0, 240);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        psychologicalInvisibilityTest(player);
        if (ClientShouldntRenderInvisibilityData.getShouldntRender()) {
            addCooldown(player);
        }
        return InteractionResult.SUCCESS;
    }

    private static void psychologicalInvisibility(Player player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            boolean shouldntRender = ClientShouldntRenderInvisibilityData.getShouldntRender();
            if (!shouldntRender) {
                for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(50))) {
                    if (mob.getTarget() == player) {
                        mob.setTarget(null);
                    }
                }
                LOTMNetworkHandler.sendToPlayer(new SyncShouldntRenderInvisibilityPacketS2C(true, player.getUUID()), (ServerPlayer) player);
                tag.putBoolean("psychologicalInvisibility", true);
                player.displayClientMessage(Component.literal("You are now invisible"), true);
            } else {
                player.displayClientMessage(Component.literal("You are now visible"), true);
                tag.putBoolean("psychologicalInvisibility", false);
                LOTMNetworkHandler.sendToPlayer(new SyncShouldntRenderInvisibilityPacketS2C(false, player.getUUID()), (ServerPlayer) player);
            }
        }
    }

    private static void psychologicalInvisibilityTest(Player player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            boolean newState = !tag.getBoolean("psychologicalInvisibility");

            if (newState) {
                for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(50))) {
                    if (mob.getTarget() == player) {
                        mob.setTarget(null);
                    }
                }
                player.displayClientMessage(Component.literal("You are now invisible"), true);
            } else {
                player.displayClientMessage(Component.literal("You are now visible"), true);
            }

            tag.putBoolean("psychologicalInvisibility", newState);

            UUID playerId = player.getUUID();
            Boolean lastState = lastSentInvisibilityStates.get(playerId);
            if (lastState == null || lastState != newState) {
                LOTMNetworkHandler.sendToAllPlayers(new SyncShouldntRenderInvisibilityPacketS2C(newState, playerId));
                lastSentInvisibilityStates.put(playerId, newState);
            }
        }
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, hypnotize all entities around you to hide your presence. If you get hit enough times in a close period of time, you will be turned visible again."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("2% of your max spirituality per second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("12 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }
}