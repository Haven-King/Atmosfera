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
                    this.nowPlaying.get(id).play(this.client.player);
                    
                    if (this.nowPlaying.get(id).isDone()) {
                        this.client.getSoundManager().stop(this.nowPlaying.get(id));
                        // this.nowPlaying.get(id).reset();
                        this.nowPlaying.remove(id);
                    }
                } else if (sound.conditions(this.client.player)) {
                    try {
                        this.nowPlaying.put(id, new AmbientSound(sound));
                        this.nowPlaying.get(id).setPlayer(this.client.player);
                        this.client.getSoundManager().play(this.nowPlaying.get(id));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
