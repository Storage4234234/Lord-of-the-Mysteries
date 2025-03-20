package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.List;

public class AuraOfGlory extends SimpleAbilityItem {


    public AuraOfGlory(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 2, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        auraOfGlory(player);
        return InteractionResult.SUCCESS;
    }

    public static void auraOfGlory(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            CompoundTag tag = livingEntity.getPersistentData();
            boolean auraOfGlory = tag.getBoolean("auraOfGlory");
            tag.putBoolean("auraOfGlory", !tag.getBoolean("auraOfGlory"));
            if (livingEntity instanceof Player player) {
                player.displayClientMessage(Component.literal("Aura of Twilight Turned " + (auraOfGlory ? "Off" : "On")).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), true);
            }
        }
    }

    public static void auraOfGloryAndTwilightTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        boolean glory = tag.getBoolean("auraOfGlory");
        boolean twilight = tag.getBoolean("auraOfTwilight");
        if (!livingEntity.level().isClientSide() && (glory || twilight)) {
            if (glory && livingEntity.tickCount % 20 == 0) {
                for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(150))) {
                    if (!BeyonderUtil.isAllyOf(livingEntity, living) && living != livingEntity) {
                        living.getPersistentData().putInt("age", (int) (living.getPersistentData().getInt("age") + BeyonderUtil.getDamage(livingEntity).get(ItemInit.AURAOFGLORY.get())));
                        living.sendSystemMessage(Component.literal("You are getting rapidly aged").withStyle(ChatFormatting.YELLOW));
                    } else {
                        living.setHealth(living.getHealth() + 1);
                        BeyonderUtil.addSpirituality(living, 40);
                        if (living instanceof Player player) {
                            for (Item item : BeyonderUtil.getAbilities(player)) {
                                if (item instanceof SimpleAbilityItem) {
                                    float percent = player.getCooldowns().getCooldownPercent(item, 0);
                                    if (percent != 0) {
                                        player.getCooldowns().addCooldown(item, -10);
                                    }
                                }
                            }
                        }
                    }
                }
                for (Projectile projectile : livingEntity.level().getEntitiesOfClass(Projectile.class, livingEntity.getBoundingBox().inflate(100))) {
                    if (projectile.getOwner() != null && projectile.getOwner() instanceof LivingEntity owner) {
                        if (owner != livingEntity && !BeyonderUtil.isAllyOf(livingEntity, owner)) {
                            projectile.setDeltaMovement(projectile.getDeltaMovement().scale(0.5).x(), projectile.getDeltaMovement().y() - 0.1, projectile.getDeltaMovement().scale(0.5).z());
                            projectile.hurtMarked = true;
                            projectile.getPersistentData().putInt("age", projectile.getPersistentData().getInt("age") + 5);
                        }
                    }
                    float scale = ScaleTypes.BASE.getScaleData(projectile).getScale();
                    if (projectile.getPersistentData().getInt("age") >= 10 * scale) {
                        projectile.discard();
                    }
                }
                BeyonderUtil.useSpirituality(livingEntity, 240);
            } else if (twilight && livingEntity.tickCount % 20 == 0) {
                for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(200))) {
                    if (!BeyonderUtil.isAllyOf(livingEntity, living) && living != livingEntity) {
                        living.getPersistentData().putInt("age", (int) (living.getPersistentData().getInt("age") + BeyonderUtil.getDamage(livingEntity).get(ItemInit.AURAOFTWILIGHT.get())));
                        living.sendSystemMessage(Component.literal("You are getting rapidly aged").withStyle(ChatFormatting.RED));
                    } else {
                        living.setHealth(living.getHealth() + 2);
                        BeyonderUtil.addSpirituality(living, 100);
                        if (living instanceof Player player) {
                            for (Item item : BeyonderUtil.getAbilities(player)) {
                                if (item instanceof SimpleAbilityItem) {
                                    float percent = player.getCooldowns().getCooldownPercent(item, 0);
                                    if (percent != 0) {
                                        player.getCooldowns().addCooldown(item, -20);
                                    }
                                }
                            }
                        }
                    }
                }
                for (Projectile projectile : livingEntity.level().getEntitiesOfClass(Projectile.class, livingEntity.getBoundingBox().inflate(100))) {
                    if (projectile.getOwner() != null && projectile.getOwner() instanceof LivingEntity owner) {
                        if (owner != livingEntity && !BeyonderUtil.isAllyOf(livingEntity, owner)) {
                            projectile.setDeltaMovement(projectile.getDeltaMovement().scale(0.3).x(), projectile.getDeltaMovement().y() - 0.2, projectile.getDeltaMovement().scale(0.3).z());
                            projectile.hurtMarked = true;
                            projectile.getPersistentData().putInt("age", projectile.getPersistentData().getInt("age") + 10);
                        }
                    }
                    float scale = ScaleTypes.BASE.getScaleData(projectile).getScale();
                    if (projectile.getPersistentData().getInt("age") >= 10 * scale) {
                        projectile.discard();
                    }
                }
                BeyonderUtil.useSpirituality(livingEntity, 350);
            }
        }
    }



    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, enable or disable your aura of glory. If enabled, all entities around you will be affected by glory. If they are an ally or yourself, they will age positively, gaining spirituality quickly, recovering fast, and have their item cooldowns reduced. If they aren't an ally, they will age rapidly, eventually turning to dust. This applies to projectiles too."));
        tooltipComponents.add(Component.literal("Left Click for Beam of Glory"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("200 per second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("WARRIOR_ABILITY", ChatFormatting.YELLOW);
    }
}

