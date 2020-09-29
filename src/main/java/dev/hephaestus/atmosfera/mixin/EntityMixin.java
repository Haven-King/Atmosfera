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
