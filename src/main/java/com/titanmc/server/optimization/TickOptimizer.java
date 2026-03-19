package com.titanmc.server.optimization;

import com.titanmc.server.config.TitanConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class TickOptimizer {

    private final TitanConfig config;
    private final Logger logger;
    private int optimizationCount = 0;
    private static final ConcurrentHashMap<Long, Long> lastHopperCheck = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, Integer> tileEntitySkipCount = new ConcurrentHashMap<>();
    private static volatile double currentTPS = 20.0;
    private static final long[] tickDurations = new long[100];
    private static int tickIndex = 0;
    private static volatile long currentTick = 0;

    public TickOptimizer(TitanConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void initialize() {
        optimizationCount = 0;

        if (config.optimizeHoppers) optimizationCount++;
        if (config.skipRedundantTileEntityTicks) optimizationCount++;
        if (config.optimizeDropperAndDispenser) optimizationCount++;
        if (config.lazyInitTileEntities) optimizationCount++;
        if (config.cacheTileEntityLookups) optimizationCount++;
        optimizationCount++;
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
    }

    
    public static void recordTickDuration(long durationNanos) {
        tickDurations[tickIndex % tickDurations.length] = durationNanos;
        tickIndex++;

        if (tickIndex % 20 == 0) {
            long totalNanos = 0;
            int count = Math.min(tickIndex, tickDurations.length);
            for (int i = 0; i < count; i++) {
                totalNanos += tickDurations[i];
            }
            double avgTickMs = (totalNanos / (double) count) / 1_000_000.0;
            currentTPS = Math.min(20.0, 1000.0 / avgTickMs);
        }
    }

    public static double getCurrentTPS() {
        return currentTPS;
    }

    
    public boolean shouldHopperCheck(int x, int y, int z) {
        if (!config.optimizeHoppers) return true;

        long posHash = positionHash(x, y, z);
        Long lastCheck = lastHopperCheck.get(posHash);

        if (lastCheck != null && currentTick - lastCheck < config.hopperCheckInterval) {
            return false;
        }

        lastHopperCheck.put(posHash, currentTick);
        return true;
    }

    
    public void forceHopperCheck(int x, int y, int z) {
        lastHopperCheck.remove(positionHash(x, y, z));
    }

    
    public boolean shouldTileEntityTick(int x, int y, int z, boolean hasChanged) {
        if (!config.skipRedundantTileEntityTicks) return true;

        long posHash = positionHash(x, y, z);

        if (hasChanged) {
            tileEntitySkipCount.remove(posHash);
            return true;
        }

        int skipCount = tileEntitySkipCount.merge(posHash, 1, Integer::sum);
        int skipThreshold = Math.min(1 << Math.min(skipCount / 10, 4), 20);
        return currentTick % skipThreshold == 0;
    }

    
    public void markTileEntityDirty(int x, int y, int z) {
        tileEntitySkipCount.remove(positionHash(x, y, z));
    }

    
    public double getAdaptiveRate() {
        if (currentTPS >= 19.5) return 1.0;
        if (currentTPS >= 18.0) return 0.8;
        if (currentTPS >= 15.0) return 0.5;
        if (currentTPS >= 10.0) return 0.3;
        return 0.1;
    }

    
    public boolean shouldRunNonCritical() {
        double rate = getAdaptiveRate();
        if (rate >= 1.0) return true;
        return Math.random() < rate;
    }

    public void shutdown() {
        lastHopperCheck.clear();
        tileEntitySkipCount.clear();
    }

    public int getOptimizationCount() {
        return optimizationCount;
    }

    private static long positionHash(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
    }
}
