package com.titanmc.server.listeners;

import com.titanmc.server.config.TitanConfig;
import com.titanmc.server.fixes.*;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;

import java.util.List;

public class DupePreventionListener implements Listener {

    private final TitanConfig config;
    private final DuplicationFixManager fixManager;

    public DupePreventionListener(TitanConfig config, DuplicationFixManager fixManager) {
        this.config = config;
        this.fixManager = fixManager;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        if (!config.fixPistonDuplication) return;

        Block piston = event.getBlock();
        int px = piston.getX(), py = piston.getY(), pz = piston.getZ();
        int chunkX = px >> 4, chunkZ = pz >> 4;
        if (!PistonDuplicationFix.canPistonOperate(px, py, pz, chunkX, chunkZ, true)) {
            event.setCancelled(true);
            return;
        }
        if (!PistonDuplicationFix.validatePistonHead(px, py, pz, false, false)) {
            event.setCancelled(true);
            return;
        }
        List<Block> blocks = event.getBlocks();
        for (Block block : blocks) {
            int bx = block.getX(), by = block.getY(), bz = block.getZ();
            if (!PistonDuplicationFix.registerBlockMove(bx, by, bz, px, py, pz)) {
                event.setCancelled(true);
                return;
            }
            if (config.fixTntDuplication && block.getType() == Material.TNT) {
                if (!TntDuplicationFix.canPistonMoveBlock(bx, by, bz)) {
                    event.setCancelled(true);
                    return;
                }
            }
            if (config.fixRailDuplication && isRail(block.getType())) {
                RailDuplicationFix.markRailMoving(bx, by, bz);
            }
            if (config.fixCarpetDuplication && isCarpet(block.getType())) {
                CarpetDuplicationFix.markCarpetMoving(bx, by, bz);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        if (!config.fixPistonDuplication) return;

        Block piston = event.getBlock();
        int px = piston.getX(), py = piston.getY(), pz = piston.getZ();
        int chunkX = px >> 4, chunkZ = pz >> 4;

        if (!PistonDuplicationFix.canPistonOperate(px, py, pz, chunkX, chunkZ, false)) {
            event.setCancelled(true);
            return;
        }

        for (Block block : event.getBlocks()) {
            int bx = block.getX(), by = block.getY(), bz = block.getZ();

            if (!PistonDuplicationFix.registerBlockMove(bx, by, bz, px, py, pz)) {
                event.setCancelled(true);
                return;
            }

            if (config.fixRailDuplication && isRail(block.getType())) {
                RailDuplicationFix.markRailMoving(bx, by, bz);
            }
            if (config.fixCarpetDuplication && isCarpet(block.getType())) {
                CarpetDuplicationFix.markCarpetMoving(bx, by, bz);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onTNTSpawn(EntitySpawnEvent event) {
        if (!config.fixTntDuplication) return;
        if (event.getEntityType() != EntityType.PRIMED_TNT) return;

        Entity entity = event.getEntity();
        int x = entity.getLocation().getBlockX();
        int y = entity.getLocation().getBlockY();
        int z = entity.getLocation().getBlockZ();
        if (!TntDuplicationFix.canCreateTntEntity(x, y, z)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFallingBlockSpawn(EntitySpawnEvent event) {
        if (!config.fixGravityBlockDuplication) return;
        if (event.getEntityType() != EntityType.FALLING_BLOCK) return;

        Entity entity = event.getEntity();
        int x = entity.getLocation().getBlockX();
        int y = entity.getLocation().getBlockY();
        int z = entity.getLocation().getBlockZ();
        if (!GravityBlockDuplicationFix.canCreateFallingBlock(x, y, z)) {
            event.setCancelled(true);
            return;
        }

        GravityBlockDuplicationFix.onFallingBlockCreated(entity.getUniqueId(), x, y, z);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onItemSpawn(ItemSpawnEvent event) {
        Material itemType = event.getEntity().getItemStack().getType();
        int x = event.getLocation().getBlockX();
        int y = event.getLocation().getBlockY();
        int z = event.getLocation().getBlockZ();
        if (config.fixRailDuplication && isRail(itemType)) {
            if (!RailDuplicationFix.shouldDropRailItem(x, y, z)) {
                event.setCancelled(true);
                return;
            }
        }
        if (config.fixCarpetDuplication && isCarpet(itemType)) {
            if (!CarpetDuplicationFix.shouldCarpetDrop(x, y, z)) {
                event.setCancelled(true);
            }
        }
    }

    private boolean isRail(Material mat) {
        return mat == Material.RAIL || mat == Material.POWERED_RAIL
            || mat == Material.DETECTOR_RAIL || mat == Material.ACTIVATOR_RAIL;
    }

    private boolean isCarpet(Material mat) {
        return mat.name().endsWith("_CARPET");
    }
}
