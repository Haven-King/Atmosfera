package dev.hephaestus.atmosfera.client.sound;

import com.google.common.collect.ImmutableCollection;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

public record AtmosphericSoundDefinition(Identifier id, Identifier soundId, @Nullable Identifier soundIdAlias,
                                         EnvironmentContext.Shape shape, EnvironmentContext.Size size,
                                         int defaultVolume, boolean hasSubtitleByDefault,
                                         ImmutableCollection<AtmosphericSoundModifier.Factory> modifierFactories) {
}
