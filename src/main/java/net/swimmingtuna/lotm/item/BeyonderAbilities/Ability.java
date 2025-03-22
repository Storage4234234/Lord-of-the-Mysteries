package net.swimmingtuna.lotm.item.BeyonderAbilities;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public interface Ability {

    default InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        System.out.println("useAbility");
        return InteractionResult.PASS;
    }

    default InteractionResult useAbilityOnBlock(UseOnContext context) {
        System.out.println("useAbilityOnBlock");
        return InteractionResult.PASS;
    }


    default InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity user, LivingEntity interactionTarget, InteractionHand usedHand) {
        System.out.println("useAbilityOnEntity");
        return InteractionResult.PASS;
    }




    default double getBlockReach() {
        return 4.5;
    }
    default double getEntityReach() {
        return 3.0f;
    }
    default float getDamage() {
        return 0.0f;
    }
}
