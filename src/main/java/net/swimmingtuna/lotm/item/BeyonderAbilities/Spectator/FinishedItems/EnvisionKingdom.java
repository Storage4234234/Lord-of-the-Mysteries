package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class EnvisionKingdom extends SimpleAbilityItem {

    public EnvisionKingdom(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 0, 0, 900);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
        int dreamIntoReality = (int) player.getAttribute(ModAttributes.DIR.get()).getValue();
        if (!checkAll(player, BeyonderClassInit.SPECTATOR.get(), 0, 6000 / dreamIntoReality)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player, 6000 / dreamIntoReality);
        generateCathedral(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, summons your Divine Kingdom, the Corpse Cathedral\n" +
                "Spirituality Used: 6000\n" +
                "Cooldown: 5 minutes ").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    private void generateCathedral(Player player) {
        if (!player.level().isClientSide) {
            int x = (int) player.getX();
            int y = (int) player.getY();
            int z = (int) player.getZ();
            CompoundTag compoundTag = player.getPersistentData();
            compoundTag.putInt("mindscapeAbilities", 50);
            compoundTag.putInt("inMindscape", 1);
            compoundTag.putInt("mindscapePlayerLocationX", x - 77); //check if this works
            compoundTag.putInt("mindscapePlayerLocationY", y - 8);
            compoundTag.putInt("mindscapePlayerLocationZ", z - 207);
        }
    }
}