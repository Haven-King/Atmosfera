package dev.hephaestus.atmosfera.client.sound.util;

import com.google.gson.JsonElement;
import dev.hephaestus.atmosfera.world.AtmosphericSoundContext;

/**
 * Used to adjust the volume of sounds played based on the world around the player.
 * Are applied in order, with the second parameter of {@link AtmosphericSoundModifier#apply} being equal to the
 * return value of the previous modifier in the list.
 */
public interface AtmosphericSoundModifier {
	AtmosphericSoundModifier DEFAULT = (context, volume) -> 1;

	/**
	 * Adjusts the volume of the sound based on context from the world around the player.
	 * @param context see {@link AtmosphericSoundContext}
	 * @param oldValue the volume the sound would play at before applying this modifier
	 * @return the volume the sound will play at (before any remaining modifiers are applied)
	 */
	float apply(AtmosphericSoundContext context, float oldValue);

	interface Builder {
		Builder DEFAULT = (context, object) -> AtmosphericSoundModifier.DEFAULT;

		AtmosphericSoundModifier from(AtmosphericSoundDescription.Context context, JsonElement element);
	}
}
