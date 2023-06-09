package dev.hephaestus.atmosfera.client.sound.modifiers.implementations;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import dev.hephaestus.atmosfera.client.sound.modifiers.AtmosphericSoundModifier;
import dev.hephaestus.atmosfera.world.context.EnvironmentContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public record RidingCondition(ImmutableList<EntityType<?>> types) implements AtmosphericSoundModifier, AtmosphericSoundModifier.Factory {

    @Override
    public float getModifier(EnvironmentContext context) {
        Entity vehicle = context.getVehicle();

        if (vehicle != null) {
            for (EntityType<?> type : this.types) {
                if (vehicle.getType().equals(type)) {
                    return 1;
                }
            }
        }

        return 0;
    }

    @Override
    public AtmosphericSoundModifier create(World world) {
        return this;
    }

    public static Factory create(JsonObject object) {
        ImmutableList.Builder<EntityType<?>> types = ImmutableList.builder();

        JsonElement value = object.get("value");

        if (value.isJsonPrimitive()) {
            Registries.ENTITY_TYPE.getOrEmpty(new Identifier(value.getAsString())).ifPresent(types::add);
        } else if (value.isJsonArray()) {
            for (JsonElement e : value.getAsJsonArray()) {
                Registries.ENTITY_TYPE.getOrEmpty(new Identifier(e.getAsString())).ifPresent(types::add);
            }
        }

        return new RidingCondition(types.build());
    }
}
