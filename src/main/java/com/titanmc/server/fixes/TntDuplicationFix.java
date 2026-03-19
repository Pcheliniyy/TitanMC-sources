package com.titanmc.server.fixes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class TntDuplicationFix {
    private static final ConcurrentHashMap<Long, Long> activatingTntPositions = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, Boolean> createdTntEntities = new ConcurrentHashMap<>();
    private static volatile long currentTick = 0;

    private TntDuplicationFix() {}

    public static void patch() {
    }

    
    public static void onTickStart(long tick) {
        currentTick = tick;
        activatingTntPositions.clear();
        createdTntEntities.clear();
    }

    
    public static boolean onTntActivation(int x, int y, int z) {
        long posHash = positionHash(x, y, z);
        Long existingTick = activatingTntPositions.putIfAbsent(posHash, currentTick);
        return existingTick == null || existingTick != currentTick;
    }

    
    public static boolean canPistonMoveBlock(int x, int y, int z) {
        long posHash = positionHash(x, y, z);
        Long activationTick = activatingTntPositions.get(posHash);
        return activationTick == null || activationTick != currentTick;
    }

    
    public static boolean canCreateTntEntity(int x, int y, int z) {
        long posHash = positionHash(x, y, z);
        return createdTntEntities.putIfAbsent(posHash, Boolean.TRUE) == null;
    }

    
    public static boolean validateTntEntity(int x, int y, int z,
                                             boolean isSourceBlockTnt,
                                             boolean isSourceBlockAir) {
        if (isSourceBlockTnt) {
            long posHash = positionHash(x, y, z);
            Boolean alreadyCreated = createdTntEntities.get(posHash);
            if (alreadyCreated != null) {
                return false;
            }
        }
        return true;
    }

    
    private static long positionHash(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
    }

    
    public static int getActiveTrackingCount() {
        return activatingTntPositions.size();
    }
}
