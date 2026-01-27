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

package dev.hephaestus.atmosfera.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import dev.hephaestus.atmosfera.AtmosferaConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MusicInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.MusicSound;
import net.minecraft.sound.MusicType;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Shadow @Nullable public ClientWorld world;

	@ModifyReturnValue(method = "getMusicInstance", at = @At("RETURN"))
	private MusicInstance atmosfera$getAmbientMusic(MusicInstance original) {
		if (!AtmosferaConfig.enableCustomMusic())
			return original;

		MusicSound sound = original.music();
		float volume = original.volume();

		if (sound != null && sound != MusicType.MENU && sound != MusicType.CREDITS && world != null) {
			MusicSound atmosphericMusic = world.atmosfera$getAtmosphericSoundHandler().getMusicSound(sound);
			return new MusicInstance(atmosphericMusic, volume); // keep the volume, so music fades out in the pale garden
		}

		return original;
	}
}
