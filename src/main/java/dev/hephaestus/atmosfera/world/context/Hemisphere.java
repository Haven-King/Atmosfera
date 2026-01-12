package dev.hephaestus.atmosfera.world.context;

import dev.hephaestus.atmosfera.Atmosfera;
import mod.adrenix.nostalgic.helper.candy.light.LightingHelper;
import mod.adrenix.nostalgic.tweak.config.CandyTweak;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

class Hemisphere implements EnvironmentContext {
    private final byte[][] offsets;
    private final Sphere sphere;

    // TODO all of this is updated on one thread and read from another, leading to inconsistent state
    private final Map<Block, Integer> blockTypes = new ConcurrentHashMap<>();
    private final Map<Identifier, Integer> blockTags = new ConcurrentHashMap<>();
    private final Map<Biome, Integer> biomeTypes = new ConcurrentHashMap<>();
    private final Map<Identifier, Integer> biomeTags = new ConcurrentHashMap<>();

    private final AtomicInteger blockCount = new AtomicInteger();
    private final AtomicInteger skyVisibility = new AtomicInteger();

    private static boolean printedError = false;

    Hemisphere(byte[][] offsets, Sphere sphere) {
        this.sphere = sphere;
        this.offsets = offsets;
    }

    @Override
    public float getBlockTypePercentage(Block block) {
        return blockTypes.getOrDefault(block, 0) / (float) blockCount.get();
    }

    @Override
    public float getBlockTagPercentage(TagKey<Block> blocks) {
        return blockTags.getOrDefault(blocks.id(), 0) / (float) blockCount.get();
    }

    @Override
    public float getBiomePercentage(Biome biome) {
        return biomeTypes.getOrDefault(biome, 0) / (float) blockCount.get();
    }

    @Override
    public float getBiomeTagPercentage(TagKey<Biome> biomes) {
        return biomeTags.getOrDefault(biomes.id(), 0) / (float) blockCount.get();
    }

    @Override
    public float getAltitude() {
        return sphere.altitude;
    }

    @Override
    public float getElevation() {
        return sphere.elevation;
    }

    @Override
    public float getSkyVisibility() {
        return skyVisibility.get() / (float) blockCount.get();
    }

    @Override
    public boolean isDaytime() {
        return sphere.isDay;
    }

    @Override
    public boolean isRainy() {
        return sphere.isRainy;
    }

    @Override
    public boolean isStormy() {
        return sphere.isStormy;
    }

    @Override
    public Entity getVehicle() {
        return sphere.vehicle;
    }

    @Override
    public Collection<String> getBossBars() {
        return sphere.bossBars;
    }

    private void clear() {
        blockCount.set(0);
        skyVisibility.set(0);
        blockTypes.replaceAll((block, integer) -> 0);
        blockTags.replaceAll((identifier, integer) -> 0);
        biomeTypes.replaceAll((biome, integer) -> 0);
        blockTags.replaceAll((identifier, integer) -> 0);
    }

    @SuppressWarnings("deprecation")
    private void add(World world, BlockPos pos) {
        Block block = world.getBlockState(pos).getBlock();
        blockTypes.merge(block, 1, Integer::sum);
        block.getRegistryEntry().streamTags().forEach(blockTag -> {
            blockTags.merge(blockTag.id(), 1, Integer::sum);
        });

        RegistryEntry<Biome> biomeEntry = world.getBiome(pos);
        Biome biome = biomeEntry.value();
        biomeEntry.streamTags().forEach(biomeTag -> {
            biomeTags.merge(biomeTag.id(), 1, Integer::sum);
        });

        biomeTypes.merge(biome, 1, Integer::sum);
        skyVisibility.addAndGet(world.getLightLevel(LightType.SKY, pos) / getMaxLightLevel());
        blockCount.incrementAndGet();
    }

    private static int getMaxLightLevel() {
        // see https://github.com/Nostalgica-Reverie/Nostalgic-Tweaks/blob/9e0c22d5fc04a52455ec3a104708e9adb8c9a794/common/src/main/java/mod/adrenix/nostalgic/mixin/tweak/candy/world_lighting/BlockAndTintGetterMixin.java#L15-L27
        try {
            if (FabricLoader.getInstance().isModLoaded("nostalgic_tweaks") && CandyTweak.ROUND_ROBIN_RELIGHT.get()) {
                return LightingHelper.getCombinedLight(15, 0);
            }
        } catch (Throwable e) {
            synchronized (Hemisphere.class) {
                if (!printedError) {
                    printedError = true;
                    Atmosfera.error("mod compat for Nostalgic Tweaks broke, report this to the Atmosfera devs!");
                }
            }
        }

        return 15;
    }

    // runs on worker threads
    void update(World world, BlockPos center) {
        clear();

        var mut = new BlockPos.Mutable();
        for (byte[] a : offsets) {
            mut.set(center.getX() + a[0], center.getY() + a[1], center.getZ() + a[2]);
            add(world, mut);
        }
    }
}
