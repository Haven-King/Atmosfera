package dev.hephaestus.atmosfera.mixin;

import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundHandler;
import dev.hephaestus.atmosfera.client.sound.util.ClientWorldDuck;
import dev.hephaestus.atmosfera.world.context.ContextUtil;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext.Size;
import dev.hephaestus.atmosfera.world.context.Sphere;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.EnumMap;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public class MixinClientWorld implements ClientWorldDuck {
    private AtmosphericSoundHandler atmosfera$soundHandler;
    private EnumMap<Size, Sphere> atmosfera$environmentContexts;
    private boolean atmosfera$initialized;
    private int atmosfera$updateTimer = 0;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeSoundHandler(ClientPlayNetworkHandler netHandler, ClientWorld.Properties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> registryEntry, int loadDistance, int simulationDistance, Supplier<Profiler> profiler, WorldRenderer worldRenderer, boolean debugWorld, long seed, CallbackInfo ci) {
        atmosfera$soundHandler = new AtmosphericSoundHandler((ClientWorld) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickSoundHandler(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
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
            var player = Objects.requireNonNull(MinecraftClient.getInstance().player);
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
