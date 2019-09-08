package dev.hephaestus.atmosfera.conditions;

import com.google.gson.JsonObject;

import dev.hephaestus.atmosfera.VolumeData;

public class Daytime extends SoundCondition {
    boolean daytime;
    public Daytime(JsonObject conditions, boolean daytime) {
        super();
        this.daytime = daytime;
        isValid = true;
    }

    @Override
    public boolean apply(VolumeData volume) {
        if (daytime) {
            return volume.getWorld().getTimeOfDay() > 450 && volume.getWorld().getTimeOfDay() < 11616;
        } else {
            return volume.getWorld().getTimeOfDay() < 450 || volume.getWorld().getTimeOfDay() > 11616;
        }
    }
}