package net.swimmingtuna.lotm.item.OtherItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class PickaxeOfDawn extends PickaxeItem {


    public PickaxeOfDawn(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }


    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof LivingEntity livingEntity && !level.isClientSide()) {
            if (livingEntity.tickCount % 20 == 0 && !livingEntity.level().isClientSide()) {
                int sequence = BeyonderUtil.getSequence(livingEntity);
                if (livingEntity instanceof Player player) {
                    BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                    if (!holder.currentClassMatches(BeyonderClassInit.WARRIOR) || sequence >= 7) {
                        player.getInventory().setItem(itemSlot, ItemStack.EMPTY);
                    } else {
                        if (holder.getSpirituality() >= 25) {
                            holder.useSpirituality(25);
                        } else {
                            player.getInventory().setItem(itemSlot, ItemStack.EMPTY);
                        }
                    }
                } else if (livingEntity instanceof PlayerMobEntity playerMobEntity) {
                    if (playerMobEntity.getCurrentPathway() != BeyonderClassInit.WARRIOR || playerMobEntity.getCurrentSequence() >= 7) {
                        removeItemFromSlot(livingEntity, stack);
                    } else {
                        if (playerMobEntity.getSpirituality() >= 25) {
                            playerMobEntity.useSpirituality(25);
                        } else {
                            removeItemFromSlot(livingEntity, stack);
                        }
                    }
                } else {
                    removeItemFromSlot(livingEntity, stack);
                }
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    @Override
    public float getDestroySpeed(ItemStack pStack, BlockState pState) {
        return super.getDestroySpeed(pStack,pState) * 1.2f;
    }

    private void removeItemFromSlot(LivingEntity entity, ItemStack stack) {
        if (entity.getItemBySlot(EquipmentSlot.MAINHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        else if (entity.getItemBySlot(EquipmentSlot.OFFHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {

        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("DAWN_ITEM", ChatFormatting.YELLOW);
    }
}
