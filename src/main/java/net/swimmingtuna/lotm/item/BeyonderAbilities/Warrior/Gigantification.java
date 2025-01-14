package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

public class Gigantification extends SimpleAbilityItem {


    public Gigantification(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        gigantification(player);
        return InteractionResult.SUCCESS;
    }

    public static void gigantification(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            CompoundTag tag = livingEntity.getPersistentData();
            boolean isGiant = tag.getBoolean("warriorGiant");
            boolean isHoGGiant = tag.getBoolean("handOfGodGiant");
            boolean isTwilightGiant = tag.getBoolean("twilightGiant");
            ScaleData scaleData = ScaleTypes.BASE.getScaleData(livingEntity);
            int sequence = BeyonderUtil.getSequence(livingEntity);
            float scaleToSet = BeyonderUtil.getDamage(livingEntity).get(ItemInit.GIGANTIFICATION.get());
            if (isGiant || isHoGGiant || isTwilightGiant) {
                scaleData.setTargetScale(1.0f);
                tag.putBoolean("warriorGiant", false);
                tag.putBoolean("handOfGodGiant", false);
                tag.putBoolean("twilightGiant", false);
            }
            else if (sequence <= 6 && sequence >= 2) {
                tag.putBoolean("warriorGiant", true);
                scaleData.setTargetScale(scaleToSet);
            } else if (sequence == 1) {
                tag.putBoolean("handOfGodGiant", true);
                scaleData.setTargetScale(scaleToSet * 2);
            } else if (sequence == 0) {
                tag.putBoolean("twilightGiant", true);
                scaleData.setTargetScale(scaleToSet * 3);
            }
        }
    }
}

