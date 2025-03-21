package net.swimmingtuna.lotm.item.BeyonderPotions;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.util.BeyonderUtil;

import java.util.Random;

public class BeyonderCharacteristic extends Item {
    public BeyonderCharacteristic(Properties pProperties) {
        super(pProperties);
    }

    public static void setData(ItemStack stack, BeyonderClass pathway, int sequence, boolean previousSequence, int texture){
        CompoundTag tag = stack.getOrCreateTag();
        tag.putString("pathway", BeyonderUtil.getPathwayName(pathway));
        tag.putInt("sequence", sequence);
        tag.putBoolean("previousSequence", previousSequence);
        //tag.putInt("texture", texture);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand){
        Random rand = new Random();
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();

        if(!level.isClientSide){
            int texture = rand.nextInt(1,3);
            if(player.isShiftKeyDown()){
                setData(stack, BeyonderUtil.getPathway(player), BeyonderUtil.getSequence(player), false, texture);
                player.getInventory().setChanged();
                player.inventoryMenu.broadcastChanges();
            }else{
                player.displayClientMessage(Component.literal(tag.getString("pathway")), false);
                player.displayClientMessage(Component.literal("" + tag.getInt("sequence")), false);
                player.displayClientMessage(Component.literal("" + tag.getInt("texture")), false);
            }
        }
        return InteractionResultHolder.success(stack);
    }
}