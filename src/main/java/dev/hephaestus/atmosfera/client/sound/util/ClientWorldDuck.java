package dev.hephaestus.atmosfera.client.sound.util;

import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundHandler;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;

public interface ClientWorldDuck {
    default AtmosphericSoundHandler atmosfera$getAtmosphericSoundHandler() {
        return null;
    }

    default EnvironmentContext atmosfera$getEnvironmentContext(EnvironmentContext.Size size, EnvironmentContext.Shape shape) {
        return null;
    }

    default void atmosfera$updateEnvironmentContext() { }

    default boolean atmosfera$isEnvironmentContextInitialized() {
        return false;
    }
}
