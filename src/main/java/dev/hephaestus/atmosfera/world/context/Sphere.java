package dev.hephaestus.atmosfera.world.context;

import dev.hephaestus.atmosfera.mixin.BossBarHudAccessor;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.biome.Biome;

import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class Sphere extends AbstractEnvironmentContext {
    final Hemisphere upperHemisphere;
    final Hemisphere lowerHemisphere;

    public Sphere(Size size) {
        this.upperHemisphere = new Hemisphere(ContextUtil.OFFSETS[Shape.UPPER_HEMISPHERE.ordinal()][size.ordinal()], this);
        this.lowerHemisphere = new Hemisphere(ContextUtil.OFFSETS[Shape.LOWER_HEMISPHERE.ordinal()][size.ordinal()], this);
        this.bossBars = new HashSet<>();
    }

    @Override
    public float getBlockTypePercentage(Block block) {
        return (upperHemisphere.getBlockTypePercentage(block) + lowerHemisphere.getBlockTypePercentage(block)) / 2F;
    }

    @Override
    public float getBlockTagPercentage(TagKey<Block> blocks) {
        return (upperHemisphere.getBlockTagPercentage(blocks) + lowerHemisphere.getBlockTagPercentage(blocks)) / 2F;
    }

    @Override
    public float getBiomePercentage(Biome biome) {
        return (upperHemisphere.getBiomePercentage(biome) + lowerHemisphere.getBiomePercentage(biome)) / 2F;
    }

    @Override
    public float getBiomeTagPercentage(TagKey<Biome> biomes) {
        return (upperHemisphere.getBiomeTagPercentage(biomes) + lowerHemisphere.getBiomeTagPercentage(biomes)) / 2F;
    }

    @Override
    public float getSkyVisibility() {
        return (upperHemisphere.getSkyVisibility() + lowerHemisphere.getSkyVisibility()) / 2F;
    }

    public void update(ClientPlayerEntity player) {
        var world = player.getWorld();
        var pos = player.getBlockPos();

        if (world.isChunkLoaded(pos.getX() >> 4, pos.getZ() << 4)) {
            BlockPos.Mutable mut = new BlockPos.Mutable().set(pos);

            int count = 0;
            while (world.getBlockState(mut).isAir() && mut.getY() > 0) {
                count += 1;
                mut.move(Direction.DOWN);
            }
            altitude = count;

            bossBars.clear();

            var bossBarHud = MinecraftClient.getInstance().inGameHud.getBossBarHud();
            Map<UUID, ClientBossBar> bossBarMap = ((BossBarHudAccessor) bossBarHud).getBossBars();

            for (var bossBar : bossBarMap.values()) {
                String value = bossBar.getName().getContent() instanceof TranslatableTextContent translatable ? translatable.getKey() : bossBar.getName().toString();
                bossBars.add(value);
            }

            elevation = pos.getY();

            // count day to sunset as "day". "night" is an hour shorter this way, which is fine
            long timeOfDay = world.getLevelProperties().getTimeOfDay() % 24000;
            isDay = 0 <= timeOfDay && timeOfDay < 13000;

            isRainy = world.getLevelProperties().isRaining();
            isStormy = world.isThundering();
            vehicle = player.getVehicle();

            ContextUtil.EXECUTOR.execute(() -> upperHemisphere.update(world, pos.up()));
            ContextUtil.EXECUTOR.execute(() -> lowerHemisphere.update(world, pos.down()));
        }
    }

    public EnvironmentContext getUpperHemisphere() {
        return upperHemisphere;
    }

    public EnvironmentContext getLowerHemisphere() {
        return lowerHemisphere;
    }
}
