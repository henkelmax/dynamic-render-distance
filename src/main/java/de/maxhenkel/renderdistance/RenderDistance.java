package de.maxhenkel.renderdistance;

import de.maxhenkel.configbuilder.ConfigBuilder;
import de.maxhenkel.renderdistance.command.RenderDistanceCommands;
import de.maxhenkel.renderdistance.config.ServerConfig;
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

    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register(RenderDistanceCommands::register);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if (server instanceof DedicatedServer) {
                SERVER_CONFIG = ConfigBuilder.build(server.getServerDirectory().toPath().resolve("config").resolve(MODID).resolve("renderdistance-server.properties"), ServerConfig::new);
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> refreshDistances(server.getPlayerList()));
        SERVER_EVENTS = new ServerEvents();
    }

    public static void refreshDistances(PlayerList playerList) {
        ServerConfig config = RenderDistance.SERVER_CONFIG;
        int simulation = config.fixedSimulationDistance.get();
        int render = config.fixedRenderDistance.get();
        if (simulation > 0) {
            if (simulation != playerList.getSimulationDistance())
                playerList.setSimulationDistance(simulation);
        }
        if (render > 0) {
            if (render != playerList.getViewDistance())
                playerList.setViewDistance(render);
        } else {
            double ratio = config.renderToSimulationRatio.get();
            if (ratio < 1) {
                return;
            }
            int newDistance = (int) Math.round(playerList.getSimulationDistance() * ratio);
            newDistance = Math.max(newDistance, RenderDistance.SERVER_CONFIG.minRenderDistance.get());
            newDistance = Math.min(newDistance, RenderDistance.SERVER_CONFIG.maxRenderDistance.get());
            if (newDistance != playerList.getViewDistance())
                playerList.setViewDistance(newDistance);
        }
    }
}
