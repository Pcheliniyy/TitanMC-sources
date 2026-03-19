package com.titanmc.server.fixes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PortalRollbackDupeFix {
    private static final ConcurrentHashMap<UUID, PortalTransitionState> transitionStates = new ConcurrentHashMap<>();

    private static volatile long currentTick = 0;

    private PortalRollbackDupeFix() {}

    public static void patch() {
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        transitionStates.entrySet().removeIf(e -> currentTick - e.getValue().startTick > 600);
    }

    
    public static void onPortalTransitionStart(UUID playerUuid, int inventoryHash, int itemCount) {
        transitionStates.put(playerUuid, new PortalTransitionState(
            inventoryHash, itemCount, currentTick));
    }

    
    public static boolean onPortalTransitionComplete(UUID playerUuid, int currentInventoryHash) {
        PortalTransitionState state = transitionStates.remove(playerUuid);
        if (state == null) return true;
        return true;
    }

    
    public static void onPortalTransitionFailed(UUID playerUuid) {
        transitionStates.remove(playerUuid);
    }

    
    public static boolean isInPortalTransition(UUID playerUuid) {
        return transitionStates.containsKey(playerUuid);
    }

    
    public static boolean canDropItems(UUID playerUuid) {
        return !isInPortalTransition(playerUuid);
    }

    
    public static boolean canInteractWithContainer(UUID playerUuid) {
        return !isInPortalTransition(playerUuid);
    }

    
    public static boolean canPickUpItems(UUID playerUuid) {
        return !isInPortalTransition(playerUuid);
    }

    
    public static void onDeathDuringTransition(UUID playerUuid) {
        PortalTransitionState state = transitionStates.remove(playerUuid);
    }

    public static void onPlayerDisconnect(UUID playerUuid) {
        transitionStates.remove(playerUuid);
    }

    private static class PortalTransitionState {
        final int inventoryHash;
        final int itemCount;
        final long startTick;

        PortalTransitionState(int inventoryHash, int itemCount, long startTick) {
            this.inventoryHash = inventoryHash;
            this.itemCount = itemCount;
            this.startTick = startTick;
        }
    }
}
