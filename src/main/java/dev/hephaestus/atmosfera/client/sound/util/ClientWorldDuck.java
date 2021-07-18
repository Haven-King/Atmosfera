package dev.hephaestus.atmosfera.client.sound.util;

import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundHandler;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;

public interface ClientWorldDuck {
    AtmosphericSoundHandler atmosfera$getAtmosphericSoundHandler();

    EnvironmentContext atmosfera$getEnvironmentContext(EnvironmentContext.Size size, EnvironmentContext.Shape shape);

    void atmosfera$updateEnvironmentContext();

    boolean atmosfera$isEnvironmentContextInitialized();
}
