package de.maxhenkel.renderdistance.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;

public class TickEvent {

    public static final Event<TickTime> SERVER_TICK_TIME = EventFactory.createArrayBacked(TickTime.class, (listeners) -> (server, duration, tick) -> {
        for (TickTime time : listeners) {
            time.tickTime(server, duration, tick);
        }
    });

    @FunctionalInterface
    public interface TickTime {
        void tickTime(MinecraftServer server, long duration, int tick);
    }

}
