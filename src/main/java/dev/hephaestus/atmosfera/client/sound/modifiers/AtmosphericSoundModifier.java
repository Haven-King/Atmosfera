package dev.hephaestus.atmosfera.client.sound.modifiers;

import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.world.level.Level;

public interface AtmosphericSoundModifier {
    float getModifier(EnvironmentContext context);

    interface Factory {
        AtmosphericSoundModifier create(Level level);
    }

    interface FactoryDeserializer {
        AtmosphericSoundModifier.Factory deserialize(JsonObject object);
    }
}
