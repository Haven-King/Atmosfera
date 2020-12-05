package dev.hephaestus.atmosfera.client;

import com.google.gson.JsonElement;
import dev.hephaestus.atmosfera.client.sound.util.AtmosphericSoundDescription;
import dev.hephaestus.atmosfera.world.AtmosphericSoundContext;

public interface AtmosphericSoundCondition {
	AtmosphericSoundCondition ALWAYS = context -> true;

	/**
	 * Determines whether or not a sound should play based on context about the world around the player.
	 * @param context see {@link AtmosphericSoundContext}
	 * @return whether the sound should play or not
	 */
	boolean test(AtmosphericSoundContext context);

	interface Builder {
		Builder ALWAYS = (context, object) -> AtmosphericSoundCondition.ALWAYS;

		AtmosphericSoundCondition from(AtmosphericSoundDescription.Context context, JsonElement element);
	}
}
