package dev.hephaestus.atmosfera.world.context;

import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.tag.Tag;
import net.minecraft.world.biome.Biome;

public interface EnvironmentContext {
    float getBlockTypePercentage(Block block);

    float getBlockTagPercentage(Tag<Block> blocks);

    float getBiomePercentage(Biome biome);

    float getBiomeTagPercentage(Tag<Biome> biomes);

    float getBiomeCategoryPercentage(Biome.Category biomes);

    /**
     * @return the distance in blocks between the player and the ground
     */
    float getAltitude();

    /**
     * @return the current y position of the player
     */
    float getElevation();

    /**
     * @return the percentage of sky visible
     */
    float getSkyVisibility();

    boolean isDaytime();

    boolean isRainy();

    boolean isStormy();

    boolean isSubmergedInFluid();

    Entity getVehicle();

    static EnvironmentContext getInstance(Size size, Shape shape) {
        return switch (shape) {
            case UPPER_HEMISPHERE -> Sphere.CONTEXTS.get(size).top;
            case LOWER_HEMISPHERE -> Sphere.CONTEXTS.get(size).bottom;
            case SPHERE -> Sphere.CONTEXTS.get(size);
        };
    }

    static void update() {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player != null && Sphere.TASK_QUEUE.isEmpty()) {
            Sphere.CONTEXTS.computeIfAbsent(Size.SMALL, Sphere::new).update(player);
            Sphere.CONTEXTS.computeIfAbsent(Size.MEDIUM, Sphere::new).update(player);
            Sphere.CONTEXTS.computeIfAbsent(Size.LARGE, Sphere::new).update(player);
        }
    }

    enum Shape {
        UPPER_HEMISPHERE, LOWER_HEMISPHERE, SPHERE
    }

    enum Size {
        SMALL(4),
        MEDIUM(8),
        LARGE(16);

        public final int radius;
        Size(int radius) {
            this.radius = radius;
        }
    }
}
