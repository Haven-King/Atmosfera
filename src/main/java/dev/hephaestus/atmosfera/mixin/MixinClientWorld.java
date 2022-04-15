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

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeSoundHandler(ClientPlayNetworkHandler netHandler, ClientWorld.Properties properties, RegistryKey registryRef, DimensionType dimensionType, int loadDistance, int simulationDistance, Supplier profiler, WorldRenderer worldRenderer, boolean debugWorld, long seed, CallbackInfo ci) {
        this.atmosfera$soundHandler = new AtmosphericSoundHandler((ClientWorld) (Object) this);
        this.atmosfera$environmentContexts = new HashMap<>();
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
        return switch (shape) {
            case UPPER_HEMISPHERE -> this.atmosfera$environmentContexts.computeIfAbsent(size, Sphere::new).getUpperHemisphere();
            case LOWER_HEMISPHERE -> this.atmosfera$environmentContexts.computeIfAbsent(size, Sphere::new).getLowerHemisphere();
            case SPHERE -> this.atmosfera$environmentContexts.get(size);
        };
    }

    @Override
    public void atmosfera$updateEnvironmentContext() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null && ContextUtil.TASK_QUEUE.isEmpty()) {
            this.atmosfera$environmentContexts.computeIfAbsent(EnvironmentContext.Size.SMALL, Sphere::new).update(player);
            this.atmosfera$environmentContexts.computeIfAbsent(EnvironmentContext.Size.MEDIUM, Sphere::new).update(player);
            this.atmosfera$environmentContexts.computeIfAbsent(EnvironmentContext.Size.LARGE, Sphere::new).update(player);
        }
    }

    @Override
    public boolean atmosfera$isEnvironmentContextInitialized() {
        return !this.atmosfera$environmentContexts.isEmpty() && this.atmosfera$environmentContexts.get(EnvironmentContext.Size.SMALL).getPlayer() != null;
    }
}
