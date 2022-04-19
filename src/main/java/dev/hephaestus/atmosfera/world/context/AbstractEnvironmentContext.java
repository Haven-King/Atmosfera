package dev.hephaestus.atmosfera.world.context;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.Nullable;

import java.util.*;

abstract class AbstractEnvironmentContext implements EnvironmentContext {
    final ClientPlayerEntity player;
    int altitude = 0;
    int elevation = -1;
    boolean isDay = false;
    boolean isRainy = false;
    boolean isStormy = false;
    @Nullable Entity vehicle = null;
    Collection<String> bossBars;

    protected AbstractEnvironmentContext(ClientPlayerEntity player) {
        this.player = player;
    }

    @Override
    public float getAltitude() {
        return this.altitude;
    }

    @Override
    public float getElevation() {
        return this.elevation;
    }

    @Override
    public boolean isDaytime() {
        return this.isDay;
    }

    @Override
    public boolean isRainy() {
        return this.isRainy;
    }

    @Override
    public boolean isStormy() {
        return this.isStormy;
    }

    @Override
    public @Nullable Entity getVehicle() {
        return this.vehicle;
    }

    @Override
    public ClientPlayerEntity getPlayer() {
        return this.player;
    }

    @Override
    public Collection<String> getBossBars() {
        return this.bossBars;
    }
}
