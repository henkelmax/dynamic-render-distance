package de.maxhenkel.renderdistance.config;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.configbuilder.ConfigEntry;

public class ServerConfig {

    public final ConfigEntry<Double> minMspt;
    public final ConfigEntry<Double> maxMspt;
    public final ConfigEntry<Integer> tickInterval;
    public final ConfigEntry<Integer> minRenderDistance;
    public final ConfigEntry<Integer> maxRenderDistance;
    public final ConfigEntry<Integer> fixedRenderDistance;
    public final ConfigEntry<Boolean> changeSimulationDistance;

    public ServerConfig(ConfigBuilder builder) {
        minMspt = builder.doubleEntry("min_mspt", 30D, 0D, 1000D);
        maxMspt = builder.doubleEntry("max_mspt", 40D, 0D, 1000D);
        tickInterval = builder.integerEntry("tick_interval", 20 * 10, 20, Integer.MAX_VALUE);
        minRenderDistance = builder.integerEntry("min_render_distance", 10, 1, 32);
        maxRenderDistance = builder.integerEntry("max_render_distance", 32, 1, 32);
        fixedRenderDistance = builder.integerEntry("fixed_render_distance", 0, 0, 32);
        changeSimulationDistance = builder.booleanEntry("change_simulation_distance", true);
    }

}
