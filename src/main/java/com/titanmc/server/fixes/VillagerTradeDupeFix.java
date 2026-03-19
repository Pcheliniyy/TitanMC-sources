package com.titanmc.server.fixes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class VillagerTradeDupeFix {
    private static final ConcurrentHashMap<UUID, Long> tradeCooldowns = new ConcurrentHashMap<>();
    private static final int MIN_TRADE_INTERVAL_MS = 50;

    private VillagerTradeDupeFix() {}

    public static void patch() {
    }

    public static boolean canTrade(UUID playerUuid) {
        long now = System.currentTimeMillis();
        Long lastTrade = tradeCooldowns.get(playerUuid);
        if (lastTrade != null && now - lastTrade < MIN_TRADE_INTERVAL_MS) {
            return false;
        }
        tradeCooldowns.put(playerUuid, now);
        return true;
    }

    public static void onPlayerDisconnect(UUID playerUuid) {
        tradeCooldowns.remove(playerUuid);
    }
}
