package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

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
import net.swimmingtuna.lotm.entity.TornadoEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class CalamityIncarnationTornado extends SimpleAbilityItem {


    public CalamityIncarnationTornado(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 2,800,800);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        calamityIncarnationTornado(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }


    public void calamityIncarnationTornado(LivingEntity player) {
        if (!player.level().isClientSide()) {
            TornadoEntity.summonCalamityTornado(player);
            player.getPersistentData().putInt("calamityIncarnationTornado", (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.CALAMITY_INCARNATION_TORNADO.get()));
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, summon a tornado around you for 15 seconds"));
        tooltipComponents.add(Component.literal("Left Click for Calamity Incarnation (Tsunami)"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("800").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("40 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null && livingEntity.getPersistentData().getInt("calamityIncarnationTsunami") == 0) {
            return (int) (80 - livingEntity.getHealth());
        }
        return 0;
    }
}
