package dev.hephaestus.atmosfera.client.sound;

import com.google.common.collect.Multimap;
import dev.hephaestus.atmosfera.util.AtmosphericSoundCondition;
import dev.hephaestus.atmosfera.util.AtmosphericSoundDescription;
import dev.hephaestus.atmosfera.util.AtmosphericSoundModifier;
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

	public AtmosphericSoundDefinition(Identifier id, AtmosphericSoundDescription soundDescription) {
		this.id = id;
		this.soundEvent = soundDescription.sound;
		this.conditions = soundDescription.conditions;
		this.modifiers = soundDescription.modifiers;
		this.size = soundDescription.context.size;
		this.defaultVolume = soundDescription.defaultVolume;
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
}
