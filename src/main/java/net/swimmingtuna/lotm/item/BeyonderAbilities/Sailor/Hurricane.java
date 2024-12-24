package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.entity.TornadoEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class Hurricane extends SimpleAbilityItem {

    public Hurricane(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 4, 1250, 1200);
    }

    public static void hurricane(CompoundTag playerPersistentData, PlayerMobEntity player) {
        //HURRICANE
        boolean sailorHurricaneRain = playerPersistentData.getBoolean("sailorHurricaneRain");
        BlockPos pos = new BlockPos((int) (player.getX() + (Math.random() * 100 - 100)), (int) (player.getY() - 100), (int) (player.getZ() + (Math.random() * 300 - 300)));
        int hurricane = playerPersistentData.getInt("sailorHurricane");
        if (hurricane < 1) {
            return;
        }
        if (sailorHurricaneRain) {
            playerPersistentData.putInt("sailorHurricane", hurricane - 1);
            if (hurricane == 600 && player.level() instanceof ServerLevel serverLevel) {
                serverLevel.setWeatherParameters(0, 700, true, true);
            }
            if (hurricane % 5 == 0) {
                SailorLightning.lightningHighPlayerMob(player, player.level());
            }
            if (hurricane == 600 || hurricane == 300) {
                for (int i = 0; i < 5; i++) {
                    TornadoEntity tornado = new TornadoEntity(player.level(), player, 0, 0, 0);
                    tornado.teleportTo(pos.getX(), pos.getY() + 100, pos.getZ());
                    tornado.setTornadoRandom(true);
                    tornado.setTornadoHeight(300);
                    tornado.setTornadoRadius(30);
                    tornado.setTornadoPickup(false);
                    player.level().addFreshEntity(tornado);
                }
            }
        }
        if (!sailorHurricaneRain && player.level() instanceof ServerLevel serverLevel && hurricane == 600) {
            playerPersistentData.putInt("sailorHurricane", hurricane - 1);
            serverLevel.setWeatherParameters(0, 700, true, false);
        }
    }

    public static void hurricane(CompoundTag playerPersistentData, Player player) {
        //HURRICANE
        boolean sailorHurricaneRain = playerPersistentData.getBoolean("sailorHurricaneRain");
        BlockPos pos = new BlockPos((int) (player.getX() + (Math.random() * 100 - 100)), (int) (player.getY() - 100), (int) (player.getZ() + (Math.random() * 300 - 300)));
        int hurricane = playerPersistentData.getInt("sailorHurricane");
        if (hurricane < 1) {
            return;
        }
        if (sailorHurricaneRain) {
            playerPersistentData.putInt("sailorHurricane", hurricane - 1);
            if (hurricane == 600 && player.level() instanceof ServerLevel serverLevel) {
                serverLevel.setWeatherParameters(0, 700, true, true);
            }
            if (hurricane % 5 == 0) {
                SailorLightning.lightningHigh(player, player.level());
            }
            if (hurricane == 600 || hurricane == 300) {
                for (int i = 0; i < 5; i++) {
                    TornadoEntity tornado = new TornadoEntity(player.level(), player, 0, 0, 0);
                    tornado.teleportTo(pos.getX(), pos.getY() + 100, pos.getZ());
                    tornado.setTornadoRandom(true);
                    tornado.setTornadoHeight(300);
                    tornado.setTornadoRadius(30);
                    tornado.setTornadoPickup(false);
                    player.level().addFreshEntity(tornado);
                }
            }
        }
        if (!sailorHurricaneRain && player.level() instanceof ServerLevel serverLevel && hurricane == 600) {
            playerPersistentData.putInt("sailorHurricane", hurricane - 1);
            serverLevel.setWeatherParameters(0, 700, true, false);
        }
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        hurricane(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    private static void hurricane(Player pPlayer) {
        if (!pPlayer.level().isClientSide()) {
            pPlayer.getPersistentData().putInt("sailorHurricane", 600);
        }
    }
    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, summons a hurricane that shoots lightning in the sky around the player and generates tornadoes\n" +
                "Spirituality Used: 1250\n" +
                "Cooldown: 1 minute").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE));
        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
