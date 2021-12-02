package de.maxhenkel.renderdistance.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.maxhenkel.configbuilder.ConfigEntry;
import de.maxhenkel.renderdistance.RenderDistance;
import de.maxhenkel.renderdistance.ServerEvents;
import de.maxhenkel.renderdistance.config.ServerConfig;
import de.maxhenkel.renderdistance.modes.ScalingSteps;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public class RenderDistanceCommands {

    public static final SimpleCommandExceptionType ERROR_INVALID_RENDER_DISTANCE = new SimpleCommandExceptionType(new TextComponent("Invalid render distance"));
    public static final SimpleCommandExceptionType ERROR_INVALID_STEPS = new SimpleCommandExceptionType(new TextComponent("Invalid steps syntax, it should be comma separated array of simulation to render distances, like: 2:3,3:3-6,4:6,..."));

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

        literalBuilder.then(Commands.literal("set").requires((commandSource) -> commandSource.hasPermission(2))
                .then(setDistanceConfigValue("render", config(c -> c.fixedRenderDistance)))
                .then(setDistanceConfigValue("simulation", config(c -> c.fixedSimulationDistance)))
                .then(setRatioConfigValue())
                .then(setScalingStepsConfigValue())
                .then(setScalingModeConfigValue())
        );

        literalBuilder.then(Commands.literal("limit").requires((commandSource) -> commandSource.hasPermission(2))
                .then(setMinMaxConfigValue("render", config(c -> c.minRenderDistance), config(c -> c.maxRenderDistance)))
                .then(setMinMaxConfigValue("simulation", config(c -> c.minSimulationDistance), config(c -> c.maxSimulationDistance)))
                .then(setMinMaxMsptConfigValue(config(c -> c.minMspt), config(c -> c.maxMspt)))
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

            if (renderDistance <= 0 || renderDistance > ServerConfig.MAX_RENDER) {
                throw ERROR_INVALID_RENDER_DISTANCE.create();
            }
            fixedDistanceEntry.get().set(renderDistance);
            fixedDistanceEntry.get().save();
            RenderDistance.refreshDistances();
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

    private static LiteralArgumentBuilder<CommandSourceStack> setRatioConfigValue() {
        return Commands.literal("ratio").then(Commands.argument("ratio", DoubleArgumentType.doubleArg()).executes((context) -> {
            double ratio = DoubleArgumentType.getDouble(context, "ratio");
            RenderDistance.SERVER_CONFIG.renderToSimulationRatio.set(ratio);
            RenderDistance.SERVER_CONFIG.renderToSimulationRatio.save();
            RenderDistance.refreshDistances();
            context.getSource().sendSuccess(
                    new TextComponent("Successfully set the view to simulation ratio distance to ")
                            .append(new TextComponent(String.valueOf(ratio)).withStyle(ChatFormatting.GREEN))
                            .append(new TextComponent("."))
                    , false);
            return 1;
        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setScalingModeConfigValue() {
        LiteralArgumentBuilder<CommandSourceStack> literal = Commands.literal("mode");
        for (String mode : RenderDistance.scalingModes.getModes()) {
            literal.then(Commands.literal(mode).executes((context) -> {
                RenderDistance.SERVER_CONFIG.mode.set(mode);
                RenderDistance.SERVER_CONFIG.mode.save();
                context.getSource().sendSuccess(
                        new TextComponent("Successfully set the scaling mode to ")
                                .append(new TextComponent(mode).withStyle(ChatFormatting.GREEN))
                                .append(new TextComponent("."))
                        , false);
                return 1;
            }));
        }
        return literal;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setScalingStepsConfigValue() {
        return Commands.literal("scalingSteps").then(Commands.argument("scalingSteps", StringArgumentType.string()).executes((context) -> {
            String scalingSteps = StringArgumentType.getString(context, "scalingSteps");
            try {
                ScalingSteps validatedSteps = ScalingSteps.valueOf(scalingSteps);
                assert validatedSteps.size() != 0;
                RenderDistance.SERVER_CONFIG.scalingSteps.set(scalingSteps);
                RenderDistance.SERVER_CONFIG.scalingSteps.save();
                RenderDistance.SERVER_CONFIG.reloadScalingSteps();
            } catch (Exception suppressed) {
                CommandSyntaxException commandSyntaxException = ERROR_INVALID_STEPS.create();
                commandSyntaxException.addSuppressed(suppressed);
                throw commandSyntaxException;
            }
            context.getSource().sendSuccess(
                    new TextComponent("Successfully set the steps to ")
                            .append(new TextComponent(String.valueOf(RenderDistance.SERVER_CONFIG.getScalingSteps())).withStyle(ChatFormatting.GREEN))
                            .append(new TextComponent("."))
                    , false);
            return 1;
        }));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setMinMaxConfigValue(String name, Supplier<ConfigEntry<Integer>> minDistanceEntry, Supplier<ConfigEntry<Integer>> maxDistanceEntry) {
        return setMinMaxConfigValue(Integer.class, name, minDistanceEntry, maxDistanceEntry, (min, max) ->
                new TextComponent("Successfully set the " + name + " distance min/max to ")
                        .append(new TextComponent(String.valueOf(min)).withStyle(ChatFormatting.GREEN))
                        .append(new TextComponent("-").withStyle(ChatFormatting.GRAY))
                        .append(new TextComponent(String.valueOf(max)).withStyle(ChatFormatting.GREEN))
                        .append(new TextComponent(" chunks"))
        );
    }

    private static LiteralArgumentBuilder<CommandSourceStack> setMinMaxMsptConfigValue(Supplier<ConfigEntry<Double>> minEntry, Supplier<ConfigEntry<Double>> maxEntry) {
        return setMinMaxConfigValue(Double.class, "mspt", minEntry, maxEntry, (min, max) ->
                new TextComponent("Successfully set the mspt min/max to ")
                        .append(new TextComponent(String.valueOf(min)).withStyle(ChatFormatting.GREEN))
                        .append(new TextComponent("-").withStyle(ChatFormatting.GRAY))
                        .append(new TextComponent(String.valueOf(max)).withStyle(ChatFormatting.GREEN))
                        .append(new TextComponent("ms per tick."))
        );
    }

    private static final Map<Class<?>, ArgumentType<?>> argumentTypes = Map.of(
            Integer.class, IntegerArgumentType.integer(),
            Double.class, DoubleArgumentType.doubleArg()
    );

    private static <T> LiteralArgumentBuilder<CommandSourceStack> setMinMaxConfigValue(Class<T> type, String name, Supplier<ConfigEntry<T>> minEntry, Supplier<ConfigEntry<T>> maxEntry, BiFunction<T, T, Component> successMessage) {
        return Commands.literal(name)
                .then(Commands.argument("min", argumentTypes.get(type))
                        .then(Commands.argument("max", argumentTypes.get(type))
                                .executes((context) -> {
                                    T min = context.getArgument("min", type);
                                    T max = context.getArgument("max", type);
                                    minEntry.get().set(min);
                                    minEntry.get().save();
                                    maxEntry.get().set(max);
                                    maxEntry.get().save();
                                    context.getSource().sendSuccess(successMessage.apply(min, max), false);
                                    return 1;
                                })));
    }

    private static <T> Supplier<ConfigEntry<T>> config(Function<ServerConfig, ConfigEntry<T>> getter) {
        return () -> getter.apply(RenderDistance.SERVER_CONFIG);
    }
}
