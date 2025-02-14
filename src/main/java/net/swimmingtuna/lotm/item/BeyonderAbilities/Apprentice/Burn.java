package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;
import virtuoel.pehkui.api.ScaleTypes;

import javax.annotation.Nullable;
import java.util.List;

public class Burn extends SimpleAbilityItem {


    public Burn(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 8, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        burn(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    public static void burn(LivingEntity livingEntity) {
        if (!livingEntity.level().isClientSide()) {
            for (Projectile projectile : livingEntity.level().getEntitiesOfClass(Projectile.class, livingEntity.getBoundingBox().inflate(200 - (BeyonderUtil.getSequence(livingEntity) * 20)))) {
                if (projectile.getOwner() != null && projectile.getOwner() instanceof LivingEntity owner) {
                    if (owner == livingEntity || BeyonderUtil.isAllyOf(livingEntity,owner)) {
                        float scale = ScaleTypes.BASE.getScaleData(projectile).getScale();
                        for (LivingEntity living : projectile.level().getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(scale * 5))) {
                            if (living != livingEntity && !BeyonderUtil.isAllyOf(livingEntity,living)) {
                                Explosion explosion = new Explosion(living.level(), null, projectile.getX(), projectile.getY(), projectile.getZ(), BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKBURN.get()) * Math.max(1, scale / 2), true, Explosion.BlockInteraction.DESTROY);
                                explosion.explode();
                                explosion.finalizeExplosion(true);
                                projectile.discard();
                            }
                        }
                        if (projectile != null) {
                            projectile.setSecondsOnFire((int) (float) BeyonderUtil.getDamage(livingEntity).get(ItemInit.TRICKBURN.get()));
                        }
                    }
                }
            }
        }
    }

    public static void smeltItem(LivingEvent.LivingTickEvent event) {
        LivingEntity player = event.getEntity();
        Level level = player.level();
        if (!player.level().isClientSide() && player.isShiftKeyDown() && player.tickCount % 20 == 0 && !player.getOffhandItem().isEmpty() && BeyonderUtil.getPathway(player) == BeyonderClassInit.APPRENTICE.get() && BeyonderUtil.getSequence(player) <= 8 && player.getMainHandItem().getItem() == ItemInit.TRICKBURN.get()) {
            int sequence = BeyonderUtil.getSequence(player);
            int smelt;
            ItemStack offHand = player.getOffhandItem();
            int amount = offHand.getCount();
            if(sequence == 8){
                smelt = 1;
            }else if(sequence == 7){
                smelt = 4;
            }else if(sequence == 6){
                smelt = 16;
            }else {
                smelt = 64;
            }
            if(BeyonderUtil.isSmeltable(offHand, level)) {
                if(offHand.getCount() <= smelt){
                    ItemStack result = BeyonderUtil.getSmeltingResult(offHand, level);
                    result.setCount(amount);
                    player.setItemInHand(InteractionHand.OFF_HAND, result);
                } else {
                    if (player instanceof Player pPlayer) {
                        pPlayer.displayClientMessage(Component.literal("Can only smelt up to ").append(Component.literal(String.valueOf(smelt)).append(Component.literal(" items."))), true);
                    }
                }
            }else {
                if (player instanceof Player pPlayer) {
                    pPlayer.displayClientMessage(Component.literal("Not smeltable"), true);
                }
            }
        }
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, can all projectiles owned by you or ally's to be set on fire, and all those projectiles near enemies will explode."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("0").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.BLUE);
    }
}