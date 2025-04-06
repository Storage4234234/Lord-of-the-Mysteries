package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


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
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.MercuryPortalEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class SilverRapier extends SimpleAbilityItem {


    public SilverRapier(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 3, 900, 600);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        silverRapier(player);
        return InteractionResult.SUCCESS;
    }

    public static void silverRapier(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {

            livingEntity.getPersistentData().putInt("silverRapierSummoning", (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.SILVERRAPIER.get()));
        }
    }

    public static void mercuryTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int x = tag.getInt("silverRapierSummoning");
        if (!livingEntity.level().isClientSide() && x >= 1) {
            tag.putInt("silverRapierSummoning", x - 1);
            MercuryPortalEntity mercuryPortal = new MercuryPortalEntity(EntityInit.MERCURY_PORTAL_ENTITY.get(), livingEntity.level());
            BeyonderUtil.setScale(mercuryPortal, 3);
            float yaw = livingEntity.getYRot() * (float) (Math.PI / 90.0);
            double sideOffset = (2 + Math.random() * 8);
            double offsetX, offsetZ;
            offsetX = -Math.sin(yaw) * sideOffset;
            offsetZ = Math.cos(yaw) * sideOffset;
            if (Math.random() < 0.5) {
                offsetX = -offsetX;
                offsetZ = -offsetZ;
            }
            double offsetY = (Math.random() * 20) - 10;
            mercuryPortal.teleportTo(livingEntity.getX() + offsetX, livingEntity.getY() + offsetY, livingEntity.getZ() + offsetZ);
            mercuryPortal.getPersistentData().putUUID("mercuryPortalOwner", livingEntity.getUUID());
            livingEntity.level().addFreshEntity(mercuryPortal);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, conjure portals of mercury around you, that will quickly transform into mercury spears that move at the speed of light. If these miss, they will teleport randomly to whatever entity you're looking at, aiming at them once more."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("900").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("30 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("WARRIOR_ABILITY", ChatFormatting.YELLOW);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target != null) {
            return 90;
        }
        return 0;
    }
}

