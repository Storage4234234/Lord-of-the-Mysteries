package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

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
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.entity.LightningEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class LightningStorm extends SimpleAbilityItem {

    public LightningStorm(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 3, 1000, 600);
    }


    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        lightningStormAbility(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    public void lightningStormAbility(LivingEntity player) { //add logic to add persitatent data of targetX,
        if (!player.level().isClientSide()) {
            int sailorStormVec = player.getPersistentData().getInt("sailorStormVec");
            Vec3 lookVec = player.getLookAngle();
            int sequence = BeyonderUtil.getSequence(player);
            double targetX = player.getX() + sailorStormVec * lookVec.x();
            double targetY = player.getY() + sailorStormVec * lookVec.y();
            double targetZ = player.getZ() + sailorStormVec * lookVec.z();
            player.getPersistentData().putDouble("sailorStormVecX", targetX);
            player.getPersistentData().putDouble("sailorStormVecY", targetY);
            player.getPersistentData().putDouble("sailorStormVecZ", targetZ);
            CompoundTag persistentData = player.getPersistentData();
            persistentData.putInt("sailorLightningStorm", (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.LIGHTNING_STORM.get()));
            if (sequence <= 0) {
                persistentData.putInt("sailorLightningStormTyrant", (int) (float) BeyonderUtil.getDamage(player).get(ItemInit.LIGHTNING_STORM.get()));
            }
        }
    }

    public static void lightningStorm(LivingEntity livingEntity) {
        //LIGHTNING STORM
        CompoundTag tag = livingEntity.getPersistentData();
        double distance = livingEntity.getPersistentData().getDouble("sailorLightningStormDistance");
        if (distance > 300) {
            tag.putDouble("sailorLightningStormDistance", 0);
            if (livingEntity instanceof Player player) {
                player.displayClientMessage(Component.literal("Storm Radius Is 0").withStyle(BeyonderUtil.getStyle(livingEntity)), true);
            }
        }
        int tyrantVer = tag.getInt("sailorLightningStormTyrant");
        int sailorMentioned = tag.getInt("tyrantMentionedInChat");
        int sailorLightningStorm1 = tag.getInt("sailorLightningStorm1");
        int x1 = tag.getInt("sailorStormVecX1");
        int y1 = tag.getInt("sailorStormVecY1");
        int z1 = tag.getInt("sailorStormVecZ1");
        if (sailorMentioned >= 1) {
            tag.putInt("tyrantMentionedInChat", sailorMentioned - 1);
            if (sailorLightningStorm1 >= 1) {
                for (int i = 0; i < (tyrantVer >= 1 ? 4 : 2); i++) {
                    LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), livingEntity.level());
                    lightningEntity.setSpeed(10.0f);
                    lightningEntity.setDamage(10);
                    lightningEntity.setDeltaMovement((Math.random() * 0.4) - 0.2, -4, (Math.random() * 0.4) - 0.2);
                    lightningEntity.setMaxLength(30);
                    lightningEntity.setOwner(livingEntity);
                    lightningEntity.setNoUp(true);
                    lightningEntity.teleportTo(x1 + ((Math.random() * 300) - (double) 300 / 2), y1 + 130, z1 + ((Math.random() * 300) - (double) 300 / 2));
                    livingEntity.level().addFreshEntity(lightningEntity);
                }
                if (tyrantVer >= 1) {
                    tag.putInt("sailorLightningStormTyrant", tyrantVer - 1);
                }
                tag.putInt("sailorLightningStorm1", sailorLightningStorm1 - 1);
            }
        }

        int sailorLightningStorm = tag.getInt("sailorLightningStorm");
        int stormVec = tag.getInt("sailorStormVec");
        double sailorStormVecX = tag.getInt("sailorStormVecX");
        double sailorStormVecY = tag.getInt("sailorStormVecY");
        double sailorStormVecZ = tag.getInt("sailorStormVecZ");
        if (sailorLightningStorm >= 1) {
            for (int i = 0; i < 2; i++) {
                LightningEntity lightningEntity = new LightningEntity(EntityInit.LIGHTNING_ENTITY.get(), livingEntity.level());
                lightningEntity.setSpeed(10.0f);
                lightningEntity.setDeltaMovement((Math.random() * 0.4) - 0.2, -4, (Math.random() * 0.4) - 0.2);
                lightningEntity.setMaxLength(30);
                lightningEntity.setOwner(livingEntity);
                lightningEntity.setNoUp(true);
                lightningEntity.teleportTo(sailorStormVecX + ((Math.random() * distance) - distance / 2), sailorStormVecY + 130, sailorStormVecZ + ((Math.random() * distance) - distance / 2));
                livingEntity.level().addFreshEntity(lightningEntity);
            }
            tag.putInt("sailorLightningStorm", sailorLightningStorm - 1);
        }
        if (BeyonderUtil.currentPathwayAndSequenceMatches(livingEntity, BeyonderClassInit.SAILOR.get(), 3) && livingEntity.getMainHandItem().getItem() instanceof LightningStorm) {
            if (livingEntity.isShiftKeyDown()) {
                tag.putInt("sailorStormVec", stormVec + 10);
                if (livingEntity instanceof Player player) {
                    player.displayClientMessage(Component.literal("Sailor Storm Spawn Distance is " + stormVec).withStyle(BeyonderUtil.getStyle(livingEntity)), true);
                }
            }
            if (stormVec >= 301) {
                if (livingEntity instanceof Player player) {
                    player.displayClientMessage(Component.literal("Sailor Storm Spawn Distance is 0").withStyle(BeyonderUtil.getStyle(player)), true);
                }
                tag.putInt("sailorStormVec", 0);
                stormVec = 0;
            }
        }
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, summons a lightning storm"));
        tooltipComponents.add(Component.literal("Left Click to increase radius. Shift to increase how far away it will spawn"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("1000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("30 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            CompoundTag tag = livingEntity.getPersistentData();
            tag.putInt("sailorLightningStormDistance", 30);
            tag.putInt("sailorStormVec", (int) livingEntity.distanceTo(target));
            return 60;
        }
        return 0;
    }
}