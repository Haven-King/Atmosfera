package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.Bound;
import dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.Range;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import java.util.function.Function;
import net.minecraft.world.level.Level;

import static dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.getBound;
import static dev.hephaestus.atmosfera.client.sound.modifiers.CommonAttributes.getRange;

public record SimpleBoundedCondition(Range range, Bound bound, Function<EnvironmentContext, Number> valueGetter) implements AtmosphericSoundModifier, AtmosphericSoundModifier.Factory {
    @Override
    public float getModifier(EnvironmentContext context) {
        float value = valueGetter.apply(context).floatValue();

        return range.apply(bound.apply(value));
    }

    @Override
    public AtmosphericSoundModifier create(Level level) {
        return this;
    }

    public static SimpleBoundedCondition altitude(JsonObject object) {
        return create(object, EnvironmentContext::getAltitude);
    }

    public static SimpleBoundedCondition elevation(JsonObject object) {
        return create(object, EnvironmentContext::getElevation);
    }

    public static SimpleBoundedCondition skyVisibility(JsonObject object) {
        return create(object, EnvironmentContext::getSkyVisibility);
    }

    public static SimpleBoundedCondition create(JsonObject object, Function<EnvironmentContext, Number> valueGetter) {
        var range = getRange(object);
        var bound = getBound(object);

        return new SimpleBoundedCondition(range, bound, valueGetter);
    }
}
