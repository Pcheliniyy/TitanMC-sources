package com.titanmc.server.fixes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class PistonDuplicationFix {
    private static final ConcurrentHashMap<Long, Long> movingBlocks = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, PistonOperation> activePistons = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, Integer> chunkPistonOps = new ConcurrentHashMap<>();

    private static final int MAX_PISTON_OPS_PER_CHUNK = 64;
    private static volatile long currentTick = 0;

    private PistonDuplicationFix() {}

    public static void patch() {
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        movingBlocks.clear();
        activePistons.clear();
        chunkPistonOps.clear();
    }

    
    public static boolean canPistonOperate(int pistonX, int pistonY, int pistonZ,
                                            int chunkX, int chunkZ, boolean extending) {
        long pistonHash = positionHash(pistonX, pistonY, pistonZ);
        long chunkHash = chunkHash(chunkX, chunkZ);
        int ops = chunkPistonOps.merge(chunkHash, 1, Integer::sum);
        if (ops > MAX_PISTON_OPS_PER_CHUNK) {
            return false;
        }
        PistonOperation existing = activePistons.putIfAbsent(pistonHash,
            new PistonOperation(extending, currentTick));
        if (existing != null && existing.tick == currentTick) {
            return false;
        }

        return true;
    }

    
    public static boolean registerBlockMove(int blockX, int blockY, int blockZ,
                                             int pistonX, int pistonY, int pistonZ) {
        long blockHash = positionHash(blockX, blockY, blockZ);
        long pistonHash = positionHash(pistonX, pistonY, pistonZ);

        Long existingPiston = movingBlocks.putIfAbsent(blockHash, pistonHash);
        if (existingPiston != null && existingPiston != pistonHash) {
            return false;
        }
        return true;
    }

    
    public static boolean validatePistonHead(int pistonX, int pistonY, int pistonZ,
                                              boolean headExists, boolean isExtended) {
        if (isExtended && !headExists) {
            return false;
        }
        if (!isExtended && headExists) {
            return false;
        }
        return true;
    }

    
    public static boolean validateZeroTickOperation(int pistonX, int pistonY, int pistonZ) {
        long pistonHash = positionHash(pistonX, pistonY, pistonZ);
        PistonOperation op = activePistons.get(pistonHash);
        if (op != null && op.tick == currentTick) {
            return false;
        }
        return true;
    }

    
    public static boolean isBlockBeingMoved(int x, int y, int z) {
        return movingBlocks.containsKey(positionHash(x, y, z));
    }

    private static long positionHash(int x, int y, int z) {
        return ((long)(x & 0x3FFFFFF) << 38) | ((long)(z & 0x3FFFFFF) << 12) | (long)(y & 0xFFF);
    }

    private static long chunkHash(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    private static class PistonOperation {
        final boolean extending;
        final long tick;

        PistonOperation(boolean extending, long tick) {
            this.extending = extending;
            this.tick = tick;
        }
    }
}
