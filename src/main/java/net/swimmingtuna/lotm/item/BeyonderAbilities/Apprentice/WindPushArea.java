package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class WindPushArea extends SimpleAbilityItem {


    public WindPushArea(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 8, 50, 200);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        push(player, level);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useAbilityOnEntity(ItemStack stack, LivingEntity player, LivingEntity interactionTarget, InteractionHand hand) {
        if(!player.level().isClientSide && !interactionTarget.level().isClientSide){
            if (!checkAll(player)) {
                return InteractionResult.FAIL;
            }
            pushTarget(player, interactionTarget);
            addCooldown(player);
            useSpirituality(player);
        }
        return InteractionResult.SUCCESS;
    }

    public static void push(LivingEntity player, Level level){
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(BeyonderUtil.getDamage(player).get(ItemInit.TRICKWINDPUSH.get())))){
            Vec3 playerPos = player.position();
            Vec3 entityPos = entity.position();
            Vec3 direction = playerPos.subtract(entityPos).normalize();
            entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(-3)));
            entity.hurtMarked = true;
        }
    }

    public static void pushTarget(LivingEntity player, LivingEntity entity) {
        Vec3 playerPos = player.position();
        Vec3 entityPos = entity.position();
        Vec3 direction = playerPos.subtract(entityPos).normalize();
        entity.setDeltaMovement(entity.getDeltaMovement().add(direction.scale(-3)));
        entity.hurtMarked = true;
    }



    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, creates wind that push all entities in a radius away from you. Can be targeted for the effect to only work on a single entity."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("50").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("10 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }
    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("APPRENTICE_ABILITY", ChatFormatting.BLUE);
    }
}