package com.titanmc.server.optimization;

import com.titanmc.server.config.TitanConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class RedstoneOptimizer {

    private final TitanConfig config;
    private final Logger logger;
    private int optimizationCount = 0;
    private static volatile int updatesThisTick = 0;
    private static final ConcurrentHashMap<Long, Integer> comparatorCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, int[]> torchBurnouts = new ConcurrentHashMap<>();

    private static volatile long currentTick = 0;

    public RedstoneOptimizer(TitanConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void initialize() {
        optimizationCount = 0;

        if (config.alternateRedstoneAlgorithm) {
            optimizationCount++;
            logger.info("[TitanMC] EIGEN redstone algorithm enabled");
        }
        if (config.optimizeRedstoneTorchChecks) optimizationCount++;
        if (config.optimizeObservers) optimizationCount++;
        if (config.optimizeComparators) optimizationCount++;
        if (config.optimizeRepeaters) optimizationCount++;
        optimizationCount++;
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        updatesThisTick = 0;
    }

    
    public int calculateRedstonePower(int x, int y, int z, int currentPower,
                                        int[] neighborPowers) {
        if (!config.alternateRedstoneAlgorithm) {
            return -1;
        }
        int maxNeighborPower = 0;
        for (int power : neighborPowers) {
            maxNeighborPower = Math.max(maxNeighborPower, power);
        }
        int newPower = Math.max(0, maxNeighborPower - 1);
        if (newPower != currentPower) {
            updatesThisTick++;
        }

        return newPower;
    }

    
    public boolean canProcessUpdate() {
        return updatesThisTick < config.maxRedstoneUpdatesPerTick;
    }

    
    public boolean registerUpdate() {
        updatesThisTick++;
        return updatesThisTick <= config.maxRedstoneUpdatesPerTick;
    }

    public static int getUpdatesThisTick() {
        return updatesThisTick;
    }

    
    public boolean shouldTorchBurnout(int x, int y, int z) {
        if (!config.optimizeRedstoneTorchChecks) return false;

        long posHash = positionHash(x, y, z);
        int[] burnoutData = torchBurnouts.computeIfAbsent(posHash, k -> new int[]{0, 0});
        if (currentTick - burnoutData[1] < 60) {
            burnoutData[0]++;
        } else {
            burnoutData[0] = 1;
        }
        burnoutData[1] = (int) currentTick;

        return burnoutData[0] >= 8;
    }

    
    public int getCachedComparatorOutput(int x, int y, int z) {
        if (!config.optimizeComparators) return -1;
        Integer cached = comparatorCache.get(positionHash(x, y, z));
        return cached != null ? cached : -1;
    }

    
    public void cacheComparatorOutput(int x, int y, int z, int signalStrength) {
        if (config.optimizeComparators) {
            comparatorCache.put(positionHash(x, y, z), signalStrength);
        }
    }

    
    public void invalidateComparatorCache(int x, int y, int z) {
        comparatorCache.remove(positionHash(x, y, z));
        comparatorCache.remove(positionHash(x + 1, y, z));
        comparatorCache.remove(positionHash(x - 1, y, z));
        comparatorCache.remove(positionHash(x, y, z + 1));
        comparatorCache.remove(positionHash(x, y, z - 1));
    }

    
    private static final ConcurrentHashMap<Long, Long> lastObserverTrigger = new ConcurrentHashMap<>();

    public boolean shouldObserverTrigger(int x, int y, int z) {
        if (!config.optimizeObservers) return true;

        long posHash = positionHash(x, y, z);
        Long lastTrigger = lastObserverTrigger.get(posHash);

        if (lastTrigger != null && currentTick - lastTrigger < 2) {
            return false;
        }

        lastObserverTrigger.put(posHash, currentTick);
        return true;
    }

    public void shutdown() {
        comparatorCache.clear();
        torchBurnouts.clear();
        lastObserverTrigger.clear();
    }

    public int getOptimizationCount() {
        return optimizationCount;
    }

    private static long positionHash(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
    }
}
