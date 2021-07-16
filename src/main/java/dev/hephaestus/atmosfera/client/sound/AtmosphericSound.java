package dev.hephaestus.atmosfera.client.sound;

import com.google.common.collect.ImmutableMultimap;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public record AtmosphericSound(Identifier id, SoundEvent soundEvent,
                                         EnvironmentContext.Shape shape,
                                         EnvironmentContext.Size size,
                                         int defaultVolume, boolean hasSubtitleByDefault,
                                         ImmutableMultimap<String, AtmosphericSoundModifier> modifiers) {
    public float getVolume() {
        float volume = 1F;

        for (AtmosphericSoundModifier modifier : this.modifiers.get("volume")) {
            volume *= modifier.getModifier(EnvironmentContext.getInstance(this.size, this.shape));
        }

        return volume;
    }

    public float getPitch() {
        float pitch = 1F;

        for (AtmosphericSoundModifier modifier : this.modifiers.get("pitch")) {
            pitch *= modifier.getModifier(EnvironmentContext.getInstance(this.size, this.shape));
        }

        return pitch;
    }
}