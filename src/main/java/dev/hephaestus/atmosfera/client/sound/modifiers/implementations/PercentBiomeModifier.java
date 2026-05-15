package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.Bound;
import dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.Range;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import static dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.getBound;
import static dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.getRange;

public record PercentBiomeModifier(Range range, Bound bound, ImmutableCollection<RegistryEntry<Biome>> biomes, ImmutableCollection<TagKey<Biome>> biomeTags, ImmutableCollection<Biome.Category> biomeCategories) implements AtmosphericSoundModifier {
    @SuppressWarnings("deprecation")
    public PercentBiomeModifier(Range range, Bound bound, ImmutableCollection<RegistryEntry<Biome>> biomes, ImmutableCollection<TagKey<Biome>> biomeTags, ImmutableCollection<Biome.Category> biomeCategories) {
        var biomesBuilder = ImmutableList.<RegistryEntry<Biome>>builder();

        // Remove biomes that are already present in tags so that they aren't counted twice
        biomes:
        for (var biomeEntry : biomes) {
            for (var tag : biomeTags) {
                if (biomeEntry.isIn(tag) || biomeCategories.contains(Biome.getCategory(biomeEntry))) {
                    continue biomes;
                }
            }

            biomesBuilder.add(biomeEntry);
        }

        this.biomes = biomesBuilder.build();
        this.biomeTags = biomeTags;
        this.biomeCategories = biomeCategories;
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

        for (Biome.Category category : this.biomeCategories) {
            modifier += context.getBiomeCategoryPercentage(category);
        }

        if (bound != null) return bound.apply(modifier);
        if (range != null) return range.apply(modifier);
        return modifier;
    }

    public static AtmosphericSoundModifier.Factory create(JsonObject object) {
        var biomes = ImmutableList.<Identifier>builder();
        var tags = ImmutableList.<Identifier>builder();
        var categories = ImmutableList.<Biome.Category>builder();

        JsonHelper.getArray(object, "biomes").forEach(biome -> {
            if (biome.getAsString().startsWith("#")) {
                String categoryOrTagName = biome.getAsString().substring(1);

                for (Biome.Category category : Biome.Category.values()) {
                    if (categoryOrTagName.equalsIgnoreCase(category.getName())) {
                        categories.add(category);
                        return;
                    }
                }

                tags.add(new Identifier(biome.getAsString().substring(1)));
            } else {
                biomes.add(new Identifier(biome.getAsString()));
            }
        });

        var range = getRange(object);
        var bound = getBound(object);

        return new PercentBiomeModifier.Factory(range, bound, biomes.build(), tags.build(), categories.build());
    }

    private record Factory(Range range, Bound bound, ImmutableCollection<Identifier> biomes, ImmutableCollection<Identifier> biomeTags, ImmutableCollection<Biome.Category> biomeCategories) implements AtmosphericSoundModifier.Factory {
        @Override
        public AtmosphericSoundModifier create(World world) {
            var biomes = ImmutableList.<RegistryEntry<Biome>>builder();

            var biomeRegistry = world.getRegistryManager().get(Registry.BIOME_KEY);

            for (var id : this.biomes) {
                var biome = biomeRegistry.get(id);
                if (biome != null) {
                    var biomeEntry = biomeRegistry.entryOf(biomeRegistry.getKey(biome).get()); // should never throw
                    biomes.add(biomeEntry);
                }
            }

            var tags = ImmutableList.<TagKey<Biome>>builder();

            for (var id : this.biomeTags) {
                tags.add(TagKey.of(Registry.BIOME_KEY, id));
            }

            return new PercentBiomeModifier(range, bound, biomes.build(), tags.build(), biomeCategories);
        }
    }
}
