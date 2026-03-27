package dev.hephaestus.atmosfera.client.sound;

import com.google.common.collect.ImmutableCollection;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.Identifier;

public record AtmosphericSound(Identifier id, Identifier soundId,
                               EnvironmentContext.Shape shape, EnvironmentContext.Size size,
                               ImmutableCollection<AtmosphericSoundModifier> modifiers) {
    public float getVolume(ClientLevel level) {
        var context = level.atmosfera$getEnvironmentContext(size, shape);
        if (context == null)
            return 0;

        float volume = 1;
        for (var modifier : modifiers) {
            volume *= modifier.getModifier(context);
        }

        return volume;
    }
}