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

package dev.hephaestus.atmosfera.client.sound;

import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.client.AtmosphericSoundCondition;
import dev.hephaestus.atmosfera.client.sound.util.AtmosphericSoundDescription;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.Vec2f;
import org.apache.commons.lang3.tuple.ImmutableTriple;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

public class AtmosphericSoundConditionRegistry {
	private static final HashMap<String, AtmosphericSoundCondition.Builder> MAP = new HashMap<>();

	public static void register(String key, AtmosphericSoundCondition.Builder builder) {
		MAP.putIfAbsent(key, builder);
	}

	public static void override(String key, AtmosphericSoundCondition.Builder builder) {
		MAP.put(key, builder);
	}

	public static AtmosphericSoundCondition.Builder get(String key) {
		return MAP.getOrDefault(key, AtmosphericSoundCondition.Builder.ALWAYS);
	}

	static {
		register("height", (context, element) -> {
			Vec2f heightBounds = getBounds(element.getAsJsonObject());
//			Atmosfera.LOG.info("[Atmosfera] Registered height: " + heightBounds.x + " - " + heightBounds.y); // Only for testing.
			return ctx -> ctx.getPlayerHeight() >= heightBounds.x && ctx.getPlayerHeight() <= heightBounds.y;
		});

		register("distance_from_ground", (context, element) -> {
			Vec2f distanceBounds = getBounds(element.getAsJsonObject());
//			Atmosfera.LOG.info("[Atmosfera] Registered distance_from_ground: " + distanceBounds.x + " - " + distanceBounds.y); // Only for testing.
			return ctx -> ctx.getDistanceFromGround() >= distanceBounds.x && ctx.getDistanceFromGround() <= distanceBounds.y;
		});

		register("percent_sky_visible", (context, element) -> {
			Vec2f skyVisibilityBounds = getBounds(element.getAsJsonObject());
//			Atmosfera.LOG.info("[Atmosfera] Registered percent_sky_visible: " + skyVisibilityBounds.x + " - " + skyVisibilityBounds.y); // Only for testing.
			return ctx -> {
				float visible = context.shape == AtmosphericSoundDescription.Context.Shape.SPHERE
						? ctx.percentSkyVisible()
						: ctx.percentSkyVisible(context.direction);

				return visible >= skyVisibilityBounds.x && visible <= skyVisibilityBounds.y;
			};
		});

		register("is_daytime", (context, element) -> ctx -> ctx.isDaytime() == element.getAsBoolean());

		register("is_rainy", (context, element) -> ctx -> ctx.isRainy() == element.getAsBoolean());

		register("is_stormy", (context, element) -> ctx -> ctx.isStormy() == element.getAsBoolean());

		register("is_submerged", (context, element) -> ctx -> ctx.isSubmerged() == element.getAsBoolean());

		register("dimension", (context, element) -> {
			Identifier dimension = new Identifier(element.getAsString());
			return ctx -> dimension.equals(ctx.getDimension());
		});

		register("percent_biome", (context, element) -> {
			Collection<Triple<Set<Identifier>, Float, Float>> collection = new ArrayDeque<>();
			element.getAsJsonArray().forEach(e -> {
				JsonObject condition = e.getAsJsonObject();
				Set<Identifier> biomes = new HashSet<>();

				JsonHelper.getArray(condition, "items").forEach(biome -> {
					Identifier biomeId = new Identifier(biome.getAsString());

					// Registers only the loaded IDs to avoid false triggers.
					// For the MC 1.16.1 and below legacy support:
//					if (Registry.BIOME.containsId(biomeId))
					if (FabricLoader.getInstance().isModLoaded(biomeId.getNamespace())) {
						biomes.add(biomeId);
						Atmosfera.LOG.debug("[Atmosfera] Registered biome: " + biome.getAsString());
					} else {
						Atmosfera.LOG.debug("[Atmosfera] Invalid biome: " + biome.getAsString());
					}
				});

				// Float.MIN_VALUE is the smallest positive value a float can represent, not the true minimum value.
				float more = condition.has("more") ? JsonHelper.getFloat(condition, "more") : -Float.MAX_VALUE;
				float less = condition.has("less") ? JsonHelper.getFloat(condition, "less") : Float.MAX_VALUE;

//				Atmosfera.LOG.info("[Atmosfera] Registered percent_biome: " + more + " - " + less); // Only for testing.
				collection.add(new ImmutableTriple<>(biomes, more, less));
			});

			return ctx -> {
				for (Triple<Set<Identifier>, Float, Float> triple : collection) {
					float percentBiomes;
					if (context.shape == AtmosphericSoundDescription.Context.Shape.SPHERE) {
						percentBiomes = ctx.percentBiomeType(triple.getLeft());
					} else {
						percentBiomes = ctx.percentBiomeType(triple.getLeft(), context.direction);
					}

					if (!(percentBiomes > triple.getMiddle() && percentBiomes <= triple.getRight())) {
						return false;
					}
				}

				return true;
			};
		});
	}

	public static Vec2f getBounds(JsonObject object) {
		float more = object.has("more") ? JsonHelper.getFloat(object, "more") : -Float.MAX_VALUE;
		float less = object.has("less") ? JsonHelper.getFloat(object, "less") : Float.MAX_VALUE;
		return new Vec2f(more, less);
	}
}
