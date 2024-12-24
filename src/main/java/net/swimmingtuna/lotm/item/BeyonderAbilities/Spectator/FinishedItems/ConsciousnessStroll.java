package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ConsciousnessStroll extends SimpleAbilityItem {

    public ConsciousnessStroll (Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 3, 300, 0);
    }

    public static void consciousnessStroll(CompoundTag playerPersistentData, Player player) {
        //CONSCIOUSNESS STROLL
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        int strollCounter = playerPersistentData.getInt("consciousnessStrollActivated");
        int consciousnessStrollActivatedX = playerPersistentData.getInt("consciousnessStrollActivatedX");
        int consciousnessStrollActivatedY = playerPersistentData.getInt("consciousnessStrollActivatedY");
        int consciousnessStrollActivatedZ = playerPersistentData.getInt("consciousnessStrollActivatedZ");
        if (strollCounter >= 1) {
            playerPersistentData.putInt("consciousnessStrollActivated", strollCounter - 1);
            serverPlayer.setGameMode(GameType.SPECTATOR);
        }
        if (strollCounter == 1) {
            player.teleportTo(consciousnessStrollActivatedX, consciousnessStrollActivatedY, consciousnessStrollActivatedZ);
            serverPlayer.setGameMode(GameType.SURVIVAL);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Type a player's name in chat to teleport to their location and turn invincible and invulnerable, teleporting back after 3 seconds\n" +
                "Spirituality Used: 500\n" +
                "Cooldown: 20 seconds").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
