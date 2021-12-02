package de.maxhenkel.renderdistance.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.maxhenkel.configbuilder.ConfigEntry;
import de.maxhenkel.renderdistance.RenderDistance;
import de.maxhenkel.renderdistance.ServerEvents;
import de.maxhenkel.renderdistance.config.ServerConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;

import java.util.function.Function;
import java.util.function.Supplier;

public class RenderDistanceCommands {

    public static final SimpleCommandExceptionType ERROR_INVALID_RENDER_DISTANCE = new SimpleCommandExceptionType(new TextComponent("Invalid render distance"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, boolean dedicated) {
        LiteralArgumentBuilder<CommandSourceStack> literalBuilder = Commands.literal("renderdistance");

        literalBuilder.then(Commands.literal("current").executes((context) -> {
            context.getSource().sendSuccess(
                    new TextComponent("The current simulation distance is ")
                            .append(new TextComponent(String.valueOf(context.getSource().getServer().getPlayerList().getSimulationDistance())).withStyle(ChatFormatting.GREEN))
                            .append(new TextComponent(" chunks, and current render distance is "))
                            .append(new TextComponent(String.valueOf(context.getSource().getServer().getPlayerList().getViewDistance())).withStyle(ChatFormatting.GREEN))
                            .append(new TextComponent(" chunks"))
                    , false);
            return 1;
        }));

        literalBuilder.then(Commands.literal("fixed").requires((commandSource) -> commandSource.hasPermission(2))
                .then(setDistanceConfigValue("render", config(c -> c.fixedRenderDistance)))
                .then(setDistanceConfigValue("simulation", config(c -> c.fixedSimulationDistance)))
                .then(setRatioConfigValue("ratio", config(c -> c.renderToSimulationRatio)))
        );

        literalBuilder.then(Commands.literal("limit").requires((commandSource) -> commandSource.hasPermission(2))
                .then(setMinMaxConfigValue("render", config(c -> c.minRenderDistance), config(c -> c.maxRenderDistance)))
                .then(setMinMaxConfigValue("simulation", config(c -> c.minSimulationDistance), config(c -> c.maxSimulationDistance)))
        );

        literalBuilder.then(Commands.literal("mspt").executes((context) -> {
            double mspt = ServerEvents.round(RenderDistance.SERVER_EVENTS.getAverageMSPT(), 2);
            context.getSource().sendSuccess(
                    new TextComponent("The average MSPT over ")
                            .append(new TextComponent(String.valueOf(RenderDistance.SERVER_EVENTS.getTicks().length)).withStyle(ChatFormatting.GREEN))
                            .append(new TextComponent(" ticks is "))
                            .append(new TextComponent(String.valueOf(mspt)).withStyle(mspt > 50D ? ChatFormatting.RED : ChatFormatting.GREEN))
                    , false);
            return 1;
        }));

        literalBuilder.then(Commands.literal("tps").executes((context) -> {
            double tps = ServerEvents.round(RenderDistance.SERVER_EVENTS.getAverageTPS(), 2);
            context.getSource().sendSuccess(
                    new TextComponent("The average TPS over ")
                            .append(new TextComponent(String.valueOf(RenderDistance.SERVER_EVENTS.getTicks().length)).withStyle(ChatFormatting.GREEN))
                            .append(new TextComponent(" ticks is "))
                            .append(new TextComponent(String.valueOf(tps)).withStyle(tps < 20D ? ChatFormatting.RED : ChatFormatting.GREEN))
                    , false);
            return 1;
        }));

        dispatcher.register(literalBuilder);
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setDistanceConfigValue(String name, Supplier<ConfigEntry<Integer>> fixedDistanceEntry) {
        return Commands.literal(name).then(Commands.argument("amount", IntegerArgumentType.integer()).executes((context) -> {
            int renderDistance = IntegerArgumentType.getInteger(context, "amount");

            if (renderDistance <= 0 || renderDistance > 32) {
                throw ERROR_INVALID_RENDER_DISTANCE.create();
            }
            fixedDistanceEntry.get().set(renderDistance);
            fixedDistanceEntry.get().save();
            RenderDistance.refreshDistances(context.getSource().getServer().getPlayerList());
            context.getSource().sendSuccess(
                    new TextComponent("Successfully set the " + name + " distance to ")
                            .append(new TextComponent(String.valueOf(renderDistance)).withStyle(ChatFormatting.GREEN))
                            .append(new TextComponent(" chunks"))
                    , false);
            return 1;
        }))
                .then(Commands.literal("auto").executes(context -> {
                    fixedDistanceEntry.get().set(0);
                    fixedDistanceEntry.get().save();
                    context.getSource().sendSuccess(
                            new TextComponent("Successfully set the " + name + " distance to ")
                                    .append(new TextComponent("auto").withStyle(ChatFormatting.GREEN))
                            , false);
                    return 1;
                }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setRatioConfigValue(String name, Supplier<ConfigEntry<Double>> ratioEntry) {
        return Commands.literal(name).then(Commands.argument("ratio", DoubleArgumentType.doubleArg()).executes((context) -> {
            double ratio = DoubleArgumentType.getDouble(context, "ratio");
            ratioEntry.get().set(ratio);
            ratioEntry.get().save();
            RenderDistance.refreshDistances(context.getSource().getServer().getPlayerList());
            context.getSource().sendSuccess(
                    new TextComponent("Successfully set the view to simulation ratio distance to ")
                            .append(new TextComponent(String.valueOf(ratio)).withStyle(ChatFormatting.GREEN))
                            .append(new TextComponent("."))
                    , false);
            return 1;
        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setMinMaxConfigValue(String name, Supplier<ConfigEntry<Integer>> minDistanceEntry, Supplier<ConfigEntry<Integer>> maxDistanceEntry) {
        return Commands.literal(name)
                .then(Commands.argument("min", IntegerArgumentType.integer())
                        .then(Commands.argument("max", IntegerArgumentType.integer())
                                .executes((context) -> {
                                    int min = IntegerArgumentType.getInteger(context, "min");
                                    int max = IntegerArgumentType.getInteger(context, "max");
                                    minDistanceEntry.get().set(min);
                                    minDistanceEntry.get().save();
                                    maxDistanceEntry.get().set(max);
                                    maxDistanceEntry.get().save();
                                    context.getSource().sendSuccess(
                                            new TextComponent("Successfully set the " + name + " distance min/max to ")
                                                    .append(new TextComponent(String.valueOf(min)).withStyle(ChatFormatting.GREEN))
                                                    .append(new TextComponent("-").withStyle(ChatFormatting.GRAY))
                                                    .append(new TextComponent(String.valueOf(max)).withStyle(ChatFormatting.GREEN))
                                                    .append(new TextComponent(" chunks"))
                                            , false);
                                    return 1;
                                })));
    }
    
    private static <T> Supplier<ConfigEntry<T>> config(Function<ServerConfig, ConfigEntry<T>> getter) {
        return () -> getter.apply(RenderDistance.SERVER_CONFIG);
    }
}
