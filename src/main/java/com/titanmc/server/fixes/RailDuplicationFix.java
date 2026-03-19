package com.titanmc.server.fixes;

import java.util.concurrent.ConcurrentHashMap;

public final class RailDuplicationFix {

    private static final ConcurrentHashMap<Long, Long> movingRails = new ConcurrentHashMap<>();
    private static volatile long currentTick = 0;

    private RailDuplicationFix() {}

    public static void patch() {
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        movingRails.clear();
    }

    
    public static void markRailMoving(int x, int y, int z) {
        movingRails.put(positionHash(x, y, z), currentTick);
    }

    
    public static boolean shouldDropRailItem(int x, int y, int z) {
        Long moveTick = movingRails.get(positionHash(x, y, z));
        return moveTick == null || moveTick != currentTick;
    }

    
    public static boolean canBreakRail(int x, int y, int z) {
        return shouldDropRailItem(x, y, z);
    }

    private static long positionHash(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
    }
}
