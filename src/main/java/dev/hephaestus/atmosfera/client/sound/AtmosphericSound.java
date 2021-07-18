package dev.hephaestus.atmosfera.client.sound;

import com.google.common.collect.ImmutableCollection;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.client.sound.util.ClientWorldDuck;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public record AtmosphericSound(Identifier id, SoundEvent soundEvent,
                                         EnvironmentContext.Shape shape,
                                         EnvironmentContext.Size size,
                                         int defaultVolume, boolean hasSubtitleByDefault,
                                         ImmutableCollection<AtmosphericSoundModifier> modifiers) {
    public float getVolume(ClientWorld world) {
        float volume = 1F;

        for (AtmosphericSoundModifier modifier : this.modifiers) {
            volume *= modifier.getModifier(((ClientWorldDuck) world).atmosfera$getEnvironmentContext(this.size, this.shape));
        }

        return volume;
    }
}