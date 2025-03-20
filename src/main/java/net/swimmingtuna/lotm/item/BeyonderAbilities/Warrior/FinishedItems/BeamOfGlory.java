package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.entity.DragonBreathEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class BeamOfGlory extends SimpleAbilityItem {


    public BeamOfGlory(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 2, 750, 140);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        beamOfGlory(player);
        return InteractionResult.SUCCESS;
    }

    public static void beamOfGlory(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            DragonBreathEntity dragonBreath = new DragonBreathEntity(livingEntity, 1);
            dragonBreath.teleportTo(livingEntity.getX(), livingEntity.getY() + 1, livingEntity.getZ());
            dragonBreath.setIsDragonbreath(false);
            dragonBreath.setSize(6);
            dragonBreath.setRange(200);
            dragonBreath.setDestroyBlocks(false);
            dragonBreath.setIsTwilight(true);
            dragonBreath.setCharge(5);
            dragonBreath.setDuration(10);
            dragonBreath.setCausesFire(false);
            livingEntity.level().addFreshEntity(dragonBreath);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, shoot out a large beam of glory, rapidly aging anything it hits, blocks or entities alike."));
        tooltipComponents.add(Component.literal("Left Click for Aura of Glory"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("750").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("7 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("WARRIOR_ABILITY", ChatFormatting.YELLOW);
    }

}

