package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior;


import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.entity.DivineHandRightEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;

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
            divineHandRight.hurtMarked = true;
            livingEntity.level().addFreshEntity(divineHandRight);
            }
        }
    }

