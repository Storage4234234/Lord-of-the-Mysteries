package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;


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
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ProphesizeTeleportPlayer extends SimpleAbilityItem {

    public ProphesizeTeleportPlayer(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 1, 750, 2400);
    }
    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        teleportEntities(player);
        return InteractionResult.SUCCESS;
    }

    private void teleportEntities(LivingEntity player) {
        if (!player.level().isClientSide()) {
            double radius = (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.PROPHESIZE_TELEPORT_BLOCK.get());
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius))) {
                if (entity != player && !entity.level().isClientSide() && !BeyonderUtil.isAllyOf(player, entity)) {
                    entity.getPersistentData().putInt("prophesizeTeleportationCounter", (int) (300 * Math.random()));
                    entity.getPersistentData().putInt("prophesizeTeleportX", (int) player.getX());
                    entity.getPersistentData().putInt("prophesizeTeleportY", (int) player.getY());
                    entity.getPersistentData().putInt("prophesizeTeleportZ", (int) player.getZ());
                }
            }
        }
    }
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, prophesize that all entities nearby will teleport to the player, making it occur to all affected entities within a minute"));
        tooltipComponents.add(Component.literal("Left Click for Prophesize Demise"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("750").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("2 Minutes").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void prophesizeTeleportation(LivingEntity livingEntity) {
        //PROPHESIZE TELEPORT BLOCK/PLAYER
        CompoundTag tag = livingEntity.getPersistentData();
        if (tag.getInt("prophesizeTeleportationCounter") >= 1) {
            tag.putInt("prophesizeTeleportationCounter", tag.getInt("prophesizeTeleportationCounter") - 1);
        }
        if (tag.getInt("prophesizeTeleportationCounter") == 1) {
            tag.putInt("prophesizeTeleportationCounter", tag.getInt("prophesizeTeleportationCounter") - 1);
            int x = tag.getInt("prophesizeTeleportX");
            int y = tag.getInt("prophesizeTeleportY");
            int z = tag.getInt("prophesizeTeleportZ");
            livingEntity.teleportTo(x, y, z);
        }
    }


    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }
}
