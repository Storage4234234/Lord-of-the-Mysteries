package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ProphesizeTeleportPlayer extends Item {

    public ProphesizeTeleportPlayer(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        if (!player.level().isClientSide()) {
            if (!holder.currentClassMatches(BeyonderClassInit.SPECTATOR)) {
                player.displayClientMessage(Component.literal("You are not of the Spectator pathway").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
            }
            if (holder.getSpirituality() < 750) {
                player.displayClientMessage(Component.literal("You need 750 spirituality in order to use this").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
            }
        }
        if (holder.currentClassMatches(BeyonderClassInit.SPECTATOR) && holder.getCurrentSequence() <= 1 && holder.useSpirituality(750)) {
            AttributeInstance dreamIntoReality = player.getAttribute(ModAttributes.DIR.get());
            teleportEntities(player, level, holder.getCurrentSequence(), (int) dreamIntoReality.getValue());
            if (!player.getAbilities().instabuild)
                player.getCooldowns().addCooldown(this, 400);
        }
        return super.use(level, player, hand);
    }

    private void teleportEntities(Player player, Level level, int sequence, int dir) {
        double radius = (500 - sequence * 100) * dir;
        for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius))) {
            if (entity != player && !entity.level().isClientSide()) {
                entity.getPersistentData().putInt("prophesizeTeleportationCounter", (int) (150 * Math.random()));
                entity.getPersistentData().putInt("prophesizeTeleportX", (int) player.getX());
                entity.getPersistentData().putInt("prophesizeTeleportY", (int) player.getY());
                entity.getPersistentData().putInt("prophesizeTeleportZ", (int) player.getZ());
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, makes all living entities around the user teleport to the user\n" +
                "Spirituality Used: 750\n" +
                "Cooldown: 20 seconds").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
