package de.maxhenkel.renderdistance.mixin;

import de.maxhenkel.renderdistance.events.TickEvent;
import net.minecraft.commands.CommandSource;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.FrameTimer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin implements CommandSource {

    @Shadow
    private int tickCount;

    @Redirect(method = "tickServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/FrameTimer;logFrameDuration(J)V"))
    private void injected(FrameTimer timer, long duration) {
        CommandSource srv = this;
        TickEvent.SERVER_TICK_TIME.invoker().tickTime((MinecraftServer) srv, duration, tickCount);
        timer.logFrameDuration(duration);
    }

}
