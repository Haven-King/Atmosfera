package dev.hephaestus.atmosfera.world.context;

import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

abstract class AbstractEnvironmentContext implements EnvironmentContext {
    int altitude = 0;
    int elevation = -1;
    boolean isDay = false;
    boolean isRainy = false;
    boolean isStormy = false;
    @Nullable Entity vehicle = null;
    Collection<String> bossBars;

    @Override
    public float getAltitude() {
        return altitude;
    }

    @Override
    public float getElevation() {
        return elevation;
    }

    @Override
    public boolean isDaytime() {
        return isDay;
    }

    @Override
    public boolean isRainy() {
        return isRainy;
    }

    @Override
    public boolean isStormy() {
        return isStormy;
    }

    @Override
    public @Nullable Entity getVehicle() {
        return vehicle;
    }

    @Override
    public Collection<String> getBossBars() {
        return bossBars;
    }
}
