package dev.hephaestus.atmosfera.conditions;

import com.google.gson.JsonObject;

import dev.hephaestus.atmosfera.VolumeData;

public class Height extends BoundedCondition {

    public Height(JsonObject conditions) {
        super(conditions);
    }

    @Override
    protected float getValue(VolumeData volume) {
        return volume.getOrigin().getY();
    }
}