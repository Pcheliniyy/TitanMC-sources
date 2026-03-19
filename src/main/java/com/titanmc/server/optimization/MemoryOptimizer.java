package com.titanmc.server.optimization;

import com.titanmc.server.config.TitanConfig;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class MemoryOptimizer {

    private final TitanConfig config;
    private final Logger logger;
    private int optimizationCount = 0;
    private static final ObjectPool<double[]> vec3Pool = new ObjectPool<>(() -> new double[3], 256);
    private static final ObjectPool<int[]> blockPosPool = new ObjectPool<>(() -> new int[3], 512);
    private static final ObjectPool<byte[]> chunkDataPool = new ObjectPool<>(() -> new byte[16384], 64);
    private static final ObjectPool<byte[]> packetBufferPool = new ObjectPool<>(() -> new byte[8192], 128);
    private static final ThreadLocal<byte[]> scratchBuffer = ThreadLocal.withInitial(() -> new byte[4096]);
    private static final ThreadLocal<int[]> scratchIntBuffer = ThreadLocal.withInitial(() -> new int[256]);
    private static final AtomicInteger pooledAllocations = new AtomicInteger(0);
    private static final AtomicInteger pooledReturns = new AtomicInteger(0);

    public MemoryOptimizer(TitanConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void initialize() {
        optimizationCount = 0;

        if (config.useObjectPooling) optimizationCount++;
        if (config.optimizeNBTCaching) optimizationCount++;
        if (config.optimizeBlockStateLookups) optimizationCount++;
        if (config.compactCollections) optimizationCount++;
        if (config.reduceObjectAllocation) optimizationCount++;
        if (config.useThreadLocalAllocators) optimizationCount++;
        if (config.poolChunkData) optimizationCount++;
        if (config.reusePacketBuffers) optimizationCount++;
    }

    
    public static double[] borrowVec3() {
        double[] vec = vec3Pool.borrow();
        pooledAllocations.incrementAndGet();
        return vec;
    }

    public static void returnVec3(double[] vec) {
        vec[0] = 0; vec[1] = 0; vec[2] = 0;
        vec3Pool.returnObject(vec);
        pooledReturns.incrementAndGet();
    }

    
    public static int[] borrowBlockPos() {
        int[] pos = blockPosPool.borrow();
        pooledAllocations.incrementAndGet();
        return pos;
    }

    public static void returnBlockPos(int[] pos) {
        pos[0] = 0; pos[1] = 0; pos[2] = 0;
        blockPosPool.returnObject(pos);
        pooledReturns.incrementAndGet();
    }

    
    public static byte[] borrowChunkBuffer() {
        byte[] buf = chunkDataPool.borrow();
        pooledAllocations.incrementAndGet();
        return buf;
    }

    public static void returnChunkBuffer(byte[] buf) {
        chunkDataPool.returnObject(buf);
        pooledReturns.incrementAndGet();
    }

    
    public static byte[] borrowPacketBuffer() {
        byte[] buf = packetBufferPool.borrow();
        pooledAllocations.incrementAndGet();
        return buf;
    }

    public static void returnPacketBuffer(byte[] buf) {
        packetBufferPool.returnObject(buf);
        pooledReturns.incrementAndGet();
    }

    
    public static byte[] getScratchBuffer() {
        return scratchBuffer.get();
    }

    
    public static int[] getScratchIntBuffer() {
        return scratchIntBuffer.get();
    }

    public static int getPooledAllocations() {
        return pooledAllocations.get();
    }

    public static int getPooledReturns() {
        return pooledReturns.get();
    }

    public static void resetMetrics() {
        pooledAllocations.set(0);
        pooledReturns.set(0);
    }

    public void shutdown() {
    }

    public int getOptimizationCount() {
        return optimizationCount;
    }

    private static class ObjectPool<T> {
        private final ConcurrentLinkedDeque<T> pool = new ConcurrentLinkedDeque<>();
        private final java.util.function.Supplier<T> factory;
        private final int maxSize;

        ObjectPool(java.util.function.Supplier<T> factory, int maxSize) {
            this.factory = factory;
            this.maxSize = maxSize;
        }

        T borrow() {
            T obj = pool.poll();
            return obj != null ? obj : factory.get();
        }

        void returnObject(T obj) {
            if (pool.size() < maxSize) {
                pool.offer(obj);
            }
        }
    }
}
