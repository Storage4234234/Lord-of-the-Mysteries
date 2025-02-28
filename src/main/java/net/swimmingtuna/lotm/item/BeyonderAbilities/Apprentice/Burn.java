package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.SendParticleS2C;
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
                if (projectile.getOwner() != null && projectile.getOwner() instanceof LivingEntity) {
                    float scale = ScaleTypes.BASE.getScaleData(projectile).getScale();
                    int minDistanceToAlly = Integer.MAX_VALUE;
                    for (LivingEntity living : projectile.level().getEntitiesOfClass(LivingEntity.class, projectile.getBoundingBox().inflate(100))) {
                        if (living == livingEntity || BeyonderUtil.isAllyOf(livingEntity, living)) {
                            int currentDistance = (int) projectile.distanceTo(living);
                            minDistanceToAlly = Math.min(minDistanceToAlly, currentDistance);
                        }
                    }
                    int sequence = BeyonderUtil.getSequence(livingEntity);
                    double projectileSize = projectile.getBoundingBox().getSize();
                    double chanceToExplode = livingEntity.getBoundingBox().getSize() * (9 - sequence);
                    double particleAmount = (int) projectileSize * 5;
                    double randomAmount = (Math.random() * projectileSize) - ((Math.random() * projectileSize) * 2);
                    for (int i = 0 ; i <= particleAmount; i++) {
                        if (livingEntity instanceof ServerPlayer player) {
                            LOTMNetworkHandler.sendToPlayer(new SendParticleS2C(ParticleTypes.EXPLOSION, projectile.getX() - randomAmount, projectile.getY() - randomAmount, projectile.getZ() - randomAmount, 0,0,0), player);
                        }
                    }
                    if (chanceToExplode * Math.max(0.5,Math.random()) > projectileSize) {
                        Explosion explosion = new Explosion(livingEntity.level(), null, projectile.getX(), projectile.getY(), projectile.getZ(), (float) (int) Math.max(1, projectileSize / 2), true, Explosion.BlockInteraction.DESTROY);
                        explosion.explode();
                        explosion.finalizeExplosion(true);
                        projectile.discard();
                    } else {
                        projectile.setSecondsOnFire(10);
                    }
                }
            }
        }
    }

    public static void smeltItem(LivingEvent.LivingTickEvent event) {
        LivingEntity player = event.getEntity();
        Level level = player.level();
        if (!player.level().isClientSide() && player.isShiftKeyDown() && player.tickCount % 20 == 0 && !player.getOffhandItem().isEmpty() && BeyonderUtil.currentPathwayAndSequenceMatches(player, BeyonderClassInit.APPRENTICE.get(), 8) && player.getMainHandItem().getItem() == ItemInit.TRICKBURN.get()) {
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
        tooltipComponents.add(Component.literal("Upon use, cause all projectile's not near your allies to have a chance to explode based on their size and your sequence. If they don't explode, they'll be set on fire."));
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