package com.titanmc.server.fixes;

import java.util.concurrent.ConcurrentHashMap;
import java.util.*;

public final class AnvilDuplicationFix {
    private static final ConcurrentHashMap<UUID, Long> anvilLocks = new ConcurrentHashMap<>();
    private AnvilDuplicationFix() {}
    public static void patch() {}
    public static boolean lockAnvil(UUID playerUuid) {
        return anvilLocks.putIfAbsent(playerUuid, System.currentTimeMillis()) == null;
    }
    public static void unlockAnvil(UUID playerUuid) { anvilLocks.remove(playerUuid); }
}
