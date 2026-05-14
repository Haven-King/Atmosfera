package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.Bound;
import dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.Range;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

import static dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.getBound;
import static dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.getRange;

public record PercentBiomeModifier(Range range, Bound bound, ImmutableCollection<Holder<Biome>> biomes, ImmutableCollection<TagKey<Biome>> biomeTags) implements AtmosphericSoundModifier {
    public PercentBiomeModifier(Range range, Bound bound, ImmutableCollection<Holder<Biome>> biomes, ImmutableCollection<TagKey<Biome>> biomeTags) {
        var biomesBuilder = ImmutableList.<Holder<Biome>>builder();

        // Remove biomes that are already present in tags so that they aren't counted twice
        biomes:
        for (var biome : biomes) {
            for (var tag : biomeTags) {
                if (biome.is(tag)) {
                    continue biomes;
                }
            }

            biomesBuilder.add(biome);
        }

        this.biomes = biomesBuilder.build();
        this.biomeTags = biomeTags;
        this.range = range;
        this.bound = bound;
    }

    @Override
    public float getModifier(EnvironmentContext context) {
        float modifier = 0;

        for (var biomeEntry : this.biomes) {
            modifier += context.getBiomePercentage(biomeEntry.value());
        }

        for (var tag : this.biomeTags) {
            modifier += context.getBiomeTagPercentage(tag);
        }

        if (bound != null) return bound.apply(modifier);
        if (range != null) return range.apply(modifier);
        return modifier;
    }

    public static AtmosphericSoundModifier.Factory create(JsonObject object) {
        var biomes = ImmutableList.<Identifier>builder();
        var tags = ImmutableList.<Identifier>builder();

        GsonHelper.getAsJsonArray(object, "biomes").forEach(biome -> {
            if (biome.getAsString().startsWith("#")) {
                tags.add(Identifier.parse(biome.getAsString().substring(1)));
            } else {
                biomes.add(Identifier.parse(biome.getAsString()));
            }
        });

        var range = getRange(object);
        var bound = getBound(object);

        return new PercentBiomeModifier.Factory(range, bound, biomes.build(), tags.build());
    }

    private record Factory(Range range, Bound bound, ImmutableCollection<Identifier> biomes, ImmutableCollection<Identifier> biomeTags) implements AtmosphericSoundModifier.Factory {
        @Override
        public AtmosphericSoundModifier create(Level level) {
            var biomes = ImmutableList.<Holder<Biome>>builder();

            var biomeRegistry = level.registryAccess().lookupOrThrow(Registries.BIOME);

            for (var id : this.biomes) {
                biomeRegistry.get(id).ifPresent(biomes::add);
            }

            var tags = ImmutableList.<TagKey<Biome>>builder();

            for (var id : this.biomeTags) {
                tags.add(TagKey.create(Registries.BIOME, id));
            }

            return new PercentBiomeModifier(range, bound, biomes.build(), tags.build());
        }
    }
}
