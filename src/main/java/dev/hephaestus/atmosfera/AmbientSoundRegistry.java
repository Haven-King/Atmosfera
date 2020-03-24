package dev.hephaestus.atmosfera;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

import com.google.gson.JsonObject;

import com.google.gson.JsonParser;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
public class AmbientSoundRegistry {
    private SortedMap<Identifier,AmbientSound> registeredSounds = new TreeMap<>();

    public Collection<AmbientSound> getRegistered() {
        return registeredSounds.values();
    }

    @Deprecated
    public void removeRegistered() {
        registeredSounds = new TreeMap<>();
    }

    public AmbientSoundRegistry() {
        AmbientSoundRegistry registry = this;

        ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener(){
            @Override
            public void apply(ResourceManager manager) {
                Collection<Identifier> resources = manager.findResources("sounds/definitions", (string) -> string.endsWith(".json"));

                for (Identifier r : resources) {
                    try {
                        JsonParser JsonParser = new JsonParser();
                        JsonObject json = (JsonObject)JsonParser.parse(new InputStreamReader(manager.getResource(r).getInputStream()));

                        registry.register(json);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                System.out.println("Atmosfera - Successfully added " + registry.getRegistered().size() + " sound events");
            }

            @Override
            public Collection<Identifier> getFabricDependencies() {
                return Collections.emptyList();
            }

            @Override
            public Identifier getFabricId() {
                return new Identifier("atmosfera:sound_json");
            }
        });
    }

    public void register(JsonObject json) {
        Identifier identifier;
        if (json.get("sound") == null) {
            System.out.println("Atmosfera - no sound provided!");
            throw new IllegalArgumentException();
        } else {
            identifier = new Identifier(json.get("sound").getAsString());
        }

        SoundEvent soundEvent = Registry.SOUND_EVENT.containsId(identifier) ? Registry.SOUND_EVENT.get(identifier) : Registry.register(Registry.SOUND_EVENT, identifier, new SoundEvent(identifier));

        AmbientSound sound = new AmbientSound(soundEvent, json);

        this.registeredSounds.put(identifier, sound);
    }

    public void update(Identifier id) {
        registeredSounds.get(id).max_volume = Atmosfera.CONFIG.get(id);
    }
}
