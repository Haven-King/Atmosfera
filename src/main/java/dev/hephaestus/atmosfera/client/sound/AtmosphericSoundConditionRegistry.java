package dev.hephaestus.atmosfera.client.sound;

import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.util.AtmosphericSoundCondition;
import dev.hephaestus.atmosfera.util.AtmosphericSoundDescription;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.Tag;
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
			return ctx -> ctx.getPlayerHeight() >= heightBounds.x && ctx.getPlayerHeight() <= heightBounds.y;
		});

		register("distance_from_ground", (context, element) -> {
			Vec2f distanceBounds = getBounds(element.getAsJsonObject());
			return ctx -> ctx.getDistanceFromGround() >= distanceBounds.x && ctx.getDistanceFromGround() <= distanceBounds.y;
		});

		register("percent_sky_visible", (context, element) -> {
			Vec2f skyVisibilityBounds = getBounds(element.getAsJsonObject());
			return ctx -> {
				float visible = context.shape == AtmosphericSoundDescription.Context.Shape.SPHERE
						? ctx.percentSkyVisible()
						: ctx.percentSkyVisible(context.direction);

				return visible >= skyVisibilityBounds.x && visible <= skyVisibilityBounds.y;
			};
		});

		register("is_daytime", (context, element) -> ctx -> ctx.isDaytime() == element.getAsBoolean());

		register("dimension", (context, element) -> {
			Identifier dimension = new Identifier(element.getAsString());
			return ctx -> dimension.equals(ctx.getDimension());
		});

		register("percent_biome", (context, element) -> {
			Collection<Triple<Set<Identifier>, Float, Float>> collection = new ArrayDeque<>();
			element.getAsJsonArray().forEach(e -> {
				JsonObject condition = e.getAsJsonObject();
				Set<Identifier> biomes = new HashSet<>();

				JsonHelper.getArray(condition, "items").forEach(biome ->
					biomes.add(new Identifier(biome.getAsString()))
				);

				float more = condition.has("more") ? JsonHelper.getFloat(condition, "more") : 0;
				float less = condition.has("less") ? JsonHelper.getFloat(condition, "less") : Float.MAX_VALUE;

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

					if (!(percentBiomes >= triple.getMiddle() && percentBiomes <= triple.getRight())) {
						return false;
					}
				}

				return true;
			};
		});

		register("submerged_in", (context, element) -> {
			Collection<Tag<Fluid>> fluids = new HashSet<>();
			JsonHelper.getArray(element.getAsJsonObject(), "fluids").forEach(fluid ->
				fluids.add(TagRegistry.fluid(new Identifier(fluid.getAsString())))
			);

			boolean defaultResult = JsonHelper.getBoolean(element.getAsJsonObject(), "invert", false);

			return ctx -> {
				for (Tag<Fluid> fluidTag : fluids) {
					if (ctx.getPlayer().isSubmergedIn(fluidTag)) {
						return !defaultResult;
					}
				}

				return defaultResult;
			};
		});
	}

	public static Vec2f getBounds(JsonObject object) {
		float more = object.has("more") ? JsonHelper.getInt(object, "more") : 0;
		float less = object.has("less") ? JsonHelper.getInt(object, "less") : Integer.MAX_VALUE;
		return new Vec2f(more, less);
	}
}
