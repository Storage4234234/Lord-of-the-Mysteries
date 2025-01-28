package net.swimmingtuna.lotm.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public abstract class MixinPotionItem {
    @Inject(
            method = "finishUsingItem",
            cancellable = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z",
                    shift = At.Shift.BEFORE
            )
    )
    public void finishUsing(ItemStack stack, Level world, LivingEntity user, CallbackInfoReturnable<ItemStack> cir) {
        if (user instanceof Player) {
            ((Player)user).getInventory().add(Items.GLASS_BOTTLE.getDefaultInstance());
        }

        cir.setReturnValue(stack);
    }
}
