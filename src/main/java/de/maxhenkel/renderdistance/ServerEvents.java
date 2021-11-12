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

        if (RenderDistance.SERVER_CONFIG.fixedRenderDistance.get() > 0) {
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
                setRenderDistance(playerList, Math.max(playerList.getViewDistance() - 1, minRenderDistance), mspt);
            }
        } else if (mspt < RenderDistance.SERVER_CONFIG.minMspt.get()) {
            if (renderDistance < maxRenderDistance) {
                setRenderDistance(playerList, Math.min(playerList.getViewDistance() + 1, maxRenderDistance), mspt);
            }
        }
    }

    public static void setRenderDistance(PlayerList playerList, int distance) {
        setRenderDistance(playerList, distance, -1D);
    }

    public static void setRenderDistance(PlayerList playerList, int distance, double mspt) {
        playerList.setViewDistance(distance);
        if (mspt < 0D) {
            RenderDistance.LOGGER.info("Set render distance to {}", playerList.getViewDistance());
        } else {
            RenderDistance.LOGGER.info("Set render distance to {} ({} mspt)", playerList.getViewDistance(), mspt);
        }

        if (RenderDistance.SERVER_CONFIG.changeSimulationDistance.get()) {
            RenderDistance.LOGGER.info("Set simulation distance to {}", distance);
            playerList.setSimulationDistance(distance);
        }
    }

    public double getAverageMSPT() {
        return Arrays.stream(ticks).average().orElse(0D) / 1_000_000D;
    }

    public double getAverageTPS() {
        return getTPS(getAverageMSPT());
    }

    public long[] getTicks() {
        return ticks;
    }

    public static double round(double value, int digits) {
        return Math.round(value * Math.pow(10D, digits)) / Math.pow(10D, digits);
    }

    public static double getTPS(double mspt) {
        return Math.min(1000D / mspt, 20D);
    }

}
