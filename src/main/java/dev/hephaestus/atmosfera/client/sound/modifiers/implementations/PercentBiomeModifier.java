package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public record PercentBiomeModifier(float min, float max, ImmutableCollection<RegistryEntry<Biome>> biomes, ImmutableCollection<TagKey<Biome>> biomeTags) implements AtmosphericSoundModifier {
    public PercentBiomeModifier(float min, float max, ImmutableCollection<RegistryEntry<Biome>> biomes, ImmutableCollection<TagKey<Biome>> biomeTags) {
        ImmutableCollection.Builder<RegistryEntry<Biome>> biomesBuilder = ImmutableList.builder();

        // Remove biomes that are already present in tags so that they aren't counted twice
        biomes:
        for (RegistryEntry<Biome> biomeEntry : biomes) {
            for (TagKey<Biome> tag : biomeTags) {
                if (biomeEntry.isIn(tag)) {
                    continue biomes;
                }
            }

            biomesBuilder.add(biomeEntry);
        }

        this.biomes = biomesBuilder.build();
        this.biomeTags = biomeTags;
        this.min = min;
        this.max = max;
    }

    @Override
    public float getModifier(EnvironmentContext context) {
        float modifier = 0F;

        for (RegistryEntry<Biome> biomeEntry : this.biomes) {
            modifier += context.getBiomePercentage(biomeEntry.value());
        }

        for (TagKey<Biome> tag : this.biomeTags) {
            modifier += context.getBiomeTagPercentage(tag);
        }

        return modifier >= this.min
                ? (modifier - this.min) * (1.0F / (this.max - this.min))
                : 0;
    }

    public static AtmosphericSoundModifier.Factory create(JsonObject object) {

        ImmutableCollection.Builder<Identifier> biomes = ImmutableList.builder();
        ImmutableCollection.Builder<Identifier> tags = ImmutableList.builder();

        JsonHelper.getArray(object, "biomes").forEach(biome -> {
            if (biome.getAsString().startsWith("#")) {
                tags.add(new Identifier(biome.getAsString().substring(1)));
            } else {
                Identifier biomeID = new Identifier(biome.getAsString());
                biomes.add(biomeID);
            }
        });

        float min = 0, max = 1;

        if (object.has("range")) {
            JsonArray array = object.getAsJsonArray("range");
            min = array.get(0).getAsFloat();
            max = array.get(1).getAsFloat();
        }

        return new PercentBiomeModifier.Factory(min, max, biomes.build(), tags.build());
    }

    private record Factory(float min, float max, ImmutableCollection<Identifier> biomes, ImmutableCollection<Identifier> biomeTags) implements AtmosphericSoundModifier.Factory {

        @Override
        public AtmosphericSoundModifier create(World world) {
            ImmutableCollection.Builder<RegistryEntry<Biome>> biomes = ImmutableList.builder();

            Registry<Biome> biomeRegistry = world.getRegistryManager().get(Registry.BIOME_KEY);

            for (Identifier id : this.biomes) {
                Biome biome = biomeRegistry.get(id);

                if (biome != null) {
                    RegistryEntry<Biome> biomeEntry = biomeRegistry.entryOf(biomeRegistry.getKey(biome).get());// should never throw
                    biomes.add(biomeEntry);
                }
            }

            ImmutableCollection.Builder<TagKey<Biome>> tags = ImmutableList.builder();

            for (Identifier id : this.biomeTags) {
                tags.add(TagKey.of(Registry.BIOME_KEY, id));
            }

            return new PercentBiomeModifier(this.min, this.max, biomes.build(), tags.build());
        }
    }
}
