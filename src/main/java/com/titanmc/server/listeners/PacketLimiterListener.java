package com.titanmc.server.listeners;

import com.titanmc.server.config.TitanConfig;
import com.titanmc.server.fixes.ExploitFixManager;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PacketLimiterListener implements Listener {

    private final TitanConfig config;
    private final ExploitFixManager exploitFixManager;
    private final Map<UUID, Long> joinTimes = new ConcurrentHashMap<>();

    public PacketLimiterListener(TitanConfig config, ExploitFixManager exploitFixManager) {
        this.config = config;
        this.exploitFixManager = exploitFixManager;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        joinTimes.put(event.getPlayer().getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        joinTimes.remove(event.getPlayer().getUniqueId());
    }
}
