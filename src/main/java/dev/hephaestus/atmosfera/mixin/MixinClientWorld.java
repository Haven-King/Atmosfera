package dev.hephaestus.atmosfera.mixin;

import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundHandler;
import dev.hephaestus.atmosfera.client.sound.util.ClientWorldDuck;
import net.minecraft.client.network.ClientPlayNetworkHandler;
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

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public class MixinClientWorld implements ClientWorldDuck {
    private AtmosphericSoundHandler atmosfera$soundHandler;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initializeSoundHandler(ClientPlayNetworkHandler networkHandler, ClientWorld.Properties properties, RegistryKey<World> registryRef, DimensionType dimensionType, int loadDistance, Supplier<Profiler> profiler, WorldRenderer worldRenderer, boolean debugWorld, long seed, CallbackInfo ci) {
        this.atmosfera$soundHandler = new AtmosphericSoundHandler((ClientWorld) (Object) this);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickSoundHandler(BooleanSupplier shouldKeepTicking, CallbackInfo ci) {
        this.atmosfera$soundHandler.tick();
    }

    @Override
    public AtmosphericSoundHandler getHandler() {
        return this.atmosfera$soundHandler;
    }
}
