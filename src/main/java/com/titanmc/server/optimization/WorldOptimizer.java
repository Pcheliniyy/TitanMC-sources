package com.titanmc.server.optimization;

import com.titanmc.server.config.TitanConfig;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class WorldOptimizer {

    private final TitanConfig config;
    private final Logger logger;
    private int optimizationCount = 0;
    private static final AtomicInteger tntThisTick = new AtomicInteger(0);
    private static final AtomicInteger fluidTicksThisTick = new AtomicInteger(0);
    private static final ConcurrentHashMap<Long, Integer> blockStateCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, int[]> heightmapCache = new ConcurrentHashMap<>();
    private static final ThreadLocal<ConcurrentHashMap<Long, Boolean>> explosionBlockCache =
        ThreadLocal.withInitial(ConcurrentHashMap::new);

    private static volatile long currentTick = 0;

    public WorldOptimizer(TitanConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void initialize() {
        optimizationCount = 0;

        if (config.optimizeExplosions) optimizationCount++;
        if (config.optimizeBlockUpdates) optimizationCount++;
        if (config.optimizeFluidTicks) optimizationCount++;
        if (config.optimizeLeafDecay) optimizationCount++;
        if (config.fastBlockStateAccess) optimizationCount++;
        if (config.optimizeHeightmaps) optimizationCount++;
        optimizationCount += 2;
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        tntThisTick.set(0);
        fluidTicksThisTick.set(0);
    }

    
    public static boolean getExplosionBlockCache(int x, int y, int z) {
        ConcurrentHashMap<Long, Boolean> cache = explosionBlockCache.get();
        Boolean cached = cache.get(positionHash(x, y, z));
        return cached != null;
    }

    public static void setExplosionBlockCache(int x, int y, int z, boolean isAir) {
        explosionBlockCache.get().put(positionHash(x, y, z), isAir);
    }

    public static void clearExplosionBlockCache() {
        explosionBlockCache.get().clear();
    }

    
    public boolean canPrimeTNT() {
        return tntThisTick.incrementAndGet() <= config.maxPrimedTntPerTick;
    }

    public static int getTNTThisTick() {
        return tntThisTick.get();
    }

    
    public boolean canProcessFluidTick() {
        if (!config.optimizeFluidTicks) return true;
        return fluidTicksThisTick.incrementAndGet() <= config.maxFluidTicksPerTick;
    }

    
    public static int getCachedBlockState(int x, int y, int z) {
        Integer state = blockStateCache.get(positionHash(x, y, z));
        return state != null ? state : -1;
    }

    
    public static void cacheBlockState(int x, int y, int z, int stateId) {
        if (blockStateCache.size() < 65536) {
            blockStateCache.put(positionHash(x, y, z), stateId);
        }
    }

    
    public static void invalidateBlockState(int x, int y, int z) {
        blockStateCache.remove(positionHash(x, y, z));
    }

    
    public int[] getCachedHeightmap(int chunkX, int chunkZ) {
        if (!config.optimizeHeightmaps) return null;
        return heightmapCache.get(chunkHash(chunkX, chunkZ));
    }

    
    public void cacheHeightmap(int chunkX, int chunkZ, int[] heightmap) {
        if (config.optimizeHeightmaps) {
            heightmapCache.put(chunkHash(chunkX, chunkZ), heightmap);
        }
    }

    
    public void invalidateHeightmap(int chunkX, int chunkZ) {
        heightmapCache.remove(chunkHash(chunkX, chunkZ));
    }

    private static final AtomicInteger leafDecayThisTick = new AtomicInteger(0);
    private static final int MAX_LEAF_DECAY_PER_TICK = 20;

    
    public boolean canProcessLeafDecay() {
        if (!config.optimizeLeafDecay) return true;
        return leafDecayThisTick.incrementAndGet() <= MAX_LEAF_DECAY_PER_TICK;
    }

    public static void resetLeafDecayCounter() {
        leafDecayThisTick.set(0);
    }

    public void shutdown() {
        blockStateCache.clear();
        heightmapCache.clear();
    }

    public int getOptimizationCount() {
        return optimizationCount;
    }

    private static long positionHash(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
    }

    private static long chunkHash(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }
}
