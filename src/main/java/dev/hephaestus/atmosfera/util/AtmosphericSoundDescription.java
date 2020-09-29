package dev.hephaestus.atmosfera.util;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.world.AtmosphericSoundContext;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.JsonHelper;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

public class AtmosphericSoundDescription {
	public SoundEvent sound;
	public Context context;
	public Multimap<String, AtmosphericSoundModifier> modifiers = LinkedHashMultimap.create();
	public Collection<AtmosphericSoundCondition> conditions = new LinkedList<>();
	public int defaultVolume = 75;

	public static class Context {
		public Shape shape;
		public AtmosphericSoundContext.Direction direction;
		public AtmosphericSoundContext.Size size;

		public Context(JsonObject context) {
			this.shape = Shape.valueOf(JsonHelper.getString(context, "shape").toUpperCase(Locale.ENGLISH));

			if (this.shape != Shape.SPHERE) {
				this.direction = AtmosphericSoundContext.Direction.valueOf(JsonHelper.getString(context, "direction").toUpperCase());
			}

			this.size = context.has("size")
					? AtmosphericSoundContext.Size.valueOf(context.get("size").getAsString().toUpperCase())
					: AtmosphericSoundContext.Size.MEDIUM;
		}

		public enum Shape {
			HEMISPHERE, SPHERE
		}
	}
}
