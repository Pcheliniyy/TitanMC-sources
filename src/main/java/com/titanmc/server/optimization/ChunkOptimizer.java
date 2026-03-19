package com.titanmc.server.optimization;

import com.titanmc.server.config.TitanConfig;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class ChunkOptimizer {

    private final TitanConfig config;
    private final Logger logger;
    private int optimizationCount = 0;
    private ExecutorService chunkGenExecutor;
    private final PriorityBlockingQueue<ChunkLoadRequest> loadQueue = new PriorityBlockingQueue<>();
    private final ConcurrentHashMap<Long, CachedChunkData> chunkCache = new ConcurrentHashMap<>();
    private final ConcurrentLinkedDeque<Long> unloadCandidates = new ConcurrentLinkedDeque<>();
    private final ConcurrentHashMap<UUID, long[]> playerChunkPositions = new ConcurrentHashMap<>();

    public ChunkOptimizer(TitanConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void initialize() {
        optimizationCount = 0;

        if (config.asyncChunkGeneration) {
            int threads = Math.min(config.maxAsyncChunkThreads,
                                    Runtime.getRuntime().availableProcessors());
            chunkGenExecutor = new ThreadPoolExecutor(
                2, threads, 60L, TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(1024),
                r -> {
                    Thread t = new Thread(r, "TitanMC-ChunkGen");
                    t.setDaemon(true);
                    t.setPriority(Thread.NORM_PRIORITY - 1);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
            );
            optimizationCount++;
            logger.info("[TitanMC] Async chunk generation: " + threads + " threads");
        }

        if (config.chunkLoadingCacheSize > 0) {
            optimizationCount++;
            logger.info("[TitanMC] Chunk cache: " + config.chunkLoadingCacheSize + " entries");
        }

        if (config.aggressiveChunkUnloading) {
            optimizationCount++;
        }

        if (config.optimizeChunkSerialization) {
            optimizationCount++;
        }
        optimizationCount += 3;
    }

    
    public CompletableFuture<CachedChunkData> generateChunkAsync(int chunkX, int chunkZ,
                                                                    int priority) {
        if (chunkGenExecutor == null || chunkGenExecutor.isShutdown()) {
            return CompletableFuture.completedFuture(null);
        }

        CompletableFuture<CachedChunkData> future = new CompletableFuture<>();
        loadQueue.offer(new ChunkLoadRequest(chunkX, chunkZ, priority, future));

        chunkGenExecutor.submit(() -> {
            try {
                ChunkLoadRequest request = loadQueue.poll();
                if (request != null) {
                    CachedChunkData data = new CachedChunkData(request.chunkX, request.chunkZ);
                    chunkCache.put(chunkHash(request.chunkX, request.chunkZ), data);
                    request.future.complete(data);
                }
            } catch (Exception e) {
                logger.warning("[TitanMC] Async chunk gen error: " + e.getMessage());
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    
    public CachedChunkData getCachedChunk(int chunkX, int chunkZ) {
        return chunkCache.get(chunkHash(chunkX, chunkZ));
    }

    
    public void updatePlayerChunkPosition(UUID playerUuid, int chunkX, int chunkZ,
                                            double motionX, double motionZ) {
        playerChunkPositions.put(playerUuid, new long[]{chunkX, chunkZ,
            Double.doubleToLongBits(motionX), Double.doubleToLongBits(motionZ)});
    }

    
    public int calculateChunkPriority(int chunkX, int chunkZ) {
        int minDistance = Integer.MAX_VALUE;

        for (long[] playerPos : playerChunkPositions.values()) {
            int dx = (int)(playerPos[0] - chunkX);
            int dz = (int)(playerPos[1] - chunkZ);
            int dist = dx * dx + dz * dz;
            double motionX = Double.longBitsToDouble(playerPos[2]);
            double motionZ = Double.longBitsToDouble(playerPos[3]);
            if (dx * motionX > 0 || dz * motionZ > 0) {
                dist = (int)(dist * 0.7);
            }

            minDistance = Math.min(minDistance, dist);
        }

        return minDistance;
    }

    
    public void markForUnload(int chunkX, int chunkZ) {
        if (!config.aggressiveChunkUnloading) return;
        unloadCandidates.offer(chunkHash(chunkX, chunkZ));
    }

    
    public List<Long> getUnloadCandidates(int maxCount) {
        List<Long> candidates = new ArrayList<>();
        for (int i = 0; i < maxCount; i++) {
            Long hash = unloadCandidates.poll();
            if (hash == null) break;
            candidates.add(hash);
        }
        return candidates;
    }

    
    public void evictCache() {
        if (chunkCache.size() > config.chunkLoadingCacheSize) {
            int toRemove = chunkCache.size() - config.chunkLoadingCacheSize;
            Iterator<Map.Entry<Long, CachedChunkData>> iter = chunkCache.entrySet().iterator();
            while (iter.hasNext() && toRemove > 0) {
                iter.next();
                iter.remove();
                toRemove--;
            }
        }
    }

    public void shutdown() {
        if (chunkGenExecutor != null) {
            chunkGenExecutor.shutdown();
            try {
                chunkGenExecutor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                chunkGenExecutor.shutdownNow();
            }
        }
        chunkCache.clear();
        playerChunkPositions.clear();
    }

    public int getOptimizationCount() {
        return optimizationCount;
    }

    private static long chunkHash(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    public static class CachedChunkData {
        public final int chunkX, chunkZ;
        public final long cachedAt = System.currentTimeMillis();

        public CachedChunkData(int chunkX, int chunkZ) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
        }
    }

    private static class ChunkLoadRequest implements Comparable<ChunkLoadRequest> {
        final int chunkX, chunkZ, priority;
        final CompletableFuture<CachedChunkData> future;

        ChunkLoadRequest(int chunkX, int chunkZ, int priority,
                         CompletableFuture<CachedChunkData> future) {
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.priority = priority;
            this.future = future;
        }

        @Override
        public int compareTo(ChunkLoadRequest o) {
            return Integer.compare(this.priority, o.priority);
        }
    }
}
