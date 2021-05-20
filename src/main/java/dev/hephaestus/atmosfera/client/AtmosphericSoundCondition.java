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
