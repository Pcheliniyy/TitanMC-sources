package com.titanmc.server.fixes;

import java.util.concurrent.ConcurrentHashMap;

public final class CarpetDuplicationFix {

    private static final ConcurrentHashMap<Long, Long> movingCarpets = new ConcurrentHashMap<>();
    private static volatile long currentTick = 0;

    private CarpetDuplicationFix() {}

    public static void patch() {
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        movingCarpets.clear();
    }

    public static void markCarpetMoving(int x, int y, int z) {
        movingCarpets.put(positionHash(x, y, z), currentTick);
    }

    public static boolean shouldCarpetDrop(int x, int y, int z) {
        Long moveTick = movingCarpets.get(positionHash(x, y, z));
        return moveTick == null || moveTick != currentTick;
    }

    public static boolean shouldCheckCarpetSupport(int x, int y, int z) {
        return shouldCarpetDrop(x, y, z);
    }

    private static long positionHash(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
    }
}
