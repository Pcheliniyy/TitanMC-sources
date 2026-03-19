package com.titanmc.server.config;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Logger;

public class TitanConfig {

    private final Properties properties = new Properties();
    private final Path configPath = Paths.get("titan.properties");
    public boolean fixTntDuplication = true;
    public boolean fixRailDuplication = true;
    public boolean fixCarpetDuplication = true;
    public boolean fixGravityBlockDuplication = true;
    public boolean fixDonkeyInventoryDupe = true;
    public boolean fixBookDuplication = true;
    public boolean fixChunkLoadDuplication = true;
    public boolean fixInventoryDesyncDupe = true;
    public boolean fixCraftingDuplication = true;
    public boolean fixDropperDuplication = true;
    public boolean fixPistonDuplication = true;
    public boolean fixEndPortalDuplication = true;
    public boolean fixShulkerBoxDuplication = true;
    public boolean fixBundleDuplication = true;
    public boolean fixVillagerTradeDupe = true;
    public boolean fixAnvilDuplication = true;
    public boolean fixBeaconDuplication = true;
    public boolean fixBrewingStandDuplication = true;
    public boolean fixFurnaceDuplication = true;
    public boolean fixCartographyDuplication = true;
    public boolean fixGrindstoneDuplication = true;
    public boolean fixLoomDuplication = true;
    public boolean fixStonecutterDuplication = true;
    public boolean fixSmithingTableDuplication = true;
    public boolean fixEntityDuplication = true;
    public boolean fixStringDuplication = true;
    public boolean fixPortalRollbackDupe = true;
    public boolean fixDeathDuplication = true;
    public boolean fixDisconnectDuplication = true;
    public boolean fixNetherPortalDupe = true;
    public boolean fixBookBan = true;
    public boolean fixSignCrash = true;
    public boolean fixChunkBan = true;
    public boolean fixNbtOverflow = true;
    public boolean fixPacketExploits = true;
    public boolean fixMovementExploits = true;
    public boolean fixCreativeExploits = true;
    public boolean fixCommandBlockExploit = true;
    public boolean fixItemNameExploit = true;
    public boolean fixMapCrash = true;
    public boolean fixEntityCrash = true;
    public boolean fixWorldBorderBypass = true;
    public boolean fixBedExploit = true;
    public boolean fixRespawnAnchorExploit = true;
    public boolean fixEndCrystalChaining = true;
    public boolean fixWitherCrash = true;
    public boolean fixBoatFly = true;
    public boolean fixElytraFly = true;
    public boolean fixPhaseExploit = true;
    public boolean fixReachExploit = true;
    public boolean fixCommandExploit = true;
    public int maxNbtSizeBytes = 2097152;
    public int maxBookPages = 100;
    public int maxBookPageLength = 32767;
    public int maxSignLineLength = 384;
    public int maxItemNameLength = 512;
    public int maxChatMessageLength = 256;
    public int maxPacketsPerSecond = 500;
    public double maxPlayerSpeed = 100.0;
    public boolean optimizeChunkLoading = true;
    public boolean asyncChunkGeneration = true;
    public int maxAsyncChunkThreads = 4;
    public int chunkLoadingCacheSize = 8192;
    public boolean aggressiveChunkUnloading = true;
    public int chunkUnloadDelay = 300;
    public boolean optimizeChunkSerialization = true;
    public boolean optimizeEntityTicking = true;
    public boolean enableEntityActivationRange = true;
    public int entityActivationRangeAnimals = 32;
    public int entityActivationRangeMonsters = 32;
    public int entityActivationRangeVillagers = 32;
    public int entityActivationRangeMisc = 16;
    public int entityActivationRangeWater = 16;
    public int entityActivationRangeFlying = 32;
    public boolean optimizeEntityCollisions = true;
    public int maxEntityCollisionsPerTick = 8;
    public boolean optimizeArmorStandTick = true;
    public boolean optimizeItemEntityMerging = true;
    public int itemMergeRadius = 4;
    public boolean skipInactiveEntityGoals = true;
    public boolean optimizeEntityPathfinding = true;
    public int maxPathfindingNodesPerTick = 500;
    public boolean disableEntityAIForFarPlayers = true;
    public int entityAIDisableRange = 64;
    public boolean optimizeMobSpawning = true;
    public int mobSpawnRange = 6;
    public int monsterSpawnLimit = 50;
    public int animalSpawnLimit = 8;
    public int waterAnimalSpawnLimit = 3;
    public int waterAmbientSpawnLimit = 8;
    public int ambientSpawnLimit = 1;
    public int spawnChunkTickRate = 3;
    public boolean perPlayerMobSpawning = true;
    public boolean countAllMobsForSpawning = false;
    public boolean optimizeRedstone = true;
    public boolean alternateRedstoneAlgorithm = true;
    public boolean optimizeRedstoneTorchChecks = true;
    public int maxRedstoneUpdatesPerTick = 10000;
    public boolean optimizeObservers = true;
    public boolean optimizeComparators = true;
    public boolean optimizeRepeaters = true;
    public boolean optimizeLighting = true;
    public boolean asyncLightingUpdates = true;
    public int maxLightUpdatesPerTick = 10000;
    public boolean optimizeSkyLightPropagation = true;
    public boolean optimizeBlockLightPropagation = true;
    public boolean disableLightForUnloadedChunks = true;
    public boolean optimizeTickLoop = true;
    public boolean skipRedundantTileEntityTicks = true;
    public boolean optimizeHoppers = true;
    public int hopperCheckInterval = 8;
    public boolean optimizeDropperAndDispenser = true;
    public boolean lazyInitTileEntities = true;
    public boolean cacheTileEntityLookups = true;
    public int tileEntityTickCacheSize = 4096;
    public boolean optimizeNetworking = true;
    public boolean compressPackets = true;
    public int packetCompressionThreshold = 256;
    public boolean batchEntityUpdates = true;
    public int entityUpdateBatchSize = 32;
    public boolean asyncPacketProcessing = true;
    public boolean optimizeChunkPackets = true;
    public boolean flushConsolidation = true;
    public int networkCompressionLevel = 6;
    public int maxPlayerTickCatchup = 200;
    public boolean optimizeMemory = true;
    public boolean useObjectPooling = true;
    public boolean optimizeNBTCaching = true;
    public int nbtCacheSize = 2048;
    public boolean optimizeBlockStateLookups = true;
    public boolean compactCollections = true;
    public boolean reduceObjectAllocation = true;
    public boolean cacheFrequentLookups = true;
    public boolean optimizeWorldGeneration = true;
    public boolean optimizeExplosions = true;
    public int maxPrimedTntPerTick = 100;
    public boolean optimizeBlockUpdates = true;
    public boolean optimizeFluidTicks = true;
    public int maxFluidTicksPerTick = 5000;
    public boolean optimizeRandomTickSpeed = true;
    public boolean optimizeLeafDecay = true;
    public boolean fastBlockStateAccess = true;
    public boolean optimizeHeightmaps = true;
    public boolean reduceGCPressure = true;
    public boolean useThreadLocalAllocators = true;
    public boolean poolChunkData = true;
    public boolean reusePacketBuffers = true;
    public boolean optimizeEntities = true;
    public boolean optimizeItemMerge = true;
    public boolean optimizeChunks = true;
    public boolean optimizeWorld = true;
    public boolean fixCrystalExploit = true;
    public boolean fixCreativeExploit = true;
    public int maxEntitiesPerChunk = 50;
    public int maxRedstonePerChunk = 256;
    public int maxTntPerChunk = 24;
    public int monsterActivationRange = 32;
    public int animalActivationRange = 32;
    public int villagerActivationRange = 32;
    public int waterActivationRange = 16;
    public int flyingActivationRange = 32;
    public int miscActivationRange = 16;

    public void load() {
        try {
            if (Files.exists(configPath)) {
                try (InputStream in = Files.newInputStream(configPath)) {
                    properties.load(in);
                }
            }
            loadValues();
            save();
        } catch (IOException e) {
            Logger.getLogger("TitanMC").warning("Failed to load titan.properties: " + e.getMessage());
            loadDefaults();
        }
    }

    private void loadValues() {
        fixTntDuplication = getBoolean("fixes.duplication.tnt", fixTntDuplication);
        fixRailDuplication = getBoolean("fixes.duplication.rail", fixRailDuplication);
        fixCarpetDuplication = getBoolean("fixes.duplication.carpet", fixCarpetDuplication);
        fixGravityBlockDuplication = getBoolean("fixes.duplication.gravity-block", fixGravityBlockDuplication);
        fixDonkeyInventoryDupe = getBoolean("fixes.duplication.donkey-inventory", fixDonkeyInventoryDupe);
        fixBookDuplication = getBoolean("fixes.duplication.book", fixBookDuplication);
        fixChunkLoadDuplication = getBoolean("fixes.duplication.chunk-load", fixChunkLoadDuplication);
        fixInventoryDesyncDupe = getBoolean("fixes.duplication.inventory-desync", fixInventoryDesyncDupe);
        fixCraftingDuplication = getBoolean("fixes.duplication.crafting", fixCraftingDuplication);
        fixDropperDuplication = getBoolean("fixes.duplication.dropper", fixDropperDuplication);
        fixPistonDuplication = getBoolean("fixes.duplication.piston", fixPistonDuplication);
        fixEndPortalDuplication = getBoolean("fixes.duplication.end-portal", fixEndPortalDuplication);
        fixShulkerBoxDuplication = getBoolean("fixes.duplication.shulker-box", fixShulkerBoxDuplication);
        fixBundleDuplication = getBoolean("fixes.duplication.bundle", fixBundleDuplication);
        fixVillagerTradeDupe = getBoolean("fixes.duplication.villager-trade", fixVillagerTradeDupe);
        fixAnvilDuplication = getBoolean("fixes.duplication.anvil", fixAnvilDuplication);
        fixBeaconDuplication = getBoolean("fixes.duplication.beacon", fixBeaconDuplication);
        fixBrewingStandDuplication = getBoolean("fixes.duplication.brewing-stand", fixBrewingStandDuplication);
        fixFurnaceDuplication = getBoolean("fixes.duplication.furnace", fixFurnaceDuplication);
        fixCartographyDuplication = getBoolean("fixes.duplication.cartography", fixCartographyDuplication);
        fixGrindstoneDuplication = getBoolean("fixes.duplication.grindstone", fixGrindstoneDuplication);
        fixLoomDuplication = getBoolean("fixes.duplication.loom", fixLoomDuplication);
        fixStonecutterDuplication = getBoolean("fixes.duplication.stonecutter", fixStonecutterDuplication);
        fixSmithingTableDuplication = getBoolean("fixes.duplication.smithing-table", fixSmithingTableDuplication);
        fixEntityDuplication = getBoolean("fixes.duplication.entity", fixEntityDuplication);
        fixStringDuplication = getBoolean("fixes.duplication.string", fixStringDuplication);
        fixPortalRollbackDupe = getBoolean("fixes.duplication.portal-rollback", fixPortalRollbackDupe);
        fixDeathDuplication = getBoolean("fixes.duplication.death", fixDeathDuplication);
        fixDisconnectDuplication = getBoolean("fixes.duplication.disconnect", fixDisconnectDuplication);
        fixNetherPortalDupe = getBoolean("fixes.duplication.nether-portal", fixNetherPortalDupe);
        fixBookBan = getBoolean("fixes.exploits.book-ban", fixBookBan);
        fixSignCrash = getBoolean("fixes.exploits.sign-crash", fixSignCrash);
        fixChunkBan = getBoolean("fixes.exploits.chunk-ban", fixChunkBan);
        fixNbtOverflow = getBoolean("fixes.exploits.nbt-overflow", fixNbtOverflow);
        fixPacketExploits = getBoolean("fixes.exploits.packet-exploits", fixPacketExploits);
        fixMovementExploits = getBoolean("fixes.exploits.movement-exploits", fixMovementExploits);
        fixCreativeExploits = getBoolean("fixes.exploits.creative-exploits", fixCreativeExploits);
        fixCommandBlockExploit = getBoolean("fixes.exploits.command-block", fixCommandBlockExploit);
        fixItemNameExploit = getBoolean("fixes.exploits.item-name", fixItemNameExploit);
        fixMapCrash = getBoolean("fixes.exploits.map-crash", fixMapCrash);
        fixEntityCrash = getBoolean("fixes.exploits.entity-crash", fixEntityCrash);
        fixWorldBorderBypass = getBoolean("fixes.exploits.world-border-bypass", fixWorldBorderBypass);
        fixBedExploit = getBoolean("fixes.exploits.bed-exploit", fixBedExploit);
        fixRespawnAnchorExploit = getBoolean("fixes.exploits.respawn-anchor", fixRespawnAnchorExploit);
        fixEndCrystalChaining = getBoolean("fixes.exploits.end-crystal-chain", fixEndCrystalChaining);
        fixWitherCrash = getBoolean("fixes.exploits.wither-crash", fixWitherCrash);
        fixBoatFly = getBoolean("fixes.exploits.boat-fly", fixBoatFly);
        fixElytraFly = getBoolean("fixes.exploits.elytra-fly", fixElytraFly);
        fixPhaseExploit = getBoolean("fixes.exploits.phase", fixPhaseExploit);
        fixReachExploit = getBoolean("fixes.exploits.reach", fixReachExploit);
        maxNbtSizeBytes = getInt("fixes.exploits.max-nbt-size-bytes", maxNbtSizeBytes);
        maxBookPages = getInt("fixes.exploits.max-book-pages", maxBookPages);
        maxBookPageLength = getInt("fixes.exploits.max-book-page-length", maxBookPageLength);
        maxSignLineLength = getInt("fixes.exploits.max-sign-line-length", maxSignLineLength);
        maxItemNameLength = getInt("fixes.exploits.max-item-name-length", maxItemNameLength);
        maxChatMessageLength = getInt("fixes.exploits.max-chat-message-length", maxChatMessageLength);
        maxPacketsPerSecond = getInt("fixes.exploits.max-packets-per-second", maxPacketsPerSecond);
        maxPlayerSpeed = getDouble("fixes.exploits.max-player-speed", maxPlayerSpeed);
        optimizeChunkLoading = getBoolean("optimization.chunk.loading", optimizeChunkLoading);
        asyncChunkGeneration = getBoolean("optimization.chunk.async-generation", asyncChunkGeneration);
        maxAsyncChunkThreads = getInt("optimization.chunk.async-threads", maxAsyncChunkThreads);
        chunkLoadingCacheSize = getInt("optimization.chunk.cache-size", chunkLoadingCacheSize);
        aggressiveChunkUnloading = getBoolean("optimization.chunk.aggressive-unloading", aggressiveChunkUnloading);
        chunkUnloadDelay = getInt("optimization.chunk.unload-delay", chunkUnloadDelay);
        optimizeChunkSerialization = getBoolean("optimization.chunk.serialize", optimizeChunkSerialization);
        optimizeEntityTicking = getBoolean("optimization.entity.ticking", optimizeEntityTicking);
        enableEntityActivationRange = getBoolean("optimization.entity.activation-range.enabled", enableEntityActivationRange);
        entityActivationRangeAnimals = getInt("optimization.entity.activation-range.animals", entityActivationRangeAnimals);
        entityActivationRangeMonsters = getInt("optimization.entity.activation-range.monsters", entityActivationRangeMonsters);
        entityActivationRangeVillagers = getInt("optimization.entity.activation-range.villagers", entityActivationRangeVillagers);
        entityActivationRangeMisc = getInt("optimization.entity.activation-range.misc", entityActivationRangeMisc);
        entityActivationRangeWater = getInt("optimization.entity.activation-range.water", entityActivationRangeWater);
        entityActivationRangeFlying = getInt("optimization.entity.activation-range.flying", entityActivationRangeFlying);
        optimizeEntityCollisions = getBoolean("optimization.entity.collisions", optimizeEntityCollisions);
        maxEntityCollisionsPerTick = getInt("optimization.entity.max-collisions-per-tick", maxEntityCollisionsPerTick);
        optimizeArmorStandTick = getBoolean("optimization.entity.armor-stand-tick", optimizeArmorStandTick);
        optimizeItemEntityMerging = getBoolean("optimization.entity.item-merging", optimizeItemEntityMerging);
        itemMergeRadius = getInt("optimization.entity.item-merge-radius", itemMergeRadius);
        skipInactiveEntityGoals = getBoolean("optimization.entity.skip-inactive-goals", skipInactiveEntityGoals);
        optimizeEntityPathfinding = getBoolean("optimization.entity.pathfinding", optimizeEntityPathfinding);
        maxPathfindingNodesPerTick = getInt("optimization.entity.max-pathfinding-nodes", maxPathfindingNodesPerTick);
        disableEntityAIForFarPlayers = getBoolean("optimization.entity.disable-ai-far-players", disableEntityAIForFarPlayers);
        entityAIDisableRange = getInt("optimization.entity.ai-disable-range", entityAIDisableRange);
        optimizeMobSpawning = getBoolean("optimization.spawning.enabled", optimizeMobSpawning);
        mobSpawnRange = getInt("optimization.spawning.range", mobSpawnRange);
        monsterSpawnLimit = getInt("optimization.spawning.limits.monster", monsterSpawnLimit);
        animalSpawnLimit = getInt("optimization.spawning.limits.animal", animalSpawnLimit);
        waterAnimalSpawnLimit = getInt("optimization.spawning.limits.water-animal", waterAnimalSpawnLimit);
        waterAmbientSpawnLimit = getInt("optimization.spawning.limits.water-ambient", waterAmbientSpawnLimit);
        ambientSpawnLimit = getInt("optimization.spawning.limits.ambient", ambientSpawnLimit);
        spawnChunkTickRate = getInt("optimization.spawning.chunk-tick-rate", spawnChunkTickRate);
        perPlayerMobSpawning = getBoolean("optimization.spawning.per-player", perPlayerMobSpawning);
        optimizeRedstone = getBoolean("optimization.redstone.enabled", optimizeRedstone);
        alternateRedstoneAlgorithm = getBoolean("optimization.redstone.alternate-algorithm", alternateRedstoneAlgorithm);
        maxRedstoneUpdatesPerTick = getInt("optimization.redstone.max-updates-per-tick", maxRedstoneUpdatesPerTick);
        optimizeLighting = getBoolean("optimization.lighting.enabled", optimizeLighting);
        asyncLightingUpdates = getBoolean("optimization.lighting.async", asyncLightingUpdates);
        maxLightUpdatesPerTick = getInt("optimization.lighting.max-updates-per-tick", maxLightUpdatesPerTick);
        optimizeTickLoop = getBoolean("optimization.tick.loop", optimizeTickLoop);
        optimizeHoppers = getBoolean("optimization.tick.hoppers", optimizeHoppers);
        hopperCheckInterval = getInt("optimization.tick.hopper-check-interval", hopperCheckInterval);
        cacheTileEntityLookups = getBoolean("optimization.tick.cache-tile-entities", cacheTileEntityLookups);
        optimizeNetworking = getBoolean("optimization.network.enabled", optimizeNetworking);
        asyncPacketProcessing = getBoolean("optimization.network.async-packets", asyncPacketProcessing);
        flushConsolidation = getBoolean("optimization.network.flush-consolidation", flushConsolidation);
        networkCompressionLevel = getInt("optimization.network.compression-level", networkCompressionLevel);
        optimizeMemory = getBoolean("optimization.memory.enabled", optimizeMemory);
        useObjectPooling = getBoolean("optimization.memory.object-pooling", useObjectPooling);
        reduceGCPressure = getBoolean("optimization.memory.reduce-gc-pressure", reduceGCPressure);
        optimizeWorldGeneration = getBoolean("optimization.world.generation", optimizeWorldGeneration);
        optimizeExplosions = getBoolean("optimization.world.explosions", optimizeExplosions);
        maxPrimedTntPerTick = getInt("optimization.world.max-primed-tnt", maxPrimedTntPerTick);
    }

    private void loadDefaults() {
    }

    public void save() {
        try {
            setBoolean("fixes.duplication.tnt", fixTntDuplication);
            setBoolean("fixes.duplication.rail", fixRailDuplication);
            setBoolean("fixes.duplication.carpet", fixCarpetDuplication);
            setBoolean("fixes.duplication.gravity-block", fixGravityBlockDuplication);
            setBoolean("fixes.duplication.donkey-inventory", fixDonkeyInventoryDupe);
            setBoolean("fixes.duplication.book", fixBookDuplication);
            setBoolean("fixes.duplication.chunk-load", fixChunkLoadDuplication);
            setBoolean("fixes.duplication.inventory-desync", fixInventoryDesyncDupe);
            setBoolean("fixes.duplication.crafting", fixCraftingDuplication);
            setBoolean("fixes.duplication.dropper", fixDropperDuplication);
            setBoolean("fixes.duplication.piston", fixPistonDuplication);
            setBoolean("fixes.duplication.end-portal", fixEndPortalDuplication);
            setBoolean("fixes.duplication.shulker-box", fixShulkerBoxDuplication);
            setBoolean("fixes.duplication.portal-rollback", fixPortalRollbackDupe);
            setBoolean("fixes.duplication.death", fixDeathDuplication);
            setBoolean("fixes.duplication.disconnect", fixDisconnectDuplication);
            setBoolean("fixes.duplication.nether-portal", fixNetherPortalDupe);

            try (OutputStream out = Files.newOutputStream(configPath)) {
                properties.store(out, "TitanMC Configuration - Optimized PaperMC 1.16.5 Fork");
            }
        } catch (IOException e) {
            Logger.getLogger("TitanMC").warning("Failed to save titan.properties: " + e.getMessage());
        }
    }
    private boolean getBoolean(String key, boolean def) {
        return Boolean.parseBoolean(properties.getProperty(key, String.valueOf(def)));
    }

    private int getInt(String key, int def) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private double getDouble(String key, double def) {
        try {
            return Double.parseDouble(properties.getProperty(key, String.valueOf(def)));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private void setBoolean(String key, boolean value) {
        properties.setProperty(key, String.valueOf(value));
    }
}
