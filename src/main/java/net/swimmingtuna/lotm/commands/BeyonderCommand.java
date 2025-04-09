package net.swimmingtuna.lotm.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.swimmingtuna.lotm.beyonder.api.BeyonderClass;
import net.swimmingtuna.lotm.caps.BeyonderHolder;
import net.swimmingtuna.lotm.caps.BeyonderHolderAttacher;
import net.swimmingtuna.lotm.init.ItemInit;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.ClearAbilitiesS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.util.ClientData.ClientAbilitiesData;
import virtuoel.pehkui.api.ScaleData;
import virtuoel.pehkui.api.ScaleTypes;

import static net.swimmingtuna.lotm.commands.AbilityRegisterCommand.REGISTERED_ABILITIES_KEY;


public class BeyonderCommand {
    public static final DynamicCommandExceptionType ERROR_UNKNOWN_BEYONDER_CLASS = new DynamicCommandExceptionType(arg1 -> Component.translatable("argument.lotm.beyonder_class.id.invalid", arg1));

    public static void register(CommandBuildContext buildContext, CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("beyonder")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("pathway", BeyonderClassArgument.beyonderClass())
                        .then(Commands.argument("sequence", IntegerArgumentType.integer(0, 9))
                                .executes(context -> {
                                    BeyonderClass result = BeyonderClassArgument.getBeyonderClass(context, "pathway");
                                    int level = IntegerArgumentType.getInteger(context, "sequence");
                                    if (result == null) {
                                        throw ERROR_UNKNOWN_BEYONDER_CLASS.create(context.getInput());
                                    }

                                    BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(context.getSource().getPlayerOrException());
                                    if (result != holder.getCurrentClass()) {
                                        Player player = context.getSource().getPlayerOrException();
                                        ScaleData scaleData = ScaleTypes.BASE.getScaleData(player);
                                        scaleData.setScale(1);
                                        Abilities playerAbilities = player.getAbilities();
                                        playerAbilities.setFlyingSpeed(0.05F);
                                        playerAbilities.setWalkingSpeed(0.1F);
                                        player.onUpdateAbilities();
                                        if (player instanceof ServerPlayer serverPlayer) {
                                            serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
                                            LOTMNetworkHandler.sendToPlayer(new ClearAbilitiesS2C(), serverPlayer);
                                            ClientAbilitiesData.clearAbilities();
                                        }
                                        CompoundTag persistentData = player.getPersistentData();
                                        if (persistentData.contains(REGISTERED_ABILITIES_KEY)) {
                                            persistentData.remove(REGISTERED_ABILITIES_KEY);
                                        }
                                    }
                                    holder.setPathwayAndSequence(result, level);

                                    String sequenceName = result.sequenceNames().get(level);
                                    context.getSource().getPlayerOrException().sendSystemMessage(Component.translatable("item.lotm.beholder_potion.alert", sequenceName)
                                            .withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.BOLD));
                                    return 1;
                                })
                        )
                )
                .then(Commands.literal("remove")
                        .executes(context -> {
                            Player player = context.getSource().getPlayerOrException();
                            BeyonderHolder holder = BeyonderHolderAttacher.getHolderUnwrap(player);
                            ScaleData scaleData = ScaleTypes.BASE.getScaleData(player);
                            holder.removePathway();
                            BeyonderUtil.removeTags(context.getSource().getPlayerOrException());
                            scaleData.setScale(1);
                            Abilities playerAbilities = player.getAbilities();
                            playerAbilities.setFlyingSpeed(0.05F);
                            playerAbilities.setWalkingSpeed(0.1F);
                            player.onUpdateAbilities();
                            if (player instanceof ServerPlayer serverPlayer) {
                                serverPlayer.connection.send(new ClientboundPlayerAbilitiesPacket(playerAbilities));
                            }
                            return 1;
                        })
                )
                .then(Commands.literal("kit")
                        .then(Commands.literal("low")
                                .executes(context -> {
                                    Player player = context.getSource().getPlayerOrException();
                                    Inventory inventory = player.getInventory();
                                    player.setItemSlot(EquipmentSlot.HEAD, createArmorLow(Items.IRON_HELMET.getDefaultInstance()));
                                    player.setItemSlot(EquipmentSlot.CHEST, createArmorLow(Items.IRON_CHESTPLATE.getDefaultInstance()));
                                    player.setItemSlot(EquipmentSlot.LEGS, createArmorLow(Items.IRON_LEGGINGS.getDefaultInstance()));
                                    player.setItemSlot(EquipmentSlot.FEET, createArmorLow(Items.IRON_BOOTS.getDefaultInstance()));
                                    inventory.setItem(findClosestEmptySlot(player), createSwordLow(Items.IRON_SWORD.getDefaultInstance()));
                                    inventory.setItem(findClosestEmptySlot(player), ItemInit.BEYONDER_ABILITY_USER.get().getDefaultInstance());
                                    return 1;
                                }))
                        .then(Commands.literal("mid")
                                .executes(context -> {
                                    Player player = context.getSource().getPlayerOrException();
                                    Inventory inventory = player.getInventory();
                                    player.setItemSlot(EquipmentSlot.HEAD, createArmorMid(Items.DIAMOND_HELMET.getDefaultInstance()));
                                    player.setItemSlot(EquipmentSlot.CHEST, createArmorMid(Items.DIAMOND_CHESTPLATE.getDefaultInstance()));
                                    player.setItemSlot(EquipmentSlot.LEGS, createArmorMid(Items.DIAMOND_LEGGINGS.getDefaultInstance()));
                                    player.setItemSlot(EquipmentSlot.FEET, createArmorMid(Items.DIAMOND_BOOTS.getDefaultInstance()));
                                    inventory.setItem(findClosestEmptySlot(player), createSwordMid(Items.DIAMOND_SWORD.getDefaultInstance()));
                                    inventory.setItem(findClosestEmptySlot(player), ItemInit.BEYONDER_ABILITY_USER.get().getDefaultInstance());
                                    return 1;
                                }))
                        .then(Commands.literal("high")
                                .executes(context -> {
                                    Player player = context.getSource().getPlayerOrException();
                                    Inventory inventory = player.getInventory();
                                    player.setItemSlot(EquipmentSlot.HEAD, createArmorHigh(Items.NETHERITE_HELMET.getDefaultInstance()));
                                    player.setItemSlot(EquipmentSlot.CHEST, createArmorHigh(Items.NETHERITE_CHESTPLATE.getDefaultInstance()));
                                    player.setItemSlot(EquipmentSlot.LEGS, createArmorHigh(Items.NETHERITE_LEGGINGS.getDefaultInstance()));
                                    player.setItemSlot(EquipmentSlot.FEET, createArmorHigh(Items.NETHERITE_BOOTS.getDefaultInstance()));
                                    inventory.setItem(findClosestEmptySlot(player), createSwordHigh(Items.NETHERITE_SWORD.getDefaultInstance()));
                                    inventory.setItem(findClosestEmptySlot(player), ItemInit.BEYONDER_ABILITY_USER.get().getDefaultInstance());
                                    return 1;
                                }))
                ));
    }

    private static ItemStack createSwordLow(ItemStack sword) {
        sword.enchant(Enchantments.SHARPNESS, 1);
        sword.enchant(Enchantments.UNBREAKING, 3);
        return sword;
    }
    private static ItemStack createSwordMid(ItemStack sword) {
        sword.enchant(Enchantments.SHARPNESS, 3);
        sword.enchant(Enchantments.UNBREAKING, 3);
        return sword;
    }
    private static ItemStack createSwordHigh(ItemStack sword) {
        sword.enchant(Enchantments.SHARPNESS, 5);
        sword.enchant(Enchantments.UNBREAKING, 3);
        return sword;
    }

    private static ItemStack createArmorLow(ItemStack armor) {
        armor.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 1);
        armor.enchant(Enchantments.UNBREAKING, 1);
        return armor;
    }
    private static ItemStack createArmorMid(ItemStack armor) {
        armor.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 3);
        armor.enchant(Enchantments.UNBREAKING, 2);
        return armor;
    }
    private static ItemStack createArmorHigh(ItemStack armor) {
        armor.enchant(Enchantments.ALL_DAMAGE_PROTECTION, 4);
        armor.enchant(Enchantments.UNBREAKING, 3);
        return armor;
    }

    public static int findClosestEmptySlot(Player player) {
        Inventory inventory = player.getInventory();
        int selectedSlot = player.getInventory().selected;
        if (inventory.getItem(selectedSlot).isEmpty()) {
            return selectedSlot;
        }
        for (int distance = 1; distance < 9; distance++) {
            int rightSlot = (selectedSlot + distance) % 9;
            if (inventory.getItem(rightSlot).isEmpty()) {
                return rightSlot;
            }
            int leftSlot = (selectedSlot - distance + 9) % 9;
            if (inventory.getItem(leftSlot).isEmpty()) {
                return leftSlot;
            }
        }
        for (int i = 9; i < 36; i++) {
            if (inventory.getItem(i).isEmpty()) {
                return i;
            }
        }
        return -1;
    }
}

