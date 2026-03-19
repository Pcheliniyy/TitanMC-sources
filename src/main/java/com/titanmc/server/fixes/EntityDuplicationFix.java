package com.titanmc.server.fixes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class EntityDuplicationFix {
    private static final ConcurrentHashMap<UUID, EntityLocation> globalEntityRegistry = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> recentlyRemoved = new ConcurrentHashMap<>();

    private static volatile long currentTick = 0;

    private EntityDuplicationFix() {}

    public static void patch() {
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        recentlyRemoved.entrySet().removeIf(e -> currentTick - e.getValue() > 20);
    }

    
    public static boolean registerEntity(UUID entityUuid, int worldId, int chunkX, int chunkZ) {
        if (recentlyRemoved.containsKey(entityUuid)) {
            return false;
        }

        EntityLocation newLoc = new EntityLocation(worldId, chunkX, chunkZ, currentTick);
        EntityLocation existing = globalEntityRegistry.putIfAbsent(entityUuid, newLoc);

        if (existing != null) {
            if (existing.worldId == worldId && existing.chunkX == chunkX && existing.chunkZ == chunkZ) {
                globalEntityRegistry.put(entityUuid, newLoc);
                return true;
            }
            return false;
        }

        return true;
    }

    
    public static void unregisterEntity(UUID entityUuid) {
        globalEntityRegistry.remove(entityUuid);
        recentlyRemoved.put(entityUuid, currentTick);
    }

    
    public static void updateEntityLocation(UUID entityUuid, int worldId, int chunkX, int chunkZ) {
        globalEntityRegistry.computeIfPresent(entityUuid, (k, v) ->
            new EntityLocation(worldId, chunkX, chunkZ, currentTick));
    }

    
    public static boolean isEntityRegistered(UUID entityUuid) {
        return globalEntityRegistry.containsKey(entityUuid);
    }

    
    public static Set<UUID> validateLoadedEntities(List<UUID> entityUuids, int worldId,
                                                     int chunkX, int chunkZ) {
        Set<UUID> duplicates = new HashSet<>();
        for (UUID uuid : entityUuids) {
            if (!registerEntity(uuid, worldId, chunkX, chunkZ)) {
                duplicates.add(uuid);
            }
        }
        return duplicates;
    }

    
    public static void clearWorld(int worldId) {
        globalEntityRegistry.entrySet().removeIf(e -> e.getValue().worldId == worldId);
    }

    
    public static void clearAll() {
        globalEntityRegistry.clear();
        recentlyRemoved.clear();
    }

    public static int getRegisteredEntityCount() {
        return globalEntityRegistry.size();
    }

    
    public static void periodicCleanup() {
        long staleThreshold = currentTick - 12000;
        globalEntityRegistry.entrySet().removeIf(e -> e.getValue().lastUpdateTick < staleThreshold);
    }

    private static class EntityLocation {
        final int worldId;
        final int chunkX;
        final int chunkZ;
        final long lastUpdateTick;

        EntityLocation(int worldId, int chunkX, int chunkZ, long lastUpdateTick) {
            this.worldId = worldId;
            this.chunkX = chunkX;
            this.chunkZ = chunkZ;
            this.lastUpdateTick = lastUpdateTick;
        }
    }
}
