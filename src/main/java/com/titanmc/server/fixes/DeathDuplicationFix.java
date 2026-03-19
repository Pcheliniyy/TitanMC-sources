package com.titanmc.server.fixes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class DeathDuplicationFix {

    private static final Set<UUID> processingDeath = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<UUID, Long> deathCooldowns = new ConcurrentHashMap<>();
    private static volatile long currentTick = 0;

    private DeathDuplicationFix() {}

    public static void patch() {
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        deathCooldowns.entrySet().removeIf(e -> currentTick - e.getValue() > 40);
    }

    public static boolean startDeathProcessing(UUID playerUuid) {
        if (processingDeath.contains(playerUuid)) return false;
        processingDeath.add(playerUuid);
        return true;
    }

    public static void endDeathProcessing(UUID playerUuid) {
        processingDeath.remove(playerUuid);
        deathCooldowns.put(playerUuid, currentTick);
    }

    public static boolean isProcessingDeath(UUID playerUuid) {
        return processingDeath.contains(playerUuid);
    }

    public static boolean canRespawn(UUID playerUuid) {
        return !processingDeath.contains(playerUuid);
    }

    public static boolean canDropItemsOnDeath(UUID playerUuid) {
        return processingDeath.contains(playerUuid);
    }

    public static void onPlayerDisconnect(UUID playerUuid) {
        processingDeath.remove(playerUuid);
        deathCooldowns.remove(playerUuid);
    }
}
