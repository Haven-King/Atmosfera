package dev.hephaestus.atmosfera.util;

import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundDefinition;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundInstance;
import dev.hephaestus.atmosfera.world.AtmosphericSoundContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.Tickable;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class AtmosphericSoundHandler implements Tickable {
	private final ClientPlayerEntity player;
	private final MinecraftClient client;
	private final HashMap<Identifier, AtmosphericSoundInstance> instances = new HashMap<>();

	public AtmosphericSoundHandler(ClientPlayerEntity player) {
		this.player = player;
		this.client = MinecraftClient.getInstance();
	}

	@Override
	public void tick() {
		SoundManager soundManager = this.client.getSoundManager();
		AtmosphericSoundContext.updateContext(player);
		for (AtmosphericSoundDefinition definition : Atmosfera.SOUND_DEFINITIONS.values()) {
			if (!instances.containsKey(definition.getId()) || instances.get(definition.getId()).isDone()) {
				float volume = definition.getVolume();
				if (volume > 0) {
					AtmosphericSoundInstance soundInstance = new AtmosphericSoundInstance(definition, 0.0001F);
					this.instances.put(definition.getId(), soundInstance);
					soundManager.playNextTick(soundInstance);
				}
			}
		}

		Collection<Identifier> done = new LinkedList<>();
		for (Map.Entry<Identifier, AtmosphericSoundInstance> entry : this.instances.entrySet()) {
			if (entry.getValue().isDone()) {
				done.add(entry.getKey());
			} else if (!soundManager.isPlaying(entry.getValue())) {
				soundManager.play(entry.getValue());
			}
		}

		for (Identifier id : done) {
			this.instances.remove(id);
		}
	}
}
