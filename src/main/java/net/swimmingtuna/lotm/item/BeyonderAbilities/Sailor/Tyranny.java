package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;


import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class Tyranny extends SimpleAbilityItem {

    public Tyranny(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 0, 3500, 2400);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        tyranny(player);
        return InteractionResult.SUCCESS;
    }

    private void tyranny(Player player) {
        if (!player.level().isClientSide()) {
            double radius = 500;
            int duration = 250;
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius))) {
                if (entity != player) {
                    entity.addEffect(new MobEffectInstance(ModEffects.STUN.get(), duration, 1, false, false));
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal(
                "Upon use, exude an aura of tyranny, not giving any entity permission to move, implanting fear strong enough to not allow them to use their abilities"
        ).withStyle(/*ChatFormatting.BOLD, ChatFormatting.BLUE*/));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

}
