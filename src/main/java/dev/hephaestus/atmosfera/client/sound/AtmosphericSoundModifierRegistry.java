package dev.hephaestus.atmosfera.client.sound;

import dev.hephaestus.atmosfera.util.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.AtmosphericSoundContext;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class AtmosphericSoundModifierRegistry {
	private static final HashMap<String, AtmosphericSoundModifier.Builder> MAP = new HashMap<>();

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

				if (Registry.BLOCK.containsId(blockId)) {
					Block b = Registry.BLOCK.get(blockId);
					blocks.add(b);
				}
			});

			double min = element.getAsJsonObject().get("range").getAsJsonArray().get(0).getAsDouble();
			double max = element.getAsJsonObject().get("range").getAsJsonArray().get(1).getAsDouble();

			return (ctx, volume) -> {
				float percent = ctx.percentBlockType(blocks);


				if (percent >= min) {
					return (float) (volume * (percent - min) * (1.0F / (max - min)));
				} else {
					return 0F;
				}
			};
		});

		register("submerged_in", (context, element) -> {
			Collection<Tag<Fluid>> fluids = new HashSet<>();
			JsonHelper.getArray(element.getAsJsonObject(), "fluids").forEach(fluid ->
					fluids.add(TagRegistry.fluid(new Identifier(fluid.getAsString())))
			);

			float factor = JsonHelper.getFloat(element.getAsJsonObject(), "factor");

			return (ctx, pitch) -> {
				for (Tag<Fluid> fluidTag : fluids) {
					if (ctx.getPlayer().isSubmergedIn(fluidTag)) {
						return factor;
					}
				}

				return pitch;
			};
		});
	}
}
