package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.entity.DragonBreathEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;

public class BeamOfTwilight extends SimpleAbilityItem {


    public BeamOfTwilight(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        beamOfTwilight(player);
        return InteractionResult.SUCCESS;
    }

    public static void beamOfTwilight(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            DragonBreathEntity beamOfTwilight = new DragonBreathEntity(livingEntity, 1);
            beamOfTwilight.teleportTo(livingEntity.getX(),livingEntity.getY()+1 ,livingEntity.getZ());
            beamOfTwilight.setIsDragonbreath(false);
            beamOfTwilight.setSize(10);
            beamOfTwilight.setRange(500);
            beamOfTwilight.setDestroyBlocks(false);
            beamOfTwilight.setIsTwilight(true);
            beamOfTwilight.setCharge(5);
            beamOfTwilight.setDuration(10);
            beamOfTwilight.setCausesFire(false);
            beamOfTwilight.setFrenzyTime(0);
            livingEntity.level().addFreshEntity(beamOfTwilight);
        }
    }
}

