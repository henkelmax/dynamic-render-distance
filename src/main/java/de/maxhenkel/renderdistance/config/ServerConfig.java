package de.maxhenkel.renderdistance.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.ConfigEntry;
import de.maxhenkel.renderdistance.modes.ScalingSteps;

import java.util.List;

public class ServerConfig {
    public static final int MAX_RENDER = 32;
    public static final int MAX_SIMULATION = 32;

    public final ConfigEntry<Double> minMspt;
    public final ConfigEntry<Double> maxMspt;
    public final ConfigEntry<Integer> tickInterval;
    public final ConfigEntry<Integer> minRenderDistance;
    public final ConfigEntry<Integer> maxRenderDistance;
    public final ConfigEntry<Integer> minSimulationDistance;
    public final ConfigEntry<Integer> maxSimulationDistance;
    public final ConfigEntry<Double> renderToSimulationRatio;
    public final ConfigEntry<Integer> fixedRenderDistance;
    public final ConfigEntry<Integer> fixedSimulationDistance;
    public final ConfigEntry<String> scalingSteps;
    public final ConfigEntry<String> mode;
    private ScalingSteps scalingStepsValue = new ScalingSteps(List.of());

    public ServerConfig(ConfigBuilder builder) {
        minMspt = builder.doubleEntry("min_mspt", 30D, 0D, 1000D);
        maxMspt = builder.doubleEntry("max_mspt", 40D, 0D, 1000D);
        tickInterval = builder.integerEntry("tick_interval", 20 * 10, 20, Integer.MAX_VALUE);
        minRenderDistance = builder.integerEntry("min_render_distance", 10, 1, MAX_RENDER);
        maxRenderDistance = builder.integerEntry("max_render_distance", MAX_RENDER, 1, MAX_RENDER);
        minSimulationDistance = builder.integerEntry("min_simulation_distance", 10, 1, MAX_SIMULATION);
        maxSimulationDistance = builder.integerEntry("max_simulation_distance", MAX_SIMULATION, 1, MAX_SIMULATION);
        renderToSimulationRatio = builder.doubleEntry("render_to_simulation_ratio", 2, 1, 4);
        fixedRenderDistance = builder.integerEntry("fixed_render_distance", 0, 0, MAX_RENDER);
        fixedSimulationDistance = builder.integerEntry("fixed_simulation_distance", 0, 0, MAX_SIMULATION);
        mode = builder.stringEntry("mode", "steps");
        scalingSteps = builder.stringEntry("scaling_steps", "default");
    }

    public void reloadScalingSteps() {
        scalingStepsValue = ScalingSteps.valueOf(scalingSteps.get());
    }

    public ScalingSteps getScalingSteps() {
        return scalingStepsValue;
    }
}
