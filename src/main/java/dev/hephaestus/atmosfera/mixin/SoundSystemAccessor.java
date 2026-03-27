package dev.hephaestus.atmosfera.mixin;

import net.minecraft.client.resources.sounds.TickableSoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SoundEngine.class)
public interface SoundSystemAccessor {
    @Accessor
    List<TickableSoundInstance> getTickingSounds();
}
