package net.swimmingtuna.lotm.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.init.BeyonderClassInit;
import net.swimmingtuna.lotm.item.BeyonderAbilities.Ability;
import net.swimmingtuna.lotm.networking.LOTMNetworkHandler;
import net.swimmingtuna.lotm.networking.packet.ClearAbilitiesS2C;
import net.swimmingtuna.lotm.networking.packet.SyncAbilitiesS2C;
import net.swimmingtuna.lotm.util.BeyonderUtil;
import net.swimmingtuna.lotm.world.worlddata.BeyonderRecipeData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbilityRegisterCommand {

    public static final String REGISTERED_ABILITIES_KEY = "RegisteredAbilities";
    private static final Map<String, Integer> COMBINATION_MAP = new HashMap<>();
    private static final Map<String, String> abilitiesToSync = new HashMap<>();

    private static final DynamicCommandExceptionType NOT_ABILITY = new DynamicCommandExceptionType(o -> Component.literal("Not an ability: " + o));

    static {
        initializeCombinationMap();
    }

    private static void initializeCombinationMap() {
        String[] combinations = {
                "LLLLL", "LLLLR", "LLLRL", "LLLRR", "LLRLL", "LLRLR", "LLRRL", "LLRRR",
                "LRLLL", "LRLLR", "LRLRL", "LRLRR", "LRRLL", "LRRLR", "LRRRL", "LRRRR",
                "RLLLL", "RLLLR", "RLLRL", "RLLRR", "RLRLL", "RLRLR", "RLRRL", "RLRRR",
                "RRLLL", "RRLLR", "RRLRL", "RRLRR", "RRRLL", "RRRLR", "RRRRL", "RRRRR"
        };
        for (int i = 0; i < combinations.length; i++) {
            COMBINATION_MAP.put(combinations[i], i + 1);
        }
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        dispatcher.register(Commands.literal("abilityput")
                .then(Commands.argument("combination", StringArgumentType.word())
                        .then(Commands.argument("item", ResourceArgument.resource(buildContext, Registries.ITEM))
                                .executes(context -> registerAbility(
                                        context.getSource(),
                                        StringArgumentType.getString(context, "combination"),
                                        ResourceArgument.getResource(context, "item", Registries.ITEM)
                                )))));
        dispatcher.register(Commands.literal("abilityput")
                .then(Commands.literal("load")
                        .executes(AbilityRegisterCommand::loadBeyonderAbilities)));

    }

    private static int registerAbility(CommandSourceStack source, String combination, Holder.Reference<Item> itemReference) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        if (!COMBINATION_MAP.containsKey(combination)) {
            source.sendFailure(Component.literal("Invalid combination. Please use a valid 5-character combination of L and R.").withStyle(ChatFormatting.RED));
            return 0;
        }
        int combinationNumber = COMBINATION_MAP.get(combination);
        List<Item> availableAbilities = BeyonderUtil.getAbilities(player);

        Item item = itemReference.get();
        ResourceLocation resourceLocation = itemReference.key().location();
        if (!(item instanceof Ability)) {
            throw NOT_ABILITY.create(resourceLocation);
        }
        if (!availableAbilities.contains(item)) {
            source.sendFailure(Component.literal("Ability not available: " + itemReference).withStyle(ChatFormatting.RED));
            return 0;
        }

        CompoundTag tag = player.getPersistentData();
        CompoundTag registeredAbilities;
        if (tag.contains(REGISTERED_ABILITIES_KEY, Tag.TAG_COMPOUND)) {
            registeredAbilities = tag.getCompound(REGISTERED_ABILITIES_KEY);
        } else {
            registeredAbilities = new CompoundTag();
            tag.put(REGISTERED_ABILITIES_KEY, registeredAbilities);
        }

        registeredAbilities.putString(String.valueOf(combinationNumber), resourceLocation.toString());
        tag.put(REGISTERED_ABILITIES_KEY, registeredAbilities);
        source.sendSuccess(() -> Component.literal("Added ability: ").append(Component.translatable(item.getDescriptionId())).append(Component.literal(" for combination " + combination).withStyle(ChatFormatting.GREEN)), true);

        return 1;
    }

    public static void syncRegisteredAbilitiesToClient(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (tag.contains(REGISTERED_ABILITIES_KEY, Tag.TAG_COMPOUND)) {
            CompoundTag registeredAbilities = tag.getCompound(REGISTERED_ABILITIES_KEY);
            Map<String, String> abilitiesToSync = new HashMap<>();

            for (String combinationNumber : registeredAbilities.getAllKeys()) {
                String abilityResourceLocationString = registeredAbilities.getString(combinationNumber);
                ResourceLocation resourceLocation = new ResourceLocation(abilityResourceLocationString);
                Item item = ForgeRegistries.ITEMS.getValue(resourceLocation);
                if (item != null) {
                    String combination = findCombinationForNumber(Integer.parseInt(combinationNumber));
                    if (!combination.isEmpty()) {
                        String localizedName = Component.translatable(item.getDescriptionId()).getString();
                        abilitiesToSync.put(combination, localizedName);
                    }
                }
            }

            if (!abilitiesToSync.isEmpty()) {
                LOTMNetworkHandler.sendToPlayer(new SyncAbilitiesS2C(abilitiesToSync), player);
            }
            if (abilitiesToSync.isEmpty()) {
                LOTMNetworkHandler.sendToPlayer(new ClearAbilitiesS2C(), player);
                player.sendSystemMessage(Component.literal("Cleared Abilities").withStyle(ChatFormatting.GREEN));
            }
        }
    }


    public static void tickEvent(ServerPlayer player) {
        syncRegisteredAbilitiesToClient(player);
    }

    public static String findCombinationForNumber(int number) {
        for (Map.Entry<String, Integer> entry : COMBINATION_MAP.entrySet()) {
            if (entry.getValue() == number) {
                return entry.getKey();
            }
        }
        return "";
    }

    private static int loadBeyonderAbilities(CommandContext<CommandSourceStack> context) {
        try {
            ServerLevel level = context.getSource().getLevel();
            BeyonderRecipeData recipeData = BeyonderRecipeData.getInstance(level);
            recipeData.clearRecipes();
            loadAbilities(context);
            context.getSource().sendSuccess(() -> Component.literal("Successfully registered all available beyonder abilities!")
                    .withStyle(ChatFormatting.GREEN), true);
            return 1;
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Error loading beyonder ability: " + e.getMessage())
                    .withStyle(ChatFormatting.RED));
            return 0;
        }
    }

    private static void excecuteAbilityCommand(CommandContext<CommandSourceStack> context, String command) {
        try {
            context.getSource().getServer().getCommands().performPrefixedCommand(
                    context.getSource(), command.substring(1)); // Remove the leading '/' from command
        } catch (Exception e) {
            context.getSource().sendFailure(Component.literal("Failed to load abilities")
                    .withStyle(ChatFormatting.RED));
        }
    }

    private static void loadAbilities(CommandContext<CommandSourceStack> context) {
        Player player = context.getSource().getPlayer();
        if (player != null) {
            int sequence = BeyonderUtil.getSequence(player);
            if (BeyonderUtil.currentPathwayMatchesNoException(player, BeyonderClassInit.SPECTATOR.get())) {
                if (sequence == 9) {
                    player.sendSystemMessage(Component.literal("No abilities to register"));
                } else if (sequence >= 8) {
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:mindreading");
                } else if (sequence == 7) {
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:mindreading");
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:awe");
                    excecuteAbilityCommand(context, "/abilityput LLLRL lotm:frenzy");
                    excecuteAbilityCommand(context, "/abilityput RRRLR lotm:placate");
                } else if (sequence == 6) {
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:mindreading");
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:awe");
                    excecuteAbilityCommand(context, "/abilityput LLLRL lotm:frenzy");
                    excecuteAbilityCommand(context, "/abilityput RRRLR lotm:placate");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:psychologicalinvisibility");
                } else if (sequence == 5) {
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:mindreading");
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:awe");
                    excecuteAbilityCommand(context, "/abilityput LLLRL lotm:frenzy");
                    excecuteAbilityCommand(context, "/abilityput RRRLR lotm:placate");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:psychologicalinvisibility");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:dreamwalking");
                } else if (sequence == 4) {
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:mindreading");
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:awe");
                    excecuteAbilityCommand(context, "/abilityput RRRLR lotm:placate");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:psychologicalinvisibility");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:dreamwalking");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:dragonbreath");
                    excecuteAbilityCommand(context, "/abilityput RLLLL lotm:mindstorm");
                } else if (sequence == 3) {
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:mindreading");
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:awe");
                    excecuteAbilityCommand(context, "/abilityput RRRLR lotm:placate");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:psychologicalinvisibility");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:dreamwalking");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:dragonbreath");
                    excecuteAbilityCommand(context, "/abilityput RLLLL lotm:plaguestorm");
                    excecuteAbilityCommand(context, "/abilityput RLRLR lotm:dreamweaving");
                } else if (sequence == 2) {
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:mindreading");
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:awe");
                    excecuteAbilityCommand(context, "/abilityput RRRLR lotm:placate");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:psychologicalinvisibility");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:dreamwalking");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:dragonbreath");
                    excecuteAbilityCommand(context, "/abilityput RLLLL lotm:plaguestorm");
                    excecuteAbilityCommand(context, "/abilityput RRLRR lotm:dreamintoreality");
                    excecuteAbilityCommand(context, "/abilityput LLLLR lotm:discern");
                    excecuteAbilityCommand(context, "/abilityput RLRLR lotm:dreamweaving");
                } else if (sequence == 1) {
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:mindreading");
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:awe");
                    excecuteAbilityCommand(context, "/abilityput RRRLR lotm:placate");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:psychologicalinvisibility");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:dreamwalking");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:dragonbreath");
                    excecuteAbilityCommand(context, "/abilityput RLLLL lotm:plaguestorm");
                    excecuteAbilityCommand(context, "/abilityput RRLRR lotm:dreamintoreality");
                    excecuteAbilityCommand(context, "/abilityput LLLLR lotm:discern");
                    excecuteAbilityCommand(context, "/abilityput LLRRR lotm:prophesizeblock");
                    excecuteAbilityCommand(context, "/abilityput LLRLR lotm:prophesizeplayer");
                    excecuteAbilityCommand(context, "/abilityput LLRLL lotm:prophesizedemise");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:meteorshower");
                    excecuteAbilityCommand(context, "/abilityput RLRLR lotm:dreamweaving");
                } else if (sequence == 0) {
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:mindreading");
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:awe");
                    excecuteAbilityCommand(context, "/abilityput RRRLR lotm:placate");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:psychologicalinvisibility");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:dreamwalking");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:dragonbreath");
                    excecuteAbilityCommand(context, "/abilityput RLLLL lotm:plaguestorm");
                    excecuteAbilityCommand(context, "/abilityput RRLRR lotm:dreamintoreality");
                    excecuteAbilityCommand(context, "/abilityput LLLLR lotm:discern");
                    excecuteAbilityCommand(context, "/abilityput LLRRR lotm:prophesizeblock");
                    excecuteAbilityCommand(context, "/abilityput LLRLR lotm:prophesizeplayer");
                    excecuteAbilityCommand(context, "/abilityput LLRLL lotm:prophesizedemise");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:meteorshower");
                    excecuteAbilityCommand(context, "/abilityput RLRRR lotm:envisionhealth");
                    excecuteAbilityCommand(context, "/abilityput LLRRL lotm:envisionlocation");
                    excecuteAbilityCommand(context, "/abilityput RLLRR lotm:envisionbarrier");
                    excecuteAbilityCommand(context, "/abilityput RLRLR lotm:dreamweaving");
                }
            } else if (BeyonderUtil.currentPathwayMatchesNoException(player, BeyonderClassInit.MONSTER.get())) {
                if (sequence == 9) {
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:monsterdangersense");
                } else if (sequence >= 8) {
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:monsterdangersense");
                } else if (sequence == 7) {
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:monsterdangersense");
                    excecuteAbilityCommand(context, "/abilityput RRLLR lotm:luckperception");
                } else if (sequence == 6) {
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:monsterdangersense");
                    excecuteAbilityCommand(context, "/abilityput RRLLR lotm:luckperception");
                    excecuteAbilityCommand(context, "/abilityput LLLRR lotm:psychestorm");
                } else if (sequence == 5) {
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:monsterdangersense");
                    excecuteAbilityCommand(context, "/abilityput RRLLR lotm:luckperception");
                    excecuteAbilityCommand(context, "/abilityput LLLRR lotm:psychestorm");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:luckfuturetelling");
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:misfortunebestowal");
                } else if (sequence == 4) {
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:monsterdangersense");
                    excecuteAbilityCommand(context, "/abilityput RRLLR lotm:luckperception");
                    excecuteAbilityCommand(context, "/abilityput LLLRR lotm:psychestorm");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:luckfuturetelling");
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:misfortunebestowal");
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:providencedomain");
                    excecuteAbilityCommand(context, "/abilityput LRLLL lotm:misfortunedomain");
                } else if (sequence == 3) {
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:monsterdangersense");
                    excecuteAbilityCommand(context, "/abilityput RRLLR lotm:luckperception");
                    excecuteAbilityCommand(context, "/abilityput LLLRR lotm:psychestorm");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:luckfuturetelling");
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:misfortunebestowal");
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:providencedomain");
                    excecuteAbilityCommand(context, "/abilityput LRLLL lotm:misfortunedomain");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:auraofchaos");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:chaoswalkercombat");
                    excecuteAbilityCommand(context, "/abilityput LRRRR lotm:enabledisableripple");
                } else if (sequence == 2) {
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:monsterdangersense");
                    excecuteAbilityCommand(context, "/abilityput RRLLR lotm:luckperception");
                    excecuteAbilityCommand(context, "/abilityput LLLRR lotm:psychestorm");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:luckfuturetelling");
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:misfortunebestowal");
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:providencedomain");
                    excecuteAbilityCommand(context, "/abilityput LRLLL lotm:misfortunedomain");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:auraofchaos");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:chaoswalkercombat");
                    excecuteAbilityCommand(context, "/abilityput LRRRR lotm:enabledisableripple");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:whisperofcorruption");
                    excecuteAbilityCommand(context, "/abilityput LRRRR lotm:misfortuneimplosion");
                } else if (sequence == 1) {
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:monsterdangersense");
                    excecuteAbilityCommand(context, "/abilityput RRLLR lotm:luckperception");
                    excecuteAbilityCommand(context, "/abilityput LLLRR lotm:psychestorm");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:luckfuturetelling");
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:misfortunebestowal");
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:providencedomain");
                    excecuteAbilityCommand(context, "/abilityput LRLLL lotm:misfortunedomain");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:auraofchaos");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:chaoswalkercombat");
                    excecuteAbilityCommand(context, "/abilityput LRRRR lotm:enabledisableripple");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:whisperofcorruption");
                    excecuteAbilityCommand(context, "/abilityput LRRRR lotm:misfortuneimplosion");
                    excecuteAbilityCommand(context, "/abilityput RLLLL lotm:rebootself");
                    excecuteAbilityCommand(context, "/abilityput RRRLR lotm:cycleoffate");
                    excecuteAbilityCommand(context, "/abilityput RRLRR lotm:fatereincarnation");
                } else if (sequence == 0) {
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:monsterdangersense");
                    excecuteAbilityCommand(context, "/abilityput RRLLR lotm:luckperception");
                    excecuteAbilityCommand(context, "/abilityput LLLRR lotm:psychestorm");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:luckfuturetelling");
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:misfortunebestowal");
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:providencedomain");
                    excecuteAbilityCommand(context, "/abilityput LRLLL lotm:misfortunedomain");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:auraofchaos");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:chaoswalkercombat");
                    excecuteAbilityCommand(context, "/abilityput LRRRR lotm:enabledisableripple");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:whisperofcorruption");
                    excecuteAbilityCommand(context, "/abilityput LRRRR lotm:misfortuneimplosion");
                    excecuteAbilityCommand(context, "/abilityput RLLLL lotm:rebootself");
                    excecuteAbilityCommand(context, "/abilityput RRRLR lotm:cycleoffate");
                    excecuteAbilityCommand(context, "/abilityput RRLRR lotm:fatereincarnation");
                    excecuteAbilityCommand(context, "/abilityput LLLRL lotm:probabilityinfinitefortune");
                    excecuteAbilityCommand(context, "/abilityput RLRRR lotm:probabilityinfinitemisfortune");
                    excecuteAbilityCommand(context, "/abilityput LRLRL lotm:probabilityfortune");
                    excecuteAbilityCommand(context, "/abilityput RLRLR lotm:probabilitymisfortune");
                }
            } else if (BeyonderUtil.currentPathwayMatchesNoException(player, BeyonderClassInit.SAILOR.get())) {
                if (sequence == 9) {
                    player.sendSystemMessage(Component.literal("No abilities to register"));
                } else if (sequence >= 8) {
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:ragingblows");
                } else if (sequence == 7) {
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:ragingblows");
                } else if (sequence == 6) {
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:ragingblows");
                } else if (sequence == 5) {
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:ragingblows");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:sailorlightning");
                    excecuteAbilityCommand(context, "/abilityput LLRRL lotm:acidicrain");
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:watersphere");
                } else if (sequence == 4) {
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:ragingblows");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:sailorlightning");
                    excecuteAbilityCommand(context, "/abilityput LLRRL lotm:acidicrain");
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:watersphere");
                    excecuteAbilityCommand(context, "/abilityput LLLRR lotm:tornado");
                    excecuteAbilityCommand(context, "/abilityput LLLLR lotm:roar");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:earthquake");
                } else if (sequence == 3) {
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:ragingblows");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:sailorlightning");
                    excecuteAbilityCommand(context, "/abilityput LLRRL lotm:acidicrain");
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:watersphere");
                    excecuteAbilityCommand(context, "/abilityput LLLRR lotm:tornado");
                    excecuteAbilityCommand(context, "/abilityput LLLLR lotm:roar");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:earthquake");
                    excecuteAbilityCommand(context, "/abilityput LRRRR lotm:sonicboom");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:lightningbranch");
                    excecuteAbilityCommand(context, "/abilityput RRLLR lotm:thunderclap");
                } else if (sequence == 2) {
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:ragingblows");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:sailorlightning");
                    excecuteAbilityCommand(context, "/abilityput LLRRL lotm:acidicrain");
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:watersphere");
                    excecuteAbilityCommand(context, "/abilityput LLLRR lotm:tornado");
                    excecuteAbilityCommand(context, "/abilityput LLLLR lotm:roar");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:earthquake");
                    excecuteAbilityCommand(context, "/abilityput LRRRR lotm:sonicboom");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:lightningbranch");
                    excecuteAbilityCommand(context, "/abilityput RRLLR lotm:thunderclap");
                    excecuteAbilityCommand(context, "/abilityput LRLRL lotm:lightningball");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:extremecoldness");
                    excecuteAbilityCommand(context, "/abilityput LRRLR lotm:raineyes");
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:volcaniceruption");
                } else if (sequence == 1) {

                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:ragingblows");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:sailorlightning");
                    excecuteAbilityCommand(context, "/abilityput LLRRL lotm:acidicrain");
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:watersphere");
                    excecuteAbilityCommand(context, "/abilityput LLLRR lotm:tornado");
                    excecuteAbilityCommand(context, "/abilityput LLLLR lotm:roar");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:earthquake");
                    excecuteAbilityCommand(context, "/abilityput LRRRR lotm:sonicboom");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:lightningbranch");
                    excecuteAbilityCommand(context, "/abilityput RRLLR lotm:thunderclap");
                    excecuteAbilityCommand(context, "/abilityput LRLRL lotm:lightningball");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:extremecoldness");
                    excecuteAbilityCommand(context, "/abilityput LRRLR lotm:raineyes");
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:volcaniceruption");
                    excecuteAbilityCommand(context, "/abilityput LRLLL lotm:lightningballabsorb");
                    excecuteAbilityCommand(context, "/abilityput RRRLR lotm:sailorlightningtravel");
                    excecuteAbilityCommand(context, "/abilityput LLRRR lotm:staroflightning");
                    excecuteAbilityCommand(context, "/abilityput LRLLR lotm:lightningredirection");

                } else if (sequence == 0) {
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:ragingblows");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:sailorlightning");
                    excecuteAbilityCommand(context, "/abilityput LLRRL lotm:acidicrain");
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:watersphere");
                    excecuteAbilityCommand(context, "/abilityput LLLRR lotm:tornado");
                    excecuteAbilityCommand(context, "/abilityput LLLLR lotm:roar");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:earthquake");
                    excecuteAbilityCommand(context, "/abilityput LRRRR lotm:sonicboom");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:lightningbranch");
                    excecuteAbilityCommand(context, "/abilityput RRLLR lotm:thunderclap");
                    excecuteAbilityCommand(context, "/abilityput LRLRL lotm:lightningball");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:extremecoldness");
                    excecuteAbilityCommand(context, "/abilityput LRRLR lotm:raineyes");
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:volcaniceruption");
                    excecuteAbilityCommand(context, "/abilityput LRLLL lotm:lightningballabsorb");
                    excecuteAbilityCommand(context, "/abilityput RRRLR lotm:sailorlightningtravel");
                    excecuteAbilityCommand(context, "/abilityput LLRRR lotm:staroflightning");
                    excecuteAbilityCommand(context, "/abilityput LRLLR lotm:lightningredirection");
                    excecuteAbilityCommand(context, "/abilityput RLLLL lotm:tyranny");
                    excecuteAbilityCommand(context, "/abilityput RLLLR lotm:stormseal");
                }
            }  else if (BeyonderUtil.currentPathwayMatchesNoException(player, BeyonderClassInit.WARRIOR.get())) {
                if (sequence == 9) {
                    player.sendSystemMessage(Component.literal("No abilities to register"));
                } else if (sequence == 8) {
                    player.sendSystemMessage(Component.literal("No abilities to register"));
                } else if (sequence == 7) {
                    player.sendSystemMessage(Component.literal("No abilities to register"));
                } else if (sequence == 6) {
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:gigantification");
                    excecuteAbilityCommand(context, "/abilityput LLLLR lotm:lightofdawn");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:dawnarmory");
                } else if (sequence == 5) {
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:gigantification");
                    excecuteAbilityCommand(context, "/abilityput LLLLR lotm:lightofdawn");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:dawnarmory");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:enabledisableprotection");
                } else if (sequence == 4) {
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:gigantification");
                    excecuteAbilityCommand(context, "/abilityput LLLLR lotm:lightofdawn");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:dawnarmory");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:enabledisableprotection");
                    excecuteAbilityCommand(context, "/abilityput LRLRL lotm:eyeofdemonhunting");
                    excecuteAbilityCommand(context, "/abilityput LRRLR lotm:warriordangersense");
                } else if (sequence == 3) {
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:gigantification");
                    excecuteAbilityCommand(context, "/abilityput LLLLR lotm:lightofdawn");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:dawnarmory");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:enabledisableprotection");
                    excecuteAbilityCommand(context, "/abilityput LRLRL lotm:eyeofdemonhunting");
                    excecuteAbilityCommand(context, "/abilityput LRRLR lotm:warriordangersense");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:mercuryliquefication");
                    excecuteAbilityCommand(context, "/abilityput LRLLR lotm:silverswordmanifestation");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:silverrapier");
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:silverarmory");
                    excecuteAbilityCommand(context, "/abilityput LLRRR lotm:lightconcealment");
                } else if (sequence == 2) {
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:gigantification");
                    excecuteAbilityCommand(context, "/abilityput LLLLR lotm:lightofdawn");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:dawnarmory");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:enabledisableprotection");
                    excecuteAbilityCommand(context, "/abilityput LRLRL lotm:eyeofdemonhunting");
                    excecuteAbilityCommand(context, "/abilityput LRRLR lotm:warriordangersense");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:mercuryliquefication");
                    excecuteAbilityCommand(context, "/abilityput LRLLR lotm:silverswordmanifestation");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:silverrapier");
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:silverarmory");
                    excecuteAbilityCommand(context, "/abilityput LLRRR lotm:lightconcealment");
                    excecuteAbilityCommand(context, "/abilityput RLLLL lotm:beamofglory");
                    excecuteAbilityCommand(context, "/abilityput LRLLL lotm:auraofglory");
                    excecuteAbilityCommand(context, "/abilityput RLRLL lotm:twilightsword");
                    excecuteAbilityCommand(context, "/abilityput RLRLR lotm:mercurycage");
                } else if (sequence == 1) {
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:gigantification");
                    excecuteAbilityCommand(context, "/abilityput LLLLR lotm:lightofdawn");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:dawnarmory");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:enabledisableprotection");
                    excecuteAbilityCommand(context, "/abilityput LRLRL lotm:eyeofdemonhunting");
                    excecuteAbilityCommand(context, "/abilityput LRRLR lotm:warriordangersense");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:mercuryliquefication");
                    excecuteAbilityCommand(context, "/abilityput LRLLR lotm:silverswordmanifestation");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:silverrapier");
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:silverarmory");
                    excecuteAbilityCommand(context, "/abilityput LLRRR lotm:lightconcealment");
                    excecuteAbilityCommand(context, "/abilityput RLLLL lotm:beamofglory");
                    excecuteAbilityCommand(context, "/abilityput LRLLL lotm:auraofglory");
                    excecuteAbilityCommand(context, "/abilityput RLRLL lotm:twilightsword");
                    excecuteAbilityCommand(context, "/abilityput RLRLR lotm:mercurycage");
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:divinehandright");
                    excecuteAbilityCommand(context, "/abilityput RLLRR lotm:divinehandleft");
                    excecuteAbilityCommand(context, "/abilityput LLRLR lotm:twilightmanifestation");
                } else if (sequence == 0) {
                    excecuteAbilityCommand(context, "/abilityput LLLLL lotm:gigantification");
                    excecuteAbilityCommand(context, "/abilityput LLLLR lotm:lightofdawn");
                    excecuteAbilityCommand(context, "/abilityput RRRRL lotm:dawnarmory");
                    excecuteAbilityCommand(context, "/abilityput RRLLL lotm:enabledisableprotection");
                    excecuteAbilityCommand(context, "/abilityput LRLRL lotm:eyeofdemonhunting");
                    excecuteAbilityCommand(context, "/abilityput LRRLR lotm:warriordangersense");
                    excecuteAbilityCommand(context, "/abilityput RRRRR lotm:mercuryliquefication");
                    excecuteAbilityCommand(context, "/abilityput LRLLR lotm:silverswordmanifestation");
                    excecuteAbilityCommand(context, "/abilityput RRRLL lotm:silverrapier");
                    excecuteAbilityCommand(context, "/abilityput RLRRL lotm:silverarmory");
                    excecuteAbilityCommand(context, "/abilityput LLRRR lotm:lightconcealment");
                    excecuteAbilityCommand(context, "/abilityput RLLLL lotm:beamoftwilight");
                    excecuteAbilityCommand(context, "/abilityput LRLLL lotm:auraoftwilight");
                    excecuteAbilityCommand(context, "/abilityput RLRLL lotm:twilightsword");
                    excecuteAbilityCommand(context, "/abilityput RLRLR lotm:mercurycage");
                    excecuteAbilityCommand(context, "/abilityput LRRLL lotm:divinehandright");
                    excecuteAbilityCommand(context, "/abilityput RLLRR lotm:divinehandleft");
                    excecuteAbilityCommand(context, "/abilityput LLRLR lotm:twilightmanifestation");

                    excecuteAbilityCommand(context, "/abilityput RLRRR lotm:twilightfreeze");
                    excecuteAbilityCommand(context, "/abilityput RLLLL lotm:twilightlight");
                    excecuteAbilityCommand(context, "/abilityput RRLRR lotm:twilightaccelerate");
                    excecuteAbilityCommand(context, "/abilityput LRRRR lotm:globeoftwilight");
                }
            }
        }
    }
}