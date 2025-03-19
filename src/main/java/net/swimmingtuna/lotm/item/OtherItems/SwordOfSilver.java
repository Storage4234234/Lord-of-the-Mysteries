package net.swimmingtuna.lotm.item.OtherItems;

import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.swimmingtuna.lotm.entity.SilverLightEntity;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.item.Renderer.SwordOfSilverRenderer;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

public class SwordOfSilver extends SwordItem implements GeoItem {


    public SwordOfSilver(Tier pTier, int pAttackDamageModifier, float pAttackSpeedModifier, Properties pProperties) {
        super(pTier, pAttackDamageModifier, pAttackSpeedModifier, pProperties);
    }


    @Override
    public boolean hurtEnemy(ItemStack pStack, LivingEntity pTarget, LivingEntity pAttacker) {
        if (BeyonderUtil.isPurifiable(pTarget)) {
            pTarget.hurt(BeyonderUtil.magicSource(pAttacker), this.getDamage());
            pTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2, true, true));
            pTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 2, true, true));
        }
        return super.hurtEnemy(pStack, pTarget, pAttacker);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        ItemStack itemstack = pPlayer.getItemInHand(pUsedHand);
        if (!pPlayer.level().isClientSide()) {
            pPlayer.startUsingItem(pUsedHand);
        }
        return InteractionResultHolder.consume(itemstack);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("A sword made out of mercury, you can hold down right click in order to throw it like a spear.").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal("On hit, if the creature is evil, it will deal additional damage, slow, and weaken them.").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SILVER_ITEM", ChatFormatting.GRAY);
    }

    @Override
    public void releaseUsing(ItemStack pStack, Level pLevel, LivingEntity livingEntity, int pTimeCharged) {
        int i = this.getUseDuration(pStack) - pTimeCharged;
        float powerScale = getPowerForTime(i);
        if (!pLevel.isClientSide) {
            SilverLightEntity silverLight = new SilverLightEntity(EntityInit.SILVER_LIGHT_ENTITY.get(), pLevel);
            silverLight.setShouldTeleport(false);
            Vec3 lookVec = livingEntity.getLookAngle().normalize().scale(10);
            silverLight.setDeltaMovement(lookVec);
            silverLight.setOwner(livingEntity);
            silverLight.hurtMarked = true;
            silverLight.teleportTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
            BeyonderUtil.setScale(silverLight, 6);
            livingEntity.level().addFreshEntity(silverLight);
            removeItemFromSlot(livingEntity, pStack);
        }
    }

    private void removeItemFromSlot(LivingEntity entity, ItemStack stack) {
        if (entity.getItemBySlot(EquipmentSlot.MAINHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        } else if (entity.getItemBySlot(EquipmentSlot.OFFHAND) == stack) {
            entity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public int getUseDuration(ItemStack pStack) {
        return 72000;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.SPEAR;
    }

    private float getPowerForTime(int pCharge) {
        float f = (float) pCharge / 20.0F;
        f = (f * f + f * 2.0F) / 3.0F;
        if (f > 1.0F) {
            f = 1.0F;
        }
        return f;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "Activation", 0, state -> PlayState.STOP));
    }

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            private SwordOfSilverRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (this.renderer == null)
                    this.renderer = new SwordOfSilverRenderer();

                return this.renderer;
            }
        });
    }

}
