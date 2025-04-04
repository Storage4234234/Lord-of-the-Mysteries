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
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.spirituality.ModAttributes;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class EnvisionKingdom extends SimpleAbilityItem {

    public EnvisionKingdom(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 0, 0, 2400);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        int dreamIntoReality = (int) player.getAttribute(ModAttributes.DIR.get()).getValue();
        if (!checkAll(player, BeyonderClassInit.SPECTATOR.get(), 0, 6000 / dreamIntoReality, true)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player, 6000 / dreamIntoReality);
        generateCathedral(player);
        return InteractionResult.SUCCESS;
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, envision your divine kingdom, the Corpse Cathedral. In here, all your abilities will be strengthened and your spirituality will be infinitely replenished."));
        tooltipComponents.add(Component.literal("Left Click for Envision Barrier"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("6000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("2 Minutes").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    public static void envisionKingdom(LivingEntity livingEntity, Level level) {
        //ENVISION KINGDOM
        CompoundTag tag = livingEntity.getPersistentData();
        if (livingEntity instanceof Player player && level instanceof ServerLevel serverLevel) {
            AttributeInstance dreamIntoReality = player.getAttribute(ModAttributes.DIR.get());
            int mindScape = tag.getInt("inMindscape");
            if (mindScape < 1) return;
            Abilities playerAbilities = player.getAbilities();
            tag.putInt("inMindscape", mindScape + 1);
            if (mindScape >= 1200) {
                tag.putInt("inMindscape", 0);
            }
            int mindscapeAbilities = tag.getInt("mindscapeAbilities");
            if (mindscapeAbilities >= 1) {
                BeyonderUtil.setSpirituality(livingEntity, BeyonderUtil.getMaxSpirituality(livingEntity));
                if (!tag.getBoolean("CAN_FLY")) {
                    if (dreamIntoReality != null) {
                        dreamIntoReality.setBaseValue(3);
                    }
                    playerAbilities.setFlyingSpeed(0.1F);
                    playerAbilities.mayfly = true;
                    player.onUpdateAbilities();
                    tag.putInt("mindscapeAbilities", mindscapeAbilities - 1);
                    if (player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
                    }
                }
            }
            if (mindscapeAbilities == 1 && !tag.getBoolean("CAN_FLY")) {
                if (dreamIntoReality != null) {
                    dreamIntoReality.setBaseValue(1);
                }
                playerAbilities.setFlyingSpeed(0.05F);
                playerAbilities.mayfly = false;
                player.onUpdateAbilities();
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
                }
            }

            int partIndex = mindScape - 2;
            if (partIndex < 0) return;

            int mindScape1 = tag.getInt("inMindscape");
            int x = tag.getInt("mindscapePlayerLocationX");
            int y = tag.getInt("mindscapePlayerLocationY");
            int z = tag.getInt("mindscapePlayerLocationZ");
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
            StructureTemplate part = serverLevel.getStructureManager().getOrCreate(new ResourceLocation(LOTM.MOD_ID, "corpse_cathedral_" + (partIndex + 1)));
            BlockPos tagPos = new BlockPos(x, y + (partIndex * 2), z);
            StructurePlaceSettings settings = BeyonderUtil.getStructurePlaceSettings(new BlockPos(x, y, z));
            part.placeInWorld(serverLevel, tagPos, tagPos, settings, null, Block.UPDATE_ALL);
            tag.putInt("inMindscape", mindScape + 1);
        }
    }

    private void generateCathedral(LivingEntity player) {
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
    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (livingEntity.getPersistentData().getInt("mindscapeAbilities") == 0 && target != null) {
            return 100;
        } else {
            return 0;
        }
    }
}