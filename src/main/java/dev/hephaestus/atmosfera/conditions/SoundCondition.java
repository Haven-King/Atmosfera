package dev.hephaestus.atmosfera.conditions;

import dev.hephaestus.atmosfera.VolumeData;

public abstract class SoundCondition {
    public SoundCondition() {

    };


    public abstract boolean apply(final VolumeData volume);
}