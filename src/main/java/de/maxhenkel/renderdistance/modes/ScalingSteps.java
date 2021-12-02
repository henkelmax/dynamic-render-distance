package de.maxhenkel.renderdistance.modes;

import de.maxhenkel.renderdistance.config.ServerConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ScalingSteps {
    private final List<ScalingStep> steps;

    public ScalingSteps(List<ScalingStep> steps) {
        if (steps.isEmpty()) {
            steps = DEFAULT_STEPS;
        }
        this.steps = steps;
    }

    public ScalingStep get(int mode) {
        return this.steps.get(mode);
    }

    public static ScalingSteps valueOf(String toParse) {
        if (toParse.trim().isEmpty() || toParse.equalsIgnoreCase("default")) {
            return new ScalingSteps(List.of());
        }
        return new ScalingSteps(parse(toParse));
    }

    /*
     Simple step format, where each element describe continuous range of possible view distances per simulation distance,
     each element have a format of either `<simulation>:<view>` or `<simulation>:<viewMin>..<viewMax>`, elements are separated by comma,
     additionally last element can be `...` to just generate rest of elements up to max of 32 by just incrementing both numbers each step.
     */
    private static List<ScalingStep> parse(String toParse) {
        String[] elements = toParse.trim().split("[ |,;]");
        List<ScalingStep> steps = new ArrayList<>(elements.length);
        for (int j = 0; j < elements.length; j++) {
            String element = elements[j];
            if (element.equals("...") && j == elements.length - 1) {
                ScalingStep lastStep = steps.get(steps.size() - 1);
                for (int sim = lastStep.simulationDistance + 1, view = lastStep.viewDistance; sim <= ServerConfig.MAX_SIMULATION || view <= ServerConfig.MAX_RENDER; sim++, view++) {
                    steps.add(new ScalingStep(Math.min(sim, ServerConfig.MAX_SIMULATION), Math.min(view, ServerConfig.MAX_RENDER)));
                }
                break;
            }
            String[] stepString = element.trim().split("[:]");
            int simDistance = Integer.parseInt(stepString[0]);
            String[] viewDistances = stepString[1].split("(-|\\.{1,3})", 2);
            int from = Integer.parseInt(viewDistances[0]);
            int to = viewDistances.length == 1 ? from : Integer.parseInt(viewDistances[1]);
            for (int i = from; i <= to; i++) {
                steps.add(new ScalingStep(simDistance, i));
            }
        }
        return steps;
    }

    public List<ScalingStep> getSteps() {
        return steps;
    }

    public int size() {
        return steps.size();
    }

    @Override
    public String toString() {
        StringBuilder toString = new StringBuilder(steps.size() * 7);
        int startView = -1;
        int maxContView = 0;
        int lastSim = 0;
        for (ScalingStep step : steps) {
            if (lastSim == 0) {
                lastSim = step.simulationDistance;
            } else if (lastSim != step.simulationDistance) {
                appendStep(toString, startView, maxContView, lastSim).append(',');
                startView = -1;
            }
            if (startView == -1) {
                startView = step.viewDistance;
                maxContView = startView;
            } else if (step.viewDistance == maxContView + 1) {
                maxContView = step.viewDistance;
            } else {
                appendStep(toString, startView, maxContView, lastSim).append(',');
                startView = step.viewDistance;
                maxContView = startView;
            }
            lastSim = step.simulationDistance;
        }
        appendStep(toString, startView, maxContView, lastSim);
        return toString.toString();
    }

    private StringBuilder appendStep(StringBuilder toString, int minView, int maxView, int lastSim) {
        toString.append(lastSim)
                .append(':')
                .append(minView);
        if (minView != maxView) {
            toString.append("..")
                    .append(maxView);
        }
        return toString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScalingSteps that = (ScalingSteps) o;
        return Objects.equals(steps, that.steps);
    }

    @Override
    public int hashCode() {
        return Objects.hash(steps);
    }

    public static class ScalingStep implements PerfDistance, Comparable<PerfDistance> {
        final int simulationDistance;
        final int viewDistance;

        public ScalingStep(int simulationDistance, int viewDistance) {
            this.simulationDistance = simulationDistance;
            this.viewDistance = viewDistance;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ScalingStep that = (ScalingStep) o;
            return viewDistance == that.viewDistance && simulationDistance == that.simulationDistance;
        }

        @Override
        public int hashCode() {
            return Objects.hash(viewDistance, simulationDistance);
        }

        @Override
        public String toString() {
            return simulationDistance + ":" + viewDistance;
        }

        @Override
        public int getViewDistance() {
            return viewDistance;
        }

        @Override
        public int getSimulationDistance() {
            return simulationDistance;
        }

        @Override
        public int compareTo(PerfDistance o) {
            return Integer.compare(this.simulationDistance, o.getSimulationDistance());
        }
    }

    public static final String DEFAULT_STEPS_STRING = "2:2..5,3:6..7,4:8,5:9..10,6:12,7:14,8:16,8:18,8:20,8:22,9:22,10:22..32,11:32,...";

    public static final List<ScalingStep> DEFAULT_STEPS = parse(DEFAULT_STEPS_STRING);
}
