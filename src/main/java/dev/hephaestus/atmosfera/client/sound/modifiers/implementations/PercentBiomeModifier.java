package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.fabricmc.fabric.api.tag.TagFactory;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

public record PercentBiomeModifier(float min, float max, ImmutableCollection<Biome> biomes, ImmutableCollection<Tag.Identified<Biome>> biomeTags, ImmutableCollection<Biome.Category> biomeCategories) implements AtmosphericSoundModifier {
    public PercentBiomeModifier(float min, float max, ImmutableCollection<Biome> biomes, ImmutableCollection<Tag.Identified<Biome>> biomeTags, ImmutableCollection<Biome.Category> biomeCategories) {
        ImmutableCollection.Builder<Biome> biomesBuilder = ImmutableList.builder();

        // Remove blocks that are already present in tags so that they aren't counted twice
        biomes:
        for (Biome biome : biomes) {
            for (Tag<Biome> tag : biomeTags) {
                if (tag.contains(biome) || biomeCategories.contains(biome.getCategory())) {
                    continue biomes;
                }
            }

            biomesBuilder.add(biome);
        }

        this.biomes = biomesBuilder.build();
        this.biomeTags = biomeTags;
        this.biomeCategories = biomeCategories;
        this.min = min;
        this.max = max;
    }

    @Override
    public float getModifier(EnvironmentContext context) {
        float modifier = 0F;

        for (Biome biome : this.biomes) {
            modifier += context.getBiomePercentage(biome);
        }

        for (Tag.Identified<Biome> tag : this.biomeTags) {
            modifier += context.getBiomeTagPercentage(tag);
        }

        for (Biome.Category category : this.biomeCategories) {
            modifier += context.getBiomeCategoryPercentage(category);
        }

        return modifier >= this.min
                ? (modifier - this.min) * (1.0F / (this.max - this.min))
                : 0;
    }

    public static AtmosphericSoundModifier.Factory create(JsonObject object) {

        ImmutableCollection.Builder<Identifier> biomes = ImmutableList.builder();
        ImmutableCollection.Builder<Identifier> tags = ImmutableList.builder();
        ImmutableCollection.Builder<Biome.Category> categories = ImmutableList.builder();

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

        return new Factory(min, max, biomes.build(), tags.build(), categories.build());
    }

    private record Factory(float min, float max, ImmutableCollection<Identifier> biomes, ImmutableCollection<Identifier> biomeTags, ImmutableCollection<Biome.Category> biomeCategories) implements AtmosphericSoundModifier.Factory {

        @Override
        public AtmosphericSoundModifier create(World world) {
            ImmutableCollection.Builder<Biome> biomes = ImmutableList.builder();

            for (Identifier id : this.biomes) {
                Biome biome = world.getRegistryManager().get(Registry.BIOME_KEY).get(id);

                if (biome != null) {
                    biomes.add(biome);
                }
            }

            ImmutableCollection.Builder<Tag.Identified<Biome>> tags = ImmutableList.builder();

            for (Identifier id : this.biomeTags) {
                tags.add(TagFactory.BIOME.create(id));
            }

            return new PercentBiomeModifier(this.min, this.max, biomes.build(), tags.build(), this.biomeCategories);
        }
    }
}
