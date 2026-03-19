package com.titanmc.api;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TitanAPI {

    
    @NotNull
    static TitanAPI getInstance() {
        return TitanAPIHolder.INSTANCE;
    }

    
    @NotNull String getVersion();

    
    double getCurrentTPS();

    
    int getActiveDuplicationFixCount();

    
    int getActiveExploitFixCount();

    
    int getActiveOptimizationCount();

    
    @Nullable Boolean isFixEnabled(@NotNull String fixName);

    
    int getTrackedEntityCount();

    
    double getChunkCacheHitRate();

    
    int getRedstoneUpdatesThisTick();

    
    int getLightUpdatesThisTick();

    
    void reloadConfig();

    class TitanAPIHolder {
        static TitanAPI INSTANCE;

        public static void setInstance(TitanAPI instance) {
            INSTANCE = instance;
        }
    }
}
