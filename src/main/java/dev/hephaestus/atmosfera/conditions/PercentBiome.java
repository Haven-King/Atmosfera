package dev.hephaestus.atmosfera.conditions;

import java.util.ArrayList;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import dev.hephaestus.atmosfera.VolumeData;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

public class PercentBiome extends BoundedCondition {
    ArrayList<Biome> biomes = new ArrayList<>();

    public PercentBiome(JsonObject conditions) {
        super(conditions);

        for (JsonElement biome : conditions.getAsJsonArray("biomes") ) {
            Biome b = Registry.BIOME.get(new Identifier(biome.getAsString()));
            if (b == null) { System.out.println("Atmosfera - No such biome: " + biome.getAsString()); }
            else { biomes.add(b); }
        }
    }

    @Override
    protected float getValue(VolumeData volume) {
        return volume.percentBlockBiome(biomes);
    }
}