package dev.hephaestus.atmosfera.world.context;

import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.*;

public final class ContextUtil {
    public static final HashMap<EnvironmentContext.Shape, HashMap<EnvironmentContext.Size, HashSet<int[]>>> OFFSETS = new HashMap<>();
    public static final BlockingQueue<Runnable> TASK_QUEUE = new LinkedBlockingQueue<>();
    public static final ExecutorService EXECUTOR = new ThreadPoolExecutor(4, 16,
            0, TimeUnit.MILLISECONDS,
            TASK_QUEUE,
            (runnable) -> {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                return thread;
            });

    private ContextUtil() {}

    static {
        ContextUtil.EXECUTOR.execute(() -> {
            for (EnvironmentContext.Size size : EnvironmentContext.Size.values()) {
                BlockPos origin = new BlockPos(0, 0, 0);

                int radius = size.radius;
                for (int x = 0; x <= radius + 1; ++x) {
                    for (int y = -radius; y <= 0; ++y) {
                        for (int z = 0; z <= radius + 1; ++z) {
                            double distance = origin.getSquaredDistance(x, y, z, true);
                            if ((x + y + z) % 3 == 0 && distance <= (radius + 1) * (radius + 1)) {
                                ContextUtil.OFFSETS.computeIfAbsent(EnvironmentContext.Shape.LOWER_HEMISPHERE, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
                                        new int[] {x, y, z}
                                );

                                ContextUtil.OFFSETS.computeIfAbsent(EnvironmentContext.Shape.UPPER_HEMISPHERE, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
                                        new int[] {x, -y + 1, z}
                                );

                                ContextUtil.OFFSETS.computeIfAbsent(EnvironmentContext.Shape.LOWER_HEMISPHERE, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
                                        new int[] {x, y, -z}
                                );

                                ContextUtil.OFFSETS.computeIfAbsent(EnvironmentContext.Shape.UPPER_HEMISPHERE, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
                                        new int[] {x, -y + 1, -z}
                                );

                                ContextUtil.OFFSETS.computeIfAbsent(EnvironmentContext.Shape.LOWER_HEMISPHERE, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
                                        new int[] {-x, y, z}
                                );

                                ContextUtil.OFFSETS.computeIfAbsent(EnvironmentContext.Shape.UPPER_HEMISPHERE, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
                                        new int[] {-x, -y + 1, z}
                                );

                                ContextUtil.OFFSETS.computeIfAbsent(EnvironmentContext.Shape.LOWER_HEMISPHERE, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
                                        new int[] {-x, y, -z}
                                );

                                ContextUtil.OFFSETS.computeIfAbsent(EnvironmentContext.Shape.UPPER_HEMISPHERE, key -> new HashMap<>()).computeIfAbsent(size, key -> new HashSet<>()).add(
                                        new int[] {-x, -y + 1, -z}
                                );
                            }
                        }
                    }
                }
            }
        });
    }

    static void init() {}
}
