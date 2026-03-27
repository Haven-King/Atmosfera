package dev.hephaestus.atmosfera.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static dev.hephaestus.atmosfera.client.sound.AtmosphericSoundHandler.TICKING_SOUNDS_LOCK;

import net.minecraft.client.sounds.SoundEngine;

// Raise Sound Limit Simplified redirects SoundSystem calls to the Sound engine thread (for some reason...?),
// thus conflicting with the tickingSounds access of AtmosphericSoundHandler from the Render thread
@Mixin(SoundEngine.class)
public abstract class SoundSystemMixin {
    @Inject(method = "stopAll", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V", ordinal = 0))
    public void synchronizeListClearLock(CallbackInfo ci) {
        TICKING_SOUNDS_LOCK.lock();
    }

    @Inject(method = "stopAll", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V", ordinal = 0, shift = At.Shift.AFTER))
    public void synchronizeListClearUnlock(CallbackInfo ci) {
        TICKING_SOUNDS_LOCK.unlock();
    }

    @Inject(method = "tickInGameSound()V", at = @At(value = "INVOKE", target = "Ljava/util/List;remove(Ljava/lang/Object;)Z"))
    public void synchronizeListRemoveLock(CallbackInfo ci) {
        TICKING_SOUNDS_LOCK.lock();
    }

    @Inject(method = "tickInGameSound()V", at = @At(value = "INVOKE", target = "Ljava/util/List;remove(Ljava/lang/Object;)Z", shift = At.Shift.AFTER))
    public void synchronizeListRemoveUnlock(CallbackInfo ci) {
        TICKING_SOUNDS_LOCK.unlock();
    }

    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z"))
    public void synchronizeListAddLock(CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        TICKING_SOUNDS_LOCK.lock();
    }

    @Inject(method = "play(Lnet/minecraft/client/resources/sounds/SoundInstance;)Lnet/minecraft/client/sounds/SoundEngine$PlayResult;", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", shift = At.Shift.AFTER))
    public void synchronizeListAddUnlock(CallbackInfoReturnable<SoundEngine.PlayResult> cir) {
        TICKING_SOUNDS_LOCK.unlock();
    }
}
