package dev.hephaestus.atmosfera;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ClientPlayerTickable;
import net.minecraft.util.Identifier;

import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class AmbientSoundHandler implements ClientPlayerTickable {
    private final MinecraftClient client;

    HashMap<Identifier, AmbientSound> nowPlaying;

    public AmbientSoundHandler(MinecraftClient client) {
        nowPlaying = new HashMap<>();
        this.client = client;
    }

    public void removeAll() {
        for (AmbientSound sound : nowPlaying.values()) {
            this.client.getSoundManager().stop(sound);
        }

        nowPlaying = new HashMap<>();
    }

    public void tick() {
        if (this.client.player != null) {
            for (AmbientSound sound : AmbientSoundRegistry.getRegistered()) {
                Identifier id = sound.getId();
                if (nowPlaying.containsKey(id)) {
                    AmbientSound current = this.nowPlaying.get(id);
                    if (!this.client.getSoundManager().isPlaying(current))
                        this.client.getSoundManager().play(current);
                    
                    current.play();

                    if (current.isDone()) {
                        this.client.getSoundManager().stop(current);
                        this.nowPlaying.remove(id);
                    }
                } else if (sound.shouldPlay()) {
                    AmbientSound newSound = new AmbientSound(sound);
                    this.nowPlaying.put(id, newSound);
                    this.client.getSoundManager().play(newSound);
                }
            }
        }
    }
}
