package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.entity.DivineHandLeftEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;

public class DivineHandLeft extends SimpleAbilityItem {


    public DivineHandLeft(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        divineHandLeft(player);
        return InteractionResult.SUCCESS;
    }

    public static void divineHandLeft(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            DivineHandLeftEntity divineHandLeft = new DivineHandLeftEntity(EntityInit.DIVINE_HAND_LEFT_ENTITY.get(), livingEntity.level());
            divineHandLeft.setDeltaMovement(livingEntity.getLookAngle().scale(3));
            BeyonderUtil.setScale(divineHandLeft, BeyonderUtil.getDamage(livingEntity).get(ItemInit.DIVINEHANDLEFT.get()));
            Vec3 scale = livingEntity.getLookAngle().scale(5.0f);
            divineHandLeft.teleportTo(livingEntity.getX() + scale.x, livingEntity.getY(), livingEntity.getZ() + scale.z);
            divineHandLeft.hurtMarked = true;
            divineHandLeft.setYaw(livingEntity.getYRot());
            divineHandLeft.setPitch(livingEntity.getXRot());
            livingEntity.level().addFreshEntity(divineHandLeft);
        }
    }
}

