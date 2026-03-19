package com.titanmc.server.fixes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ChunkLoadDuplicationFix {
    private static final ConcurrentHashMap<UUID, Long> entityChunkMap = new ConcurrentHashMap<>();
    private static final Set<UUID> transitioningEntities = ConcurrentHashMap.newKeySet();

    private ChunkLoadDuplicationFix() {}

    public static void patch() {
    }

    
    public static boolean onEntityAddToChunk(UUID entityUuid, int chunkX, int chunkZ) {
        long chunkHash = chunkHash(chunkX, chunkZ);

        if (transitioningEntities.contains(entityUuid)) {
            transitioningEntities.remove(entityUuid);
            entityChunkMap.put(entityUuid, chunkHash);
            return true;
        }

        Long existingChunk = entityChunkMap.putIfAbsent(entityUuid, chunkHash);
        if (existingChunk != null && existingChunk != chunkHash) {
            return false;
        }
        return true;
    }

    
    public static void onEntityRemoveFromChunk(UUID entityUuid, int chunkX, int chunkZ) {
        long chunkHash = chunkHash(chunkX, chunkZ);
        entityChunkMap.remove(entityUuid, chunkHash);
    }

    
    public static void onEntityChunkTransition(UUID entityUuid) {
        transitioningEntities.add(entityUuid);
    }

    
    public static List<UUID> validateChunkEntities(List<UUID> entityUuids, int chunkX, int chunkZ) {
        List<UUID> duplicates = new ArrayList<>();
        long chunkHash = chunkHash(chunkX, chunkZ);

        for (UUID uuid : entityUuids) {
            Long existing = entityChunkMap.putIfAbsent(uuid, chunkHash);
            if (existing != null && existing != chunkHash) {
                duplicates.add(uuid);
            }
        }

        return duplicates;
    }

    
    public static void onChunkUnload(int chunkX, int chunkZ) {
        long chunkHash = chunkHash(chunkX, chunkZ);
        entityChunkMap.entrySet().removeIf(e -> e.getValue() == chunkHash);
    }

    
    public static void clearAll() {
        entityChunkMap.clear();
        transitioningEntities.clear();
    }

    private static long chunkHash(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    public static int getTrackedEntityCount() {
        return entityChunkMap.size();
    }
}
