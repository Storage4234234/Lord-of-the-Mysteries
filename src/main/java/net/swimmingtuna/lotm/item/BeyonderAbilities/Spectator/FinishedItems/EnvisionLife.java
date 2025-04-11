package net.swimmingtuna.lotm.item.BeyonderAbilities.Spectator.FinishedItems;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EnvisionLife extends SimpleAbilityItem {

    public EnvisionLife(Properties properties) {
        super(properties, BeyonderClassInit.SPECTATOR, 0, 0, 400);
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @javax.annotation.Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("While holding this item, type in a mob's name (for example, minecraft:cow) in order to envision it into the world."));
        tooltipComponents.add(Component.literal("Left Click for Envision Weather"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("Envisioned Mob's Max Health * 3").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("None").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    public static void envisionLife(LivingEntity player) {
        //ENVISION LIFE
        int waitMakeLifeCounter = player.getPersistentData().getInt("waitMakeLifeTimer");
        if (waitMakeLifeCounter >= 1) {
            waitMakeLifeCounter++;
        }
        if (waitMakeLifeCounter >= 600) {
            waitMakeLifeCounter = 0;
        }
        player.getPersistentData().putInt("waitMakeLifeTimer", waitMakeLifeCounter);
    }

    public static void spawnMob(Player player, String mobName) {
        if (!player.level().isClientSide() && player.level() instanceof ServerLevel serverLevel) {
            Level level = player.level();
            ResourceLocation resourceLocation = new ResourceLocation(mobName);

            if (!ForgeRegistries.ENTITY_TYPES.containsKey(resourceLocation)) {
                player.sendSystemMessage(Component.literal("Invalid mob name: " + mobName));
                return;
            }

            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(resourceLocation);
            int cooldownTimer = (int) player.getCooldowns().getCooldownPercent(ItemInit.ENVISION_LIFE.get(), 0.0f);

            if (cooldownTimer == 0) {
                // Find highest health target
                LivingEntity highestHealthTarget = null;
                float maxHealth = Float.MIN_VALUE;
                for (LivingEntity livingEntity : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(150))) {
                    if (livingEntity != player) {
                        float currentHealth = livingEntity.getHealth();
                        if (currentHealth > maxHealth) {
                            maxHealth = currentHealth;
                            highestHealthTarget = livingEntity;
                        }
                    }
                }

                // Spawn the entity using EntityType.spawn
                BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                if (entityType != null) {
                    Entity entity = entityType.spawn(serverLevel, player.blockPosition(), MobSpawnType.NATURAL);

                    if (entity instanceof Mob mob) {
                        if (holder.getSpirituality() >= mob.getMaxHealth() * BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_LIFE.get())) {
                            holder.useSpirituality((int) (mob.getMaxHealth() * BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_LIFE.get())));

                            if (highestHealthTarget != null) {
                                mob.setTarget(highestHealthTarget);
                            }

                            player.getCooldowns().addCooldown(ItemInit.ENVISION_LIFE.get(), 400);
                            String entityName = entity.getType().getDescription().getString();
                            player.displayClientMessage(Component.literal("Envisioned a " + entityName + " into the world")
                                    .withStyle(ChatFormatting.BOLD)
                                    .withStyle(ChatFormatting.BLUE), true);
                        } else {
                            entity.remove(Entity.RemovalReason.DISCARDED);
                            String entityName = entity.getType().getDescription().getString();
                            player.sendSystemMessage(Component.literal("You need " +
                                    (mob.getMaxHealth() * BeyonderUtil.getDamage(player).get(ItemInit.ENVISION_LIFE.get()) - holder.getSpirituality()) +
                                    " more spirituality in order to envision " + entityName));
                        }
                    }
                } else {
                    player.sendSystemMessage(Component.literal("Entity doesn't exist").withStyle(ChatFormatting.RED));
                }
            } else {
                player.sendSystemMessage(Component.literal("Ability on Cooldown for " + (400 - cooldownTimer) / 20 + " seconds"));
            }
        }
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SPECTATOR_ABILITY", ChatFormatting.AQUA);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        int projectileSize = 0;
        for (Projectile projectile : livingEntity.level().getEntitiesOfClass(Projectile.class, livingEntity.getBoundingBox().inflate(BeyonderUtil.getDamage(livingEntity).get(this)))) {
            projectileSize += (int) BeyonderUtil.getScale(projectile);
            if (projectileSize >= 100) {
                projectileSize = 100;
                break;
            }
        }
        return projectileSize;
    }
}