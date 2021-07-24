package de.maxhenkel.renderdistance;

import de.maxhenkel.renderdistance.command.RenderDistanceCommands;
import de.maxhenkel.renderdistance.config.ConfigBuilder;
import de.maxhenkel.renderdistance.config.ServerConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.dedicated.DedicatedServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RenderDistance implements DedicatedServerModInitializer {

    public static final String MODID = "renderdistance";
    public static final Logger LOGGER = LogManager.getLogger(MODID);
    public static ServerConfig SERVER_CONFIG;

    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register(RenderDistanceCommands::register);
        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            if (server instanceof DedicatedServer) {
                ConfigBuilder.create(server.getServerDirectory().toPath().resolve("config").resolve(MODID).resolve("renderdistance-server.properties"), builder -> SERVER_CONFIG = new ServerConfig(builder));
            }
        });
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            int distance = SERVER_CONFIG.fixedRenderDistance.get();
            if (distance > 0) {
                LOGGER.info("Set render distance to {}", distance);
                server.getPlayerList().setViewDistance(distance);
            }
        });
        new ServerEvents();
    }
}
