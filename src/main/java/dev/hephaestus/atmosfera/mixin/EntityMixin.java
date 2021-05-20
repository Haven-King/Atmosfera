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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Environment(EnvType.CLIENT)
@Mixin(Entity.class)
public class EntityMixin {
	@Shadow @Nullable protected Tag<Fluid> field_25599;

	/**
	 * @reason
	 * The vanilla version of this method only checks that the instances are the same.
	 * This is not compatible with the tags returned by {@link TagRegistry#fluid(Identifier)}.
	 * @author Haven King
	 */
	@Overwrite
	public boolean isSubmergedIn(Tag<Fluid> tag) {
		return this.field_25599 == tag || (
				tag instanceof Tag.Identified && this.field_25599 instanceof Tag.Identified
				&& ((Tag.Identified<Fluid>) tag).getId().equals(((Tag.Identified<Fluid>) this.field_25599).getId()));
	}
}
