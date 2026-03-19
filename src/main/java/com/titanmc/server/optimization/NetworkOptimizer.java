package com.titanmc.server.optimization;

import com.titanmc.server.config.TitanConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class NetworkOptimizer {

    private final TitanConfig config;
    private final Logger logger;
    private int optimizationCount = 0;
    private static final ConcurrentHashMap<UUID, PacketBatch> playerPacketBatches = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, Long>> sentPacketHashes = new ConcurrentHashMap<>();

    private static volatile long currentTick = 0;

    public NetworkOptimizer(TitanConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void initialize() {
        optimizationCount = 0;

        if (config.compressPackets) optimizationCount++;
        if (config.batchEntityUpdates) optimizationCount++;
        if (config.asyncPacketProcessing) optimizationCount++;
        if (config.optimizeChunkPackets) optimizationCount++;
        if (config.flushConsolidation) optimizationCount++;
        optimizationCount += 2;
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        for (PacketBatch batch : playerPacketBatches.values()) {
            batch.entityUpdateCount = 0;
        }
        if (tick % 100 == 0) {
            for (ConcurrentHashMap<Integer, Long> hashes : sentPacketHashes.values()) {
                hashes.entrySet().removeIf(e -> currentTick - e.getValue() > 20);
            }
        }
    }

    
    public boolean batchEntityUpdate(UUID playerUuid, int entityId) {
        if (!config.batchEntityUpdates) return false;

        PacketBatch batch = playerPacketBatches.computeIfAbsent(playerUuid, k -> new PacketBatch());
        batch.entityUpdateCount++;

        return batch.entityUpdateCount < config.entityUpdateBatchSize;
    }

    
    public int getBatchedUpdateCount(UUID playerUuid) {
        PacketBatch batch = playerPacketBatches.get(playerUuid);
        return batch != null ? batch.entityUpdateCount : 0;
    }

    
    public boolean isDuplicatePacket(UUID playerUuid, int packetHash) {
        ConcurrentHashMap<Integer, Long> hashes = sentPacketHashes.computeIfAbsent(
            playerUuid, k -> new ConcurrentHashMap<>());

        Long lastSent = hashes.get(packetHash);
        if (lastSent != null && currentTick - lastSent < 2) {
            return true;
        }

        hashes.put(packetHash, currentTick);
        return false;
    }

    
    public int getCompressionLevel(int packetSize) {
        if (!config.compressPackets) return 0;

        if (packetSize < config.packetCompressionThreshold) {
            return 0;
        } else if (packetSize < 1024) {
            return Math.min(config.networkCompressionLevel, 3);
        } else if (packetSize < 8192) {
            return config.networkCompressionLevel;
        } else {
            return Math.min(config.networkCompressionLevel + 1, 9);
        }
    }

    
    public boolean shouldSendChunkUpdate(int playerChunkX, int playerChunkZ,
                                           int chunkX, int chunkZ) {
        int dx = playerChunkX - chunkX;
        int dz = playerChunkZ - chunkZ;
        int distSq = dx * dx + dz * dz;

        if (distSq > 100) {
            return currentTick % 10 == 0;
        } else if (distSq > 36) {
            return currentTick % 4 == 0;
        }

        return true;
    }

    
    public int getMaxCatchupTicks(UUID playerUuid) {
        return config.maxPlayerTickCatchup;
    }

    public void onPlayerDisconnect(UUID playerUuid) {
        playerPacketBatches.remove(playerUuid);
        sentPacketHashes.remove(playerUuid);
    }

    public void shutdown() {
        playerPacketBatches.clear();
        sentPacketHashes.clear();
    }

    public int getOptimizationCount() {
        return optimizationCount;
    }

    private static class PacketBatch {
        int entityUpdateCount = 0;
    }
}
