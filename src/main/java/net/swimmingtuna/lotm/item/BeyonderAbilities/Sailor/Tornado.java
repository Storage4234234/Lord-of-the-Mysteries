package net.swimmingtuna.lotm.item.BeyonderAbilities.Sailor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.entity.TornadoEntity;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class Tornado extends SimpleAbilityItem {

    public Tornado(Properties properties) {
        super(properties, BeyonderClassInit.SAILOR, 4, 500, 200); //spirituality use change
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if (!checkAll(player)) {
            return InteractionResult.FAIL;
        }
        addCooldown(player);
        useSpirituality(player);
        tornado(player);
        return InteractionResult.SUCCESS;
    }

    private static void tornado(Player pPlayer) {
        if (!pPlayer.level().isClientSide()) {
            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(pPlayer);
            summonTornado(pPlayer, holder.getCurrentSequence());
            holder.useSpirituality(500);
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, summons a tornado that moves in the direction you're looking. If used at the highest sequence, it deals mental damage instead of physical"));
        tooltipComponents.add(Component.literal("Spirituality Used: ").append(Component.literal("500").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(Component.literal("Cooldown: ").append(Component.literal("10 Seconds").withStyle(ChatFormatting.YELLOW)));
        tooltipComponents.add(SimpleAbilityItem.getPathwayText(this.requiredClass.get()));
        tooltipComponents.add(SimpleAbilityItem.getClassText(this.requiredSequence, this.requiredClass.get()));
        super.baseHoverText(stack, level, tooltipComponents, tooltipFlag);
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.create("SAILOR_ABILITY", ChatFormatting.BLUE);
    }

    public static void summonTornado(Player player, int sequence) {
        if (!player.level().isClientSide()) {
            TornadoEntity tornado = new TornadoEntity(player.level(), player, 0, 0, 0);
            tornado.setTornadoHeight((int) (float) BeyonderUtil.getDamage(player).get(ItemInit.TORNADO.get()));
            tornado.setTornadoRadius((int) (float) BeyonderUtil.getDamage(player).get(ItemInit.TORNADO.get()) / 4);
            if (sequence <= 0) {
                tornado.setTornadoLightning(true);
                tornado.setTornadoLifecount(400);
            } else {
                tornado.setTornadoLifecount(200);
            }
            tornado.setTornadoMov(player.getLookAngle().scale(0.5f).toVector3f());
            player.level().addFreshEntity(tornado);
        }
    }
}
