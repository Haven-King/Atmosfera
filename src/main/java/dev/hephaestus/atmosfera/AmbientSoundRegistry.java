package dev.hephaestus.atmosfera;

import java.util.Collection;
import java.util.HashMap;

import com.google.gson.JsonObject;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

@Environment(EnvType.CLIENT)
public class AmbientSoundRegistry {
    private static HashMap<Identifier,AmbientSound> registeredSounds = new HashMap<>();

    public static Collection<AmbientSound> getRegistered() {
        return registeredSounds.values();
    }

    public static void removeRegistered() {
        registeredSounds = new HashMap<>();
    }

    // This function is much better now.
    public static void register(JsonObject json) {
        Identifier identifier;
        if (json.get("sound") == null) {
            System.out.println("Atmosfera - no sound provided!");
            throw new IllegalArgumentException();
        } else {
            identifier = new Identifier(json.get("sound").getAsString());
        }

        SoundEvent soundEvent = Registry.SOUND_EVENT.containsId(identifier) ? Registry.SOUND_EVENT.get(identifier) : Registry.register(Registry.SOUND_EVENT, identifier, new SoundEvent(identifier));

        AmbientSound sound = new AmbientSound(soundEvent, json);

        registeredSounds.put(identifier, sound);
    }
}
