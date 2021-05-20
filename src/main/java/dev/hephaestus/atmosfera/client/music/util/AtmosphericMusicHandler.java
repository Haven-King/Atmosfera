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

package dev.hephaestus.atmosfera.client.music.util;

import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.AtmosferaConfig;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundDefinition;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MusicType;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import java.util.*;

public class AtmosphericMusicHandler {
	private static final Map<Identifier, MusicSound> MUSIC = new HashMap<>();
	private static final List<Pair<Integer, MusicSound>> SOUNDS = new ArrayList<>();
	private static final Random RANDOM = new Random();

	public static MusicSound getSound(MusicSound sound) {
		SoundManager soundManager = MinecraftClient.getInstance().getSoundManager();
		int total = Objects.requireNonNull(soundManager.get(sound.getSound().getId())).getWeight();

		SOUNDS.add(new Pair<>(total, sound));

		for (AtmosphericSoundDefinition definition : Atmosfera.MUSIC_DEFINITIONS.values()) {
			float volume = definition.getVolume();

			if (volume > 0.0125 && MinecraftClient.getInstance().options.getSoundVolume(SoundCategory.MUSIC) > 0) {
				int weight = Objects.requireNonNull(soundManager.get(definition.getSoundEvent().getId())).getWeight();

				SOUNDS.add(new Pair<>(weight, MUSIC.computeIfAbsent(definition.getId(), id -> {
//					Atmosfera.LOG.info("[Atmosfera] createIngameMusic: " + definition.getId()); // Only for testing.
					return MusicType.createIngameMusic(definition.getSoundEvent());
				})));

				total += AtmosferaConfig.weight(definition.getId()) * volume;
			}
		}

		MusicSound result = sound;

		int i = RANDOM.nextInt(total);

		for (Pair<Integer, MusicSound> pair : SOUNDS) {
			i -= pair.getLeft();

			if (i <= 0) {
				result = pair.getRight();
			}
		}

		SOUNDS.clear();

		return result;
	}

	public static void clear() {
		MUSIC.clear();
	}
}
