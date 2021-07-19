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
import dev.hephaestus.atmosfera.AtmosferaConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.WeightedSoundSet;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WeightedSoundSet.class)
@Environment(EnvType.CLIENT)
public class WeightedSoundSetMixin {
    @Shadow @Final @Mutable private Identifier id;

    @Inject(method = "getSubtitle", at = @At("HEAD"), cancellable = true)
    public void atmosfera$disableSubtitle(CallbackInfoReturnable<Text> cir) {
        if (Atmosfera.SOUND_DEFINITIONS.containsKey(this.id) && !AtmosferaConfig.showSubtitle(this.id)) {
            if (AtmosferaConfig.printDebugMessages()) {
                Atmosfera.log("Mixin disableSubtitle: {} - {}", this.id, AtmosferaConfig.showSubtitle(this.id));
            }

            cir.setReturnValue(null);
        }
    }
}
