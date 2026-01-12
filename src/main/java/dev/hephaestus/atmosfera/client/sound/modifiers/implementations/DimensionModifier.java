package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.Objects;

public record DimensionModifier(Identifier id) implements AtmosphericSoundModifier, AtmosphericSoundModifier.Factory {
    @Override
    public float getModifier(EnvironmentContext context) {
        return Objects.requireNonNull(MinecraftClient.getInstance().world).getRegistryKey().getValue().equals(id) ? 1 : 0;
    }

    @Override
    public AtmosphericSoundModifier create(World world) {
        return this;
    }

    public static Factory create(JsonObject object) {
        // Valid options for vanilla are "overworld", "the_nether", and "the_end"
        return new DimensionModifier(Identifier.of(object.get("id").getAsString()));
    }
}
