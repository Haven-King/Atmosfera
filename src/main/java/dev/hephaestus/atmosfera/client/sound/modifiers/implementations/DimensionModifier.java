package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;

public record DimensionModifier(Identifier id) implements AtmosphericSoundModifier, AtmosphericSoundModifier.Factory {
    @Override
    public float getModifier(EnvironmentContext context) {
        return Objects.requireNonNull(Minecraft.getInstance().level).dimension().identifier().equals(id) ? 1 : 0;
    }

    @Override
    public AtmosphericSoundModifier create(Level level) {
        return this;
    }

    public static Factory create(JsonObject object) {
        // Valid options for vanilla are "overworld", "the_nether", and "the_end"
        return new DimensionModifier(Identifier.parse(object.get("id").getAsString()));
    }
}
