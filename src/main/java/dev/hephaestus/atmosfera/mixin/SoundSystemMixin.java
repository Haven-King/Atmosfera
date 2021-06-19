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

import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.client.sound.AtmosphericSoundInstance;
import dev.hephaestus.atmosfera.client.sound.util.AtmosphericSoundHandler;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SoundSystem.class)
@Environment(EnvType.CLIENT)
public class SoundSystemMixin {
	@Inject(method = "tick()V", at = @At("HEAD"))
	private void joinSoundContext(CallbackInfo ci) {
		AtmosphericSoundHandler.beginTick();
	}

	@Redirect(method = "tick()V", at = @At(value = "INVOKE", target = "Ljava/util/List;remove(Ljava/lang/Object;)Z"))
	private boolean removeAtmosphericSoundInstance(List<?> list, Object o) {
		if (o instanceof AtmosphericSoundInstance) {
			((AtmosphericSoundInstance) o).markDone();

			/*
			 * The return statement does not remove the `Atmospheric Sound Instance`s which end up bloating the list.
			 *
			 * The repeating instances lock the `createSource` in the `SoundEngine` with `Maximum sound pool size 247 reached`
			 * causing `Failed to create new sound handle` in the `SoundSystem`.
			 *
			 * `Atmospheric Sound Instance`s are removed separately to prevent the bloat.
			 */
			list.remove(((AtmosphericSoundInstance) o));
//			Atmosfera.LOG.info("[Atmosfera] Mixin markDone: " + ((AtmosphericSoundInstance) o).getId()); // Only for testing.
		}
//		Atmosfera.LOG.info("[Atmosfera] Mixin list: " + list.size() + " - Mixin remove: " + o.toString()); // Only for testing.
		return list.remove(o);
	}

	@Inject(method = "tick()V", at = @At("TAIL"))
	private void updateSoundContext(CallbackInfo ci) {
		AtmosphericSoundHandler.endTick();
	}
}
