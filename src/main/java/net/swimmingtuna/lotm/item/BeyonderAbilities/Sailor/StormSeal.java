package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.MCLightningBoltEntity;
import net.swimmingtuna.lotm.entity.StormSealEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.List;

public class StormSeal extends SimpleAbilityItem {

    public StormSeal(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 0, 5000, 2400);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        stormSeal(player);
        return InteractionResult.SUCCESS;
    }

    public static void stormSeal(LivingEntity player) {
        if (!player.level().isClientSide()) {
            StormSealEntity stormSealEntity = new StormSealEntity(EntityInit.STORM_SEAL_ENTITY.get(), player.level());
            Vec3 lookVec = player.getLookAngle().normalize().scale(3.0f);
            stormSealEntity.setOwner(player);
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(stormSealEntity);
            scaleData.setTargetScale(BeyonderUtil.getDamage(player).get(ItemInit.STORM_SEAL.get()));
            stormSealEntity.teleportTo(player.getX(), player.getY(), player.getZ());
            stormSealEntity.setDeltaMovement(lookVec.x, lookVec.y, lookVec.z);
            player.level().addFreshEntity(stormSealEntity);
        }
    }

    public static void sealTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        CompoundTag tag = entity.getPersistentData();
        if (!entity.level().isClientSide()) {
            if (tag.getInt("inStormSeal") >= 3) {
                int stormSeal = tag.getInt("inStormSeal");
                int x = tag.getInt("stormSealX");
                int y = tag.getInt("stormSealY");
                int z = tag.getInt("stormSealZ");
                entity.teleportTo(x, y + 4000, z);
                BlockPos lightningSpawnPos = new BlockPos((int) (entity.getX() + (Math.random() * 20) - 10), (int) (entity.getY() + (Math.random() * 20) - 10), (int) (entity.getZ() + (Math.random() * 20) - 10));
                MCLightningBoltEntity lightningBolt = new MCLightningBoltEntity(EntityInit.MC_LIGHTNING_BOLT.get(), entity.level());
                lightningBolt.teleportTo(lightningSpawnPos.getX(), lightningSpawnPos.getY(), lightningSpawnPos.getZ());
                if (entity.tickCount % 3 == 0) {
                    if (!entity.level().isClientSide()) {
                        entity.level().addFreshEntity(lightningBolt);
                    }
                }
                tag.putInt("inStormSeal", stormSeal - 1);
                if (entity.tickCount % 10 == 0) {
                    entity.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 20, 0, false, false));
                    entity.addEffect(new MobEffectInstance(ModEffects.STUN.get(), 20, 0, false, false));
                }
                if (stormSeal % 20 == 0) {
                    if (entity instanceof Player player) {
                        int sealSeconds = (int) stormSeal / 20;
                        player.displayClientMessage(Component.literal("You are stuck in the storm seal for " + sealSeconds + " seconds").withStyle(ChatFormatting.BOLD, ChatFormatting.BLUE), true);
                    }
                }
            }
            if (tag.getInt("inStormSeal") == 2 || tag.getInt("inStormSeal") == 1) {
                int x = tag.getInt("stormSealX");
                int y = tag.getInt("stormSealY");
                int z = tag.getInt("stormSealZ");
                tag.putInt("inStormSeal", tag.getInt("inStormSeal") - 1);
                entity.teleportTo(x, y, z);
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, shoots out a compressed storm, which damages entities around and anything hit directly will be sealed for 3 minutes"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("5000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("2 Minutes").withStyle(ChatFormatting.YELLOW)));
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
            float healthPercent = livingEntity.getMaxHealth() / livingEntity.getHealth();
            float targetHealthPercent = target.getMaxHealth() / target.getHealth();
            if (healthPercent <= targetHealthPercent) {
                return 80;
            }
            else {
                return 45;
            }
        }
        return 0;
    }
}
