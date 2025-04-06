package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class AuraOfTwilight extends SimpleAbilityItem {


    public AuraOfTwilight(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 0, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        auraOfTwilight(player);
        return InteractionResult.SUCCESS;
    }

    public static void auraOfTwilight(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            CompoundTag tag = livingEntity.getPersistentData();
            boolean auraOfGlory = tag.getBoolean("auraOfTwilight");
            tag.putBoolean("auraOfTwilight", !tag.getBoolean("auraOfTwilight"));
            if (livingEntity instanceof Player player) {
                player.displayClientMessage(Component.literal("Aura of Twilight Turned " + (auraOfGlory ? "Off" : "On")).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), true);
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, enable or disable your aura of twilight. If enabled, all entities around you will approach their twilight. If they are an ally or yourself, they will age positively, gaining spirituality rapidly, recovering health quickly, and have their item cooldowns be heavily reduced. If they aren't an ally, they will age to the point of dust in seconds. This applies to projectiles too."));
        tooltipComponents.add(Component.literal("Left Click for Twilight: Freeze"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("350 per second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("WARRIOR_ABILITY", ChatFormatting.YELLOW);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (livingEntity.getPersistentData().getBoolean("auraOfTwilight") && BeyonderUtil.getSequence(livingEntity) <= 900) {
            return 100;
        } else if (!livingEntity.getPersistentData().getBoolean("auraOfTwilight") && target != null) {
            return 80;
        }
        return 0;
    }
}

