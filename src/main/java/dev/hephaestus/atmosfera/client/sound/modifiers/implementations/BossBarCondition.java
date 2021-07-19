package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.world.World;

import java.util.regex.Pattern;

public record BossBarCondition(String text, boolean isRegex) implements AtmosphericSoundModifier, AtmosphericSoundModifier.Factory {
    @Override
    public float getModifier(EnvironmentContext context) {
        if (this.isRegex) {
            for (String value : context.getBossBars()) {
                if (Pattern.matches(this.text, value)) return 1;
            }
        } else if (context.getBossBars().contains(this.text)) {
            return 1;
        }

        return 0;
    }

    @Override
    public AtmosphericSoundModifier create(World world) {
        return this;
    }

    public static Factory create(JsonObject object) {
        if(object.has("matches")) {
            return new BossBarCondition(object.get("matches").getAsString(), true);
        } else if(object.has("text")) {
            return new BossBarCondition(object.get("text").getAsString(), false);
        } else {
            throw new RuntimeException("Modifier for 'boss_bar' is missing 'matches' or 'text' field.");
        }
    }
}
