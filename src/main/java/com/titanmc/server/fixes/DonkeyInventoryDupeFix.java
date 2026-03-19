package com.titanmc.server.fixes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DonkeyInventoryDupeFix {
    private static final Set<UUID> lockedInventories = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<UUID, UUID> openMountInventories = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, InventorySnapshot> inventorySnapshots = new ConcurrentHashMap<>();

    private DonkeyInventoryDupeFix() {}

    public static void patch() {
    }

    
    public static boolean onOpenMountInventory(UUID playerUuid, UUID mountUuid,
                                                 int[] itemHashes, int totalItems) {
        if (lockedInventories.contains(mountUuid)) {
            return false;
        }

        openMountInventories.put(playerUuid, mountUuid);
        inventorySnapshots.put(playerUuid, new InventorySnapshot(itemHashes, totalItems));
        return true;
    }

    
    public static void onCloseMountInventory(UUID playerUuid) {
        openMountInventories.remove(playerUuid);
        inventorySnapshots.remove(playerUuid);
    }

    
    public static boolean onPlayerDismount(UUID playerUuid) {
        UUID mountUuid = openMountInventories.get(playerUuid);
        if (mountUuid != null) {
            onCloseMountInventory(playerUuid);
            return true;
        }
        return false;
    }

    
    public static void onMountDeath(UUID mountUuid) {
        lockedInventories.add(mountUuid);
        openMountInventories.entrySet().removeIf(entry -> {
            if (entry.getValue().equals(mountUuid)) {
                inventorySnapshots.remove(entry.getKey());
                return true;
            }
            return false;
        });
    }

    
    public static void onMountRemoved(UUID mountUuid) {
        lockedInventories.remove(mountUuid);
        openMountInventories.entrySet().removeIf(e -> e.getValue().equals(mountUuid));
    }

    
    public static boolean canAccessInventory(UUID mountUuid) {
        return !lockedInventories.contains(mountUuid);
    }

    
    public static void onPlayerDisconnect(UUID playerUuid) {
        onCloseMountInventory(playerUuid);
    }

    
    private static class InventorySnapshot {
        final int[] itemHashes;
        final int totalItems;

        InventorySnapshot(int[] itemHashes, int totalItems) {
            this.itemHashes = itemHashes;
            this.totalItems = totalItems;
        }
    }
}
