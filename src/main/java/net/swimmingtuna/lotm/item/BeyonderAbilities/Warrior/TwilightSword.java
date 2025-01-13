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

public class TwilightSword extends SimpleAbilityItem {


    public TwilightSword(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        twilightSword(player);
        return InteractionResult.SUCCESS;
    }

    public static void twilightSword(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(livingEntity);
            scaleData.setTargetScale(3.0f);
            }
        }
    }

