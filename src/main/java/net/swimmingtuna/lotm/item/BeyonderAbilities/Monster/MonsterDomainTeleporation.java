package net.swimmingtuna.lotm.item.BeyonderAbilities.Monster;


import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.blocks.MonsterDomainBlockEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MonsterDomainTeleporation extends SimpleAbilityItem {

    public MonsterDomainTeleporation(Properties properties) {
        super(properties, BeyonderClassInit.MONSTER, 3, 300, 200);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        teleportToDomain(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    private void teleportToDomain(Player player) {
        if (!player.level().isClientSide()) {
            ItemStack heldItem = player.getMainHandItem();
            CompoundTag tag = heldItem.getOrCreateTag();
            int currentIndex = tag.getInt("CurrentDomainIndex");
            List<MonsterDomainBlockEntity> ownedDomains = MonsterDomainBlockEntity.getDomainsOwnedBy(player.level(), player);
            MonsterDomainBlockEntity selectedDomain = ownedDomains.get(currentIndex);
            BlockPos pos = selectedDomain.getBlockPos();
            player.teleportTo(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
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
