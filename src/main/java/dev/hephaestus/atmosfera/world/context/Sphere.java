package dev.hephaestus.atmosfera.world.context;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

@Environment(EnvType.CLIENT)
public class Sphere extends AbstractEnvironmentContext {
    final Hemisphere upperHemisphere;
    final Hemisphere lowerHemisphere;

    public Sphere(Size size) {
        this.upperHemisphere = new Hemisphere(ContextUtil.OFFSETS.get(Shape.UPPER_HEMISPHERE).get(size));
        this.lowerHemisphere = new Hemisphere(ContextUtil.OFFSETS.get(Shape.LOWER_HEMISPHERE).get(size));
    }

    @Override
    public float getBlockTypePercentage(Block block) {
        return (this.upperHemisphere.getBlockTypePercentage(block) + this.lowerHemisphere.getBlockTypePercentage(block)) / 2F;
    }

    @Override
    public float getBlockTagPercentage(Tag<Block> blocks) {
        return (this.upperHemisphere.getBlockTagPercentage(blocks) + this.lowerHemisphere.getBlockTagPercentage(blocks)) / 2F;
    }

    @Override
    public float getBiomePercentage(Biome biome) {
        return (this.upperHemisphere.getBiomePercentage(biome) + this.lowerHemisphere.getBiomePercentage(biome)) / 2F;
    }

    @Override
    public float getBiomeTagPercentage(Tag<Biome> biomes) {
        return (this.upperHemisphere.getBiomeTagPercentage(biomes) + this.lowerHemisphere.getBiomeTagPercentage(biomes)) / 2F;
    }

    @Override
    public float getBiomeCategoryPercentage(Biome.Category biomes) {
        return (this.upperHemisphere.getBiomeCategoryPercentage(biomes) + this.lowerHemisphere.getBiomeCategoryPercentage(biomes)) / 2F;
    }

    @Override
    public float getSkyVisibility() {
        return (this.upperHemisphere.getSkyVisibility() + this.lowerHemisphere.getSkyVisibility()) / 2F;
    }

    public void update(ClientPlayerEntity player) {
        World world = player.world;
        BlockPos pos = player.getBlockPos();

        if (world.isChunkLoaded(pos.getX() >> 4, pos.getZ() << 4)) {
            this.altitude = 0;

            BlockPos.Mutable mut = new BlockPos.Mutable().set(pos);

            while (world.getBlockState(mut).isAir() && mut.getY() > 0) {
                this.altitude += 1;
                mut.move(Direction.DOWN);
            }

            this.player = player;
            this.elevation = pos.getY();
            this.isDay = world.isDay();
            this.isRainy = world.isRaining();
            this.isStormy = world.isThundering();
            this.vehicle = player.getVehicle();

            this.upperHemisphere.copy(this);
            this.lowerHemisphere.copy(this);

            ContextUtil.EXECUTOR.execute(() -> this.upperHemisphere.update(player, pos.up()));
            ContextUtil.EXECUTOR.execute(() -> this.lowerHemisphere.update(player, pos.down()));
        }
    }

    public EnvironmentContext getUpperHemisphere() {
        return this.upperHemisphere;
    }

    public EnvironmentContext getLowerHemisphere() {
        return this.lowerHemisphere;
    }
}
