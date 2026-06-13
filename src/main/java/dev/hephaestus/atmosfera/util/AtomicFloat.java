package dev.hephaestus.atmosfera.util;

import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("UnusedReturnValue")
public class AtomicFloat {
    private final AtomicInteger value;

    public AtomicFloat() {
        this(0f);
    }

    public AtomicFloat(float initialValue) {
        this.value = new AtomicInteger(Float.floatToIntBits(initialValue));
    }

    public float get() {
        return Float.intBitsToFloat(value.get());
    }

    public void set(float newValue) {
        value.set(Float.floatToIntBits(newValue));
    }

    public float addAndGet(float x) {
        return value.updateAndGet(v -> Float.floatToIntBits(x + Float.intBitsToFloat(v)));
    }
}
