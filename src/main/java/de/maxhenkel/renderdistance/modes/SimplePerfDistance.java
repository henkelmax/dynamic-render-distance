package de.maxhenkel.renderdistance.modes;

public record SimplePerfDistance(int simulationDistance, int viewDistance) implements PerfDistance {
    @Override
    public int getViewDistance() {
        return viewDistance;
    }

    @Override
    public int getSimulationDistance() {
        return simulationDistance;
    }
}
