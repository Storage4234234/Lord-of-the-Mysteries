package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SyncSequencePacketS2C;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ReachChangeUUIDs;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber(modid = LOTM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ProphesizeDemise extends SimpleAbilityItem {

    public ProphesizeDemise(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 1, 1000, 3000, 400, 400);
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity player, LivingEntity interactionTarget, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        int dir = (int) player.getAttribute(ModAttributes.DIR.get()).getValue();
        addCooldown(player, stack.getItem(), 3000 / dir);
        useSpirituality(player);
        prophesizeDemise(player,interactionTarget);
        return InteractionResult.SUCCESS;
    }

    private final Lazy<Multimap<Attribute, AttributeModifier>> lazyAttributeMap = Lazy.of(this::createAttributeMap);

    @SuppressWarnings("deprecation")
    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        if (slot == EquipmentSlot.MAINHAND) {
            return this.lazyAttributeMap.get();
        }
        return super.getDefaultAttributeModifiers(slot);
    }

    private Multimap<Attribute, AttributeModifier> createAttributeMap() {

        ImmutableMultimap.Builder<Attribute, AttributeModifier> attributeBuilder = ImmutableMultimap.builder();
        attributeBuilder.putAll(super.getDefaultAttributeModifiers(EquipmentSlot.MAINHAND));
        attributeBuilder.put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_ENTITY_REACH, "Reach modifier", 400, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with entities
        attributeBuilder.put(ForgeMod.BLOCK_REACH.get(), new AttributeModifier(ReachChangeUUIDs.BEYONDER_BLOCK_REACH, "Reach modifier", 400, AttributeModifier.Operation.ADDITION)); //adds a 12 block reach for interacting with blocks, p much useless for this item
        return attributeBuilder.build();
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use on an entity, prophesizes their demise, making it so they have to stay still for 10 seconds out of 30 seconds, otherwise they won't evade this prophecy and die"));
        tooltipComponents.add(Component.literal("Left Click for Prophesize Teleport (Block)"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("1000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("2.5 Minutes").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public void prophesizeDemise(LivingEntity player, LivingEntity interactionTarget) {
        if (!interactionTarget.level().isClientSide() && !player.level().isClientSide()) {
            if (BeyonderUtil.isBeyonderCapable(interactionTarget)) {
                if (player.getAttribute(ModAttributes.DIR.get()).getBaseValue() > 1) {
                    interactionTarget.addEffect(new MobEffectInstance(ModEffects.FRENZY.get(), 40, 1, false, false));
                }
                interactionTarget.addEffect(new MobEffectInstance(ModEffects.SPECTATORDEMISE.get(), 600, 1, false, false));
            } else {
                interactionTarget.addEffect(new MobEffectInstance(MobEffects.WITHER, 300, 6, false, false));
            }
        }
    }

    public static void demiseTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        CompoundTag tag = entity.getPersistentData();
        if (!entity.level().isClientSide()) {
            double prevX = tag.getDouble("prevX");
            double prevY = tag.getDouble("prevY");
            double prevZ = tag.getDouble("prevZ");
            double currentX = tag.getDouble("currentX");
            double currentY = tag.getDouble("currentY");
            double currentZ = tag.getDouble("currentZ");
            int tickCounter = tag.getInt("tickCounter");
            boolean hasSpectatorDemise = entity.hasEffect(ModEffects.SPECTATORDEMISE.get());
            int messageCounter = tag.getInt("MessageCounter");
            if (!hasSpectatorDemise) {
                int demise = tag.getInt("EntityDemise");
                demise = 0;
                tag.putInt("EntityDemise", 0);
                messageCounter = 0;
                tag.putInt("MessageCounter", 0);
                int nonDemise = tag.getInt("NonDemise");
                nonDemise = 0;
                tag.putInt("NonDemise", 0);
            }

            if (hasSpectatorDemise) {
                MobEffectInstance demiseEffect = entity.getEffect(ModEffects.SPECTATORDEMISE.get());
                if (demiseEffect != null) {
                    int effectDuration = demiseEffect.getDuration();
                    int effectDurationSeconds;
                    if (effectDuration < 20) {
                        effectDurationSeconds = 1;
                    } else {
                        effectDurationSeconds = (effectDuration + 19) / 20;
                    }
                    if (hasSpectatorDemise) {

                        int demise = tag.getInt("EntityDemise");
                        int nonDemise = tag.getInt("NonDemise");

                        int nonDemiseSeconds = (nonDemise + 19) / 20;
                        if (tickCounter == 0) {
                            prevX = entity.getX();
                            tag.putDouble("prevX", prevX);

                            prevY = entity.getY();
                            tag.putDouble("prevY", prevY);

                            prevZ = entity.getZ();
                            tag.putDouble("prevZ", prevZ);

                            tag.putInt("tickCounter", 1);
                        } else if (tickCounter == 1) {
                            currentX = entity.getX();
                            tag.putDouble("currentX", currentX);

                            currentY = entity.getY();
                            tag.putDouble("currentY", currentY);

                            currentZ = entity.getZ();
                            tag.putDouble("currentZ", currentZ);

                            tag.putInt("tickCounter", 0);
                        }
                        if (Math.abs(prevX - currentX) > 0.0023 || Math.abs(prevY - currentY) > 0.0023 || Math.abs(prevZ - currentZ) > 0.0023) { //movement check more accurate
                            demise++;
                            tag.putInt("EntityDemise", demise);
                        } else {
                            nonDemise++;
                            tag.putInt("NonDemise", nonDemise);
                        }
                        if (demise == 400) {
                            entity.kill();
                            messageCounter = 0;
                            tag.putInt("MessageCounter", messageCounter);
                            nonDemise = 0;
                            tag.putInt("NonDemise", nonDemise);
                        }
                        if (nonDemise > 200) {
                            demise = 0;
                            tag.putInt("EntityDemise", demise);
                            entity.removeEffect(ModEffects.SPECTATORDEMISE.get());
                            nonDemise = 0;
                            tag.putInt("NonDemise", nonDemise);
                            messageCounter = 0;
                            tag.putInt("MessageCounter", messageCounter);
                        }
                        if (nonDemise == 200) {
                            demise = 0;
                            tag.putInt("EntityDemise", demise);
                            entity.removeEffect(ModEffects.SPECTATORDEMISE.get());
                            entity.sendSystemMessage(Component.literal("You survived your fate").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD));
                            nonDemise = 0;
                            tag.putInt("NonDemise", nonDemise);
                            messageCounter = 0;
                            tag.putInt("MessageCounter", messageCounter);
                        }
                        if (demise == 20 && messageCounter == 0) {
                            messageCounter = 1;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 19 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 40 && messageCounter == 1) {
                            messageCounter = 2;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 18 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 60 && messageCounter == 2) {
                            messageCounter = 3;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 17 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 80 && messageCounter == 3) {
                            messageCounter = 4;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 16 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 100 && messageCounter == 4) {
                            messageCounter = 5;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 15 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 120 && messageCounter == 5) {
                            messageCounter = 6;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 14 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 140 && messageCounter == 6) {
                            messageCounter = 7;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 13 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 160 && messageCounter == 7) {
                            messageCounter = 8;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 12 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 180 && messageCounter == 8) {
                            messageCounter = 9;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 11 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 200 && messageCounter == 9) {
                            messageCounter = 10;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 10 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 220 && messageCounter == 10) {
                            messageCounter = 11;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 9 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 240 && messageCounter == 11) {
                            messageCounter = 12;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 8 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 260 && messageCounter == 12) {
                            messageCounter = 13;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 7 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 280 && messageCounter == 13) {
                            messageCounter = 14;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 6 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 300 && messageCounter == 14) {
                            messageCounter = 15;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 5 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 320 && messageCounter == 15) {
                            messageCounter = 16;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 4 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 340 && messageCounter == 16) {
                            messageCounter = 17;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 3 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 360 && messageCounter == 17) {
                            messageCounter = 18;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 2 seconds, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (demise == 380 && messageCounter == 18) {
                            messageCounter = 19;
                            tag.putInt("MessageCounter", messageCounter);
                            entity.sendSystemMessage(Component.literal("You need to stand still or you will die in 1 second, remaining time left on Death Prophecy is " + effectDurationSeconds).withStyle(ChatFormatting.RED).withStyle(ChatFormatting.BOLD));
                        }
                        if (nonDemise >= 20 && nonDemise <= 180 && nonDemise % 20 == 0) {
                            int standStillSecondsLeft = (200 - nonDemise) / 20;
                            entity.sendSystemMessage(Component.literal("You need to stand still for " + standStillSecondsLeft + " more seconds").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD));
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void handlePlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            LOTMNetworkHandler.sendToPlayer(new SyncSequencePacketS2C(holder.getSequence()), (ServerPlayer) player);
            CompoundTag persistentData = player.getPersistentData();

            if (persistentData.contains("DemiseCounter")) {
                int demiseCounter = persistentData.getInt("DemiseCounter");

                if (!persistentData.contains("EntityDemise") || persistentData.getInt("EntityDemise") == 0) {
                    player.getPersistentData().putInt("EntityDemise", demiseCounter);
                }
            } else {
                if (!persistentData.contains("EntityDemise") || persistentData.getInt("EntityDemise") == 0) {

                    player.getPersistentData().putInt("EntityDemise", 0);
                }
            }
        }
    }
    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            return (int) (100 - livingEntity.getHealth());
        }
        return 0;
    }
}