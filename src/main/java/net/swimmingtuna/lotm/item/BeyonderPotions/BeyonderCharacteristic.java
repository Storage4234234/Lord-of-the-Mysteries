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
import net.swimmingtuna.lotm.init.BeyonderClassInit;
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
        //tag.putInt("texture", texture);
    }

    public int getSequence(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getInt("sequence");
    }

    @Override
    public Component getName(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int sequence = tag.getInt("sequence");
        BeyonderClass pathway = getPathway(stack);
        if (pathway != null && pathway.sequenceNames() != null && !pathway.sequenceNames().isEmpty()) {
            if (sequence >= 0 && sequence < pathway.sequenceNames().size()) {
                return Component.literal(pathway.sequenceNames().get(sequence) + " Characteristic").withStyle(pathway.getColorFormatting());
            }
        }
        return super.getName(stack);
    }

    public BeyonderClass getPathway(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        String pathway = tag.getString("pathway").toLowerCase();
        if (pathway.contains("apothecary")) {
            return BeyonderClassInit.APOTHECARY.get();
        } else if (pathway.contains("spectator")) {
            return BeyonderClassInit.SPECTATOR.get();
        } else if (pathway.contains("sailor")) {
            return BeyonderClassInit.SAILOR.get();
        } else if (pathway.contains("seer")) {
            return BeyonderClassInit.SEER.get();
        } else if (pathway.contains("apprentice")) {
            return BeyonderClassInit.APPRENTICE.get();
        } else if (pathway.contains("marauder")) {
            return BeyonderClassInit.MARAUDER.get();
        } else if (pathway.contains("secrets_supplicant")) {
            return BeyonderClassInit.SECRETSSUPPLICANT.get();
        } else if (pathway.contains("bard")) {
            return BeyonderClassInit.BARD.get();
        } else if (pathway.contains("reader")) {
            return BeyonderClassInit.READER.get();
        } else if (pathway.contains("sleepless")) {
            return BeyonderClassInit.SLEEPLESS.get();
        } else if (pathway.contains("warrior")) {
            return BeyonderClassInit.WARRIOR.get();
        } else if (pathway.contains("hunter")) {
            return BeyonderClassInit.HUNTER.get();
        } else if (pathway.contains("assassin")) {
            return BeyonderClassInit.ASSASSIN.get();
        } else if (pathway.contains("savant")) {
            return BeyonderClassInit.SAVANT.get();
        } else if (pathway.contains("mystery_pryer")) {
            return BeyonderClassInit.MYSTERYPRYER.get();
        } else if (pathway.contains("corpse_collector")) {
            return BeyonderClassInit.CORPSECOLLECTOR.get();
        } else if (pathway.contains("lawyer")) {
            return BeyonderClassInit.LAWYER.get();
        } else if (pathway.contains("monster")) {
            return BeyonderClassInit.MONSTER.get();
        } else if (pathway.contains("planter")) {
            return BeyonderClassInit.PLANTER.get();
        } else if (pathway.contains("arbiter")) {
            return BeyonderClassInit.ARBITER.get();
        } else if (pathway.contains("prisoner")) {
            return BeyonderClassInit.PRISONER.get();
        } else if (pathway.contains("criminal")) {
            return BeyonderClassInit.CRIMINAL.get();
        }
        return null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand){
        Random rand = new Random();
        ItemStack stack = player.getItemInHand(hand);
        CompoundTag tag = stack.getOrCreateTag();

        if(!level.isClientSide()) {
            int texture = rand.nextInt(1,3);
            if(player.isShiftKeyDown()) {
                setData(stack, BeyonderUtil.getPathway(player), BeyonderUtil.getSequence(player), false, texture);
                player.getInventory().setChanged();
                player.inventoryMenu.broadcastChanges();
            } else {
                player.displayClientMessage(Component.literal(tag.getString("pathway")), false);
                player.displayClientMessage(Component.literal("" + tag.getInt("sequence")), false);
                player.displayClientMessage(Component.literal("" + tag.getInt("texture")), false);
            }
        }
        return InteractionResultHolder.success(stack);
    }


}