package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public record DimensionEffectsModifier(Identifier skyProperties) implements AtmosphericSoundModifier, AtmosphericSoundModifier.Factory {
    @Override
    public float getModifier(EnvironmentContext context) {
        return context.getPlayer().getWorld().getDimension().effects().equals(this.skyProperties) ? 1 : 0;
    }

    @Override
    public AtmosphericSoundModifier create(World world) {
        return this;
    }

    public static Factory create(JsonObject object) {
        // Valid options here for vanilla are "overworld", "the_nether", and "the_end"
        return new DimensionEffectsModifier(new Identifier(object.get("id").getAsString()));
    }
}
