package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior;


import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.MercuryCageEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import virtuoel.pehkui.api.ScaleTypes;

public class MercuryCage extends SimpleAbilityItem {


    public MercuryCage(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        mercuryCage(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack pStack, Player player, LivingEntity pInteractionTarget, InteractionHand pUsedHand) {
        if (!checkAll(player, BeyonderClassInit.WARRIOR.get(), 2, (int) ScaleTypes.BASE.getScaleData(pInteractionTarget).getScale() * 100, false)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player, (int) (Math.max(pInteractionTarget.getBbHeight(), pInteractionTarget.getBbWidth()) * 100));
        mercuryCageTarget(player, pInteractionTarget);
        return InteractionResult.SUCCESS;
    }

    public static void mercuryCage(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            MercuryCageEntity mercuryCage = new MercuryCageEntity(EntityInit.MERCURY_CAGE_ENTITY.get(), livingEntity.level());
            mercuryCage.teleportTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
            mercuryCage.getPersistentData().putUUID("cageOwnerUUID", livingEntity.getUUID());
            mercuryCage.setLife((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.MERCURYCAGE.get()));
            BeyonderUtil.setScale(mercuryCage, 30);
            livingEntity.level().addFreshEntity(mercuryCage);
        }
    }

    public static void mercuryCageTarget(LivingEntity livingEntity, LivingEntity target) {
        if (!livingEntity.level().isClientSide()) {
            MercuryCageEntity mercuryCage = new MercuryCageEntity(EntityInit.MERCURY_CAGE_ENTITY.get(), livingEntity.level());
            mercuryCage.teleportTo(target.getX(), target.getY(), target.getZ());
            mercuryCage.getPersistentData().putUUID("cageOwnerUUID", livingEntity.getUUID());
            mercuryCage.setLife((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.MERCURYCAGE.get()));
            BeyonderUtil.setScale(mercuryCage, Math.max(target.getBbHeight(), target.getBbWidth()));
            livingEntity.level().addFreshEntity(mercuryCage);
        }
    }
}

