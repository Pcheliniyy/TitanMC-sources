package com.titanmc.server.fixes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class InventoryDesyncDupeFix {
    private static final ConcurrentHashMap<UUID, ActionTracker> playerActions = new ConcurrentHashMap<>();
    private static final int MAX_CLICKS_PER_TICK = 16;
    private static final int MAX_WINDOW_OPS_PER_SECOND = 60;

    private static volatile long currentTick = 0;

    private InventoryDesyncDupeFix() {}

    public static void patch() {
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        for (ActionTracker tracker : playerActions.values()) {
            tracker.tickActions = 0;
        }
    }

    
    public static boolean validateInventoryClick(UUID playerUuid, int windowId,
                                                   int slotId, int buttonId,
                                                   int actionType) {
        ActionTracker tracker = playerActions.computeIfAbsent(playerUuid, k -> new ActionTracker());
        tracker.tickActions++;
        if (tracker.tickActions > MAX_CLICKS_PER_TICK) {
            tracker.needsResync = true;
            return false;
        }
        if (windowId != tracker.currentWindowId && tracker.currentWindowId != -1) {
            tracker.windowMismatchCount++;
            if (tracker.windowMismatchCount > 3) {
                tracker.needsResync = true;
                return false;
            }
        }
        if (slotId < -999 || slotId > 255) {
            return false;
        }
        if (actionType < 0 || actionType > 6) {
            return false;
        }

        tracker.lastActionTick = currentTick;
        return true;
    }

    
    public static void onWindowOpen(UUID playerUuid, int windowId) {
        ActionTracker tracker = playerActions.computeIfAbsent(playerUuid, k -> new ActionTracker());
        tracker.currentWindowId = windowId;
        tracker.windowMismatchCount = 0;
    }

    
    public static void onWindowClose(UUID playerUuid) {
        ActionTracker tracker = playerActions.get(playerUuid);
        if (tracker != null) {
            tracker.currentWindowId = -1;
            tracker.windowMismatchCount = 0;
        }
    }

    
    public static boolean needsResync(UUID playerUuid) {
        ActionTracker tracker = playerActions.get(playerUuid);
        if (tracker != null && tracker.needsResync) {
            tracker.needsResync = false;
            return true;
        }
        return false;
    }

    
    public static void forceResync(UUID playerUuid) {
        ActionTracker tracker = playerActions.get(playerUuid);
        if (tracker != null) {
            tracker.needsResync = true;
        }
    }

    
    public static void onPlayerDisconnect(UUID playerUuid) {
        playerActions.remove(playerUuid);
    }

    
    public static boolean validateSlotState(int actualHash, int claimedHash) {
        return actualHash == claimedHash;
    }

    private static class ActionTracker {
        int tickActions = 0;
        int currentWindowId = -1;
        int windowMismatchCount = 0;
        long lastActionTick = 0;
        boolean needsResync = false;
    }
}
