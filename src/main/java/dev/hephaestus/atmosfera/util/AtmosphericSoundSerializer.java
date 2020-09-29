package dev.hephaestus.atmosfera.util;

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
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.registry.Registry;

import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AtmosphericSoundSerializer implements SimpleSynchronousResourceReloadListener {
	@Override
	public Identifier getFabricId() {
		return Atmosfera.id("sound");
	}

	@Override
	public void apply(ResourceManager manager) {
		Atmosfera.SOUND_DEFINITIONS = new HashMap<>();
		Collection<Identifier> resources = manager.findResources("sounds/definitions", (string) -> string.endsWith(".json"));
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
				soundDescription.modifiers.add((context, volume) -> volume * AtmosferaConfig.modifier(soundId));

				if (json.has("default_volume")) {
					JsonElement element = json.get("default_volume");

					if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
						soundDescription.defaultVolume = element.getAsNumber().intValue();
					}
				}

				if (json.has("modifiers")) {
					for (JsonElement element : json.get("modifiers").getAsJsonArray()) {
						soundDescription.modifiers.add(AtmosphericSoundModifierRegistry
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

					Atmosfera.SOUND_DEFINITIONS.put(id, new AtmosphericSoundDefinition(
							id, soundDescription)
					);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	static Vec2f getBounds(JsonObject object) {
		float more = object.has("more") ? JsonHelper.getInt(object, "more") : 0;
		float less = object.has("less") ? JsonHelper.getInt(object, "less") : Integer.MAX_VALUE;
		return new Vec2f(more, less);
	}

}
