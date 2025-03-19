package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.List;

public class TwilightSword extends SimpleAbilityItem {


    public TwilightSword(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 0, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        twilightSword(player);
        return InteractionResult.SUCCESS;
    }

    public static void twilightSword(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(livingEntity);
            scaleData.setTargetScale(3.0f);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, conjure a sword made of twilight. You can swing this sword and cause it to teleport to the entity you're looking at and getting huge, before swinging down and dealing immense damage. You can also right click the air to summon a strengthened hurricane of twilight which ages entities hit and destroys their armor quickly. You can also shift and right click to summon two boxes of twililght around you, protecting you from everything around it."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("175").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("25 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}

