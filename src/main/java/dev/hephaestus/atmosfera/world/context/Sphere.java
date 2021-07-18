package dev.hephaestus.atmosfera.world.context;

import dev.hephaestus.atmosfera.Atmosfera;
import dev.hephaestus.atmosfera.mixin.BossBarHudAccessor;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.BossBarHud;
import net.minecraft.client.gui.hud.ClientBossBar;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.tag.Tag;
import net.minecraft.text.MutableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.*;
import java.util.concurrent.*;

public class Sphere extends AbstractEnvironmentContext {
    static final BlockingQueue<Runnable> TASK_QUEUE = new LinkedBlockingQueue<>();
    static final ExecutorService EXECUTOR = new ThreadPoolExecutor(4, 16,
            0, TimeUnit.MILLISECONDS,
            TASK_QUEUE,
            (runnable) -> {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                return thread;
            });

    static final HashMap<Size, Sphere> CONTEXTS = new HashMap<>();
    static final HashMap<Shape, HashMap<Size, HashSet<int[]>>> OFFSETS = new HashMap<>();

    static {
        for (Size size : Size.values()) {
            BlockPos origin = new BlockPos(0, 0, 0);

            int radius = size.radius;
            for (int x = 0; x <= radius + 1; ++x) {
                for (int y = -radius; y <= 0; ++y) {
                    for (int z = 0; z <= radius + 1; ++z) {
                        double distance = origin.getSquaredDistance(x, y, z, true);
                        if (distance <= (radius + 1) * (radius + 1)) {
                            OFFSETS.computeIfAbsent(Shape.LOWER_HEMISPHERE, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
                                    new int[] {x, y, z}
                            );

                            OFFSETS.computeIfAbsent(Shape.UPPER_HEMISPHERE, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
                                    new int[] {x, -y + 1, z}
                            );

                            OFFSETS.computeIfAbsent(Shape.LOWER_HEMISPHERE, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
                                    new int[] {x, y, -z}
                            );

                            OFFSETS.computeIfAbsent(Shape.UPPER_HEMISPHERE, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
                                    new int[] {x, -y + 1, -z}
                            );

                            OFFSETS.computeIfAbsent(Shape.LOWER_HEMISPHERE, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
                                    new int[] {-x, y, z}
                            );

                            OFFSETS.computeIfAbsent(Shape.UPPER_HEMISPHERE, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
                                    new int[] {-x, -y + 1, z}
                            );

                            OFFSETS.computeIfAbsent(Shape.LOWER_HEMISPHERE, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
                                    new int[] {-x, y, -z}
                            );

                            OFFSETS.computeIfAbsent(Shape.UPPER_HEMISPHERE, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
                                    new int[] {-x, -y + 1, -z}
                            );
                        }
                    }
                }
            }
        }
    }

    final Hemisphere top;
    final Hemisphere bottom;

    Sphere(Size size) {
        this.top = new Hemisphere(OFFSETS.get(Shape.UPPER_HEMISPHERE).get(size));
        this.bottom = new Hemisphere(OFFSETS.get(Shape.LOWER_HEMISPHERE).get(size));
    }

    @Override
    public float getBlockTypePercentage(Block block) {
        return (this.top.getBlockTypePercentage(block) + this.bottom.getBlockTypePercentage(block)) / 2F;
    }

    @Override
    public float getBlockTagPercentage(Tag<Block> blocks) {
        return (this.top.getBlockTagPercentage(blocks) + this.bottom.getBlockTagPercentage(blocks)) / 2F;
    }

    @Override
    public float getBiomePercentage(Biome biome) {
        return (this.top.getBiomePercentage(biome) + this.bottom.getBiomePercentage(biome)) / 2F;
    }

    @Override
    public float getBiomeTagPercentage(Tag<Biome> biomes) {
        return (this.top.getBiomeTagPercentage(biomes) + this.bottom.getBiomeTagPercentage(biomes)) / 2F;
    }

    @Override
    public float getBiomeCategoryPercentage(Biome.Category biomes) {
        return (this.top.getBiomeCategoryPercentage(biomes) + this.bottom.getBiomeCategoryPercentage(biomes)) / 2F;
    }

    @Override
    public float getSkyVisibility() {
        return (this.top.getSkyVisibility() + this.bottom.getSkyVisibility()) / 2F;
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

            BossBarHud bossBarHud = MinecraftClient.getInstance().inGameHud.getBossBarHud();
            Map<UUID, ClientBossBar> bossBars = ((BossBarHudAccessor) bossBarHud).getBossBars();

            this.isInRaid = false;
            this.isDefeatedInRaid = false;
            this.isVictoriousInRaid = false;
            this.isInWitherFight = false;
            this.isInEnderDragonFight = false;
            if (!bossBars.isEmpty()) {
                Iterator var1 = bossBars.values().iterator();

                while(var1.hasNext()) {
                    BossBar bossBar = (BossBar)var1.next();
                    MutableText bossBarText = bossBar.getName().copy();
                    if(bossBarText.toString().contains("'event.minecraft.raid'")) {
                        this.isInRaid = true;
                        if(bossBarText.toString().contains("'event.minecraft.raid.victory'")) {
                            this.isVictoriousInRaid = true;
                            this.isDefeatedInRaid = false;
                        } else if(bossBarText.toString().contains("'event.minecraft.raid.defeat'")) {
                            this.isDefeatedInRaid = true;
                            this.isVictoriousInRaid = false;
                        }
                    } else if(bossBarText.toString().contains("'entity.minecraft.wither'")) {
                        this.isInWitherFight = true;
                    } else if(bossBarText.toString().contains("'entity.minecraft.ender_dragon'")) {
                        this.isInEnderDragonFight = true;
                    } else {
                        // Atmosfera.debug(String.format("[%s] Unhandled Bar: %s", Atmosfera.MOD_NAME, bossBarText));
                    }
                }
            }

            this.elevation = pos.getY();
            this.isDay = world.isDay();
            this.isRainy = world.isRaining();
            this.isStormy = world.isThundering();
            this.isSubmergedInFluid = !world.getFluidState(player.getBlockPos().up((int) player.getHeight())).isEmpty();
            this.vehicle = player.getVehicle();

            this.top.copy(this);
            this.bottom.copy(this);

            EXECUTOR.execute(() -> this.top.update(player, pos.up()));
            EXECUTOR.execute(() -> this.bottom.update(player, pos.up()));
        }
    }
}
