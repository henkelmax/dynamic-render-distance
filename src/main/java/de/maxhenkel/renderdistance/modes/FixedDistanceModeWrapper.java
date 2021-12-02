package de.maxhenkel.renderdistance.modes;

import de.maxhenkel.renderdistance.RenderDistance;

public class FixedDistanceModeWrapper implements ScalingMode {
    private final ScalingMode mode;

    public FixedDistanceModeWrapper(ScalingMode mode) {this.mode = mode;}

    @Override
    public String name() {
        return "fixed";
    }

    @Override
    public PerfDistance startup() {
        return normalize(mode.startup());
    }

    @Override
    public PerfDistance scaleUp(PerfDistance current) {
        return normalize(mode.scaleUp(current));
    }

    @Override
    public PerfDistance scaleDown(PerfDistance current) {
        return normalize(mode.scaleDown(current));
    }

    @Override
    public PerfDistance normalize(PerfDistance current) {
        Integer fixedSim = RenderDistance.SERVER_CONFIG.fixedSimulationDistance.get();
        Integer fixedView = RenderDistance.SERVER_CONFIG.fixedRenderDistance.get();
        if (fixedSim <= 0 && fixedView <= 0) {
            return mode.normalize(current);
        }
        PerfDistance normalize = mode.normalize(current);
        int correctSim = fixedSim > 0 ? fixedSim : normalize.getSimulationDistance();
        int correctView = fixedView > 0 ? fixedView : normalize.getViewDistance();
        if (correctSim == normalize.getSimulationDistance() && correctView == normalize.getViewDistance()) {
            return normalize;
        }
        return new SimplePerfDistance(correctSim, correctView);
    }

    @Override
    public int limitSimulationDistance(int sim) {
        Integer fixedDistance = RenderDistance.SERVER_CONFIG.fixedSimulationDistance.get();
        return fixedDistance > 0 ? fixedDistance : mode.limitSimulationDistance(sim);
    }

    @Override
    public int limitViewDistance(int view) {
        Integer fixedDistance = RenderDistance.SERVER_CONFIG.fixedRenderDistance.get();
        return fixedDistance > 0 ? fixedDistance : mode.limitViewDistance(view);
    }
}
