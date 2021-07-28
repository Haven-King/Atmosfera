package dev.hephaestus.atmosfera.mixin;

import dev.hephaestus.atmosfera.AtmosferaConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.MusicTracker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Environment(EnvType.CLIENT)
@Mixin(MusicTracker.class)
public class MusicTrackerMixin {
    @Shadow private int timeUntilNextSong;

    @Inject(method = "tick", at = @At("TAIL"))
    private void injectMaxDelayCheck(CallbackInfo info) {
        if(AtmosferaConfig.overrideMusicDelay()) {
            this.timeUntilNextSong = Math.min(this.timeUntilNextSong, AtmosferaConfig.getMaxMusicDelay());
        }
    }
}
