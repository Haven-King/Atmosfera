package dev.hephaestus.atmosfera.client.sound.modifiers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

public class CommonAttributes {
    public record Bound(float min, float max) {
        public float apply(float x) {
            return (min <= x && x <= max ? 1 : 0);
        }
    }

    public static boolean hasBound(JsonObject object) {
        return object.has("min") || object.has("max");
    }

    @Nullable
    public static Bound getBound(JsonObject object) {
        if (!hasBound(object))
            return null;

        if (hasRange(object))
            throw new AssertionError("\"max\" or \"min\" cannot be combined with \"range\"");

        float min = object.has("min") ? object.get("min").getAsFloat() : -Float.MAX_VALUE;
        float max = object.has("max") ? object.get("max").getAsFloat() : Float.MAX_VALUE;
        return new Bound(min, max);
    }

    public record Range(float lower, float upper) {
        public float apply(float x) {
            float y = (x - lower) / (upper - lower);
            if (y <= 0) return 0;
            if (y >= 1) return 1;
            return y;
        }
    }

    public static boolean hasRange(JsonObject object) {
        return object.has("range");
    }

    @Nullable
    public static Range getRange(JsonObject object) {
        if (!hasRange(object))
            return null;

        if (hasBound(object))
            throw new AssertionError("\"range\" cannot be combined with \"max\" or \"min\"");

        JsonArray array = object.getAsJsonArray("range");
        float lower = array.get(0).getAsFloat();
        float upper = array.get(1).getAsFloat();

        if (lower == upper)
            throw new IllegalArgumentException("\"range\" lower and upper cannot be equal");

        return new Range(lower, upper);
    }
}
