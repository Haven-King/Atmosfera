package dev.hephaestus.atmosfera.mixin;

import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundHandler;
import dev.hephaestus.atmosfera.client.sound.util.ClientWorldDuck;
import dev.hephaestus.atmosfera.world.context.ContextUtil;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext.Size;
import dev.hephaestus.atmosfera.world.context.Sphere;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

@Mixin(ClientLevel.class)
public class MixinClientWorld implements ClientWorldDuck {
    private AtmosphericSoundHandler atmosfera$soundHandler;
    private EnumMap<Size, Sphere> atmosfera$environmentContexts;
    private boolean atmosfera$initialized;
    private int atmosfera$updateTimer = 0;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeSoundHandler(CallbackInfo ci) {
        atmosfera$soundHandler = new AtmosphericSoundHandler((ClientLevel) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickSoundHandler(CallbackInfo ci) {
        atmosfera$soundHandler.tick();
    }

    @Override
    public AtmosphericSoundHandler atmosfera$getAtmosphericSoundHandler() {
        return atmosfera$soundHandler;
    }

    @Override
    public EnvironmentContext atmosfera$getEnvironmentContext(Size size, EnvironmentContext.Shape shape) {
        if (!atmosfera$isEnvironmentContextInitialized()) return null;
        return switch (shape) {
            case UPPER_HEMISPHERE -> atmosfera$environmentContexts.get(size).getUpperHemisphere();
            case LOWER_HEMISPHERE -> atmosfera$environmentContexts.get(size).getLowerHemisphere();
            case SPHERE -> atmosfera$environmentContexts.get(size);
        };
    }

    @Override
    public void atmosfera$updateEnvironmentContext() {
        if (!atmosfera$initialized) {
            atmosfera$environmentContexts = new EnumMap<>(Size.class);
            atmosfera$environmentContexts.put(Size.SMALL,  new Sphere(Size.SMALL));
            atmosfera$environmentContexts.put(Size.MEDIUM, new Sphere(Size.MEDIUM));
            atmosfera$environmentContexts.put(Size.LARGE,  new Sphere(Size.LARGE));
            atmosfera$initialized = true;
        }

        if (--atmosfera$updateTimer <= 0 && ContextUtil.EXECUTOR.getQueue().isEmpty()) {
            var player = Objects.requireNonNull(Minecraft.getInstance().player);
            atmosfera$environmentContexts.get(Size.SMALL ).update(player);
            atmosfera$environmentContexts.get(Size.MEDIUM).update(player);
            atmosfera$environmentContexts.get(Size.LARGE ).update(player);
            atmosfera$updateTimer = 20;
        }
    }

    @Override
    public boolean atmosfera$isEnvironmentContextInitialized() {
        return atmosfera$initialized;
    }
}
