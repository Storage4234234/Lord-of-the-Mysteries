package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
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

public class EnvisionHealth extends Item {

    public EnvisionHealth(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        AttributeInstance dreamIntoReality = player.getAttribute(ModAttributes.DIR.get());
        if (!player.level().isClientSide()) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            if (!holder.currentClassMatches(BeyonderClassInit.SPECTATOR)) {
                player.displayClientMessage(Component.literal("You are not of the Spectator pathway").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
            }
            if (holder.getSpirituality() < (int) 3500/dreamIntoReality.getValue()) {
                player.displayClientMessage(Component.literal("You need "  + (int) 3500/dreamIntoReality.getValue() + " spirituality in order to use this").withStyle(ChatFormatting.BOLD, ChatFormatting.AQUA), true);
            }
        }
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        if (holder.currentClassMatches(BeyonderClassInit.SPECTATOR) && holder.getCurrentSequence() == 0 && holder.useSpirituality((int) (3500 / dreamIntoReality.getValue()))) {
            setHealth(player);
            if (!player.getAbilities().instabuild)
                player.getCooldowns().addCooldown(this, 2400);
        }
        return super.use(level, player, hand);
    }

    private void setHealth(Player player) {
        AttributeInstance maxHP = player.getAttribute(Attributes.MAX_HEALTH);
        double maxHealth = maxHP.getValue();
        double health = player.getHealth();
        double x = (health + (maxHealth - health * 0.66));
        player.setHealth((float) x);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, heals to full health\n" +
                "Left Click for Envision Health\n" +
                "Spirituality Used: 4000\n" +
                "Cooldown: 2 minutes seconds").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
