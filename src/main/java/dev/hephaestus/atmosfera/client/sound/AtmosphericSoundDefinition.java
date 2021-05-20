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

package dev.hephaestus.atmosfera.client.sound;

import com.google.common.collect.Multimap;
import dev.hephaestus.atmosfera.AtmosferaConfig;
import dev.hephaestus.atmosfera.client.AtmosphericSoundCondition;
import dev.hephaestus.atmosfera.client.sound.util.AtmosphericSoundDescription;
import dev.hephaestus.atmosfera.client.sound.util.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.AtmosphericSoundContext;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

import java.util.Collection;

public class AtmosphericSoundDefinition {
	private final Identifier id;
	private final SoundEvent soundEvent;
	private final Collection<AtmosphericSoundCondition> conditions;
	private final Multimap<String, AtmosphericSoundModifier> modifiers;
	private final AtmosphericSoundContext.Size size;
	private final int defaultVolume;
	private final boolean defaultSubtitle;

	public AtmosphericSoundDefinition(Identifier id, AtmosphericSoundDescription soundDescription) {
		this.id = id;
		this.soundEvent = soundDescription.sound;
		this.conditions = soundDescription.conditions;
		this.modifiers = soundDescription.modifiers;
		this.size = soundDescription.context.size;
		this.defaultVolume = soundDescription.defaultVolume;
		this.defaultSubtitle = soundDescription.defaultSubtitle;
	}

	public SoundEvent getSoundEvent() {
		return this.soundEvent;
	}

	public float getVolume() {
		AtmosphericSoundContext context = AtmosphericSoundContext.getContext(this.size);

		for (AtmosphericSoundCondition condition : this.conditions) {
			if (!condition.test(context)) {
				return 0F;
			}
		}

		float volume = 1;

		for (AtmosphericSoundModifier modifier : this.modifiers.get("volume")) {
			volume = modifier.apply(context, volume);
		}

		return volume;
	}

	public boolean showSubtitle() {
		return AtmosferaConfig.subtitleModifier(getId());
	}

	public float getPitch() {
		float pitch = 1f;

		for (AtmosphericSoundModifier modifier : this.modifiers.get("pitch")) {
			pitch = modifier.apply(AtmosphericSoundContext.getContext(this.size), pitch);
		}

		return pitch;
	}

	public Identifier getId() {
		return this.id;
	}

	public int getDefaultVolume() {
		return this.defaultVolume;
	}

	public boolean getDefaultSubtitle() {
		return this.defaultSubtitle;
	}
}
