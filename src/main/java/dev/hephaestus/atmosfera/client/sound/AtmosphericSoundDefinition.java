package dev.hephaestus.atmosfera.client.sound;

import dev.hephaestus.atmosfera.util.AtmosphericSoundDescription;
import dev.hephaestus.atmosfera.util.AtmosphericSoundPredicate;
import dev.hephaestus.atmosfera.world.AtmosphericSoundContext;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class AtmosphericSoundDefinition {
	private final Identifier id;
	private final SoundEvent soundEvent;
	private final AtmosphericSoundPredicate predicate;
	private final AtmosphericSoundContext.Size size;
	private final int defaultVolume;

	public AtmosphericSoundDefinition(Identifier id, AtmosphericSoundDescription soundDescription) {
		this.id = id;
		this.soundEvent = soundDescription.sound;
		this.predicate = AtmosphericSoundPredicate.fromDescription(soundDescription);
		this.size = soundDescription.context.size;
		this.defaultVolume = soundDescription.defaultVolume;
	}

	public SoundEvent getSoundEvent() {
		return this.soundEvent;
	}

	public float getVolume() {
		return this.predicate.getVolume(AtmosphericSoundContext.getContext(this.size));
	}

	public Identifier getId() {
		return this.id;
	}

	public int getDefaultVolume() {
		return this.defaultVolume;
	}
}
