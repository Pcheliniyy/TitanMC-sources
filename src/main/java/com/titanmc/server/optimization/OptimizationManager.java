package com.titanmc.server.optimization;

import com.titanmc.server.config.TitanConfig;

import java.util.logging.Logger;

public class OptimizationManager {

    private final TitanConfig config;
    private final Logger logger;
    private int optimizationCount = 0;
    private ChunkOptimizer chunkOptimizer;
    private EntityOptimizer entityOptimizer;
    private RedstoneOptimizer redstoneOptimizer;
    private LightingOptimizer lightingOptimizer;
    private NetworkOptimizer networkOptimizer;
    private MemoryOptimizer memoryOptimizer;
    private TickOptimizer tickOptimizer;
    private WorldOptimizer worldOptimizer;

    public OptimizationManager(TitanConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void initialize() {
        optimizationCount = 0;
        if (config.optimizeChunkLoading) {
            chunkOptimizer = new ChunkOptimizer(config, logger);
            chunkOptimizer.initialize();
            optimizationCount += chunkOptimizer.getOptimizationCount();
            logger.info("[TitanMC] Chunk optimizer: " + chunkOptimizer.getOptimizationCount() + " optimizations");
        }
        if (config.optimizeEntityTicking) {
            entityOptimizer = new EntityOptimizer(config, logger);
            entityOptimizer.initialize();
            optimizationCount += entityOptimizer.getOptimizationCount();
            logger.info("[TitanMC] Entity optimizer: " + entityOptimizer.getOptimizationCount() + " optimizations");
        }
        if (config.optimizeRedstone) {
            redstoneOptimizer = new RedstoneOptimizer(config, logger);
            redstoneOptimizer.initialize();
            optimizationCount += redstoneOptimizer.getOptimizationCount();
            logger.info("[TitanMC] Redstone optimizer: " + redstoneOptimizer.getOptimizationCount() + " optimizations");
        }
        if (config.optimizeLighting) {
            lightingOptimizer = new LightingOptimizer(config, logger);
            lightingOptimizer.initialize();
            optimizationCount += lightingOptimizer.getOptimizationCount();
            logger.info("[TitanMC] Lighting optimizer: " + lightingOptimizer.getOptimizationCount() + " optimizations");
        }
        if (config.optimizeNetworking) {
            networkOptimizer = new NetworkOptimizer(config, logger);
            networkOptimizer.initialize();
            optimizationCount += networkOptimizer.getOptimizationCount();
            logger.info("[TitanMC] Network optimizer: " + networkOptimizer.getOptimizationCount() + " optimizations");
        }
        if (config.optimizeMemory) {
            memoryOptimizer = new MemoryOptimizer(config, logger);
            memoryOptimizer.initialize();
            optimizationCount += memoryOptimizer.getOptimizationCount();
            logger.info("[TitanMC] Memory optimizer: " + memoryOptimizer.getOptimizationCount() + " optimizations");
        }
        if (config.optimizeTickLoop) {
            tickOptimizer = new TickOptimizer(config, logger);
            tickOptimizer.initialize();
            optimizationCount += tickOptimizer.getOptimizationCount();
            logger.info("[TitanMC] Tick optimizer: " + tickOptimizer.getOptimizationCount() + " optimizations");
        }
        if (config.optimizeWorldGeneration) {
            worldOptimizer = new WorldOptimizer(config, logger);
            worldOptimizer.initialize();
            optimizationCount += worldOptimizer.getOptimizationCount();
            logger.info("[TitanMC] World optimizer: " + worldOptimizer.getOptimizationCount() + " optimizations");
        }
    }

    public void reload() {
        shutdown();
        initialize();
    }

    public void shutdown() {
        if (chunkOptimizer != null) chunkOptimizer.shutdown();
        if (entityOptimizer != null) entityOptimizer.shutdown();
        if (redstoneOptimizer != null) redstoneOptimizer.shutdown();
        if (lightingOptimizer != null) lightingOptimizer.shutdown();
        if (networkOptimizer != null) networkOptimizer.shutdown();
        if (memoryOptimizer != null) memoryOptimizer.shutdown();
        if (tickOptimizer != null) tickOptimizer.shutdown();
        if (worldOptimizer != null) worldOptimizer.shutdown();
    }

    public int getOptimizationCount() {
        return optimizationCount;
    }
    public ChunkOptimizer getChunkOptimizer() { return chunkOptimizer; }
    public EntityOptimizer getEntityOptimizer() { return entityOptimizer; }
    public RedstoneOptimizer getRedstoneOptimizer() { return redstoneOptimizer; }
    public LightingOptimizer getLightingOptimizer() { return lightingOptimizer; }
    public NetworkOptimizer getNetworkOptimizer() { return networkOptimizer; }
    public MemoryOptimizer getMemoryOptimizer() { return memoryOptimizer; }
    public TickOptimizer getTickOptimizer() { return tickOptimizer; }
    public WorldOptimizer getWorldOptimizer() { return worldOptimizer; }
}
