package dev.hephaestus.atmosfera.client.sound;

import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.client.sound.modifiers.implementations.*;

import java.util.HashMap;
import java.util.Map;

public final class AtmosphericSoundModifierRegistry {
    private static final Map<String, AtmosphericSoundModifier.FactoryFactory> FACTORIES = new HashMap<>();

    private AtmosphericSoundModifierRegistry() {}

    public static void register(AtmosphericSoundModifier.FactoryFactory factory, String... keys) {
        for (String key : keys) {
            FACTORIES.putIfAbsent(key, factory);
        }
    }

    /**
     * Used to replace existing factories. Be careful!
     */
    public static void set(AtmosphericSoundModifier.FactoryFactory factory, String... keys) {
        for (String key : keys) {
            FACTORIES.put(key, factory);
        }
    }

    public static AtmosphericSoundModifier.FactoryFactory get(String type) {
        return FACTORIES.get(type);
    }

    static {
        register(SimpleBoundedCondition::altitude, "altitude", "distance_from_ground");
        register(SimpleBoundedCondition::elevation, "elevation", "height");
        register(SimpleBoundedCondition::skyVisibility, "sky_visibility", "percent_sky_visible");
        register(SimpleBooleanCondition::isDaytime, "is_daytime");
        register(SimpleBooleanCondition::isRainy, "is_rainy");
        register(SimpleBooleanCondition::isStormy, "is_stormy");
        register(PercentBlockModifier::create, "percent_block");
        register(PercentBiomeModifier::create, "percent_biome");
        register(RidingCondition::create, "vehicle", "riding");
        register(SkyPropertiesModifier::create, "sky_properties");
        register(BossBarCondition::create, "boss_bar");
    }
}
