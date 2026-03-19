package com.titanmc.server.fixes;

import java.util.concurrent.ConcurrentHashMap;

public final class BeaconDuplicationFix {
    private static final ConcurrentHashMap<Long, Long> beaconLocks = new ConcurrentHashMap<>();
    private BeaconDuplicationFix() {}
    public static void patch() {}
    public static boolean lockBeacon(int x, int y, int z) {
        long h = ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
        return beaconLocks.putIfAbsent(h, System.currentTimeMillis()) == null;
    }
    public static void unlockBeacon(int x, int y, int z) {
        long h = ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
        beaconLocks.remove(h);
    }
}
