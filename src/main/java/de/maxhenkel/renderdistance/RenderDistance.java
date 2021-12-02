package de.maxhenkel.renderdistance;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.renderdistance.command.RenderDistanceCommands;
import de.maxhenkel.renderdistance.config.ServerConfig;
import de.maxhenkel.renderdistance.modes.PerfDistance;
import de.maxhenkel.renderdistance.modes.ScalingModes;
import de.maxhenkel.renderdistance.modes.SimplePerfDistance;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.server.players.PlayerList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RenderDistance implements DedicatedServerModInitializer {

    public static final String MODID = "renderdistance";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static ServerConfig SERVER_CONFIG;
    public static ServerEvents SERVER_EVENTS;
    public static ScalingModes scalingModes;
    private static PlayerList playerList;

    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register(RenderDistanceCommands::register);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            playerList = server.getPlayerList();
            if (server instanceof DedicatedServer) {
                SERVER_CONFIG = ConfigBuilder.build(server.getServerDirectory().toPath().resolve("config").resolve(MODID).resolve("renderdistance-server.properties"), ServerConfig::new);
                SERVER_CONFIG.reloadScalingSteps();
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            playerList = server.getPlayerList();
            refreshDistances();
        });
        scalingModes = new ScalingModes();
        SERVER_EVENTS = new ServerEvents(scalingModes);
    }

    public static PerfDistance current() {
        return new SimplePerfDistance(playerList.getSimulationDistance(), playerList.getViewDistance());
    }

    public static void refreshDistances() {
        refreshDistances(current());
    }

    public static boolean refreshDistances(PerfDistance distance) {
        PerfDistance normalized = scalingModes.getScalingMode().normalize(distance);

        boolean anythingChanged = false;
        if (normalized.getSimulationDistance() != playerList.getSimulationDistance()) {
            playerList.setSimulationDistance(normalized.getSimulationDistance());
            anythingChanged = true;
        }
        if (normalized.getViewDistance() != playerList.getViewDistance()) {
            playerList.setViewDistance(normalized.getViewDistance());
            anythingChanged = true;
        }
        return anythingChanged;
    }
}
