package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior;


import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class TwilightManifestation extends SimpleAbilityItem {


    public TwilightManifestation(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
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
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(livingEntity);
            scaleData.setTargetScale(3.0f);
            }
        }
    }

