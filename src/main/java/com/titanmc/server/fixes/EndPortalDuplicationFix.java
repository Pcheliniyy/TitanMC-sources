package com.titanmc.server.fixes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class EndPortalDuplicationFix {
    private static final Set<UUID> transitioning = ConcurrentHashMap.newKeySet();
    private static final ConcurrentHashMap<UUID, Long> transitionCooldowns = new ConcurrentHashMap<>();

    private static final int PORTAL_COOLDOWN_TICKS = 100;
    private static volatile long currentTick = 0;

    private EndPortalDuplicationFix() {}

    public static void patch() {
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        transitionCooldowns.entrySet().removeIf(e -> currentTick - e.getValue() > PORTAL_COOLDOWN_TICKS);
    }

    public static boolean startTransition(UUID entityUuid) {
        if (transitioning.contains(entityUuid)) return false;
        if (isOnCooldown(entityUuid)) return false;
        transitioning.add(entityUuid);
        return true;
    }

    public static void endTransition(UUID entityUuid) {
        transitioning.remove(entityUuid);
        transitionCooldowns.put(entityUuid, currentTick);
    }

    public static boolean isTransitioning(UUID entityUuid) {
        return transitioning.contains(entityUuid);
    }

    public static boolean isOnCooldown(UUID entityUuid) {
        Long cooldownStart = transitionCooldowns.get(entityUuid);
        return cooldownStart != null && currentTick - cooldownStart < PORTAL_COOLDOWN_TICKS;
    }

    public static void onEntityRemoved(UUID entityUuid) {
        transitioning.remove(entityUuid);
        transitionCooldowns.remove(entityUuid);
    }
}
