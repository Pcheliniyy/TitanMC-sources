package com.titanmc.server.fixes;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class CraftingDuplicationFix {

    private static final ConcurrentHashMap<UUID, CraftingState> playerCraftingState = new ConcurrentHashMap<>();
    private static final int MAX_CRAFTS_PER_TICK = 8;
    private static volatile long currentTick = 0;

    private CraftingDuplicationFix() {}

    public static void patch() {
    }

    public static void onTickStart(long tick) {
        currentTick = tick;
        for (CraftingState state : playerCraftingState.values()) {
            state.craftsThisTick = 0;
        }
    }

    
    public static boolean validateCraftOperation(UUID playerUuid, int recipeHash,
                                                   int[] ingredientHashes) {
        CraftingState state = playerCraftingState.computeIfAbsent(playerUuid,
            k -> new CraftingState());
        state.craftsThisTick++;
        if (state.craftsThisTick > MAX_CRAFTS_PER_TICK) {
            return false;
        }
        if (state.lastCraftTick == currentTick && state.lastRecipeHash == recipeHash) {
            state.sameCraftCount++;
            if (state.sameCraftCount > 1) {
                return false;
            }
        } else {
            state.sameCraftCount = 1;
        }

        state.lastCraftTick = currentTick;
        state.lastRecipeHash = recipeHash;
        return true;
    }

    
    public static void onCraftComplete(UUID playerUuid) {
        CraftingState state = playerCraftingState.get(playerUuid);
        if (state != null) {
            state.totalCrafts++;
        }
    }

    
    public static void onCraftingClose(UUID playerUuid) {
        CraftingState state = playerCraftingState.get(playerUuid);
        if (state != null) {
            state.sameCraftCount = 0;
        }
    }

    public static void onPlayerDisconnect(UUID playerUuid) {
        playerCraftingState.remove(playerUuid);
    }

    private static class CraftingState {
        int craftsThisTick = 0;
        long lastCraftTick = 0;
        int lastRecipeHash = 0;
        int sameCraftCount = 0;
        long totalCrafts = 0;
    }
}
