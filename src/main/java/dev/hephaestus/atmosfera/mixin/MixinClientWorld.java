package dev.hephaestus.atmosfera.mixin;

import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundHandler;
import dev.hephaestus.atmosfera.client.sound.util.ClientWorldDuck;
import dev.hephaestus.atmosfera.world.context.ContextUtil;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import dev.hephaestus.atmosfera.world.context.Sphere;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
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

import java.util.HashMap;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public class MixinClientWorld implements ClientWorldDuck {
    private AtmosphericSoundHandler atmosfera$soundHandler;
    private HashMap<EnvironmentContext.Size, Sphere> atmosfera$environmentContexts;
    private boolean atmosfera$initialized;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeSoundHandler(ClientPlayNetworkHandler netHandler, ClientWorld.Properties properties, RegistryKey<World> registryRef, RegistryEntry<DimensionType> registryEntry, int loadDistance, int simulationDistance, Supplier<Profiler> profiler, WorldRenderer worldRenderer, boolean debugWorld, long seed, CallbackInfo ci) {
        this.atmosfera$soundHandler = new AtmosphericSoundHandler((ClientWorld) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickSoundHandler(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        this.atmosfera$soundHandler.tick();
    }

    @Override
    public AtmosphericSoundHandler atmosfera$getAtmosphericSoundHandler() {
        return this.atmosfera$soundHandler;
    }

    @Override
    public EnvironmentContext atmosfera$getEnvironmentContext(EnvironmentContext.Size size, EnvironmentContext.Shape shape) {
        if(!atmosfera$isEnvironmentContextInitialized()) return null;
        return switch (shape) {
            case UPPER_HEMISPHERE -> this.atmosfera$environmentContexts.get(size).getUpperHemisphere();
            case LOWER_HEMISPHERE -> this.atmosfera$environmentContexts.get(size).getLowerHemisphere();
            case SPHERE -> this.atmosfera$environmentContexts.get(size);
        };
    }

    @Override
    public void atmosfera$updateEnvironmentContext() {
        if(!this.atmosfera$initialized) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            this.atmosfera$environmentContexts = new HashMap<>();
            this.atmosfera$environmentContexts.put(EnvironmentContext.Size.SMALL, new Sphere(EnvironmentContext.Size.SMALL, player));
            this.atmosfera$environmentContexts.put(EnvironmentContext.Size.MEDIUM, new Sphere(EnvironmentContext.Size.MEDIUM, player));
            this.atmosfera$environmentContexts.put(EnvironmentContext.Size.LARGE, new Sphere(EnvironmentContext.Size.LARGE, player));
            // set initialized last to prevent race condition for filling the hash map
            atmosfera$initialized = true;
        }
        if (ContextUtil.TASK_QUEUE.isEmpty()) {
            this.atmosfera$environmentContexts.get(EnvironmentContext.Size.SMALL).update();
            this.atmosfera$environmentContexts.get(EnvironmentContext.Size.MEDIUM).update();
            this.atmosfera$environmentContexts.get(EnvironmentContext.Size.LARGE).update();
        }
    }

    @Override
    public boolean atmosfera$isEnvironmentContextInitialized() {
        return atmosfera$initialized;
    }
}
