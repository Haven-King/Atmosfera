package dev.hephaestus.atmosfera.conditions;

import com.google.gson.JsonObject;

import dev.hephaestus.atmosfera.VolumeData;

public abstract class BoundedCondition extends SoundCondition {
    protected float more = Float.NEGATIVE_INFINITY;
    protected float less = Float.POSITIVE_INFINITY;

    public BoundedCondition(JsonObject conditions) {
        super();
        if (conditions.getAsJsonObject().get("more") != null) { this.more = conditions.getAsJsonObject().get("more").getAsFloat(); }
        if (conditions.getAsJsonObject().get("less") != null) { this.less = conditions.getAsJsonObject().get("less").getAsFloat(); }
    }

    @Override
    public boolean apply(VolumeData volume) {
        float percent = getValue(volume);
        return percent > more && percent < less;
    }

    protected abstract float getValue(VolumeData volume);
}