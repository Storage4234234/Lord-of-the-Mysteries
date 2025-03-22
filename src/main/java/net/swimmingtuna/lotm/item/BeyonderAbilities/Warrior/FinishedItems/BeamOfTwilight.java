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

public class BeamOfTwilight extends SimpleAbilityItem {


    public BeamOfTwilight(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 0, 1000, 200);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        beamOfTwilight(player);
        return InteractionResult.SUCCESS;
    }

    public static void beamOfTwilight(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            DragonBreathEntity beamOfTwilight = new DragonBreathEntity(livingEntity, 1);
            beamOfTwilight.teleportTo(livingEntity.getX(),livingEntity.getY()+1 ,livingEntity.getZ());
            beamOfTwilight.setIsDragonbreath(false);
            beamOfTwilight.setSize(10);
            beamOfTwilight.setRange(500);
            beamOfTwilight.setDestroyBlocks(false);
            beamOfTwilight.setIsTwilight(true);
            beamOfTwilight.setCharge(5);
            beamOfTwilight.setDuration(10);
            beamOfTwilight.setCausesFire(false);
            beamOfTwilight.setFrenzyTime(0);
            livingEntity.level().addFreshEntity(beamOfTwilight);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, shoot out a massive beam of twilight, aging anything it hits and turning it to dust."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("1000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("10 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("WARRIOR_ABILITY", ChatFormatting.YELLOW);
    }

}

