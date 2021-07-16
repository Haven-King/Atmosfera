package dev.hephaestus.atmosfera.client.sound.modifiers;

import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.world.World;

public interface AtmosphericSoundModifier {
    float getModifier(EnvironmentContext context);

    interface Factory {
        AtmosphericSoundModifier create(World world);
    }

    interface FactoryFactory {
        AtmosphericSoundModifier.Factory create(JsonObject object);
    }
}
