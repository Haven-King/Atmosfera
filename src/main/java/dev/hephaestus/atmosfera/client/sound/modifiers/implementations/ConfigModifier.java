package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import dev.hephaestus.atmosfera.AtmosferaConfig;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public record ConfigModifier(Identifier soundId) implements AtmosphericSoundModifier, AtmosphericSoundModifier.Factory {
    @Override
    public float getModifier(EnvironmentContext context) {
        return AtmosferaConfig.volumeModifier(soundId);
    }

    @Override
    public AtmosphericSoundModifier create(Level level) {
        return this;
    }
}
