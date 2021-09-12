package de.maxhenkel.renderdistance.config;

import de.maxhenkel.configbuilder.ConfigBuilder;

public class ServerConfig {

    public final ConfigBuilder.ConfigEntry<Double> minMspt;
    public final ConfigBuilder.ConfigEntry<Double> maxMspt;
    public final ConfigBuilder.ConfigEntry<Integer> tickInterval;
    public final ConfigBuilder.ConfigEntry<Integer> minRenderDistance;
    public final ConfigBuilder.ConfigEntry<Integer> maxRenderDistance;
    public final ConfigBuilder.ConfigEntry<Integer> fixedRenderDistance;

    public ServerConfig(ConfigBuilder builder) {
        minMspt = builder.doubleEntry("min_mspt", 30D, 0D, 1000D);
        maxMspt = builder.doubleEntry("max_mspt", 40D, 0D, 1000D);
        tickInterval = builder.integerEntry("tick_interval", 20 * 10, 20, Integer.MAX_VALUE);
        minRenderDistance = builder.integerEntry("min_render_distance", 10, 1, 32);
        maxRenderDistance = builder.integerEntry("max_render_distance", 32, 1, 32);
        fixedRenderDistance = builder.integerEntry("fixed_render_distance", 0, 0, 32);
    }

}
