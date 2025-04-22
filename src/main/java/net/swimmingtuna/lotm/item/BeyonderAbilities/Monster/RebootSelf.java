package net.swimmingtuna.lotm.item.BeyonderAbilities.Monster;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RebootSelf extends SimpleAbilityItem {

    public RebootSelf(Properties properties) {
        super(properties, BeyonderClassInit.MONSTER, 1, 2000, 1200);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        activateSpiritVision(player);
        if (!player.isShiftKeyDown()) {
            useSpirituality(player);
            addCooldown(player);
        }
        return InteractionResult.SUCCESS;
    }

    private void activateSpiritVision(LivingEntity player) {
        if (!player.level().isClientSide()) {
            if (player.isShiftKeyDown()) {
                saveDataReboot(player, player.getPersistentData());
                if (player instanceof Player pPlayer) {
                    pPlayer.displayClientMessage(Component.literal("Saved State.").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD), true);
                }
            } else {
                restoreDataReboot(player, player.getPersistentData());
                if (player instanceof Player pPlayer) {
                    pPlayer.displayClientMessage(Component.literal("Loaded State.").withStyle(ChatFormatting.GREEN).withStyle(ChatFormatting.BOLD), true);
                }
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use if you're shifting, save your current state including health, spirituality, potion effects, luck, misfortune, sanity, and corruption. If not shifting, revert to your saved state."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("2000").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Minute").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    private static void saveDataReboot(LivingEntity player, CompoundTag tag) {
        Collection<MobEffectInstance> activeEffects = player.getActiveEffects();
        tag.putInt("monsterRebootPotionEffectsCount", activeEffects.size());
        int i = 0;
        for (MobEffectInstance effect : activeEffects) {
            CompoundTag effectTag = new CompoundTag();
            effect.save(effectTag);
            tag.put("monsterRebootPotionEffect_" + i, effectTag);
            i++;
        }
        double luck = tag.getDouble("luck");
        double misfortune = tag.getDouble("misfortune");
        double sanity = tag.getDouble("sanity");
        double corruption = tag.getDouble("corruption");
        int age = tag.getInt("age");
        tag.putInt("monsterRebootAge", age);
        tag.putInt("monsterRebootLuck", (int) luck);
        tag.putInt("monsterRebootMisfortune", (int) misfortune);
        tag.putInt("monsterRebootSanity", (int) sanity);
        tag.putInt("monsterRebootCorruption", (int) corruption);
        tag.putInt("monsterRebootHealth", (int) player.getHealth());
        tag.putInt("monsterRebootSpirituality", (int) BeyonderUtil.getSpirituality(player));
        tag.putInt("monsterRebootAgeDecay", tag.getInt("ageDecay"));
        List<Item> beyonderAbilities = BeyonderUtil.getAbilities(player);
        if (player instanceof Player pPlayer) {
            for (Item item : beyonderAbilities) {
                if (item != ItemInit.REBOOTSELF.get()) {
                    String itemCooldowns = item.getDescription().toString();
                    tag.putFloat("monsterRebootCooldown" + itemCooldowns, pPlayer.getCooldowns().getCooldownPercent(item, 0));
                }
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
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("MONSTER_ABILITY", ChatFormatting.GRAY);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        CompoundTag tag = livingEntity.getPersistentData();
        if (tag.getInt("monsterRebootHealth") == 0) {
            if (livingEntity.getHealth() >= livingEntity.getMaxHealth() - 1) {
                return 100;
            }
        } else if (livingEntity.getMaxHealth() / livingEntity.getHealth() < 0.3) {
            return 100;
        }
        return 0;
    }
}
