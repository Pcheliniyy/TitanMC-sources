package com.titanmc.server.fixes;

import java.util.concurrent.ConcurrentHashMap;

public final class BrewingStandDuplicationFix {
    private static final ConcurrentHashMap<Long, Boolean> activeBrewing = new ConcurrentHashMap<>();
    private BrewingStandDuplicationFix() {}
    public static void patch() {}
    public static void markBrewing(int x, int y, int z) {
        activeBrewing.put(posHash(x, y, z), true);
    }
    public static void markComplete(int x, int y, int z) {
        activeBrewing.remove(posHash(x, y, z));
    }
    public static boolean isBrewing(int x, int y, int z) {
        return activeBrewing.containsKey(posHash(x, y, z));
    }
    private static long posHash(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
    }
}
