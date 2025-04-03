package net.swimmingtuna.lotm.item.BeyonderAbilities.Monster;


import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static net.swimmingtuna.lotm.util.BeyonderUtil.removePathway;
import static net.swimmingtuna.lotm.util.BeyonderUtil.setPathwayAndSequence;

public class FateReincarnation extends SimpleAbilityItem {

    public FateReincarnation(Properties properties) {
        super(properties, BeyonderClassInit.MONSTER, 1, 1250, 14400);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        fateReincarnation(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    private void fateReincarnation(LivingEntity player) {
        if (!player.level().isClientSide()) {
            int x = (int) (player.getX() + (Math.random() * 5000) - 2500);
            int z = (int) (player.getZ() + (Math.random() * 5000) - 2500);
            int surfaceY = getNonAirSurfaceBlock(player.level(),x,z);
            player.teleportTo(x, surfaceY + 4, z);
            player.getPersistentData().putInt("monsterReincarnationCounter", 7200);
            if (BeyonderUtil.getSequence(player) == 0) {
                player.getPersistentData().putBoolean("monsterReincarnation", true);
            } else {
                player.getPersistentData().putBoolean("monsterReincarnation", false);
            }
        }
    }

    private static void restoreDataReboot(LivingEntity player, CompoundTag tag) {
        for (MobEffectInstance activeEffect : new ArrayList<>(player.getActiveEffects())) {
            player.removeEffect(activeEffect.getEffect());
        }
        int age = tag.getInt("monsterRebootAge");
        int sanity = tag.getInt("monsterRebootSanity");
        int luck = tag.getInt("monsterRebootLuck");
        int misfortune = tag.getInt("monsterRebootMisfortune");
        int corruption = tag.getInt("monsterRebootCorruption");
        int health = tag.getInt("monsterRebootHealth");
        int spirituality = tag.getInt("monsterRebootSpirituality");
        int effectCount = tag.getInt("monsterRebootPotionEffectsCount");
        int ageDecay = tag.getInt("monsterRebootAgeDecay");
        for (int i = 0; i < effectCount; i++) {
            CompoundTag effectTag = tag.getCompound("monsterRebootPotionEffect_" + i);
            MobEffectInstance effect = MobEffectInstance.load(effectTag);
            if (effect != null) {
                player.addEffect(effect);
            }
        }
        tag.putInt("ageDecay", ageDecay);
        tag.putInt("age", age);
        tag.putDouble("sanity", sanity);
        tag.putDouble("corruption", corruption);
        tag.putDouble("luck", luck);
        tag.putDouble("misfortune", misfortune);
        BeyonderUtil.setSpirituality(player, spirituality);
        player.setHealth(Math.max(1, health));
        List<Item> beyonderAbilities = BeyonderUtil.getAbilities(player);
        for (Item item : beyonderAbilities) {
            if (player instanceof Player pPlayer) {
                if (item instanceof SimpleAbilityItem simpleAbilityItem) {
                    String itemCooldowns = item.getDescription().toString();
                    float savedCooldownPercent = tag.getFloat("monsterRebootCooldown" + itemCooldowns);
                    int remainingCooldownTicks = (int) (simpleAbilityItem.getCooldown() * savedCooldownPercent);
                    pPlayer.getCooldowns().addCooldown(item, remainingCooldownTicks);
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, reincarnates randomly up to 5000 blocks away in both directions, as a non-sequence player. You will automatically advance through the monster pathway over the course of two hours, until you reach your original sequence."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("1250").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("2 Hours").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void monsterReincarnationChecker(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!livingEntity.level().isClientSide() && livingEntity.tickCount % 20 == 0) {
            CompoundTag tag = livingEntity.getPersistentData();
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(livingEntity);
            boolean y = livingEntity.getPersistentData().getBoolean("monsterReincarnation");
            int x = tag.getInt("monsterReincarnationCounter");
            if (!y) {
                if (x >= 1) {
                    tag.putInt("monsterReincarnationCounter", x - 1);
                }
                if (x >= 7100) {
                    scaleData.setScale(0.2f);
                    removePathwayForMonster(livingEntity);
                } else if (x >= 6900) {
                    scaleData.setScale(0.25f);
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 9);
                } else if (x >= 6600) {
                    scaleData.setScale(0.35f);
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 8);
                } else if (x >= 6050) {
                    scaleData.setScale(0.5f);
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 7);
                } else if (x >= 5300) {
                    scaleData.setScale(0.6f);
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 6);
                } else if (x >= 4200) {
                    scaleData.setScale(0.7f);
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 5);
                } else if (x >= 3200) {
                    scaleData.setScale(0.85f);
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 4);
                } else if (x >= 1800) {
                    scaleData.setScale(0.9f);
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 3);
                } else if (x >= 2) {
                    scaleData.setScale(1.0f);
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 2);
                } else if (x == 1) {
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 1);
                }
            } else {
                if (x >= 1) {
                    tag.putInt("monsterReincarnationCounter", x - 1);
                }
                if (x >= 7140) {
                    scaleData.setScale(0.2f);
                    removePathwayForMonster(livingEntity);
                }
                if (x >= 7010 && x <= 7139) {
                    scaleData.setScale(0.25f);
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 9);
                }
                if (x >= 6800 && x <= 7009) {
                    scaleData.setScale(0.35f);
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 8);
                }
                if (x >= 6400 && x <= 6799) {
                    scaleData.setScale(0.5f);
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 7);
                }
                if (x >= 5850 && x <= 6399) {
                    scaleData.setScale(0.6f);
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 6);
                }
                if (x >= 5050 && x <= 5849) {
                    scaleData.setScale(0.7f);
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 5);
                }
                if (x >= 4150 && x <= 5049) {
                    scaleData.setScale(0.85f);
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 4);
                }
                if (x >= 2949 && x <= 4149) {
                    scaleData.setScale(0.9f);
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 3);
                }
                if (x >= 1650 && x <= 2950) {
                    scaleData.setScale(1.0f);
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 2);
                }
                if (x >= 2 && x <= 1649) {
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 1);
                }
                if (x == 1) {
                    setPathwayAndSequence(livingEntity,BeyonderClassInit.MONSTER.get(), 0);
                }
            }
        }
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("MONSTER_ABILITY", ChatFormatting.GRAY);
    }
    public static int getNonAirSurfaceBlock(Level level, int x, int z) {
        int y = level.getMaxBuildHeight() - 1;
        boolean foundGround = false;
        while (!foundGround && y >= level.getMinBuildHeight()) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState blockState = level.getBlockState(pos);
            foundGround = !blockState.isAir();
            y--;
        }
        return y;
    }

    public static void removePathwayForMonster(LivingEntity living) {
        if (living instanceof Player player) {
            BeyonderHolderAttacher.getHolderUnwrap(player).removePathway();
        } else {
            setPathwayAndSequence(living, null, -1);
        }
        if (living instanceof Player player) {
            Abilities playerAbilities = player.getAbilities();
            playerAbilities.setFlyingSpeed(0.05F);
            playerAbilities.setWalkingSpeed(0.1F);
            player.onUpdateAbilities();
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
            }
        }
    }
}
