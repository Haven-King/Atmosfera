package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.world.World;

import java.util.function.Function;

public record SimpleBooleanCondition(boolean expectedValue, Function<EnvironmentContext, Boolean> valueGetter) implements AtmosphericSoundModifier, AtmosphericSoundModifier.Factory {

    @Override
    public float getModifier(EnvironmentContext context) {
        return this.valueGetter.apply(context) == this.expectedValue ? 1 : 0;
    }

    @Override
    public AtmosphericSoundModifier create(World world) {
        return this;
    }

    public static SimpleBooleanCondition isDaytime(JsonObject object) {
        return create(object, EnvironmentContext::isDaytime);
    }

    public static SimpleBooleanCondition isRainy(JsonObject object) {
        return create(object, EnvironmentContext::isRainy);
    }

    public static SimpleBooleanCondition isStormy(JsonObject object) {
        return create(object, EnvironmentContext::isStormy);
    }

    public static SimpleBooleanCondition create(JsonObject object, Function<EnvironmentContext, Boolean> valueGetter) {
        boolean value = true;

        if (object.has("value")) {
            value = object.get("value").getAsBoolean();
        }

        return new SimpleBooleanCondition(value, valueGetter);
    }
}
