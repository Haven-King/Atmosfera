package dev.hephaestus.atmosfera.conditions;

import com.google.gson.JsonObject;

import dev.hephaestus.atmosfera.VolumeData;

public class DistanceFromGround extends BoundedCondition {

    public DistanceFromGround(JsonObject conditions) {
        super(conditions);
        isValid = true;
    }

    @Override
    protected float getValue(VolumeData volume) {
        return volume.distanceFromGround();
    }
}