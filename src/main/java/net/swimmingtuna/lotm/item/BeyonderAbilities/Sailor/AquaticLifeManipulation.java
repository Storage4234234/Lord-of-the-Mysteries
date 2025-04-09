package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;

public class AquaticLifeManipulation extends SimpleAbilityItem {

    public AquaticLifeManipulation(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 3, 125, 200);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        aquaticLifeManipulation(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    public void aquaticLifeManipulation(LivingEntity player) {
        if (!player.level().isClientSide()) {
            int sequence = BeyonderUtil.getSequence(player);
            if (player.level().isClientSide()) {
                return;
            }
            List<LivingEntity> aquaticEntities = player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(BeyonderUtil.getDamage(player).get(ItemInit.AQUATIC_LIFE_MANIPULATION.get())), entity -> entity instanceof WaterAnimal);
            if (aquaticEntities.isEmpty()) {
                return;
            }
            LivingEntity nearestAquaticEntity = aquaticEntities.stream().min(Comparator.comparingDouble(player::distanceTo)).orElse(null);
            List<Player> nearbyPlayers = nearestAquaticEntity.level().getEntitiesOfClass(Player.class, nearestAquaticEntity.getBoundingBox().inflate(200 - (sequence * 20)));
            Player nearestPlayer = nearbyPlayers.stream().filter(nearbyPlayer -> nearbyPlayer != player).min(Comparator.comparingDouble(nearestAquaticEntity::distanceTo)).orElse(null);
            if (nearestPlayer == null) {
                return;
            }
            if (player instanceof Player) {
                if (sequence >= 2) {
                    player.sendSystemMessage(Component.literal("Nearest Player is " + nearestPlayer.getName().getString() + ". Pathway is " + BeyonderUtil.getPathway(nearestPlayer)).withStyle(BeyonderUtil.getStyle(player)));
                } else {
                    player.sendSystemMessage(Component.literal("Nearest Player is " + nearestPlayer.getName().getString() + ". Pathway is " + BeyonderUtil.getPathway(nearestPlayer) + ". Sequence is" + BeyonderUtil.getSequence(nearestPlayer)).withStyle(BeyonderUtil.getStyle(player)));
                }
            } else if (player instanceof Mob mob && !BeyonderUtil.areAllies(nearestPlayer, mob)) {
                mob.setTarget(nearestPlayer);
            }
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, communicates with all aquatic life near you to get information about the nearest player and their pathway."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("125").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("10 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        if (target == null && livingEntity.getHealth() >= 30) {
            return 20;
        }
        return 0;
    }
}
