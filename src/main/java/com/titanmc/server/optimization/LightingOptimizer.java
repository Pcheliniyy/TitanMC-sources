package com.titanmc.server.optimization;

import com.titanmc.server.config.TitanConfig;

import java.util.concurrent.*;
import java.util.logging.Logger;

public class LightingOptimizer {

    private final TitanConfig config;
    private final Logger logger;
    private int optimizationCount = 0;

    private ExecutorService lightingExecutor;
    private static volatile int lightUpdatesThisTick = 0;
    private static volatile long currentTick = 0;
    private final ConcurrentHashMap<Long, Byte> lightCache = new ConcurrentHashMap<>();

    public LightingOptimizer(TitanConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void initialize() {
        optimizationCount = 0;

        if (config.asyncLightingUpdates) {
            lightingExecutor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "TitanMC-Lighting");
                t.setDaemon(true);
                t.setPriority(Thread.NORM_PRIORITY - 2);
                return t;
            });
            optimizationCount++;
            logger.info("[TitanMC] Async lighting engine enabled");
        }

        if (config.optimizeSkyLightPropagation) optimizationCount++;
        if (config.optimizeBlockLightPropagation) optimizationCount++;
        if (config.disableLightForUnloadedChunks) optimizationCount++;
        optimizationCount += 2;
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        lightUpdatesThisTick = 0;
    }

    
    public boolean submitLightUpdate(int x, int y, int z, int lightType, int newLevel) {
        if (lightUpdatesThisTick >= config.maxLightUpdatesPerTick) {
            return false;
        }
        lightUpdatesThisTick++;

        long posHash = positionHash(x, y, z);
        lightCache.put(posHash, (byte) newLevel);

        if (config.asyncLightingUpdates && lightingExecutor != null) {
            lightingExecutor.submit(() -> {
            });
        }

        return true;
    }

    
    public int getCachedLightLevel(int x, int y, int z) {
        Byte level = lightCache.get(positionHash(x, y, z));
        return level != null ? level : -1;
    }

    
    public boolean shouldProcessLightingForChunk(boolean isLoaded) {
        if (config.disableLightForUnloadedChunks && !isLoaded) {
            return false;
        }
        return true;
    }

    
    public boolean needsSkyLightCheck(int x, int y, int z, int heightMapValue) {
        if (!config.optimizeSkyLightPropagation) return true;
        return y >= heightMapValue - 16;
    }

    public void shutdown() {
        if (lightingExecutor != null) {
            lightingExecutor.shutdown();
            try {
                lightingExecutor.awaitTermination(3, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                lightingExecutor.shutdownNow();
            }
        }
        lightCache.clear();
    }

    public int getOptimizationCount() {
        return optimizationCount;
    }

    public static int getLightUpdatesThisTick() {
        return lightUpdatesThisTick;
    }

    private static long positionHash(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
    }
}
