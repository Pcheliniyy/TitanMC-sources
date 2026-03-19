package com.titanmc.server.fixes;

import com.titanmc.server.config.TitanConfig;

import java.util.*;
import java.util.logging.Logger;

public class DuplicationFixManager {

    private final TitanConfig config;
    private final Logger logger;
    private final List<DuplicationFix> fixes = new ArrayList<>();
    private int activeFixCount = 0;

    public DuplicationFixManager(TitanConfig config, Logger logger) {
        this.config = config;
        this.logger = logger;
    }

    public void initialize() {
        fixes.clear();
        registerAllFixes();
        for (DuplicationFix fix : fixes) {
            if (fix.isEnabled()) {
                fix.apply();
                activeFixCount++;
            }
        }
    }

    public void reload() {
        activeFixCount = 0;
        for (DuplicationFix fix : fixes) {
            fix.reload(config);
            if (fix.isEnabled()) {
                activeFixCount++;
            }
        }
    }

    private void registerAllFixes() {
        fixes.add(new DuplicationFix("TNT Duplication", config.fixTntDuplication,
            "Prevents TNT entities from being duplicated when pushed by pistons during the " +
            "same tick as their explosion. Patches EntityTNTPrimed to validate entity state " +
            "before piston movement can create copies.") {
            @Override
            public void apply() {
                TntDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Rail Duplication", config.fixRailDuplication,
            "Fixes rail items being duplicated when pushed by slime/honey blocks while being " +
            "broken simultaneously. Adds state validation in BlockMinecartTrack to prevent " +
            "the block from dropping items if it was already moved by a piston.") {
            @Override
            public void apply() {
                RailDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Carpet Duplication", config.fixCarpetDuplication,
            "Prevents carpet blocks from being duplicated through piston push mechanics. " +
            "Validates block state transitions in BlockCarpet to ensure items are not " +
            "dropped when the block is moved rather than broken.") {
            @Override
            public void apply() {
                CarpetDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Gravity Block Duplication", config.fixGravityBlockDuplication,
            "Fixes falling block entities (sand, gravel, concrete powder, anvils) being " +
            "duplicated when they fall across chunk boundaries during chunk load/unload. " +
            "Tracks falling block source positions and validates against double-placement.") {
            @Override
            public void apply() {
                GravityBlockDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Donkey Inventory Dupe", config.fixDonkeyInventoryDupe,
            "Prevents item duplication through donkey/llama/mule chest inventories by " +
            "validating inventory state during mount/dismount and inventory open/close " +
            "events. Adds mutex locks on container access.") {
            @Override
            public void apply() {
                DonkeyInventoryDupeFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Book Duplication", config.fixBookDuplication,
            "Fixes written books being duplicated through lectern placement and retrieval " +
            "race conditions. Validates book NBT integrity and prevents simultaneous " +
            "access to lectern contents.") {
            @Override
            public void apply() {
                BookDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Chunk Load Duplication", config.fixChunkLoadDuplication,
            "Prevents entities and items from being duplicated when crossing chunk " +
            "boundaries during chunk load/unload cycles. Implements entity tracking " +
            "across chunk transitions with UUID-based deduplication.") {
            @Override
            public void apply() {
                ChunkLoadDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Inventory Desync Dupe", config.fixInventoryDesyncDupe,
            "Fixes item duplication caused by inventory desynchronization between client " +
            "and server. Implements server-authoritative inventory management with " +
            "transaction validation and rollback on mismatch.") {
            @Override
            public void apply() {
                InventoryDesyncDupeFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Crafting Duplication", config.fixCraftingDuplication,
            "Prevents item duplication through rapid crafting table interactions, " +
            "shift-click crafting race conditions, and crafting result slot manipulation. " +
            "Adds atomic crafting operations with server-side validation.") {
            @Override
            public void apply() {
                CraftingDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Dropper Duplication", config.fixDropperDuplication,
            "Fixes items being duplicated when dropper/dispenser fires during chunk " +
            "save/load. Validates item transfer atomicity in TileEntityDropper.") {
            @Override
            public void apply() {
                DropperDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Piston Duplication", config.fixPistonDuplication,
            "Comprehensive fix for all piston-based duplication methods including " +
            "headless pistons, piston retraction duplication, and quasi-connectivity " +
            "exploits. Validates block state integrity during piston operations.") {
            @Override
            public void apply() {
                PistonDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("End Portal Duplication", config.fixEndPortalDuplication,
            "Prevents item and entity duplication through end portal mechanics, " +
            "including the end portal frame item dupe and entity teleportation " +
            "duplication during dimension transitions.") {
            @Override
            public void apply() {
                EndPortalDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Shulker Box Duplication", config.fixShulkerBoxDuplication,
            "Fixes shulker box content duplication through various methods including " +
            "breaking while open, piston manipulation, and explosive destruction. " +
            "Implements container locking during state transitions.") {
            @Override
            public void apply() {
                ShulkerBoxDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Villager Trade Dupe", config.fixVillagerTradeDupe,
            "Prevents item duplication through villager trade mechanics, including " +
            "rapid trade clicking, trade window desync, and merchant inventory " +
            "manipulation. Adds server-side trade validation.") {
            @Override
            public void apply() {
                VillagerTradeDupeFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Anvil Duplication", config.fixAnvilDuplication,
            "Fixes item duplication through anvil renaming and combining operations. " +
            "Validates input/output slot consistency and prevents race conditions " +
            "during anvil operations.") {
            @Override
            public void apply() {
                AnvilDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Beacon Duplication", config.fixBeaconDuplication,
            "Prevents payment item duplication in beacon activation by validating " +
            "item consumption atomicity and preventing simultaneous beacon access.") {
            @Override
            public void apply() {
                BeaconDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Brewing Stand Duplication", config.fixBrewingStandDuplication,
            "Fixes potion and ingredient duplication through brewing stand manipulation. " +
            "Validates slot contents during brewing tick and prevents extraction during " +
            "active brewing cycles.") {
            @Override
            public void apply() {
                BrewingStandDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Furnace Duplication", config.fixFurnaceDuplication,
            "Prevents item duplication through furnace input/output slot manipulation " +
            "and smelting result extraction race conditions.") {
            @Override
            public void apply() {
                FurnaceDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Portal Rollback Dupe", config.fixPortalRollbackDupe,
            "Fixes item duplication caused by portal transition rollbacks during " +
            "server lag or chunk loading delays. Implements player inventory snapshots " +
            "during portal transitions with rollback prevention.") {
            @Override
            public void apply() {
                PortalRollbackDupeFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Death Duplication", config.fixDeathDuplication,
            "Prevents item duplication through death mechanics including totem " +
            "activation timing, death during dimension change, and inventory " +
            "preservation race conditions on respawn.") {
            @Override
            public void apply() {
                DeathDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Disconnect Duplication", config.fixDisconnectDuplication,
            "Fixes item duplication caused by disconnecting during item transfers, " +
            "crafting, or container interactions. Implements proper save-on-disconnect " +
            "with transaction rollback.") {
            @Override
            public void apply() {
                DisconnectDuplicationFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Nether Portal Dupe", config.fixNetherPortalDupe,
            "Prevents entity and item duplication through nether portal entry timing " +
            "exploits. Validates entity state before and after portal transitions.") {
            @Override
            public void apply() {
                NetherPortalDupeFix.patch();
            }
        });
        fixes.add(new DuplicationFix("Entity Duplication", config.fixEntityDuplication,
            "Comprehensive entity duplication prevention covering all entity types. " +
            "Implements UUID-based entity tracking with automatic cleanup of " +
            "duplicate entity instances across all loaded chunks.") {
            @Override
            public void apply() {
                EntityDuplicationFix.patch();
            }
        });
    }

    public int getFixCount() {
        return activeFixCount;
    }

    public List<DuplicationFix> getFixes() {
        return Collections.unmodifiableList(fixes);
    }

    
    public static abstract class DuplicationFix {
        private final String name;
        private boolean enabled;
        private final String description;

        public DuplicationFix(String name, boolean enabled, String description) {
            this.name = name;
            this.enabled = enabled;
            this.description = description;
        }

        public abstract void apply();

        public void reload(TitanConfig config) {
        }

        public String getName() { return name; }
        public boolean isEnabled() { return enabled; }
        public void setEnabled(boolean enabled) { this.enabled = enabled; }
        public String getDescription() { return description; }
    }
}
