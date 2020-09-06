package dev.hephaestus.atmosfera.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.AtmosferaConfig;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundDefinition;
import dev.hephaestus.atmosfera.world.AtmosphericSoundContext;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.Block;
import net.minecraft.resource.ResourceManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionType;

import java.io.InputStreamReader;
import java.util.*;

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
					for (JsonElement modifier : json.get("modifiers").getAsJsonArray()) {
						if (modifier.isJsonPrimitive() && modifier.getAsJsonPrimitive().isNumber()) {
							soundDescription.modifiers.add((context, volume) -> volume * modifier.getAsDouble());
						} else if (modifier.isJsonObject()) {
							JsonObject mod = modifier.getAsJsonObject();
							// Reserved for future modifiers
							//noinspection SwitchStatementWithTooFewBranches
							switch (mod.get("type").getAsString()) {
								case "percent_block":
									Set<Block> blocks = new HashSet<>();
									JsonHelper.getArray(mod, "blocks").forEach(block -> {
										Block b = Registry.BLOCK.get(new Identifier(block.getAsString()));
										blocks.add(b);
									});

									double min = mod.get("range").getAsJsonArray().get(0).getAsDouble();
									double max = mod.get("range").getAsJsonArray().get(1).getAsDouble();

									soundDescription.modifiers.add(((context, volume) -> {
										double percent = context.percentBlockType(blocks);

										if (percent >= min) {
											return volume * (percent - min) * (1.0D / (max - min));
										} else {
											return 0D;
										}
									}));
									break;

								default:
							}
						}
					}
				}

				JsonObject conditions = json.get("conditions").getAsJsonObject();
				for (Map.Entry<String, JsonElement> entry : conditions.entrySet()) {
					switch (entry.getKey()) {
						case "height":
							Vec2f heightBounds = getBounds(entry.getValue().getAsJsonObject());
							soundDescription.conditions.add((context -> context.getPlayerHeight() >= heightBounds.x && context.getPlayerHeight() <= heightBounds.y));
							break;
						case "distance_from_ground":
							Vec2f distanceBounds = getBounds(entry.getValue().getAsJsonObject());
							soundDescription.conditions.add((context -> context.getDistanceFromGround() >= distanceBounds.x && context.getDistanceFromGround() <= distanceBounds.y));
							break;
						case "percent_sky_visible":
							Vec2f skyVisibilityBounds = getBounds(entry.getValue().getAsJsonObject());
							if (soundDescription.context.shape == Shape.SPHERE) {
								soundDescription.conditions.add((context -> context.percentSkyVisible() >= skyVisibilityBounds.x && context.percentSkyVisible() <= skyVisibilityBounds.y));
							} else {
								soundDescription.conditions.add((context -> context.percentSkyVisible(soundDescription.context.direction) >= skyVisibilityBounds.x && context.percentSkyVisible(soundDescription.context.direction) <= skyVisibilityBounds.y));
							}
							break;
						case "is_daytime":
							boolean isDaytime = entry.getValue().getAsBoolean();
							soundDescription.conditions.add(context -> context.isDaytime() == isDaytime);
							break;
						case "dimension":
							RegistryKey<DimensionType> dimension = RegistryKey.of(Registry.DIMENSION_TYPE_KEY, new Identifier(entry.getValue().getAsString()));
							soundDescription.conditions.add(context -> dimension.getValue().equals(context.getDimension()));
							break;
						case "percent_biome":
							JsonHelper.getArray(conditions, "percent_biome").forEach(biomeCondition -> {
								JsonObject condition = biomeCondition.getAsJsonObject();
								Set<Identifier> biomes = new HashSet<>();
								JsonHelper.getArray(condition, "items").forEach(biome -> {
									Identifier b = new Identifier(biome.getAsString());
									biomes.add(b);
									soundDescription.biomes.add(b);
								});
								float more = condition.has("more") ? JsonHelper.getFloat(condition, "more") : 0;
								float less = condition.has("less") ? JsonHelper.getFloat(condition, "less") : Float.MAX_VALUE;

								soundDescription.conditions.add(context -> {
									float percentBiomes;
									if (soundDescription.context.shape == Shape.SPHERE) {
										percentBiomes = context.percentBiomeType(biomes);
									} else {
										percentBiomes = context.percentBiomeType(biomes, soundDescription.context.direction);
									}

									return percentBiomes > more && percentBiomes <= less;
								});
							});
							break;
					}
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

	public enum Shape {
		HEMISPHERE, SPHERE
	}
}
