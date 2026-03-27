package dev.hephaestus.atmosfera.world.context;

import net.minecraft.core.BlockPos;

import java.util.Collection;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class ContextUtil {
    public static final byte[][][][] OFFSETS = new byte[3][][][];

    public static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(2, 2,
            0L, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<>(6), // (SMALL, MEDIUM, LARGE) * (upper + lower)
            runnable -> {
                Thread thread = new Thread(runnable);
                thread.setDaemon(true);
                return thread;
            });

    private ContextUtil() {}

    static {
        Map<EnvironmentContext.Shape, Map<EnvironmentContext.Size, Collection<byte[]>>> offsets = new EnumMap<>(EnvironmentContext.Shape.class);

        for (var size : EnvironmentContext.Size.values()) {
            var origin = new BlockPos(0, 0, 0);

            byte radius = size.radius;
            for (byte x = 0; x <= radius + 1; ++x) {
                for (byte y = (byte) -radius; y <= 0; ++y) {
                    for (byte z = 0; z <= radius + 1; ++z) {
                        double distance = origin.distToCenterSqr(x, y, z);
                        if ((x + y + z) % 3 == 0 && distance <= (radius + 1) * (radius + 1)) {
                            offsets.computeIfAbsent(EnvironmentContext.Shape.LOWER_HEMISPHERE, key -> new EnumMap<>(EnvironmentContext.Size.class)).computeIfAbsent(size, key -> new HashSet<>()).add(
                                    new byte[] {x, y, z}
                            );

                            offsets.computeIfAbsent(EnvironmentContext.Shape.UPPER_HEMISPHERE, key -> new EnumMap<>(EnvironmentContext.Size.class)).computeIfAbsent(size, key -> new HashSet<>()).add(
                                    new byte[] {x, (byte) (-y + 1), z}
                            );

                            offsets.computeIfAbsent(EnvironmentContext.Shape.LOWER_HEMISPHERE, key -> new EnumMap<>(EnvironmentContext.Size.class)).computeIfAbsent(size, key -> new HashSet<>()).add(
                                    new byte[] {x, y, (byte) -z}
                            );

                            offsets.computeIfAbsent(EnvironmentContext.Shape.UPPER_HEMISPHERE, key -> new EnumMap<>(EnvironmentContext.Size.class)).computeIfAbsent(size, key -> new HashSet<>()).add(
                                    new byte[] {x, (byte) (-y + 1), (byte) -z}
                            );

                            offsets.computeIfAbsent(EnvironmentContext.Shape.LOWER_HEMISPHERE, key -> new EnumMap<>(EnvironmentContext.Size.class)).computeIfAbsent(size, key -> new HashSet<>()).add(
                                    new byte[] {(byte) -x, y, z}
                            );

                            offsets.computeIfAbsent(EnvironmentContext.Shape.UPPER_HEMISPHERE, key -> new EnumMap<>(EnvironmentContext.Size.class)).computeIfAbsent(size, key -> new HashSet<>()).add(
                                    new byte[] {(byte) -x, (byte) (-y + 1), z}
                            );

                            offsets.computeIfAbsent(EnvironmentContext.Shape.LOWER_HEMISPHERE, key -> new EnumMap<>(EnvironmentContext.Size.class)).computeIfAbsent(size, key -> new HashSet<>()).add(
                                    new byte[] {(byte) -x, y, (byte) -z}
                            );

                            offsets.computeIfAbsent(EnvironmentContext.Shape.UPPER_HEMISPHERE, key -> new EnumMap<>(EnvironmentContext.Size.class)).computeIfAbsent(size, key -> new HashSet<>()).add(
                                    new byte[] {(byte) -x, (byte) (-y + 1), (byte) -z}
                            );
                        }
                    }
                }
            }
        }

        for (var shape : offsets.keySet()) {
            int shapeOrdinal = shape.ordinal();
            var shapes = offsets.get(shape);
            OFFSETS[shapeOrdinal] = new byte[shapes.size()][][];

            for (var size : shapes.keySet()) {
                int sizeOrdinal = size.ordinal();
                var sizes = shapes.get(size);
                OFFSETS[shapeOrdinal][sizeOrdinal] = new byte[sizes.size()][];

                int i = 0;
                for (byte[] bytes : sizes) {
                    OFFSETS[shapeOrdinal][sizeOrdinal][i++] = bytes;
                }
            }
        }
    }

    static void init() {}
}
