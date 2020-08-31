package dev.hephaestus.atmosfera.mixin;

import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundInstance;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(SoundSystem.class)
@Environment(EnvType.CLIENT)
public class SoundSystemMixin {
	@Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Ljava/util/List;remove(Ljava/lang/Object;)Z"))
	private boolean removeAtmosphericSoundInstance(List<?> list, Object o) {
		if (o instanceof AtmosphericSoundInstance) {
			((AtmosphericSoundInstance) o).markDone();
		}

		return list.remove(o);
	}
}
