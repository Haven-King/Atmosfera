package dev.hephaestus.atmosfera.mixin;

import dev.hephaestus.atmosfera.client.music.util.AtmosphericMusicHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MusicType;
import net.minecraft.sound.MusicSound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Inject(method = "getMusicType", at = @At("RETURN"), cancellable = true)
	private void getAmbientMusicType(CallbackInfoReturnable<MusicSound> cir) {
		MusicSound sound = cir.getReturnValue();

		if (!sound.equals(MusicType.CREATIVE) && !sound.equals(MusicType.MENU)) {
			MusicSound atmosphericMusic = AtmosphericMusicHandler.getSound(sound);
			cir.setReturnValue(atmosphericMusic);
		}
	}
}
