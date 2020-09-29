package dev.hephaestus.atmosfera.util;

import dev.hephaestus.atmosfera.world.AtmosphericSoundContext;

import java.util.function.BiFunction;

public interface AtmosphericSoundPredicate {
	/**
	 * Returns the volume the sound should have based on the context provided.
	 *
	 * @param context information about the world around the player. @see {@link AtmosphericSoundContext}
	 * @return A value in the range of 0.0 to 1.0 of how loud a sound should be.
	 */
	float getVolume(AtmosphericSoundContext context);

	static AtmosphericSoundPredicate fromDescription(AtmosphericSoundDescription description) {
		return (context -> {
			for (AtmosphericSoundCondition condition : description.conditions) {
				if (!condition.test(context)) {
					return 0F;
				}
			}

			double volume = 1;

			for (AtmosphericSoundModifier modifier : description.modifiers) {
				volume = modifier.apply(context, volume);
			}

			return (float) volume;
		});
	}
}
