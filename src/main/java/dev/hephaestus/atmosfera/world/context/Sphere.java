package dev.hephaestus.atmosfera.world.context;

import dev.hephaestus.atmosfera.mixin.BossBarHudAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.*;

@Environment(EnvType.CLIENT)
public class Sphere extends AbstractEnvironmentContext {
    final Hemisphere upperHemisphere;
    final Hemisphere lowerHemisphere;

    public Sphere(Size size, ClientPlayerEntity player) {
        super(player);
        this.upperHemisphere = new Hemisphere(ContextUtil.OFFSETS[Shape.UPPER_HEMISPHERE.ordinal()][size.ordinal()], this);
        this.lowerHemisphere = new Hemisphere(ContextUtil.OFFSETS[Shape.LOWER_HEMISPHERE.ordinal()][size.ordinal()], this);
        this.bossBars = new HashSet<>();
    }

    @Override
    public float getBlockTypePercentage(Block block) {
        return (this.upperHemisphere.getBlockTypePercentage(block) + this.lowerHemisphere.getBlockTypePercentage(block)) / 2F;
    }

    @Override
    public float getBlockTagPercentage(TagKey<Block> blocks) {
        return (this.upperHemisphere.getBlockTagPercentage(blocks) + this.lowerHemisphere.getBlockTagPercentage(blocks)) / 2F;
    }

    @Override
    public float getBiomePercentage(Biome biome) {
        return (this.upperHemisphere.getBiomePercentage(biome) + this.lowerHemisphere.getBiomePercentage(biome)) / 2F;
    }

    @Override
    public float getBiomeTagPercentage(TagKey<Biome> biomes) {
        return (this.upperHemisphere.getBiomeTagPercentage(biomes) + this.lowerHemisphere.getBiomeTagPercentage(biomes)) / 2F;
    }

    @Override
    public float getSkyVisibility() {
        return (this.upperHemisphere.getSkyVisibility() + this.lowerHemisphere.getSkyVisibility()) / 2F;
    }

    public void update() {
        World world = getPlayer().getWorld();
        BlockPos pos = getPlayer().getBlockPos();

        if (world.isChunkLoaded(pos.getX() >> 4, pos.getZ() << 4)) {
            this.altitude = 0;

            BlockPos.Mutable mut = new BlockPos.Mutable().set(pos);

            while (world.getBlockState(mut).isAir() && mut.getY() > 0) {
                this.altitude += 1;
                mut.move(Direction.DOWN);
            }

            this.bossBars.clear();

            BossBarHud bossBarHud = MinecraftClient.getInstance().inGameHud.getBossBarHud();
            Map<UUID, ClientBossBar> bossBarMap = ((BossBarHudAccessor) bossBarHud).getBossBars();

            for(BossBar bossBar : bossBarMap.values()) {
                String value = bossBar.getName().getContent() instanceof TranslatableTextContent translatable ? translatable.getKey() : bossBar.getName().toString();
                this.bossBars.add(value);
            }

            this.elevation = pos.getY();
            this.isDay = world.isDay();
            this.isRainy = world.isRaining();
            this.isStormy = world.isThundering();
            this.vehicle = getPlayer().getVehicle();

            ContextUtil.EXECUTOR.execute(() -> this.upperHemisphere.update(pos.up()));
            ContextUtil.EXECUTOR.execute(() -> this.lowerHemisphere.update(pos.down()));
        }
    }

    public EnvironmentContext getUpperHemisphere() {
        return this.upperHemisphere;
    }

    public EnvironmentContext getLowerHemisphere() {
        return this.lowerHemisphere;
    }
}
