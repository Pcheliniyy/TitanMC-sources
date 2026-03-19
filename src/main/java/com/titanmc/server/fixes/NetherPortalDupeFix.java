package com.titanmc.server.fixes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class NetherPortalDupeFix {

    private static final Set<UUID> portalProcessing = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<UUID, Long> portalCooldown = new ConcurrentHashMap<>();
    private static final int COOLDOWN_TICKS = 80;
    private static volatile long currentTick = 0;

    private NetherPortalDupeFix() {}

    public static void patch() {
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        portalCooldown.entrySet().removeIf(e -> currentTick - e.getValue() > COOLDOWN_TICKS);
    }

    public static boolean startPortalProcessing(UUID entityUuid) {
        if (portalProcessing.contains(entityUuid)) return false;
        Long cooldownTick = portalCooldown.get(entityUuid);
        if (cooldownTick != null && currentTick - cooldownTick < COOLDOWN_TICKS) return false;
        portalProcessing.add(entityUuid);
        return true;
    }

    public static void endPortalProcessing(UUID entityUuid) {
        portalProcessing.remove(entityUuid);
        portalCooldown.put(entityUuid, currentTick);
    }

    public static boolean isProcessing(UUID entityUuid) {
        return portalProcessing.contains(entityUuid);
    }

    public static boolean isOnCooldown(UUID entityUuid) {
        Long tick = portalCooldown.get(entityUuid);
        return tick != null && currentTick - tick < COOLDOWN_TICKS;
    }

    public static void onEntityRemoved(UUID entityUuid) {
        portalProcessing.remove(entityUuid);
        portalCooldown.remove(entityUuid);
    }
}
