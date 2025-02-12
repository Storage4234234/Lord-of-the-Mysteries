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
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import virtuoel.pehkui.api.ScaleTypes;

public class AuraOfGlory extends SimpleAbilityItem {


    public AuraOfGlory(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
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
                for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(200))) {
                    if (!BeyonderUtil.isAllyOf(livingEntity, living) && living != livingEntity) {
                        living.getPersistentData().putInt("age", (int) (living.getPersistentData().getInt("age") + BeyonderUtil.getDamage(livingEntity).get(ItemInit.AURAOFGLORY.get())));
                        livingEntity.sendSystemMessage(Component.literal("You are getting rapidly aged").withStyle(ChatFormatting.YELLOW));
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
                            projectile.getPersistentData().putInt("age", projectile.getPersistentData().getInt("age") + 4);
                        }
                    }
                    float scale = ScaleTypes.BASE.getScaleData(projectile).getScale();
                    if (projectile.getPersistentData().getInt("age") >= 10 * scale) {
                        projectile.discard();
                    }
                }
                BeyonderUtil.useSpirituality(livingEntity, 250);
            } else if (twilight && livingEntity.tickCount % 20 == 0) {
                for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(200))) {
                    if (!BeyonderUtil.isAllyOf(livingEntity, living) && living != livingEntity) {
                        living.getPersistentData().putInt("age", (int) (living.getPersistentData().getInt("age") + BeyonderUtil.getDamage(livingEntity).get(ItemInit.AURAOFTWILIGHT.get())));
                        livingEntity.sendSystemMessage(Component.literal("You are getting rapidly aged").withStyle(ChatFormatting.RED));
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
                            projectile.getPersistentData().putInt("age", projectile.getPersistentData().getInt("age") + 8);
                        }
                    }
                    float scale = ScaleTypes.BASE.getScaleData(projectile).getScale();
                    if (projectile.getPersistentData().getInt("age") >= 10 * scale) {
                        projectile.discard();
                    }
                }
                BeyonderUtil.useSpirituality(livingEntity, 250);
            }
        }
    }
}

