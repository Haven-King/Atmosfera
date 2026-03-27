package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import java.util.regex.Pattern;
import net.minecraft.world.level.Level;

public record BossBarCondition(String text, Pattern regex) implements AtmosphericSoundModifier, AtmosphericSoundModifier.Factory {
    @Override
    public float getModifier(EnvironmentContext context) {
        if (regex != null) {
            for (String value : context.getBossBars()) {
                if (regex.matcher(value).matches()) return 1;
            }
        } else if (context.getBossBars().contains(text)) {
            return 1;
        }

        return 0;
    }

    @Override
    public AtmosphericSoundModifier create(Level level) {
        return this;
    }

    public static Factory create(JsonObject object) {
        if (object.has("matches")) {
            return new BossBarCondition(null, Pattern.compile(object.get("matches").getAsString()));
        } else if (object.has("text")) {
            return new BossBarCondition(object.get("text").getAsString(), null);
        } else {
            throw new RuntimeException("Modifier for 'boss_bar' is missing 'matches' or 'text' field.");
        }
    }
}
