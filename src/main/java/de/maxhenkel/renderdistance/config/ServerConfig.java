package de.maxhenkel.renderdistance.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.ConfigEntry;

public class ServerConfig {

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

    public ServerConfig(ConfigBuilder builder) {
        minMspt = builder.doubleEntry("min_mspt", 30D, 0D, 1000D);
        maxMspt = builder.doubleEntry("max_mspt", 40D, 0D, 1000D);
        tickInterval = builder.integerEntry("tick_interval", 20 * 10, 20, Integer.MAX_VALUE);
        minRenderDistance = builder.integerEntry("min_render_distance", 10, 1, 32);
        maxRenderDistance = builder.integerEntry("max_render_distance", 32, 1, 32);
        minSimulationDistance = builder.integerEntry("min_simulation_distance", 10, 1, 32);
        maxSimulationDistance = builder.integerEntry("max_simulation_distance", 32, 1, 32);
        renderToSimulationRatio = builder.doubleEntry("render_to_simulation_ratio", 2, 1, 4);
        fixedRenderDistance = builder.integerEntry("fixed_render_distance", 0, 0, 32);
        fixedSimulationDistance = builder.integerEntry("fixed_simulation_distance", 0, 0, 32);
    }

}
