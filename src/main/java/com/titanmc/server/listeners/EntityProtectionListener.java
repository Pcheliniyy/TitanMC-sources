package com.titanmc.server.listeners;

import com.titanmc.server.config.TitanConfig;
import com.titanmc.server.fixes.*;

import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.util.*;

public class EntityProtectionListener implements Listener {

    private final TitanConfig config;

    public EntityProtectionListener(TitanConfig config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (!config.fixEntityDuplication) return;

        Entity entity = event.getEntity();
        int worldId = entity.getWorld().getUID().hashCode();
        int chunkX = entity.getLocation().getBlockX() >> 4;
        int chunkZ = entity.getLocation().getBlockZ() >> 4;

        if (!EntityDuplicationFix.registerEntity(entity.getUniqueId(), worldId, chunkX, chunkZ)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        EntityDuplicationFix.unregisterEntity(entity.getUniqueId());
        GravityBlockDuplicationFix.onFallingBlockRemoved(entity.getUniqueId());
        NetherPortalDupeFix.onEntityRemoved(entity.getUniqueId());
        EndPortalDuplicationFix.onEntityRemoved(entity.getUniqueId());
        if (config.fixDonkeyInventoryDupe) {
            DonkeyInventoryDupeFix.onMountDeath(entity.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!config.fixDeathDuplication) return;

        Player player = event.getEntity();
        UUID uuid = player.getUniqueId();
        if (!DeathDuplicationFix.startDeathProcessing(uuid)) {
            event.getDrops().clear();
            event.setDroppedExp(0);
            return;
        }
        if (config.fixPortalRollbackDupe) {
            PortalRollbackDupeFix.onDeathDuringTransition(uuid);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!config.fixDeathDuplication) return;
        DeathDuplicationFix.endDeathProcessing(event.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!config.fixChunkLoadDuplication) return;

        Entity[] entities = event.getChunk().getEntities();
        int chunkX = event.getChunk().getX();
        int chunkZ = event.getChunk().getZ();
        int worldId = event.getWorld().getUID().hashCode();

        List<UUID> entityUuids = new ArrayList<>();
        for (Entity entity : entities) {
            entityUuids.add(entity.getUniqueId());
        }
        Set<UUID> duplicates = EntityDuplicationFix.validateLoadedEntities(
            entityUuids, worldId, chunkX, chunkZ);

        if (!duplicates.isEmpty()) {
            for (Entity entity : entities) {
                if (duplicates.contains(entity.getUniqueId())) {
                    entity.remove();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onChunkUnload(ChunkUnloadEvent event) {
        if (!config.fixChunkLoadDuplication) return;
        ChunkLoadDuplicationFix.onChunkUnload(event.getChunk().getX(), event.getChunk().getZ());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEntityChangeBlock(EntityChangeBlockEvent event) {
        if (!config.fixGravityBlockDuplication) return;

        if (event.getEntity() instanceof FallingBlock) {
            GravityBlockDuplicationFix.onFallingBlockLanded(event.getEntity().getUniqueId());
        }
    }
}
