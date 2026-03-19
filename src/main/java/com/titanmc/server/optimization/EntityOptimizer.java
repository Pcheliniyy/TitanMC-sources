package com.titanmc.server.optimization;

import com.titanmc.server.config.TitanConfig;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class EntityOptimizer {

    private final TitanConfig config;
    private final Logger logger;
    private int optimizationCount = 0;
    private static final ConcurrentHashMap<UUID, EntityActivationState> activationStates = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Integer> collisionCounts = new ConcurrentHashMap<>();
    private static volatile int pathfindingNodesUsed = 0;

    private static volatile long currentTick = 0;

    public EntityOptimizer(TitanConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void initialize() {
        optimizationCount = 0;

        if (config.enableEntityActivationRange) optimizationCount++;
        if (config.optimizeEntityCollisions) optimizationCount++;
        if (config.optimizeArmorStandTick) optimizationCount++;
        if (config.optimizeItemEntityMerging) optimizationCount++;
        if (config.skipInactiveEntityGoals) optimizationCount++;
        if (config.optimizeEntityPathfinding) optimizationCount++;
        if (config.disableEntityAIForFarPlayers) optimizationCount++;
        optimizationCount++;
    }

    
    public static void onTickStart(long tick) {
        currentTick = tick;
        collisionCounts.clear();
        pathfindingNodesUsed = 0;
    }

    
    public boolean shouldEntityTick(UUID entityUuid, EntityType entityType,
                                     double distanceToNearestPlayerSq) {
        if (!config.enableEntityActivationRange) return true;

        int activationRange = getActivationRange(entityType);
        double activationRangeSq = (double) activationRange * activationRange;

        boolean active = distanceToNearestPlayerSq <= activationRangeSq;

        EntityActivationState state = activationStates.computeIfAbsent(entityUuid,
            k -> new EntityActivationState());
        state.active = active;
        state.lastCheckTick = currentTick;
        if (!active && currentTick - state.lastActiveTick > 20) {
            state.lastActiveTick = currentTick;
            return true;
        }

        if (active) {
            state.lastActiveTick = currentTick;
        }

        return active;
    }

    private int getActivationRange(EntityType type) {
        switch (type) {
            case ANIMAL: return config.entityActivationRangeAnimals;
            case MONSTER: return config.entityActivationRangeMonsters;
            case VILLAGER: return config.entityActivationRangeVillagers;
            case WATER: return config.entityActivationRangeWater;
            case FLYING: return config.entityActivationRangeFlying;
            case MISC:
            default: return config.entityActivationRangeMisc;
        }
    }

    
    public boolean canProcessCollision(UUID entityUuid) {
        if (!config.optimizeEntityCollisions) return true;

        int count = collisionCounts.merge(entityUuid, 1, Integer::sum);
        return count <= config.maxEntityCollisionsPerTick;
    }

    
    public ArmorStandTickLevel getArmorStandTickLevel(UUID entityUuid, boolean hasPassenger,
                                                       boolean hasVelocity, boolean nearPlayer) {
        if (!config.optimizeArmorStandTick) return ArmorStandTickLevel.FULL;

        if (hasPassenger || hasVelocity) return ArmorStandTickLevel.FULL;
        if (nearPlayer) return ArmorStandTickLevel.MINIMAL;
        return ArmorStandTickLevel.NONE;
    }

    
    public int getItemMergeRadius() {
        return config.optimizeItemEntityMerging ? config.itemMergeRadius : 0;
    }

    
    public boolean canItemsMerge(int item1Hash, int item1Count, int item1MaxStack,
                                  int item2Hash, int item2Count) {
        if (!config.optimizeItemEntityMerging) return false;
        if (item1Hash != item2Hash) return false;
        return item1Count + item2Count <= item1MaxStack;
    }

    
    public boolean requestPathfindingBudget(int nodesNeeded) {
        if (!config.optimizeEntityPathfinding) return true;

        synchronized (EntityOptimizer.class) {
            if (pathfindingNodesUsed + nodesNeeded > config.maxPathfindingNodesPerTick) {
                return false;
            }
            pathfindingNodesUsed += nodesNeeded;
            return true;
        }
    }

    
    public static void reportPathfindingNodes(int nodesUsed) {
        pathfindingNodesUsed += nodesUsed;
    }

    
    public boolean shouldDisableAI(double distanceToNearestPlayerSq) {
        if (!config.disableEntityAIForFarPlayers) return false;
        double disableRangeSq = (double) config.entityAIDisableRange * config.entityAIDisableRange;
        return distanceToNearestPlayerSq > disableRangeSq;
    }

    
    public boolean shouldSkipGoals(UUID entityUuid) {
        if (!config.skipInactiveEntityGoals) return false;

        EntityActivationState state = activationStates.get(entityUuid);
        if (state == null || state.active) return false;
        return currentTick % 4 != 0;
    }

    public void shutdown() {
        activationStates.clear();
        collisionCounts.clear();
    }

    public int getOptimizationCount() {
        return optimizationCount;
    }

    public void onEntityRemoved(UUID entityUuid) {
        activationStates.remove(entityUuid);
    }

    public enum EntityType {
        ANIMAL, MONSTER, VILLAGER, MISC, WATER, FLYING
    }

    public enum ArmorStandTickLevel {
        FULL,
        MINIMAL,
        NONE
    }

    private static class EntityActivationState {
        boolean active = true;
        long lastCheckTick = 0;
        long lastActiveTick = 0;
    }
}
