package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.effect.ModEffects;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class TwilightManifestation extends SimpleAbilityItem {


    public TwilightManifestation(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 1, 2500, 1200);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        auraOfTwilight(player);
        return InteractionResult.SUCCESS;
    }

    public static void auraOfTwilight(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            CompoundTag tag = livingEntity.getPersistentData();
            tag.putInt("twilightManifestation", (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TWILIGHTMANIFESTATION.get()));
            tag.putInt("twilightManifestationSaveMovement", 1);
        }
    }

    public static void twilightManifestationTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int x = tag.getInt("twilightManifestation");
        int y = tag.getInt("twilightManifestationSaveMovement");
        if (!livingEntity.level().isClientSide() && x >= 1) {
            tag.putInt("twilightManifestation", x - 1);
            for (LivingEntity living : livingEntity.level().getEntitiesOfClass(LivingEntity.class, livingEntity.getBoundingBox().inflate(200))) {
                if (y == 1) {
                    tag.putInt("twilightManifestationSaveMovement", 0);
                    living.getPersistentData().putDouble("twilightManifestationX", living.getDeltaMovement().x());
                    living.getPersistentData().putDouble("twilightManifestationY", living.getDeltaMovement().y());
                    living.getPersistentData().putDouble("twilightManifestationZ", living.getDeltaMovement().z());
                    living.getPersistentData().putInt("twilightManifestationTimer", 200);
                } else {
                    if (living != livingEntity && !BeyonderUtil.isAllyOf(livingEntity, living)) {
                        living.addEffect(new MobEffectInstance(ModEffects.STUN.get(), 10, 1, false, false));
                    }
                }
            }
            for (Projectile projectile : livingEntity.level().getEntitiesOfClass(Projectile.class, livingEntity.getBoundingBox().inflate(200))) {
                if (y == 1) {
                    tag.putInt("twilightManifestationSaveMovement", 0);
                    projectile.getPersistentData().putDouble("twilightManifestationX", livingEntity.getDeltaMovement().x());
                    projectile.getPersistentData().putDouble("twilightManifestationY", livingEntity.getDeltaMovement().y());
                    projectile.getPersistentData().putDouble("twilightManifestationZ", livingEntity.getDeltaMovement().z());
                } else {
                    if (projectile.getOwner() != null && projectile.getOwner() instanceof LivingEntity owner && owner != livingEntity && !BeyonderUtil.isAllyOf(livingEntity, owner)) {
                        projectile.setDeltaMovement(0, 0, 0);
                        projectile.teleportTo(projectile.getX(), projectile.getY(), projectile.getZ());
                    }
                }
            }
            if (x == 1) {
                tag.putInt("twilightManifestation", 0);
                for (Entity entity : livingEntity.level().getEntitiesOfClass(Entity.class, livingEntity.getBoundingBox().inflate(200))) {
                    double movementX = entity.getPersistentData().getDouble("twilightManifestationX");
                    double movementY = entity.getPersistentData().getDouble("twilightManifestationY");
                    double movementZ = entity.getPersistentData().getDouble("twilightManifestationZ");
                    Vec3 movement = new Vec3(movementX, movementY, movementZ);
                    if (movementX != 0 || movementY != 0 || movementZ != 0) {
                        entity.setDeltaMovement(movement);
                        entity.getPersistentData().putDouble("twilightManifestationX", 0);
                        entity.getPersistentData().putDouble("twilightManifestationY", 0);
                        entity.getPersistentData().putDouble("twilightManifestationZ", 0);
                        entity.getPersistentData().putInt("twilightManifestationTimer", 200);
                    }
                }
            }
        }
        if (!livingEntity.level().isClientSide()) {
            double movementX = livingEntity.getPersistentData().getDouble("twilightManifestationX");
            double movementY = livingEntity.getPersistentData().getDouble("twilightManifestationY");
            double movementZ = livingEntity.getPersistentData().getDouble("twilightManifestationZ");
            if (movementX != 0 || movementY != 0 || movementZ != 0) {
                if (livingEntity.invulnerableTime > 5) {
                    livingEntity.invulnerableTime = 5;
                }
                if (livingEntity.hurtTime > 5) {
                    livingEntity.hurtTime = 5;
                }
                livingEntity.hurtMarked = false;
                event.setCanceled(true);
            }
            if (livingEntity.getPersistentData().getInt("unableToUseAbility") >= 1 && livingEntity.tickCount % 10 == 0) {
                livingEntity.getPersistentData().putInt("unableToUseAbility", 0);
            }
        }
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, use the authority of twilight to freeze all around you in place, leaving them unable to do anything temporarily."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("2500").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Minute").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("WARRIOR_ABILITY", ChatFormatting.YELLOW);
    }


}

