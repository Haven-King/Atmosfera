package dev.hephaestus.atmosfera.util;

import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.world.AtmosphericSoundContext;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.function.BiFunction;

public class AtmosphericSoundDescription {
	public SoundEvent sound;
	public Context context;
	public Collection<Identifier> biomes = new HashSet<>();
	public Collection<BiFunction<AtmosphericSoundContext, Double, Double>> modifiers = new LinkedList<>();
	public Collection<Condition> conditions = new LinkedList<>();
	public int defaultVolume = 75;

	public interface Condition {
		boolean test(AtmosphericSoundContext context);
	}

	public static class Context {
		public AtmosphericSoundSerializer.Shape shape;
		public AtmosphericSoundContext.Direction direction;
		public AtmosphericSoundContext.Size size;

		public Context(JsonObject context) {
			this.shape = AtmosphericSoundSerializer.Shape.valueOf(JsonHelper.getString(context, "shape").toUpperCase(Locale.ENGLISH));

			if (this.shape != AtmosphericSoundSerializer.Shape.SPHERE) {
				this.direction = AtmosphericSoundContext.Direction.valueOf(JsonHelper.getString(context, "direction").toUpperCase());
			}

			this.size = context.has("size")
					? AtmosphericSoundContext.Size.valueOf(context.get("size").getAsString().toUpperCase())
					: AtmosphericSoundContext.Size.MEDIUM;
		}
	}
}
