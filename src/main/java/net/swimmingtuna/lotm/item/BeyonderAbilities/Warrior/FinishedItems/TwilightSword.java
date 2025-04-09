package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

import static net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems.SilverSwordManifestation.findClosestEmptySlot;

public class TwilightSword extends SimpleAbilityItem {


    public TwilightSword(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 2, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
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
            if (livingEntity instanceof Player player) {
                int selectedSlot = findClosestEmptySlot(player);
                Inventory inventory = player.getInventory();
                inventory.setItem(selectedSlot, ItemInit.SWORDOFTWILIGHT.get().getDefaultInstance());
            } else {
                livingEntity.setItemInHand(InteractionHand.MAIN_HAND, ItemInit.SWORDOFTWILIGHT.get().getDefaultInstance());
            }
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

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("WARRIOR_ABILITY", ChatFormatting.YELLOW);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            return 100;
        }
        return 0;
    }
}

