package de.maxhenkel.renderdistance.modes;

import de.maxhenkel.renderdistance.RenderDistance;

public interface ScalingMode {
    String name();

    default PerfDistance startup() {
        return normalize(new SimplePerfDistance(limitSimulationDistance(5), limitViewDistance(10)));
    }

    PerfDistance scaleUp(PerfDistance current);

    PerfDistance scaleDown(PerfDistance current);

    default PerfDistance normalize(PerfDistance current) {
        int limitedSim = limitSimulationDistance(current.getSimulationDistance());
        int limitedView = limitViewDistance(current.getViewDistance());
        if (limitedSim == current.getSimulationDistance() && limitedView == current.getViewDistance()) {
            return current;
        }
        return new SimplePerfDistance(limitedSim, limitedView);
    }

    default int limitSimulationDistance(int sim) {
        return Math.max(Math.min(sim, RenderDistance.SERVER_CONFIG.maxSimulationDistance.get()), RenderDistance.SERVER_CONFIG.minSimulationDistance.get());
    }

    default int limitViewDistance(int view) {
        return Math.max(Math.min(view, RenderDistance.SERVER_CONFIG.maxRenderDistance.get()), RenderDistance.SERVER_CONFIG.minRenderDistance.get());
    }
}
