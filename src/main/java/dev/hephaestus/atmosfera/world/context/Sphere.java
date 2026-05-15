package dev.hephaestus.atmosfera.world.context;

import dev.hephaestus.atmosfera.mixin.BossBarHudAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;

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

    public void update(LocalPlayer player) {
        var level = player.level();
        var pos = player.blockPosition();

        if (level.hasChunk(pos.getX() >> 4, pos.getZ() << 4)) {
            BlockPos.MutableBlockPos mut = new BlockPos.MutableBlockPos().set(pos);

            int count = 0;
            while (level.getBlockState(mut).isAir() && mut.getY() > 0) {
                count += 1;
                mut.move(Direction.DOWN);
            }
            altitude = count;

            bossBars.clear();

            var bossBarHud = Minecraft.getInstance().gui.getBossOverlay();
            Map<UUID, LerpingBossEvent> bossBarMap = ((BossBarHudAccessor) bossBarHud).getEvents();

            for (var bossBar : bossBarMap.values()) {
                String value = bossBar.getName().getContents() instanceof TranslatableContents translatable ? translatable.getKey() : bossBar.getName().toString();
                bossBars.add(value);
            }

            elevation = pos.getY();

            // count day to sunset as "day". "night" is an hour shorter this way, which is fine
            long timeOfDay = level.getDefaultClockTime() % 24000;
            isDay = 0 <= timeOfDay && timeOfDay < 13000;

            isRainy = level.isRaining();
            isStormy = level.isThundering();
            isSubmerged = player.isEyeInFluid(FluidTags.WATER) || player.isEyeInFluid(FluidTags.LAVA);
            vehicle = player.getVehicle();

            ContextUtil.EXECUTOR.execute(() -> upperHemisphere.update(level, pos.above()));
            ContextUtil.EXECUTOR.execute(() -> lowerHemisphere.update(level, pos.below()));
        }
    }

    public EnvironmentContext getUpperHemisphere() {
        return upperHemisphere;
    }

    public EnvironmentContext getLowerHemisphere() {
        return lowerHemisphere;
    }
}
