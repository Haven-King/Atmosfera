package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import dev.hephaestus.atmosfera.AtmosferaConfig;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public record ConfigModifier(Identifier sound) implements AtmosphericSoundModifier, AtmosphericSoundModifier.Factory {
    @Override
    public float getModifier(EnvironmentContext context) {
        return AtmosferaConfig.volumeModifier(this.sound);
    }

    @Override
    public AtmosphericSoundModifier create(World world) {
        return this;
    }
}
