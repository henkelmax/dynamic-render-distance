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

        if (RenderDistance.SERVER_CONFIG.fixedSimulationDistance.get() > 0 && RenderDistance.SERVER_CONFIG.fixedRenderDistance.get() > 0) {
            return;
        }

        PlayerList playerList = server.getPlayerList();
        if (playerList.getPlayerCount() <= 0) {
            return;
        }
        double mspt = getAverageMSPT();
        int simulationDistance = playerList.getSimulationDistance();
        int minSimulationDistance = RenderDistance.SERVER_CONFIG.minSimulationDistance.get();
        int maxSimulationDistance = RenderDistance.SERVER_CONFIG.maxSimulationDistance.get();
        if (mspt > RenderDistance.SERVER_CONFIG.maxMspt.get()) {
            if (simulationDistance > minSimulationDistance) {
                setSimulationDistance(playerList, Math.max(playerList.getSimulationDistance() - 1, minSimulationDistance), mspt);
            }
        } else if (mspt < RenderDistance.SERVER_CONFIG.minMspt.get()) {
            if (simulationDistance < maxSimulationDistance) {
                setSimulationDistance(playerList, Math.min(playerList.getSimulationDistance() + 1, maxSimulationDistance), mspt);
            }
        }
    }

    public static void setSimulationDistance(PlayerList playerList, int distance) {
        setSimulationDistance(playerList, distance, -1D);
    }

    public static void setSimulationDistance(PlayerList playerList, int distance, double mspt) {
        if (RenderDistance.SERVER_CONFIG.fixedSimulationDistance.get() < 1) {
            playerList.setSimulationDistance(distance);
            RenderDistance.refreshDistances(playerList);
            if (mspt < 0D) {
                RenderDistance.LOGGER.info("Set simulation distance to {} (render: {})", playerList.getSimulationDistance(), playerList.getViewDistance());
            } else {
                RenderDistance.LOGGER.info("Set simulation distance to {}  (render: {}) ({} mspt)", playerList.getSimulationDistance(), playerList.getViewDistance(), mspt);
            }
        } else
            RenderDistance.refreshDistances(playerList);
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
