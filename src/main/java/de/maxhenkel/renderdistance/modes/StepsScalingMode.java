package de.maxhenkel.renderdistance.modes;

import de.maxhenkel.renderdistance.RenderDistance;
import de.maxhenkel.renderdistance.modes.ScalingSteps.ScalingStep;
import de.maxhenkel.renderdistance.config.ServerConfig;

public class StepsScalingMode implements ScalingMode {
    private int currentStep;

    @Override
    public String name() {
        return "steps";
    }

    @Override
    public PerfDistance startup() {
        currentStep = RenderDistance.SERVER_CONFIG.getScalingSteps().getSteps().size() / 3;
        return current();
    }

    public ScalingStep current() {
        ScalingSteps scalingSteps = RenderDistance.SERVER_CONFIG.getScalingSteps();
        return scalingSteps.get(this.currentStep = Math.min(scalingSteps.size() - 1, Math.max(this.currentStep, 0)));
    }

    @Override
    public PerfDistance scaleUp(PerfDistance current) {
        return move(current, 1);
    }

    @Override
    public PerfDistance scaleDown(PerfDistance current) {
        return move(current, -1);
    }

    private ScalingStep move(PerfDistance from, int by) {
        if (by == 0) return current();
        int direction = by / Math.abs(by);
        ScalingStep current = current();

        // to prevent increases while limits are active
        if (direction == 1) {
            if (limitSimulationDistance(current.getSimulationDistance()) > from.getSimulationDistance() ||
                    limitViewDistance(current.getViewDistance()) > from.getViewDistance()) {
                return current;
            }
        } else {
            if (limitSimulationDistance(current.getSimulationDistance()) < from.getSimulationDistance() ||
                    limitViewDistance(current.getViewDistance()) < from.getViewDistance()) {
                return current;
            }
        }

        currentStep += by;
        ScalingStep next = current();
        if (current == next) {
            return next;
        }
        if (current.equals(next)) {
            return move(from, direction);
        }
        if (!wouldSwitchingToStepDoAnything(next, from)) {
            return move(from, direction);
        }
        return next;
    }

    private boolean wouldSwitchingToStepDoAnything(ScalingStep step, PerfDistance from) {
        int curView = from.getViewDistance();
        int curSim = from.getSimulationDistance();
        ServerConfig config = RenderDistance.SERVER_CONFIG;
        int nextView = Math.min(Math.max(step.getViewDistance(), config.minRenderDistance.get()), config.maxRenderDistance.get());
        int nextSim = Math.min(Math.max(step.getSimulationDistance(), config.minSimulationDistance.get()), config.maxSimulationDistance.get());
        return curView != nextView || curSim != nextSim;
    }
}
