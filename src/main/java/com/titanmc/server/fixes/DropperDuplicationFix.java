package com.titanmc.server.fixes;

import java.util.concurrent.ConcurrentHashMap;

public final class DropperDuplicationFix {

    private static final ConcurrentHashMap<Long, Long> activeDroppers = new ConcurrentHashMap<>();
    private static volatile long currentTick = 0;

    private DropperDuplicationFix() {}

    public static void patch() {
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        activeDroppers.clear();
    }

    public static boolean lockDropper(int x, int y, int z) {
        long posHash = positionHash(x, y, z);
        return activeDroppers.putIfAbsent(posHash, currentTick) == null;
    }

    public static void unlockDropper(int x, int y, int z) {
        activeDroppers.remove(positionHash(x, y, z));
    }

    public static boolean isDropperLocked(int x, int y, int z) {
        return activeDroppers.containsKey(positionHash(x, y, z));
    }

    private static long positionHash(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
    }
}
