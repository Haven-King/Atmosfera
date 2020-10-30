package dev.hephaestus.atmosfera.mixin;

import dev.hephaestus.atmosfera.util.AtmosphericSoundHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundSystem.class)
@Environment(EnvType.CLIENT)
public class InvokeSoundHandler {
	@Inject(method = "tick()V", at = @At("HEAD"))
	private void joinSoundContext(CallbackInfo ci) {
		AtmosphericSoundHandler.beginTick();
	}

	@Inject(method = "tick()V", at = @At("TAIL"))
	private void updateSoundContext(CallbackInfo ci) {
		AtmosphericSoundHandler.endTick();
	}
}
