package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

@Mod.EventBusSubscriber(modid = LOTM.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class Tsunami extends Item {
    public Tsunami(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!player.level().isClientSide()) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
            if (!holder.currentClassMatches(BeyonderClassInit.SAILOR)) {
                player.displayClientMessage(Component.literal("You are not of the Sailor pathway").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
                return super.use(level, player, hand);
            }
            if (holder.getSpirituality() < 500) {
                player.displayClientMessage(Component.literal("You need 500 spirituality in order to use this").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
                return super.use(level, player, hand);
            }

            if (holder.getCurrentSequence() <= 4 && holder.useSpirituality(500)) {
                startTsunami(player);
            }
            if (!player.getAbilities().instabuild)
                player.getCooldowns().addCooldown(this, 900); // 60 seconds cooldown
        }
        return super.use(level, player, hand);
    }

    public static void startTsunami(Player player) {
        player.getPersistentData().putInt("sailorTsunami", 600);
        float yaw = player.getYRot();
        String direction = getDirectionFromYaw(yaw);
        player.getPersistentData().putString("sailorTsunamiDirection", direction);
        player.getPersistentData().putInt("sailorTsunamiX", (int) player.getX());
        player.getPersistentData().putInt("sailorTsunamiY", (int) player.getY());
        player.getPersistentData().putInt("sailorTsunamiZ", (int) player.getZ());
    }

    private static String getDirectionFromYaw(float yaw) {
        if (yaw < 0) {
            yaw += 360;
        }
        if (yaw >= 315 || yaw < 45) {
            return "N";
        } else if (yaw >= 45 && yaw < 135) {
            return "E";
        } else if (yaw >= 135 && yaw < 225) {
            return "S";
        } else if (yaw >= 225 && yaw < 315) {
            return "W";
        }
        return "N";
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Creates a massive wave of water in front of you\n" +
                "Spirituality Used: 500\n" +
                "Cooldown: 45 seconds").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void summonTsunami(Player player) {
        CompoundTag tag = player.getPersistentData();
        int playerX = tag.getInt("sailorTsunamiX");
        int playerY = tag.getInt("sailorTsunamiY");
        int playerZ = tag.getInt("sailorTsunamiZ");
        int tsunami = tag.getInt("sailorTsunami");
        String direction = tag.getString("sailorTsunamiDirection");

        int offsetX = 0;
        int offsetZ = 0;

        switch (direction) {
            case "N":
                offsetZ = 1;
                break;
            case "E":
                offsetX = -1;
                break;
            case "S":
                offsetZ = -1;
                break;
            case "W":
                offsetX = 1;
                break;
        }

        int waveWidth = 80;
        int waveHeight = 10;
        int startDistance = 85;

        for (int w = -waveWidth / 2; w < waveWidth / 2; w++) {
            for (int h = 0; h < waveHeight; h++) {
                int x = playerX + (offsetX * startDistance) + (offsetX * (200 - tsunami) / 5);
                int y = playerY + h;
                int z = playerZ + (offsetZ * startDistance) + (offsetZ * (200 - tsunami) / 5);

                if (offsetX == 0) {
                    x += w;
                } else {
                    z += w;
                }

                BlockPos blockPos = new BlockPos(x, y, z);
                if (player.level().getBlockState(blockPos).isAir()) {
                    player.level().setBlock(blockPos, Blocks.WATER.defaultBlockState(), 3);
                }
            }
        }
    }

}
