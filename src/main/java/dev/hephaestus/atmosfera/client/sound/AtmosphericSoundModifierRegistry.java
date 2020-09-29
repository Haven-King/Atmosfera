package dev.hephaestus.atmosfera.client.sound;

import dev.hephaestus.atmosfera.util.AtmosphericSoundModifier;
import net.minecraft.block.Block;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

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
				double percent = ctx.percentBlockType(blocks);


				if (percent >= min) {
					return volume * (percent - min) * (1.0D / (max - min));
				} else {
					return 0D;
				}
			};
		});
	}
}
