package com.titanmc.server.listeners;

import com.titanmc.server.config.TitanConfig;
import com.titanmc.server.fixes.*;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.BeaconInventory;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.MerchantInventory;

import java.util.UUID;

public class InventoryProtectionListener implements Listener {

    private final TitanConfig config;

    public InventoryProtectionListener(TitanConfig config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        UUID uuid = player.getUniqueId();
        if (config.fixInventoryDesyncDupe) {
            if (!InventoryDesyncDupeFix.validateInventoryClick(
                    uuid,
                    event.getView().hashCode(),
                    event.getRawSlot(),
                    event.getHotbarButton(),
                    event.getClick().ordinal())) {
                event.setCancelled(true);
                player.updateInventory();
                return;
            }
        }
        if (config.fixCraftingDuplication && event.getInventory().getType() == InventoryType.WORKBENCH) {
            if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                if (!CraftingDuplicationFix.validateCraftOperation(uuid, 0, new int[0])) {
                    event.setCancelled(true);
                    player.updateInventory();
                    return;
                }
            }
        }
        if (config.fixVillagerTradeDupe && event.getInventory() instanceof MerchantInventory) {
            if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                if (!VillagerTradeDupeFix.canTrade(uuid)) {
                    event.setCancelled(true);
                    player.updateInventory();
                    return;
                }
            }
        }
        if (config.fixAnvilDuplication && event.getInventory() instanceof AnvilInventory) {
            if (event.getSlotType() == InventoryType.SlotType.RESULT) {
                if (!AnvilDuplicationFix.lockAnvil(uuid)) {
                    event.setCancelled(true);
                    player.updateInventory();
                    return;
                }
            }
        }
        if (config.fixBeaconDuplication && event.getInventory() instanceof BeaconInventory) {
            int x = event.getInventory().getLocation() != null ? event.getInventory().getLocation().getBlockX() : 0;
            int y = event.getInventory().getLocation() != null ? event.getInventory().getLocation().getBlockY() : 0;
            int z = event.getInventory().getLocation() != null ? event.getInventory().getLocation().getBlockZ() : 0;
            if (!BeaconDuplicationFix.lockBeacon(x, y, z)) {
                event.setCancelled(true);
                player.updateInventory();
                return;
            }
        }
        if (config.fixBrewingStandDuplication && event.getInventory() instanceof BrewerInventory) {
            if (event.getInventory().getLocation() != null) {
                int x = event.getInventory().getLocation().getBlockX();
                int y = event.getInventory().getLocation().getBlockY();
                int z = event.getInventory().getLocation().getBlockZ();
                if (BrewingStandDuplicationFix.isBrewing(x, y, z)) {
                    if (event.getSlotType() == InventoryType.SlotType.RESULT ||
                        event.getSlotType() == InventoryType.SlotType.CRAFTING) {
                        event.setCancelled(true);
                        player.updateInventory();
                        return;
                    }
                }
            }
        }
        if (config.fixPortalRollbackDupe && PortalRollbackDupeFix.isInPortalTransition(uuid)) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        UUID uuid = event.getPlayer().getUniqueId();

        InventoryDesyncDupeFix.onWindowClose(uuid);
        CraftingDuplicationFix.onCraftingClose(uuid);
        AnvilDuplicationFix.unlockAnvil(uuid);
        if (InventoryDesyncDupeFix.needsResync(uuid)) {
            ((Player) event.getPlayer()).updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        UUID uuid = event.getPlayer().getUniqueId();

        InventoryDesyncDupeFix.onWindowOpen(uuid, event.getView().hashCode());
        if (config.fixShulkerBoxDuplication &&
            event.getInventory().getType() == InventoryType.SHULKER_BOX &&
            event.getInventory().getLocation() != null) {
            int x = event.getInventory().getLocation().getBlockX();
            int y = event.getInventory().getLocation().getBlockY();
            int z = event.getInventory().getLocation().getBlockZ();
            if (!ShulkerBoxDuplicationFix.lockShulkerBox(x, y, z, uuid)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();

        if (config.fixPortalRollbackDupe && !PortalRollbackDupeFix.canDropItems(uuid)) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        InventoryDesyncDupeFix.onPlayerDisconnect(uuid);
        CraftingDuplicationFix.onPlayerDisconnect(uuid);
        PortalRollbackDupeFix.onPlayerDisconnect(uuid);
        DonkeyInventoryDupeFix.onPlayerDisconnect(uuid);
        VillagerTradeDupeFix.onPlayerDisconnect(uuid);
        DeathDuplicationFix.onPlayerDisconnect(uuid);
        AnvilDuplicationFix.unlockAnvil(uuid);
        DisconnectDuplicationFix.TransactionState pending = DisconnectDuplicationFix.onDisconnect(uuid);
        if (pending != null) {
            event.getPlayer().updateInventory();
        }
    }
}
