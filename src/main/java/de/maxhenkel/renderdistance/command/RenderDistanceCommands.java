package de.maxhenkel.renderdistance.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.maxhenkel.renderdistance.RenderDistance;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

public class RenderDistanceCommands {

    public static final SimpleCommandExceptionType ERROR_INVALID_RENDER_DISTANCE = new SimpleCommandExceptionType(new TextComponent("Invalid render distance"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        LiteralArgumentBuilder<CommandSourceStack> literalBuilder = Commands.literal("renderdistance");

        literalBuilder.then(Commands.literal("current").executes((context) -> {
            context.getSource().sendSuccess(new TextComponent("The current render distance is " + context.getSource().getServer().getPlayerList().getViewDistance()), false);
            return 1;
        }));

        literalBuilder.then(Commands.literal("set").requires((commandSource) -> commandSource.hasPermission(2))
                .then(Commands.argument("amount", IntegerArgumentType.integer()).executes((context) -> {
                    int renderDistance = IntegerArgumentType.getInteger(context, "amount");

                    if (renderDistance <= 0 || renderDistance > 32) {
                        throw ERROR_INVALID_RENDER_DISTANCE.create();
                    }
                    RenderDistance.SERVER_CONFIG.fixedRenderDistance.set(renderDistance);
                    RenderDistance.SERVER_CONFIG.fixedRenderDistance.save();
                    context.getSource().getServer().getPlayerList().setViewDistance(renderDistance);
                    context.getSource().sendSuccess(new TextComponent("Successfully set the render distance to " + renderDistance + " chunks"), false);
                    return 1;
                }))
                .then(Commands.literal("auto").executes(context -> {
                    RenderDistance.SERVER_CONFIG.fixedRenderDistance.set(0);
                    RenderDistance.SERVER_CONFIG.fixedRenderDistance.save();
                    context.getSource().sendSuccess(new TextComponent("Successfully set the render distance to auto"), false);
                    return 1;
                }))
        );

        dispatcher.register(literalBuilder);
    }

}
