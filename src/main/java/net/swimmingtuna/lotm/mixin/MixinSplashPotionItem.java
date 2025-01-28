package net.swimmingtuna.lotm.mixin;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SplashPotionItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SplashPotionItem.class)
public abstract class MixinSplashPotionItem {
    @Inject(
            method = "use",
            at = @At("RETURN")
    )
    private void onUse(Level world, Player user, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> info) {
        user.getCooldowns().addCooldown(info.getReturnValue().getObject().getItem(), 20);
    }
}