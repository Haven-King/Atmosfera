package dev.hephaestus.atmosfera.client.sound;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.AtmosferaConfig;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.client.sound.modifiers.implementations.ConfigModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.SynchronousResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.Locale;
import java.util.Map;

public class SoundDefinitionsReloader implements SynchronousResourceReloader {
    @Override
    public void reload(ResourceManager manager) {
        loadSoundDefinitions(manager, "sounds/ambient", Atmosfera.SOUND_DEFINITIONS);
        loadSoundDefinitions(manager, "sounds/music", Atmosfera.MUSIC_DEFINITIONS);
        AtmosferaConfig.loadedSoundDefinitions();

        var client = MinecraftClient.getInstance();
        if (client != null && client.world != null) {
            client.world.atmosfera$getAtmosphericSoundHandler().reloadDefinitions();
        }
    }

    private static void loadSoundDefinitions(ResourceManager manager, String sourceFolder, Map<Identifier, AtmosphericSoundDefinition> destination) {
        destination.clear();

        Map<Identifier, Resource> resources = manager.findResources(sourceFolder + "/definitions", id -> id.getPath().endsWith(".json"));

        for (Identifier resource : resources.keySet()) {
            Identifier id = Identifier.of(
                    resource.getNamespace(),
                    resource.getPath().substring(
                            resource.getPath().indexOf("definitions/") + "definitions/".length(),
                            resource.getPath().indexOf(".json")
                    )
            );

            try (var reader = resources.get(resource).getReader()) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                Identifier soundId = Identifier.of(JsonHelper.getString(json, "sound"));
                String soundAlias = JsonHelper.getString(json, "sound_alias", null);
                Identifier soundIdAlias = (soundAlias != null ? Identifier.of(soundAlias) : null);

                EnvironmentContext.Shape shape = getShape(json, id);
                EnvironmentContext.Size size = getSize(json, id);
                ImmutableCollection<AtmosphericSoundModifier.Factory> modifiers = getModifiers(json, id);
                int defaultVolume = JsonHelper.getInt(json, "default_volume", 100);
                boolean showSubtitlesByDefault = JsonHelper.getBoolean(json, "default_subtitle", true);

                destination.put(id, new AtmosphericSoundDefinition(id, soundId, soundIdAlias, shape, size, defaultVolume, showSubtitlesByDefault, modifiers));
            } catch (Exception e) {
                Atmosfera.error("Failed to load sound event '{}'", id, e);
            }
        }
    }

    private static EnvironmentContext.Shape getShape(JsonObject json, Identifier id) {
        if (json.has("shape")) {
            return EnvironmentContext.Shape.valueOf(json.getAsJsonPrimitive("shape").getAsString().toUpperCase(Locale.ROOT));
        } else {
            throw new RuntimeException("Sound definition '%s' is missing \"shape\" field.".formatted(id));
        }
    }

    private static EnvironmentContext.Size getSize(JsonObject json, Identifier id) {
        if (json.has("size")) {
            return json.has("size") ? EnvironmentContext.Size.valueOf(json.getAsJsonPrimitive("size").getAsString().toUpperCase(Locale.ROOT)) : EnvironmentContext.Size.MEDIUM;
        } else {
            throw new RuntimeException("Sound definition '%s' is missing \"size\" field.".formatted(id));
        }
    }

    private static ImmutableCollection<AtmosphericSoundModifier.Factory> getModifiers(JsonObject json, Identifier id) {
        var modifiers = ImmutableList.<AtmosphericSoundModifier.Factory>builder();

        modifiers.add(new ConfigModifier(id));

        if (json.has("modifiers")) {
            for (JsonElement element : json.get("modifiers").getAsJsonArray()) {
                JsonObject modifierJson = element.getAsJsonObject();

                if (!modifierJson.has("type")) {
                    throw new RuntimeException("Modifier for sound definition '%s' is missing \"type\" field.".formatted(id));
                }

                String type = modifierJson.get("type").getAsString();
                var factory = AtmosphericSoundModifierRegistry.get(type);

                if (factory == null) {
                    Atmosfera.warn("Modifier type \"{}\" does not exist", type);
                } else {
                    modifiers.add(factory.deserialize(modifierJson));
                }
            }
        }

        return modifiers.build();
    }
}
