package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

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
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TravelDoor extends SimpleAbilityItem {
    public TravelDoor(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 5, 0, 0);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        blink(player);
        return InteractionResult.SUCCESS;
    }

    public static void blink(LivingEntity player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            int blinkDistance = tag.getInt("travelBlinkDistance");
            int damage = (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.TRAVELDOOR.get());
            if (BeyonderUtil.getSpirituality(player) >= blinkDistance * damage) {
                Vec3 lookVector = player.getLookAngle();
                double targetX = player.getX() + blinkDistance * lookVector.x();
                double targetY = player.getY();
                double targetZ = player.getZ() + blinkDistance * lookVector.z();
                player.teleportTo(targetX, targetY, targetZ);
                BeyonderUtil.useSpirituality(player,blinkDistance * damage);
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, blink in the direction you're looking. You can also type coordinates in chat in order to go to that location."));
        tooltipComponents.add(Component.literal("Hold shift to increase blink range."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("300").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("None").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static boolean coordsTravel(String message) {
        message = message.replace(",", " ").trim();
        message = message.replaceAll("\\s+", " ");
        try {
            String[] parts = message.split(" ");
            if (parts.length != 3) return false;
            for (String part : parts) {
                Integer.parseInt(part);
            }
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.BLUE);
    }
}
