package dev.hephaestus.atmosfera.client.sound;

import com.google.common.collect.ImmutableMultimap;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.client.sound.modifiers.implementations.ConfigModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public class AtmosphericSoundSerializer implements SimpleSynchronousResourceReloadListener {
    private final String sourceFolder;
    private final Map<Identifier, AtmosphericSoundDefinition> destination;

    public AtmosphericSoundSerializer(String sourceFolder, Map<Identifier, AtmosphericSoundDefinition> destination) {
        this.sourceFolder = sourceFolder;
        this.destination = destination;
    }

    @Override
    public Identifier getFabricId() {
        return Atmosfera.id(this.sourceFolder);
    }

    @Override
    public void reload(ResourceManager manager) {
        this.destination.clear();

        Collection<Identifier> resources = manager.findResources(this.sourceFolder + "/definitions", (string) -> string.endsWith(".json"));

        JsonParser parser = new JsonParser();

        for (Identifier resource : resources) {
            Identifier id = new Identifier(
                    resource.getNamespace(),
                    resource.getPath().substring(
                            resource.getPath().indexOf("definitions/") + 12,
                            resource.getPath().indexOf(".json")
                    )
            );

            try {
                JsonObject json = parser.parse(new InputStreamReader(manager.getResource(resource).getInputStream())).getAsJsonObject();

                Identifier soundId = new Identifier(JsonHelper.getString(json, "sound"));
                SoundEvent sound = Registry.SOUND_EVENT.containsId(soundId)
                        ? Registry.SOUND_EVENT.get(soundId)
                        : Registry.register(Registry.SOUND_EVENT, soundId, new SoundEvent(soundId));

                if (sound != null) {
                    EnvironmentContext.Shape shape = getShape(json, id);
                    EnvironmentContext.Size size = getSize(json, id);
                    ImmutableMultimap<String, AtmosphericSoundModifier.Factory> modifiers = getModifiers(json, id);
                    int defaultVolume = getInteger(json, "default_volume", 100);
                    boolean showSubtitlesByDefault = getBoolean(json, "show_subtitles_by_default", true);

                    this.destination.put(id, new AtmosphericSoundDefinition(id, sound, shape, size, defaultVolume, showSubtitlesByDefault, modifiers));
                }
            } catch (IOException exception) {
                Atmosfera.LOG.info("[Atmosfera] Failed to load sound event '{}'", id);
            }
        }
    }

    private int getInteger(JsonObject json, String key, int ifAbsent) {
        JsonElement element = json.get(key);
        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
            return element.getAsInt();
        }

        return ifAbsent;
    }

    private boolean getBoolean(JsonObject json, String key, boolean ifAbsent) {
        JsonElement element = json.get(key);

        if (element != null && element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
            return element.getAsBoolean();
        }

        return ifAbsent;
    }

    private static EnvironmentContext.Shape getShape(JsonObject json, Identifier id) {
        if (json.has("shape")) {
            return EnvironmentContext.Shape.valueOf(json.getAsJsonPrimitive("shape").getAsString().toUpperCase(Locale.ROOT));
        } else {
            throw new RuntimeException(String.format("Sound definition '%s' is missing 'shape' field.", id));
        }
    }

    private static EnvironmentContext.Size getSize(JsonObject json, Identifier id) {
        if (json.has("size")) {
            return json.has("size") ? EnvironmentContext.Size.valueOf(json.getAsJsonPrimitive("size").getAsString().toUpperCase(Locale.ROOT)) : EnvironmentContext.Size.MEDIUM;
        } else {
            throw new RuntimeException(String.format("Sound definition '%s' is missing 'size' field.", id));
        }
    }

    private static ImmutableMultimap<String, AtmosphericSoundModifier.Factory> getModifiers(JsonObject json, Identifier id) {
        ImmutableMultimap.Builder<String, AtmosphericSoundModifier.Factory> modifiers = ImmutableMultimap.builder();

        modifiers.put("volume", new ConfigModifier(id));

        if (json.has("modifiers")) {
            for (JsonElement element : json.get("modifiers").getAsJsonArray()) {
                if (!element.getAsJsonObject().has("type")) {
                    throw new RuntimeException(String.format("Modifier for sound definition '%s' is missing 'type' field.", id));
                }

                String type = element.getAsJsonObject().get("type").getAsString();
                AtmosphericSoundModifier.FactoryFactory factory = AtmosphericSoundModifierRegistry
                        .get(type);

                if (factory == null) {
                    Atmosfera.LOG.info("[Atmosfera] Failed to create modifier of type '{}'", type);
                } else {
                    modifiers.put(
                            JsonHelper.getString(element.getAsJsonObject(), "modifies", "volume"),
                            factory.create(element.getAsJsonObject()));
                }
            }
        }

        return modifiers.build();
    }
}
