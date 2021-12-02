package de.maxhenkel.renderdistance.modes;

import de.maxhenkel.renderdistance.RenderDistance;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ScalingModes {
    private final Map<String, ScalingMode> modes = new HashMap<>();

    {
        addScalingMode(new RatioScalingMode());
        addScalingMode(new StepsScalingMode());
    }

    public Set<String> getModes() {
        return modes.keySet();
    }

    public void addScalingMode(ScalingMode scalingMode) {
        this.modes.put(scalingMode.name(), wrap(scalingMode));
    }

    public ScalingMode wrap(ScalingMode scalingMode) {
        // fixed might be more important, but doing big jumps can cause timeouts to all players so big jump preventions takes priority over fixed, 
        // making changes to fixed range will result in fluent slow change to target value instead of 1 big lag.
        return new PreventBigJumpsModeWrapper(new FixedDistanceModeWrapper(scalingMode));
    }

    public ScalingMode getScalingMode(String name) {
        return this.modes.get(name);
    }

    public ScalingMode getScalingMode() {
        return getScalingMode(RenderDistance.SERVER_CONFIG.mode.get());
    }
}
