package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.entity.DivineHandRightEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;

public class DivineHandRight extends SimpleAbilityItem {


    public DivineHandRight(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        divineHandRight(player);
        return InteractionResult.SUCCESS;
    }

    public static void divineHandRight(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            DivineHandRightEntity divineHandRight = new DivineHandRightEntity(EntityInit.DIVINE_HAND_RIGHT_ENTITY.get(), livingEntity.level());
            divineHandRight.setDeltaMovement(livingEntity.getLookAngle().scale(3));
            BeyonderUtil.setScale(divineHandRight, BeyonderUtil.getDamage(livingEntity).get(ItemInit.DIVINEHANDLEFT.get()));
            Vec3 scale = livingEntity.getLookAngle().scale(5.0f);
            divineHandRight.teleportTo(livingEntity.getX() + scale.x, livingEntity.getY(), livingEntity.getZ() + scale.z);
            divineHandRight.hurtMarked = true;
            divineHandRight.setOwner(livingEntity);
            divineHandRight.setYaw(livingEntity.getYRot());
            divineHandRight.setPitch(livingEntity.getXRot());
            livingEntity.level().addFreshEntity(divineHandRight);
        }
    }
}

