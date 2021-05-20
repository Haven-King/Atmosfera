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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.AtmosferaConfig;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundConditionRegistry;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundDefinition;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundModifierRegistry;
import dev.hephaestus.atmosfera.world.AtmosphericSoundContext;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;

public class AtmosphericSoundSerializer implements SimpleSynchronousResourceReloadListener {
	private final String folder;
	private final Map<Identifier, AtmosphericSoundDefinition> destination;

	public AtmosphericSoundSerializer(String folder, Map<Identifier, AtmosphericSoundDefinition> destination) {
		this.folder = folder;
		this.destination = destination;
	}

	@Override
	public Identifier getFabricId() {
		return Atmosfera.id(folder);
	}

	@Override
	public void apply(ResourceManager manager) {
		destination.clear();
		Collection<Identifier> resources = manager.findResources(folder + "/definitions", (string) -> string.endsWith(".json"));
		AtmosphericSoundContext.clear();

		JsonParser parser = new JsonParser();
		for (Identifier resource : resources) {
			try {
				JsonObject json = parser.parse(new InputStreamReader(manager.getResource(resource).getInputStream())).getAsJsonObject();
				Identifier id = new Identifier(
						resource.getNamespace(),
						resource.getPath().substring(
								resource.getPath().indexOf("definitions/") + 12,
								resource.getPath().indexOf(".json")
						)
				);

				AtmosphericSoundDescription soundDescription = new AtmosphericSoundDescription();

				Identifier soundId = new Identifier(JsonHelper.getString(json, "sound"));
				soundDescription.sound = Registry.SOUND_EVENT.containsId(soundId)
						? Registry.SOUND_EVENT.get(soundId)
						: Registry.register(Registry.SOUND_EVENT, soundId, new SoundEvent(soundId));
				soundDescription.context = new AtmosphericSoundDescription.Context(json.get("context").getAsJsonObject());
				soundDescription.modifiers.put("volume", (context, volume) -> (volume * AtmosferaConfig.volumeModifier(soundId)));

				if (json.has("default_volume")) {
					JsonElement element = json.get("default_volume");
					if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
						soundDescription.defaultVolume = element.getAsNumber().intValue();
					}
				}

				if (json.has("default_subtitle")) {
					JsonElement element = json.get("default_subtitle");
					if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isBoolean()) {
						soundDescription.defaultSubtitle = element.getAsBoolean();
					}
				}

				if (json.has("modifiers")) {
					for (JsonElement element : json.get("modifiers").getAsJsonArray()) {
						soundDescription.modifiers.put(
								JsonHelper.getString(element.getAsJsonObject(), "modifies", "volume"),
								AtmosphericSoundModifierRegistry
										.get(element.getAsJsonObject().get("type").getAsString())
										.from(soundDescription.context, element));
					}
				}

				JsonObject conditions = json.get("conditions").getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : conditions.entrySet()) {
					soundDescription.conditions.add(
							AtmosphericSoundConditionRegistry
									.get(entry.getKey())
									.from(
											soundDescription.context,
											entry.getValue()
									)
					);
				}

				if (soundDescription.sound != null) {
					destination.put(id, new AtmosphericSoundDefinition(id, soundDescription));
					Atmosfera.LOG.debug("[Atmosfera] Loaded sound event: " + id);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Proceeds after the last ResourceManagerHelper is finished to get the final result.
		if (this.folder.equals("sounds/music")) {
			if (Atmosfera.SOUND_DEFINITIONS.values().size() + Atmosfera.MUSIC_DEFINITIONS.values().size() == 0) {
				Atmosfera.LOG.info("[Atmosfera] No sound event was registered. Activate the built-in or a custom Atmosfera resource pack.");
			} else if (Atmosfera.SOUND_DEFINITIONS.values().size() + Atmosfera.MUSIC_DEFINITIONS.values().size() >= 1) {
				Atmosfera.LOG.info("[Atmosfera] " +
						Atmosfera.SOUND_DEFINITIONS.values().size() + " ambient sound and " +
						Atmosfera.MUSIC_DEFINITIONS.values().size() + " music events were registered.");

				// Creates the config file at the start to gain access in case the config screen has not been opened before the world load.
				AtmosferaConfig.refreshConfig();
			}
		}
	}
}
