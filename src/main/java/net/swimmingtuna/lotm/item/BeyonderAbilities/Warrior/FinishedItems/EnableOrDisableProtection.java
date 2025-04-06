package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class EnableOrDisableProtection extends SimpleAbilityItem {


    public EnableOrDisableProtection(Properties properties) {
        super(properties, BeyonderClassInit.WARRIOR, 5, 0, 20);
    }

    @Override
    public InteractionResult useAbility(Level level, LivingEntity player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        enableOrDisableLightning(player);
        addCooldown(player);
        useSpirituality(player);
        return InteractionResult.SUCCESS;
    }

    private void enableOrDisableLightning(LivingEntity player) {
        if (!player.level().isClientSide()) {
            CompoundTag tag = player.getPersistentData();
            boolean protection = tag.getBoolean("warriorProtection");
            tag.putBoolean("warriorProtection", !protection);
            if (player instanceof Player pPlayer) {
                pPlayer.displayClientMessage(Component.literal("Protection effect turned " + (protection ? "off" : "on")).withStyle(ChatFormatting.BLUE).withStyle(ChatFormatting.BOLD), true);
            }
        }
    }


    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, disable or enable the protection of all your allies. If enabled, you will take half the damage of all allies around you onto yourself."));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("0").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("1 Second").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public @NotNull Rarity getRarity(ItemStack pStack) {
        return Rarity.create("WARRIOR_ABILITY", ChatFormatting.YELLOW);
    }

    public static void warriorProtectionTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide()) {
            CompoundTag tag = entity.getPersistentData();
            if (tag.getBoolean("warriorProtection")) {
                int sequence = BeyonderUtil.getSequence(entity);
                for (LivingEntity livingEntity : entity.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(300 - (sequence * 45)))) {
                    if (BeyonderUtil.areAllies(entity, livingEntity) && livingEntity != entity) {
                        livingEntity.getPersistentData().putInt("guardianProtectionTimer", 10);
                        livingEntity.getPersistentData().putUUID("guardianProtection", entity.getUUID());
                    }
                }
            }
        }
    }

    @Override
    public int getPriority(LivingEntity livingEntity, LivingEntity target) {
        return 0;
    }

}