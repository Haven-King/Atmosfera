package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.World;

import java.util.function.Function;

public record SimpleBoundedCondition(float min, float max, Function<EnvironmentContext, Number> valueGetter) implements AtmosphericSoundModifier, AtmosphericSoundModifier.Factory {
    @Override
    public float getModifier(EnvironmentContext context) {
        float value = this.valueGetter.apply(context).floatValue();

        return value > this.min && value <= this.max ? 1 : 0;
    }

    @Override
    public final AtmosphericSoundModifier create(World world) {
        return this;
    }

    public static SimpleBoundedCondition altitude(JsonElement element) {
        return create(element, EnvironmentContext::getAltitude);
    }

    public static SimpleBoundedCondition elevation(JsonElement element) {
        return create(element, EnvironmentContext::getElevation);
    }

    public static SimpleBoundedCondition skyVisibility(JsonElement element) {
        return create(element, EnvironmentContext::getSkyVisibility);
    }

    public static SimpleBoundedCondition create(JsonElement element, Function<EnvironmentContext, Number> valueGetter) {
        JsonObject object = element.getAsJsonObject();

        float min = object.has("min") ? JsonHelper.getFloat(object, "min") : -Float.MAX_VALUE;
        float max = object.has("max") ? JsonHelper.getFloat(object, "max") : Float.MAX_VALUE;

        return new SimpleBoundedCondition(min, max, valueGetter);
    }
}
