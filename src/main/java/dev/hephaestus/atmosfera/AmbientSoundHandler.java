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

    public void tick() {
        if (this.client.player != null) {
            for (AmbientSound sound : AmbientSoundRegistry.getRegistered()) {
                Identifier id = sound.getId();
                if (nowPlaying.containsKey(id)) {
                    AmbientSound current = this.nowPlaying.get(id);
                    if (this.client.getSoundManager().isPlaying(current)) {
                        current.play();
                    } else {
                        this.client.getSoundManager().play(current);
                        current.play();
                    }
                    
                    if (current.isDone()) {
                        this.client.getSoundManager().stop(current);
                        this.nowPlaying.remove(id);
                    }
                } else if (sound.conditions(this.client.player)) {
                    try {
                        AmbientSound newSound = new AmbientSound(sound);
                        this.nowPlaying.put(id, newSound);
                        newSound.setPlayer(this.client.player);
                        this.client.getSoundManager().play(newSound);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
