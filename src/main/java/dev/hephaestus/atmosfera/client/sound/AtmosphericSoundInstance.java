package dev.hephaestus.atmosfera.client.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.MathHelper;

public class AtmosphericSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
	private final AtmosphericSoundDefinition definition;

	private int transitionTimer = 0;
	private boolean done = false;

	public AtmosphericSoundInstance(AtmosphericSoundDefinition definition, float volume) {
		super(definition.getSoundEvent(), SoundCategory.AMBIENT);
		this.definition = definition;
		this.volume = volume;
	}

	@Override
	public boolean isDone() {
		return this.done;
	}

	public void markDone() {
		this.done = true;
	}

	@Override
	public void tick() {
		MinecraftClient client = MinecraftClient.getInstance();

		if (client != null && client.player != null && this.transitionTimer >= 0) {
			this.x = client.player.getX();
			this.y = client.player.getY();
			this.z = client.player.getZ();

			float volume = this.definition.getVolume();
			if (volume >= this.volume + 0.0125) {
				++this.transitionTimer;
			} else if (volume < this.volume - 0.0125) {
				this.transitionTimer -= 1;
			}

			this.transitionTimer = Math.min(this.transitionTimer, 80);
			this.volume = MathHelper.clamp(this.transitionTimer / 80F, 0F, 1F);
		} else {
			this.done = true;
		}
	}
}
