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
import net.minecraft.client.sound.MusicType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.MusicSound;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Shadow @Nullable public ClientWorld world;

	@ModifyReturnValue(method = "getMusicType", at = @At("RETURN"))
	private MusicSound atmosfera$getAmbientMusicType(MusicSound original) {
		if (!AtmosferaConfig.enableCustomMusic())
			return original;

		if (original != MusicType.MENU && original != MusicType.CREDITS && world != null) {
			return world.atmosfera$getAtmosphericSoundHandler().getMusicSound(original);
		}

		return original;
	}
}
