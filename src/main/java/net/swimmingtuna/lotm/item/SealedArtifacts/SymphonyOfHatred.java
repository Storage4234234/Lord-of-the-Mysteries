package net.swimmingtuna.lotm.item.SealedArtifacts;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.PlayerMobEntity;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

public class SymphonyOfHatred extends Item {
    public SymphonyOfHatred(Properties pProperties) {
        super(pProperties.stacksTo(1));
    }


    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pHand) {
        ItemStack stack = pPlayer.getItemInHand(pHand);
        if (!pLevel.isClientSide() && pPlayer.getMainHandItem().getItem() instanceof SymphonyOfHatred) {
            if (pLevel instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, pPlayer.getX(), pPlayer.getY(), pPlayer.getZ(), SoundEvents.GHAST_SCREAM, SoundSource.PLAYERS, 0.5F, 0.4F / (pLevel.getRandom().nextFloat() * 0.4F + 0.8F));
            }
            symphonyOfHatred(pPlayer);
            pPlayer.getCooldowns().addCooldown(this, 300);
        }
        return InteractionResultHolder.sidedSuccess(stack, pLevel.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof LivingEntity player) {
            if (player.tickCount % 1200 == 0 && !level.isClientSide()) {
                player.getPersistentData().putDouble("misfortune", player.getPersistentData().getDouble("misfortune") + (Math.random() * 5));
            }
        }
        super.inventoryTick(stack, level, entity, itemSlot, isSelected);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (entity instanceof LivingEntity target && !player.getCooldowns().isOnCooldown(this)) {
            float damage;
            float healthPercentage = target.getHealth() / target.getMaxHealth();
            if (healthPercentage <= 0.1f) {
                damage = target.getHealth();
            } else {
                damage = 12.0f;
            }
            target.hurt(BeyonderUtil.magicSource(player), damage);
            player.getCooldowns().addCooldown(this, 400);
            return true;
        }
        return false;
    }

    public static void symphonyOfHatred(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide() && livingEntity.level() instanceof ServerLevel serverLevel) {
            for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(40))) {
                if (living != livingEntity) {
                    Random random = new Random();
                    living.hurt(BeyonderUtil.magicSource(livingEntity), 35.0f);
                    living.getPersistentData().putDouble("sanity", livingEntity.getPersistentData().getDouble("sanity") + 10);
                    BeyonderUtil.applyMobEffect(living, MobEffects.CONFUSION, 100, 1, true, true);
                    int sequence = 10;
                    if (living instanceof Player player) {
                        BeyonderHolder pHolder = BeyonderHolderAttacher.getHolderUnwrap(player);
                        sequence = pHolder.getCurrentSequence();
                    } else if (living instanceof PlayerMobEntity playerMobEntity) {
                        sequence = playerMobEntity.getCurrentSequence();
                    }
                    if (sequence >= 7) {
                        Random random1 = new Random();
                        if (random1.nextInt(2) == 0) {
                            BeyonderUtil.applyMobEffect(living, MobEffects.BLINDNESS, 60, 1, false, false);
                        } else {
                            living.hurt(BeyonderUtil.magicSource(livingEntity), 15.0f);
                        }
                    }
                    if (random.nextInt(3) == 0) {
                        BeyonderUtil.applyMobEffect(living, ModEffects.FRENZY.get(), 100, 1, true, true);
                    } else if (random.nextInt(3) == 1) {
                        BeyonderUtil.applyMobEffect(living, ModEffects.FRENZY.get(), 50, 1, true, true);
                    }
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("A flute made of a charred bone that you can play to inflict damage and cause mental array to those around you").withStyle(ChatFormatting.DARK_GREEN).withStyle(ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("You can also stab this into opponents to deal massive damage, or if they're at less than 10% of their maximum health, instantly kill them").withStyle(ChatFormatting.DARK_RED).withStyle(ChatFormatting.BOLD));
        tooltipComponents.add(Component.literal("Drawback: You will gain misfortune every minute that Symphony of Hatred is in your inventory").withStyle(ChatFormatting.GRAY));

        super.appendHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
}
