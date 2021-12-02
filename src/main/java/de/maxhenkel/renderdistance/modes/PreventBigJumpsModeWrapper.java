package de.maxhenkel.renderdistance.modes;

import de.maxhenkel.renderdistance.RenderDistance;

// big jumps (~linear to amount of chunks) can cause big lag when switching, so I limit increases to update up to 256 chunks per jump 
// (a difference between 31 and 32 distance)
public class PreventBigJumpsModeWrapper implements ScalingMode {
    private static final int MAX_UPDATE = 256;
    private final ScalingMode mode;

    public PreventBigJumpsModeWrapper(ScalingMode mode) {
        this.mode = mode;
    }

    @Override
    public String name() {
        return "noJumps";
    }

    @Override
    public PerfDistance startup() {
        return normalize(mode.startup());
    }

    @Override
    public PerfDistance scaleUp(PerfDistance current) {
        return filtered(mode.scaleUp(current), current);
    }

    @Override
    public PerfDistance scaleDown(PerfDistance current) {
        return filtered(mode.scaleDown(current), current);
    }

    private PerfDistance filtered(PerfDistance next, PerfDistance current) {
        PerfDistance filtered = next;
        int simChunkDiff = calculateChunkDifference(current.getSimulationDistance(), next.getSimulationDistance());
        int viewChunkDiff = calculateChunkDifference(current.getViewDistance(), next.getViewDistance());
        if (simChunkDiff > MAX_UPDATE || viewChunkDiff > MAX_UPDATE) {
            filtered = new SimplePerfDistance(
                    findAllowedUpdate(current.getSimulationDistance(), filtered.getSimulationDistance()),
                    findAllowedUpdate(current.getViewDistance(), filtered.getViewDistance())
            );
        }
        return filtered;
    }

    private int findAllowedUpdate(int from, int to) {
        if (from == to) return from;
        int direction = (to - from) / Math.abs(to - from);
        int allowedUpdate = from;
        int totalChunkDifference;
        do {
            allowedUpdate += direction;
            totalChunkDifference = calculateChunkDifference(from, allowedUpdate);
        } while (totalChunkDifference < MAX_UPDATE && allowedUpdate < to);
        return allowedUpdate;
    }

    @Override
    public PerfDistance normalize(PerfDistance current) {
        return filtered(mode.normalize(current), RenderDistance.current());
    }

    private int calculateChunkDifference(int from, int to) {
        return Math.abs(calculateChunksPerDistance(to) - calculateChunksPerDistance(from));
    }

    private int calculateChunksPerDistance(int distance) {
        int size = distance * 2 + 1;
        return size * size;
    }
}
