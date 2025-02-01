package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SyncShouldntRenderInvisibilityPacketS2C;
import net.swimmingtuna.lotm.util.ClientData.ClientShouldntRenderInvisibilityData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;


public class PsychologicalInvisibility extends SimpleAbilityItem {

    public PsychologicalInvisibility(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 6, 0, 240);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        psychologicalInvisibility(player);
        if (ClientShouldntRenderInvisibilityData.getShouldntRender()) {
            addCooldown(player);
        }
        return InteractionResult.SUCCESS;
    }

    private static void psychologicalInvisibility(Player player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            boolean newState = !tag.getBoolean("psychologicalInvisibility");

            if (newState) {
                for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(50))) {
                    if (mob.getTarget() == player) {
                        mob.setTarget(null);
                    }
                }
                player.displayClientMessage(Component.literal("You are now invisible").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN), true);
            } else {
                player.displayClientMessage(Component.literal("You are now visible").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.RED), true);
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

    public static final Map<UUID, Boolean> lastSentInvisibilityStates = new HashMap<>();

    public static void psychologicalInvisibility(Player player, CompoundTag playerPersistentData, BeyonderHolder holder) {
        if (player.tickCount % 10 == 0) {
            boolean currentState = playerPersistentData.getBoolean("psychologicalInvisibility");
            if (currentState) {
                Collection<MobEffectInstance> effects = player.getActiveEffects();
                effects.forEach(effect -> {
                    if (effect.isAmbient() || effect.isVisible()) {
                        MobEffectInstance newEffect = new MobEffectInstance(effect.getEffect(), effect.getDuration(), effect.getAmplifier(), false, false, false);
                        effect.update(newEffect);
                    }
                });
                for (Mob mob : player.level().getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(40))) {
                    if (mob.getTarget() == player) {
                        mob.setTarget(null);
                    }
                }
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 11, 1, false, false));
                holder.useSpirituality((int) holder.getMaxSpirituality() / 100);
            }
            UUID playerId = player.getUUID();
            Boolean lastState = lastSentInvisibilityStates.get(playerId);
            if (lastState == null || lastState != currentState) {
                LOTMNetworkHandler.sendToAllPlayers(new SyncShouldntRenderInvisibilityPacketS2C(currentState, playerId));
                lastSentInvisibilityStates.put(playerId, currentState);
            }
        }
    }


    public static void psychologicalInvisibilityHurt(LivingHurtEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            if (entity.getPersistentData().getBoolean("psychologicalInvisibility")) {
                CompoundTag tag = entity.getPersistentData();
                int x = tag.getInt("psychologicalInvisibilityHurt");
                tag.putInt("psychologicalInvisibilityHurt", x + 100);
            }
        }
    }

    public static void psychologicalInvisibilityHurtTick(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            CompoundTag tag = livingEntity.getPersistentData();
            int x = tag.getInt("psychologicalInvisibilityHurt");
            if (x >= 1) {
                tag.putInt("psychologicalInvisibilityHurt", x - 1);
            }
            if (x >= 400 && livingEntity.getPersistentData().getBoolean("psychologicalInvisibility")) {
                livingEntity.getPersistentData().putBoolean("psychologicalInvisibility", false);
                LOTMNetworkHandler.sendToAllPlayers(new SyncShouldntRenderInvisibilityPacketS2C(false, livingEntity.getUUID()));
                tag.putInt("psychologicalInvisibilityHurt", 0);
                if (livingEntity instanceof Player player) {
                    player.displayClientMessage(Component.literal("You got hit too many times, you're now visible").withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD), true);
                    player.getCooldowns().addCooldown(ItemInit.PSYCHOLOGICAL_INVISIBILITY.get(), 240);
                }
            }
        }
    }

}