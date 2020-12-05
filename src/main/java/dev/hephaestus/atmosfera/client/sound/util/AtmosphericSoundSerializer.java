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
				soundDescription.sound = Registry.SOUND_EVENT.containsId(soundId) ? Registry.SOUND_EVENT.get(soundId) : Registry.register(Registry.SOUND_EVENT, soundId, new SoundEvent(soundId));
				soundDescription.context = new AtmosphericSoundDescription.Context(json.get("context").getAsJsonObject());
				soundDescription.modifiers.put("volume",
						(context, volume) -> (float) (volume * AtmosferaConfig.modifier(soundId))
				);

				if (json.has("default_volume")) {
					JsonElement element = json.get("default_volume");

					if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
						soundDescription.defaultVolume = element.getAsNumber().intValue();
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
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
