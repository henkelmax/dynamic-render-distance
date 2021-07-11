package de.maxhenkel.renderdistance;

import de.maxhenkel.renderdistance.events.TickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;

import java.util.Arrays;

public class ServerEvents {

    private long[] ticks;

    public ServerEvents() {
        TickEvent.SERVER_TICK_TIME.register(this::onServerTickTime);
    }

    private void onServerTickTime(MinecraftServer server, long time, int tick) {
        if (ticks == null) {
            ticks = new long[RenderDistance.SERVER_CONFIG.tickInterval.get()];
        }
        ticks[tick % ticks.length] = time;
        if (tick % ticks.length != 0) {
            return;
        }

        PlayerList playerList = server.getPlayerList();
        if (playerList.getPlayerCount() <= 0) {
            return;
        }
        double mspt = getAverageMSPT();
        int renderDistance = playerList.getViewDistance();
        int minRenderDistance = RenderDistance.SERVER_CONFIG.minRenderDistance.get();
        int maxRenderDistance = RenderDistance.SERVER_CONFIG.maxRenderDistance.get();
        if (mspt > RenderDistance.SERVER_CONFIG.maxMspt.get()) {
            if (renderDistance > minRenderDistance) {
                playerList.setViewDistance(Math.max(playerList.getViewDistance() - 1, minRenderDistance));
                RenderDistance.LOGGER.info("Setting render distance to: {} ({} mspt)", playerList.getViewDistance(), mspt);
            }
        } else if (mspt < RenderDistance.SERVER_CONFIG.minMspt.get()) {
            if (renderDistance < maxRenderDistance) {
                playerList.setViewDistance(Math.min(playerList.getViewDistance() + 1, maxRenderDistance));
                RenderDistance.LOGGER.info("Setting render distance to: {} ({} mspt)", playerList.getViewDistance(), mspt);
            }
        }
    }

    public double getAverageMSPT() {
        return Arrays.stream(ticks).average().orElse(0D) / 1_000_000D;
    }

}
