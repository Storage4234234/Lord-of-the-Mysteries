package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TravelDoorWaypoint extends SimpleAbilityItem {

    public TravelDoorWaypoint(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 5, 300, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = player.getPersistentData();
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        if (player.isShiftKeyDown()) {
            setWaypoint(player, stack, tag);
        } else {
            useSpirituality(player);
            teleportToWaypoint(player, tag, stack);
        }
        addCooldown(player);
        return InteractionResult.SUCCESS;
    }

    private void setWaypoint(LivingEntity player, ItemStack stack, CompoundTag tag) {
        if (!player.level().isClientSide) {
            int waypoint = tag.getInt("doorWaypoint");
            tag.putDouble("x" + waypoint, player.getX());
            tag.putDouble("y" + waypoint, player.getY());
            tag.putDouble("z" + waypoint, player.getZ());
            String coords = String.format("Waypoint %d set at: %.1f, %.1f, %.1f", waypoint, player.getX(), player.getY(), player.getZ());
            if (player instanceof Player pPlayer) {
                pPlayer.displayClientMessage(Component.literal(coords).withStyle(BeyonderUtil.getStyle(player)), true);
            }
        }
    }

    private void teleportToWaypoint(LivingEntity livingEntity, CompoundTag tag, ItemStack stack) {
        if (!livingEntity.level().isClientSide) {
            int waypoint = tag.getInt("doorWaypoint");
            double x = tag.getDouble("x" + waypoint);
            double y = tag.getDouble("y" + waypoint);
            double z = tag.getDouble("z" + waypoint);
            if (x != 0 && y != 0 && z != 0) {
                livingEntity.teleportTo(x, y, z);
                String coords = String.format("Teleported to %.1f, %.1f, %.1f", x, y, z);
                if (livingEntity instanceof Player pPlayer) {
                    pPlayer.displayClientMessage(Component.literal(coords).withStyle(BeyonderUtil.getStyle(pPlayer)), true);
                }
            } else if (livingEntity instanceof Player pPlayer) {
                pPlayer.displayClientMessage(Component.literal("No waypoint found").withStyle(ChatFormatting.RED), true);
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Right-click to teleport to selected waypoint"));
        tooltipComponents.add(Component.literal("Shift + Right-click to set waypoint at current position"));
        tooltipComponents.add(Component.literal("Left-click air to cycle between waypoints"));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (entity instanceof Player player && !level.isClientSide && player.getMainHandItem().getItem() == ItemInit.TRAVELDOORHOME.get()) {
            if (isSelected) {
                CompoundTag tag = player.getPersistentData();
                int currentWaypoint = tag.getInt("doorWaypoint");
                double x = tag.getDouble("x" + currentWaypoint);
                double y = tag.getDouble("y" + currentWaypoint);
                double z = tag.getDouble("z" + currentWaypoint);

                if (tag.contains("x" + currentWaypoint)) {
                    String coords = String.format("Waypoint %d: %.1f, %.1f, %.1f",
                            currentWaypoint, x, y, z);
                    player.displayClientMessage(Component.literal(coords)
                            .withStyle(BeyonderUtil.getStyle(player)), true);
                }
            }
        }
    }

    public static void clearAllWaypoints(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide) {
            CompoundTag tag = livingEntity.getPersistentData();
            for (int i = 0; i < 100; i++) {
                tag.remove("x" + i);
                tag.remove("y" + i);
                tag.remove("z" + i);
            }
        }
    }


    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICES_ABILITY", ChatFormatting.BLUE);
    }
}