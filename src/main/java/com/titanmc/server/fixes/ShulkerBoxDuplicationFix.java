package com.titanmc.server.fixes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class ShulkerBoxDuplicationFix {
    private static final ConcurrentHashMap<Long, ShulkerState> lockedShulkers = new ConcurrentHashMap<>();

    private ShulkerBoxDuplicationFix() {}

    public static void patch() {
    }

    public static boolean lockShulkerBox(int x, int y, int z, UUID accessorUuid) {
        long posHash = positionHash(x, y, z);
        ShulkerState state = lockedShulkers.putIfAbsent(posHash,
            new ShulkerState(accessorUuid, System.currentTimeMillis()));
        return state == null || state.accessorUuid.equals(accessorUuid);
    }

    public static void unlockShulkerBox(int x, int y, int z) {
        lockedShulkers.remove(positionHash(x, y, z));
    }

    public static boolean isShulkerBoxLocked(int x, int y, int z) {
        return lockedShulkers.containsKey(positionHash(x, y, z));
    }

    public static boolean canBreakShulkerBox(int x, int y, int z) {
        ShulkerState state = lockedShulkers.get(positionHash(x, y, z));
        if (state != null) {
            return System.currentTimeMillis() - state.lockTime > 30000;
        }
        return true;
    }

    public static boolean canMoveShulkerBox(int x, int y, int z) {
        return !lockedShulkers.containsKey(positionHash(x, y, z));
    }

    private static long positionHash(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
    }

    private static class ShulkerState {
        final UUID accessorUuid;
        final long lockTime;

        ShulkerState(UUID accessorUuid, long lockTime) {
            this.accessorUuid = accessorUuid;
            this.lockTime = lockTime;
        }
    }
}
