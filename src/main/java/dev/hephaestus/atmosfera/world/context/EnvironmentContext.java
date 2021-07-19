package dev.hephaestus.atmosfera.world.context;

import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.tag.Tag;
import net.minecraft.world.biome.Biome;

import java.util.Collection;

public interface EnvironmentContext {
    ClientPlayerEntity getPlayer();

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
  
    Entity getVehicle();

    Collection<String> getBossBars();

    static void init() {
        ContextUtil.init();
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
