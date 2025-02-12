package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.entity.DragonBreathEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;

public class BeamOfGlory extends SimpleAbilityItem {


    public BeamOfGlory(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        beamOfGlory(player);
        return InteractionResult.SUCCESS;
    }

    public static void beamOfGlory(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            DragonBreathEntity dragonBreath = new DragonBreathEntity(livingEntity, 1);
            dragonBreath.teleportTo(livingEntity.getX(),livingEntity.getY()+1 ,livingEntity.getZ());
            dragonBreath.setIsDragonbreath(false);
            dragonBreath.setSize(6);
            dragonBreath.setRange(200);
            dragonBreath.setDestroyBlocks(false);
            dragonBreath.setIsTwilight(true);
            dragonBreath.setCharge(5);
            dragonBreath.setDuration(10);
            dragonBreath.setCausesFire(false);
            livingEntity.level().addFreshEntity(dragonBreath);
            }
        }
    }

