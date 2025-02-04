package net.swimmingtuna.lotm.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.registries.ForgeRegistries;
import net.swimmingtuna.lotm.util.ClientData.ClientShouldntRenderTransformData;

import java.util.Objects;

public class TransformCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("transform")
                .then(Commands.argument("mob", ResourceLocationArgument.id())
                        .suggests((commandContext, suggestionsBuilder) -> {
                            for (ResourceLocation key : ForgeRegistries.ENTITY_TYPES.getKeys()) {
                                EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(key);
                                if (entityType != null && Entity.class.isAssignableFrom(entityType.getBaseClass())) {
                                    suggestionsBuilder.suggest(key.toString());
                                }
                            }
                            return suggestionsBuilder.buildFuture();
                        }).executes(commandContext ->
                                ClientShouldntRenderTransformData.getInstance()
                                        .transform(ResourceLocationArgument.getId(commandContext, "mob").toString(), Objects.requireNonNull(commandContext.getSource().getPlayer())))
                )
                .then(Commands.literal("remove")
                        .executes(context -> ClientShouldntRenderTransformData.getInstance().removeTransform(context.getSource().getPlayer()))
                )
        );
    }
}
