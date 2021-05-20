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

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.AtmosphericSoundCondition;
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
	public int defaultVolume = 100;
	public boolean defaultSubtitle = true;

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
