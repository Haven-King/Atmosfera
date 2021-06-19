/*
 * Copyright 2021 Haven King
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.hephaestus.atmosfera.client.sound.util;

import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundDefinition;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundInstance;
import dev.hephaestus.atmosfera.world.AtmosphericSoundContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class AtmosphericSoundHandler {
	private static final HashMap<Identifier, AtmosphericSoundInstance> INSTANCES = new HashMap<>();
//	private static int tickCounter = 0; // Only for testing.

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

		// Only for testing.
//		tickCounter++;
//		if (tickCounter >= 10) {
//			tickCounter = 0;
//			// Thread
//		}

		// Stops the sound events if the player exits the world.
		if (MinecraftClient.getInstance().player != null) {
			THREAD = new Thread(() -> {
//				Profiler.push("Handler.Tick");
				SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();

//				Profiler.push("Handler.Tick.UpdateContext");
				AtmosphericSoundContext.updateContext(MinecraftClient.getInstance().player);
//				Profiler.pop();

//				Profiler.push("Handler.Tick.AddNewSounds");
				for (AtmosphericSoundDefinition definition : Atmosfera.SOUND_DEFINITIONS.values()) {
					Identifier id = definition.getId();
					if (!INSTANCES.containsKey(id) || INSTANCES.get(id).isDone()) {
//						Profiler.push("Handler.Tick.AddNewSounds.GetVolume");
						float volume = definition.getVolume();
//						Profiler.pop();

						// The non-zero volume prevents the events getting triggered multiple times at volumes near zero.
						if (volume >= 0.0125 && MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.AMBIENT) > 0) {
//							Profiler.push("Handler.Tick.AddNewSounds.NewSoundInstance");
							AtmosphericSoundInstance soundInstance = new AtmosphericSoundInstance(definition, 0.0001F);
							INSTANCES.put(id, soundInstance);
							soundManager.playNextTick(soundInstance);
//							Profiler.pop();
//							Atmosfera.LOG.info("[Atmosfera] volume > 0: " + definition.getId() + " - " + volume); // Only for testing.
						}
					}
				}
//				Profiler.pop();

//				Profiler.push("Handler.Tick.PlayExistingSounds");
				Collection<Identifier> done = new LinkedList<>();
				for (Map.Entry<Identifier, AtmosphericSoundInstance> entry : INSTANCES.entrySet()) {
					if (entry.getValue().isDone()) {
						done.add(entry.getKey());
//						soundManager.stop(entry.getValue());
//						Atmosfera.LOG.info("[Atmosfera] isDone: " + entry.getValue().getId()); // Only for testing.
					} else if (!soundManager.isPlaying(entry.getValue()) /* && !entry.getValue().isDone() */) {
						soundManager.play(entry.getValue());
//						Atmosfera.LOG.info("[Atmosfera] play: " + entry.getValue().getId()); // Only for testing.
					}
				}
//				Profiler.pop();

//				Profiler.push("Handler.Tick.RemoveDoneSounds");
				for (Identifier id : done) {
					INSTANCES.remove(id);
//					Atmosfera.LOG.info("[Atmosfera] remove: " + id); // Only for testing.
				}
//				Profiler.pop();
			});

			THREAD.start();
		}
	}
}
