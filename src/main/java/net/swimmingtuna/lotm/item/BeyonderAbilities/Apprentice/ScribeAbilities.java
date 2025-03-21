package net.swimmingtuna.lotm.item.BeyonderAbilities.Apprentice;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.SimpleAbilityItem;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ScribeRecording.CapabilityScribeAbilities;
import net.swimmingtuna.lotm.util.ScribeRecording.ScribeMenu;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;

public class ScribeAbilities extends SimpleAbilityItem {


    public ScribeAbilities(Properties properties) {
        super(properties, BeyonderClassInit.APPRENTICE, 6, 0, 0);
    }

    @Override
    public InteractionResult useAbility(Level level, Player player, InteractionHand hand) {
        if(!checkAll(player)){
            return InteractionResult.FAIL;
        }
        openOrCheck(player);
        return InteractionResult.SUCCESS;
    }

    public static void openOrCheck(Player player){
        if(player.getOffhandItem().getItem() instanceof SimpleAbilityItem){
            if(!player.isShiftKeyDown()){
                checkRemainingUses(player);
            }else{
                deleteAbility(player);
            }
        }else{
            openMenu(player);
        }
    }

    public static void checkRemainingUses(Player player){
        Item offHand = player.getOffhandItem().getItem();
        if(offHand instanceof SimpleAbilityItem ability){
            player.getCapability(CapabilityScribeAbilities.SCRIBE_CAPABILITY, null).ifPresent(storage -> {
                if(storage.hasScribedAbility(ability)) {
                    player.displayClientMessage(Component.literal("Scribed copies: ").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN).append(Component.literal(String.valueOf(storage.getRemainUses(ability))).withStyle(ChatFormatting.WHITE).withStyle(ChatFormatting.BOLD)), true);
                } else {
                    player.displayClientMessage(Component.literal("Haven`t scribed this ability yet.").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.WHITE), true);
                }
            });
        }
    }

    public static void deleteAbility(Player player){
        Item offHand = player.getOffhandItem().getItem();
        if(offHand instanceof SimpleAbilityItem ability){
            player.getCapability(CapabilityScribeAbilities.SCRIBE_CAPABILITY, null).ifPresent(storage -> {
                if(storage.hasScribedAbility(ability)){
                    storage.useScribeAbility(ability);
                    player.displayClientMessage(Component.literal("1 copy deleted.").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.GREEN), true);
                }else{
                    player.displayClientMessage(Component.literal("All copies have been deleted, or haven`t scribed this ability yet.").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.WHITE), true);
                }
            });
        }
    }

    public static void openMenu(Player player){
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                int max = player.getPersistentData().getInt("maxScribedAbilities");
                int amount = player.getCapability(CapabilityScribeAbilities.SCRIBE_CAPABILITY, null).map(storage -> storage.getScribedAbilitiesCount()).orElse(0);
                return Component.literal(amount + "/" + max).withStyle(ChatFormatting.BOLD);
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
                return new ScribeMenu(containerId, playerInventory, player.getCapability(CapabilityScribeAbilities.SCRIBE_CAPABILITY, null).map(storage -> storage.getScribedAbilities()).orElse(new HashMap<>()));
            }
        });
    }

    public static void acceptCopiedAbilities(Player player) {
        BeyonderUtil.confirmCopyAbility(player);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.literal("Upon use, opens a menu that you can use to get your scribed abilities."));
        tooltipComponents.add(Component.literal("Use while sneaking with an ability in your off-hand to get how many copies of that ability you have scribed so far."));
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