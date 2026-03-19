package com.titanmc.server.fixes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class BookDuplicationFix {
    private static final ConcurrentHashMap<Long, UUID> lecternLocks = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> signingBooks = new ConcurrentHashMap<>();

    private static volatile long currentTick = 0;

    private BookDuplicationFix() {}

    public static void patch() {
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        signingBooks.entrySet().removeIf(e -> currentTick - e.getValue() > 20);
    }

    public static boolean tryLockLectern(int x, int y, int z, UUID playerUuid) {
        long posHash = positionHash(x, y, z);
        UUID existing = lecternLocks.putIfAbsent(posHash, playerUuid);
        return existing == null || existing.equals(playerUuid);
    }

    public static void unlockLectern(int x, int y, int z, UUID playerUuid) {
        long posHash = positionHash(x, y, z);
        lecternLocks.remove(posHash, playerUuid);
    }

    public static boolean isLecternLocked(int x, int y, int z) {
        return lecternLocks.containsKey(positionHash(x, y, z));
    }

    public static boolean canTakeBookFromLectern(int x, int y, int z, UUID playerUuid) {
        long posHash = positionHash(x, y, z);
        UUID lockOwner = lecternLocks.get(posHash);
        return lockOwner == null || lockOwner.equals(playerUuid);
    }

    public static void markBookSigning(UUID playerUuid) {
        signingBooks.put(playerUuid, currentTick);
    }

    public static boolean isPlayerSigningBook(UUID playerUuid) {
        Long signTick = signingBooks.get(playerUuid);
        return signTick != null && currentTick - signTick < 10;
    }

    
    public static boolean validateBookGeneration(int generation) {
        return generation >= 0 && generation <= 2;
    }

    private static long positionHash(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
    }
}
