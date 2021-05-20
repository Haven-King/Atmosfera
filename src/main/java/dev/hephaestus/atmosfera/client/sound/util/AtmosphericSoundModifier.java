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
