package net.swimmingtuna.lotm.item.BeyonderAbilities.Warrior.FinishedItems;


import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;

public class AuraOfTwilight extends SimpleAbilityItem {


    public AuraOfTwilight(Properties properties) {
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
            CompoundTag tag = livingEntity.getPersistentData();
            boolean auraOfGlory = tag.getBoolean("auraOfTwilight");
            tag.putBoolean("auraOfGlory", !tag.getBoolean("auraOfTwilight"));
            if (livingEntity instanceof Player player) {
                player.displayClientMessage(Component.literal("Aura of Twilight Turned " + (auraOfGlory ? "Off" : "On")).withStyle(ChatFormatting.BOLD, ChatFormatting.YELLOW), true);
            }
        }
    }
}

