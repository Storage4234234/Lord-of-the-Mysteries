package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.TickEventUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class EnvisionKingdom extends SimpleAbilityItem {

    public EnvisionKingdom(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 0, 0, 900);
    }

    public static void envisionKingdom(CompoundTag playerPersistentData, PlayerMobEntity player, ServerLevel serverLevel) {
        //ENVISION KINGDOM

        AttributeInstance dreamIntoReality = player.getAttribute(ModAttributes.DIR.get());
        int mindScape = playerPersistentData.getInt("inMindscape");
        if (mindScape < 1) return;
        playerPersistentData.putInt("inMindscape", mindScape + 1);
        if (mindScape >= 1200) {
            playerPersistentData.putInt("inMindscape", 0);
        }
        int mindscapeAbilities = playerPersistentData.getInt("mindscapeAbilities");
        if (mindscapeAbilities >= 1) { //
            player.setSpirituality(player.getMaxSpirituality());
            if (!playerPersistentData.getBoolean("CAN_FLY")) {
                dreamIntoReality.setBaseValue(3);
                //allow it to fly with a fly speed of 0.15
                playerPersistentData.putInt("mindscapeAbilities", mindscapeAbilities - 1);
            }
        }
        if (mindscapeAbilities == 1 && !playerPersistentData.getBoolean("CAN_FLY")) {
            dreamIntoReality.setBaseValue(1);
            //disable flight, return flight speed to 0.05

        }

        int partIndex = mindScape - 2;
        if (partIndex < 0) return;

        int mindScape1 = playerPersistentData.getInt("inMindscape");
        int x = playerPersistentData.getInt("mindscapePlayerLocationX");
        int y = playerPersistentData.getInt("mindscapePlayerLocationY");
        int z = playerPersistentData.getInt("mindscapePlayerLocationZ");
        if (mindScape1 < 1) return;
        if (mindScape1 == 6) {
            player.teleportTo(player.getX() + 77, player.getY() + 8, player.getZ() + 206);
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(250))) {
                if (entity != player) {
                    entity.teleportTo(player.getX(), player.getY(), player.getZ() - 10);
                }
            }
        }
        TickEventUtil.placeCorpseCathedral(playerPersistentData, serverLevel, mindScape, partIndex, x, y, z);
        player.sendSystemMessage(Component.literal("ENVISIONEd"));
    }

    public static void envisionKingdom(CompoundTag playerPersistentData, Player player, BeyonderHolder holder, ServerLevel serverLevel) {
        //ENVISION KINGDOM

        AttributeInstance dreamIntoReality = player.getAttribute(ModAttributes.DIR.get());
        int mindScape = playerPersistentData.getInt("inMindscape");
        if (mindScape < 1) return;
        Abilities playerAbilities = player.getAbilities();
        playerPersistentData.putInt("inMindscape", mindScape + 1);
        if (mindScape >= 1200) {
            playerPersistentData.putInt("inMindscape", 0);
        }
        int mindscapeAbilities = playerPersistentData.getInt("mindscapeAbilities");
        if (mindscapeAbilities >= 1) {
            holder.setSpirituality(holder.getMaxSpirituality());
            if (!playerPersistentData.getBoolean("CAN_FLY")) {
                dreamIntoReality.setBaseValue(3);
                playerAbilities.setFlyingSpeed(0.1F);
                playerAbilities.mayfly = true;
                player.onUpdateAbilities();
                playerPersistentData.putInt("mindscapeAbilities", mindscapeAbilities - 1);
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
                }
            }
        }
        if (mindscapeAbilities == 1 && !playerPersistentData.getBoolean("CAN_FLY")) {
            dreamIntoReality.setBaseValue(1);
            playerAbilities.setFlyingSpeed(0.05F);
            playerAbilities.mayfly = false;
            player.onUpdateAbilities();
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
            }
        }

        int partIndex = mindScape - 2;
        if (partIndex < 0) return;

        int mindScape1 = playerPersistentData.getInt("inMindscape");
        int x = playerPersistentData.getInt("mindscapePlayerLocationX");
        int y = playerPersistentData.getInt("mindscapePlayerLocationY");
        int z = playerPersistentData.getInt("mindscapePlayerLocationZ");
        if (mindScape1 < 1) return;
        if (mindScape1 == 11) {
            for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(250))) {
                if (entity != player) {
                    if (entity instanceof Player) {
                        entity.teleportTo(player.getX(), player.getY() + 1, player.getZ() - 10);
                    } else if (entity.getMaxHealth() >= 50) {
                        entity.teleportTo(player.getX(), player.getY() + 1, player.getZ() - 10);
                    }
                }
            }
        }
        if (mindScape == 2 || mindScape == 4 || mindScape == 6 || mindScape == 8 || mindScape == 10) {
            player.teleportTo(player.getX(), player.getY() + 4.5, player.getZ());
        }
        TickEventUtil.placeCorpseCathedral(playerPersistentData, serverLevel, mindScape, partIndex, x, y, z);
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
        envisionKingdom(player.getPersistentData(), player, holder, (ServerLevel) level);
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