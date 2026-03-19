package com.titanmc.server.listeners;

import com.titanmc.server.config.TitanConfig;
import com.titanmc.server.fixes.*;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.UUID;

public class PortalProtectionListener implements Listener {

    private final TitanConfig config;

    public PortalProtectionListener(TitanConfig config) {
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        if (config.fixPortalRollbackDupe) {
            int invHash = player.getInventory().getContents().hashCode();
            int itemCount = 0;
            for (var item : player.getInventory().getContents()) {
                if (item != null) itemCount += item.getAmount();
            }
            PortalRollbackDupeFix.onPortalTransitionStart(uuid, invHash, itemCount);
        }
        if (config.fixNetherPortalDupe &&
            event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            if (!NetherPortalDupeFix.startPortalProcessing(uuid)) {
                event.setCancelled(true);
                return;
            }
        }
        if (config.fixEndPortalDuplication &&
            event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            if (!EndPortalDuplicationFix.startTransition(uuid)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityPortal(EntityPortalEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) return;
        UUID uuid = entity.getUniqueId();

        if (config.fixNetherPortalDupe) {
            if (!NetherPortalDupeFix.startPortalProcessing(uuid)) {
                event.setCancelled(true);
                return;
            }
        }

        if (config.fixEndPortalDuplication) {
            if (!EndPortalDuplicationFix.startTransition(uuid)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.NETHER_PORTAL) {
            NetherPortalDupeFix.endPortalProcessing(uuid);

            if (config.fixPortalRollbackDupe) {
                int currentInvHash = event.getPlayer().getInventory().getContents().hashCode();
                PortalRollbackDupeFix.onPortalTransitionComplete(uuid, currentInvHash);
            }
        }

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.END_PORTAL) {
            EndPortalDuplicationFix.endTransition(uuid);
        }
    }
}
