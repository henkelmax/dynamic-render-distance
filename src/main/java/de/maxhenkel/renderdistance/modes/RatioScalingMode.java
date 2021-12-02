package de.maxhenkel.renderdistance.modes;

import de.maxhenkel.renderdistance.RenderDistance;

public class RatioScalingMode implements ScalingMode {
    @Override
    public String name() {
        return "ratio";
    }

    @Override
    public PerfDistance normalize(PerfDistance current) {
        return new SimplePerfDistance(current.getSimulationDistance(), calcView(current.getSimulationDistance()));
    }

    @Override
    public PerfDistance scaleUp(PerfDistance current) {
        int nextSim = limitSimulationDistance(current.getSimulationDistance() + 1);
        if (current.getSimulationDistance() == nextSim) {
            int nextView = limitViewDistance(current.getViewDistance() + 1);
            if (current.getViewDistance() == nextView) {
                return current;
            }
            return new SimplePerfDistance(nextSim, nextView);
        }
        return new SimplePerfDistance(nextSim, calcView(nextSim));
    }

    @Override
    public PerfDistance scaleDown(PerfDistance current) {
        int nextSim = limitSimulationDistance(current.getSimulationDistance() - 1);
        if (current.getSimulationDistance() == nextSim) {
            int nextView = limitViewDistance(current.getViewDistance() - 1);
            if (current.getViewDistance() == nextView) {
                return current;
            }
            return new SimplePerfDistance(nextSim, nextView);
        }
        return new SimplePerfDistance(nextSim, calcView(nextSim));
    }

    private int calcView(int sim) {
        return limitViewDistance((int) (sim * RenderDistance.SERVER_CONFIG.renderToSimulationRatio.get()));
    }
}
