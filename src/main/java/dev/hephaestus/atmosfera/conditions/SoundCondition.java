package dev.hephaestus.atmosfera.conditions;

import dev.hephaestus.atmosfera.VolumeData;

public abstract class SoundCondition {
    protected boolean isValid;
    public SoundCondition() {
        isValid = false;
    };


    public abstract boolean apply(final VolumeData volume);
}