package dev.hephaestus.atmosfera.client.sound;

import com.google.common.collect.ImmutableCollection;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import org.jspecify.annotations.Nullable;

public record AtmosphericSound(Identifier id, Identifier soundId, @Nullable Identifier soundIdAlias,
                               EnvironmentContext.Shape shape, EnvironmentContext.Size size,
                               ImmutableCollection<AtmosphericSoundModifier> modifiers) {
    public float getVolume(ClientWorld world) {
        var context = world.atmosfera$getEnvironmentContext(size, shape);
        if (context == null)
            return 0;

        float volume = 1;
        for (var modifier : modifiers) {
            volume *= modifier.getModifier(context);
        }

        return volume;
    }

    // different sound definitions can play at the same time.
    // this is an issue if one wants to split up definitions into smaller parts, e.g. to give finer control over volume in the config
    // this was done for owls in https://github.com/Haven-King/Atmosfera/pull/26, which ironically increased the amount of hooting tenfold
    // to solve this, owls are aliased to the original sound, so when one plays, the other will not
    public Identifier getAliasedSoundId() {
        return soundIdAlias != null ? soundIdAlias : soundId;
    }
}