package dev.hephaestus.atmosfera.client.sound;

import com.google.common.collect.ImmutableMultimap;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public record AtmosphericSoundDefinition(Identifier id, SoundEvent soundEvent,
                                         EnvironmentContext.Shape shape,
                                         EnvironmentContext.Size size,
                                         int defaultVolume, boolean hasSubtitleByDefault,
                                         ImmutableMultimap<String, AtmosphericSoundModifier.Factory> modifiers) {
}
