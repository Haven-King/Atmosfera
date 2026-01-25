package dev.hephaestus.atmosfera.util;

import org.jspecify.annotations.NonNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class NopLock implements Lock, java.io.Serializable {
    @Override public void lock() {} @Override
    public void lockInterruptibly() throws InterruptedException {}
    @Override public boolean tryLock() { return false; }
    @Override public boolean tryLock(long time, @NonNull TimeUnit unit) throws InterruptedException { return false; }
    @Override public void unlock() {}
    @Override public @NonNull Condition newCondition() { throw new UnsupportedOperationException(); }
}
