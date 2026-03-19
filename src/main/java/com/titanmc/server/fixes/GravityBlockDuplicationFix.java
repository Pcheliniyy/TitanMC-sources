package com.titanmc.server.fixes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class GravityBlockDuplicationFix {
    private static final ConcurrentHashMap<UUID, Long> fallingBlockSources = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, UUID> positionToEntity = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, Long> consumedPositions = new ConcurrentHashMap<>();

    private static volatile long currentTick = 0;

    private GravityBlockDuplicationFix() {}

    public static void patch() {
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        consumedPositions.entrySet().removeIf(e -> currentTick - e.getValue() > 100);
    }

    
    public static void onFallingBlockCreated(UUID entityUuid, int x, int y, int z) {
        long posHash = positionHash(x, y, z);
        fallingBlockSources.put(entityUuid, posHash);
        positionToEntity.put(posHash, entityUuid);
        consumedPositions.put(posHash, currentTick);
    }

    
    public static void onFallingBlockLanded(UUID entityUuid) {
        Long posHash = fallingBlockSources.remove(entityUuid);
        if (posHash != null) {
            positionToEntity.remove(posHash, entityUuid);
            consumedPositions.remove(posHash);
        }
    }

    
    public static void onFallingBlockRemoved(UUID entityUuid) {
        onFallingBlockLanded(entityUuid);
    }

    
    public static boolean canCreateFallingBlock(int x, int y, int z) {
        long posHash = positionHash(x, y, z);
        return !positionToEntity.containsKey(posHash);
    }

    
    public static boolean validateLoadedFallingBlock(UUID entityUuid, int x, int y, int z,
                                                      boolean sourceBlockExists) {
        long posHash = positionHash(x, y, z);
        UUID existingEntity = positionToEntity.get(posHash);
        if (existingEntity != null && !existingEntity.equals(entityUuid)) {
            return false;
        }
        fallingBlockSources.put(entityUuid, posHash);
        positionToEntity.put(posHash, entityUuid);
        return true;
    }

    
    public static boolean validateDimensionTransition(UUID entityUuid) {
        return fallingBlockSources.containsKey(entityUuid);
    }

    
    public static boolean wasRecentlyConsumed(int x, int y, int z) {
        Long consumedTick = consumedPositions.get(positionHash(x, y, z));
        return consumedTick != null && currentTick - consumedTick < 100;
    }

    private static long positionHash(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
    }

    public static int getTrackedEntityCount() {
        return fallingBlockSources.size();
    }
}
