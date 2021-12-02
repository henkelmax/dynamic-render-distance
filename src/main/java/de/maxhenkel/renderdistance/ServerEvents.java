package de.maxhenkel.renderdistance;

import de.maxhenkel.renderdistance.events.TickEvent;
import de.maxhenkel.renderdistance.modes.PerfDistance;
import de.maxhenkel.renderdistance.modes.ScalingModes;
import de.maxhenkel.renderdistance.modes.SimplePerfDistance;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.players.PlayerList;

import java.util.Arrays;

public class ServerEvents {

    private long[] ticks;
    private final ScalingModes modes;

    public ServerEvents(ScalingModes modes) {
        this.modes = modes;
        TickEvent.SERVER_TICK_TIME.register(this::onServerTickTime);
    }

    private void onServerTickTime(MinecraftServer server, long time, int tick) {
        if (ticks == null) {
            ticks = new long[RenderDistance.SERVER_CONFIG.tickInterval.get()];
            RenderDistance.refreshDistances(modes.getScalingMode().startup());
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
        PerfDistance distance;
        if (mspt > RenderDistance.SERVER_CONFIG.maxMspt.get()) {
            distance = modes.getScalingMode().scaleDown(new SimplePerfDistance(playerList.getSimulationDistance(), playerList.getViewDistance()));
        } else if (mspt < RenderDistance.SERVER_CONFIG.minMspt.get()) {
            distance = modes.getScalingMode().scaleUp(new SimplePerfDistance(playerList.getSimulationDistance(), playerList.getViewDistance()));
        } else {
            return;
        }
        if (RenderDistance.refreshDistances(distance)) {
            RenderDistance.LOGGER.info("Set simulation distance to {} (render: {}) ({} mspt)", playerList.getSimulationDistance(), playerList.getViewDistance(), mspt);
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
