package dev.hephaestus.atmosfera.client.sound.util;

import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundDefinition;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundInstance;
import dev.hephaestus.atmosfera.world.AtmosphericSoundContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class AtmosphericSoundHandler {
	private static final HashMap<Identifier, AtmosphericSoundInstance> INSTANCES = new HashMap<>();

	private static Thread THREAD = null;

	public static void beginTick() {
		if (THREAD != null && THREAD.isAlive()) {
			try {
				THREAD.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public static void endTick() {
		THREAD = new Thread(() -> {
			Profiler.push("Handler.Tick");
			SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();

			Profiler.push("Handler.Tick.UpdateContext");
			AtmosphericSoundContext.updateContext(MinecraftClient.getInstance().player);
			Profiler.pop();

			Profiler.push("Handler.Tick.AddNewSounds");
			for (AtmosphericSoundDefinition definition : Atmosfera.SOUND_DEFINITIONS.values()) {
				if (!INSTANCES.containsKey(definition.getId()) || INSTANCES.get(definition.getId()).isDone()) {
					Profiler.push("Handler.Tick.AddNewSounds.GetVolume");
					float volume = definition.getVolume();
					Profiler.pop();

					if (volume > 0) {
						Profiler.push("Handler.Tick.AddNewSounds.NewSoundInstance");
						AtmosphericSoundInstance soundInstance = new AtmosphericSoundInstance(definition, 0.0001F);
						INSTANCES.put(definition.getId(), soundInstance);
						soundManager.playNextTick(soundInstance);
						Profiler.pop();
					}
				}
			}
			Profiler.pop();

			Profiler.push("Handler.Tick.PlayExistingSounds");
			Collection<Identifier> done = new LinkedList<>();
			for (Map.Entry<Identifier, AtmosphericSoundInstance> entry : INSTANCES.entrySet()) {
				if (entry.getValue().isDone()) {
					done.add(entry.getKey());
				} else if (!soundManager.isPlaying(entry.getValue())) {
					soundManager.play(entry.getValue());
				}
			}
			Profiler.pop();

			Profiler.push("Handler.Tick.RemoveDoneSounds");
			for (Identifier id : done) {
				INSTANCES.remove(id);
			}
			Profiler.pop();
		});

		THREAD.start();
	}
}
