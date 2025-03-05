package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior;


import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.entity.MercuryPortalEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;

public class SilverRapier extends SimpleAbilityItem {


    public SilverRapier(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 6, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        silverRapier(player);
        return InteractionResult.SUCCESS;
    }

    public static void silverRapier(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {

            livingEntity.getPersistentData().putInt("silverRapierSummoning", (int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.SILVERRAPIER.get()));
        }
    }

    public static void mercuryTick(LivingEvent.LivingTickEvent event) {
        LivingEntity livingEntity = event.getEntity();
        CompoundTag tag = livingEntity.getPersistentData();
        int x = tag.getInt("silverRapierSummoning");
        if (!livingEntity.level().isClientSide() && x >= 1) {
            tag.putInt("silverRapierSummoning", x - 1);
            MercuryPortalEntity mercuryPortal = new MercuryPortalEntity(EntityInit.MERCURY_PORTAL_ENTITY.get(), livingEntity.level());
            BeyonderUtil.setScale(mercuryPortal, 3);
            float yaw = livingEntity.getYRot() * (float) (Math.PI / 90.0);
            double sideOffset = (2 + Math.random() * 8);
            double offsetX, offsetZ;
            offsetX = -Math.sin(yaw) * sideOffset;
            offsetZ = Math.cos(yaw) * sideOffset;
            if (Math.random() < 0.5) {
                offsetX = -offsetX;
                offsetZ = -offsetZ;
            }
            double offsetY = (Math.random() * 20) - 10;
            mercuryPortal.teleportTo(livingEntity.getX() + offsetX, livingEntity.getY() + offsetY, livingEntity.getZ() + offsetZ);
            mercuryPortal.getPersistentData().putUUID("mercuryPortalOwner", livingEntity.getUUID());
            livingEntity.level().addFreshEntity(mercuryPortal);
        }
    }
}

