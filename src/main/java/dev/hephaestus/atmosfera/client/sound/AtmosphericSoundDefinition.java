package dev.hephaestus.atmosfera.client.sound;

import com.google.common.collect.ImmutableCollection;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public record AtmosphericSoundDefinition(Identifier id, Identifier soundId,
                                         EnvironmentContext.Shape shape,
                                         EnvironmentContext.Size size,
                                         int defaultVolume, boolean hasSubtitleByDefault,
                                         ImmutableCollection<AtmosphericSoundModifier.Factory> modifiers) {
}
