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

import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.client.sound.util.AtmosphericSoundDescription;
import dev.hephaestus.atmosfera.client.sound.util.AtmosphericSoundModifier;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class AtmosphericSoundModifierRegistry {
	public static final Collection<Tag<Block>> USED_BLOCK_TAGS = new HashSet<>();
	private static final Map<String, AtmosphericSoundModifier.Builder> MAP = new HashMap<>();

	public static void register(String key, AtmosphericSoundModifier.Builder builder) {
		MAP.putIfAbsent(key, builder);
	}

	public static void override(String key, AtmosphericSoundModifier.Builder builder) {
		MAP.put(key, builder);
	}

	public static AtmosphericSoundModifier.Builder get(String key) {
		return MAP.getOrDefault(key, AtmosphericSoundModifier.Builder.DEFAULT);
	}

	static {
		register("percent_block", (context, element) -> {
			Set<Block> blocks = new HashSet<>();
			JsonHelper.getArray(element.getAsJsonObject(), "blocks").forEach(block -> {
				Identifier blockId = new Identifier(block.getAsString());

				// Registers only the loaded IDs to avoid false triggers.
				if (Registry.BLOCK.containsId(blockId)) {
					Block b = Registry.BLOCK.get(blockId);
					blocks.add(b);
					Atmosfera.LOG.debug("[Atmosfera] Registered block: " + block.getAsString());
				} else {
					Atmosfera.LOG.debug("[Atmosfera] Invalid block: " + block.getAsString());
				}
			});

			float min = element.getAsJsonObject().get("range").getAsJsonArray().get(0).getAsFloat();
			float max = element.getAsJsonObject().get("range").getAsJsonArray().get(1).getAsFloat();

//			Atmosfera.LOG.info("[Atmosfera] Registered percent_block: " + min + " - " + max); // Only for testing.
			return (ctx, volume) -> {
				float percent = context.shape == AtmosphericSoundDescription.Context.Shape.HEMISPHERE
					? ctx.percentBlockType(blocks, context.direction)
					: ctx.percentBlockType(blocks);


				if (percent >= min) {
					return (volume * (percent - min) * (1.0F / (max - min)));
				} else {
					return 0F;
				}
			};
		});

		register("percent_block_tag", (context, element) -> {
			Set<Tag<Block>> blockTags = new HashSet<>();
			JsonHelper.getArray(element.getAsJsonObject(), "tags").forEach(tag -> {
				Identifier blockTagID = new Identifier(tag.getAsString().substring(1));

				if (FabricLoader.getInstance().isModLoaded(blockTagID.getNamespace())) {
					Tag<Block> blockTag = TagRegistry.block(blockTagID);
					USED_BLOCK_TAGS.add(blockTag);
					blockTags.add(blockTag);
					Atmosfera.LOG.debug("[Atmosfera] Registered block tag: " + tag.getAsString().substring(1));
				} else {
					Atmosfera.LOG.debug("[Atmosfera] Invalid block tag: " + tag.getAsString().substring(1));
				}
			});

			float min = element.getAsJsonObject().get("range").getAsJsonArray().get(0).getAsFloat();
			float max = element.getAsJsonObject().get("range").getAsJsonArray().get(1).getAsFloat();

//			Atmosfera.LOG.info("[Atmosfera] Registered percent_block_tag: " + min + " - " + max); // Only for testing.
			return (ctx, volume) -> {
				float percent = context.shape == AtmosphericSoundDescription.Context.Shape.HEMISPHERE
						? ctx.percentBlockTag(blockTags, context.direction)
						: ctx.percentBlockTag(blockTags);

				if (percent >= min) {
					return (volume * (percent - min) * (1.0F / (max - min)));
				} else {
					return 0F;
				}
			};
		});

		register("submerged_in", (context, element) -> {
			Collection<Tag<Fluid>> fluids = new HashSet<>();
			JsonHelper.getArray(element.getAsJsonObject(), "fluids").forEach(fluid -> {
				Identifier fluidId = new Identifier(fluid.getAsString());

				if (Registry.FLUID.containsId(fluidId)) {
					fluids.add(TagRegistry.fluid(fluidId));
					Atmosfera.LOG.debug("[Atmosfera] Registered fluid: " + fluid.getAsString());
				} else {
					Atmosfera.LOG.debug("[Atmosfera] Invalid fluid: " + fluid.getAsString());
				}
			});

			float factor = JsonHelper.getFloat(element.getAsJsonObject(), "factor");

//			Atmosfera.LOG.info("[Atmosfera] Registered submerged_in: " + factor); // Only for testing.
			return (ctx, pitch) -> {
				for (Tag<Fluid> fluidTag : fluids) {

					// For the MC 1.15.2 legacy support:
//					if (ctx.getPlayer().isSubmergedIn(fluidTag, false))
					if (ctx.getPlayer().isSubmergedIn(fluidTag)) {
						return factor;
					}
				}

				return pitch;
			};
		});
	}
}
