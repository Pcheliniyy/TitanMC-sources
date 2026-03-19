package com.titanmc.server;

import com.titanmc.server.config.TitanConfig;
import com.titanmc.server.fixes.DuplicationFixManager;
import com.titanmc.server.fixes.ExploitFixManager;
import com.titanmc.server.listeners.*;
import com.titanmc.server.optimization.OptimizationManager;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPluginLoader;

import java.io.File;
import java.util.logging.Logger;

public class TitanBootstrap {

    private static boolean initialized = false;

    public static void initialize() {
        if (initialized) return;
        initialized = true;

        try {
            Logger logger = Logger.getLogger("TitanMC");

            System.out.println("[TitanMC] ====================================");
            System.out.println("[TitanMC]   TitanMC 1.16.5-R0.1");
            System.out.println("[TitanMC]   Built-in Server Protection");
            System.out.println("[TitanMC] ====================================");
            TitanConfig config = new TitanConfig();
            config.load();
            System.out.println("[TitanMC] Configuration loaded.");
            DuplicationFixManager dupeMgr = new DuplicationFixManager(config, logger);
            dupeMgr.initialize();
            System.out.println("[TitanMC] Duplication fixes: " + dupeMgr.getFixCount() + " active");

            ExploitFixManager exploitMgr = new ExploitFixManager(config, logger);
            exploitMgr.initialize();
            System.out.println("[TitanMC] Exploit fixes: " + exploitMgr.getFixCount() + " active");

            OptimizationManager optMgr = new OptimizationManager(config, logger);
            optMgr.initialize();
            System.out.println("[TitanMC] Optimizations: " + optMgr.getOptimizationCount() + " active");
            Plugin ownerPlugin = getOwnerPlugin();
            Bukkit.getPluginManager().registerEvents(
                new DupePreventionListener(config, dupeMgr), ownerPlugin);
            Bukkit.getPluginManager().registerEvents(
                new InventoryProtectionListener(config), ownerPlugin);
            Bukkit.getPluginManager().registerEvents(
                new EntityProtectionListener(config), ownerPlugin);
            Bukkit.getPluginManager().registerEvents(
                new PortalProtectionListener(config), ownerPlugin);
            Bukkit.getPluginManager().registerEvents(
                new ExploitPreventionListener(config, exploitMgr), ownerPlugin);
            Bukkit.getPluginManager().registerEvents(
                new PacketLimiterListener(config, exploitMgr), ownerPlugin);
            Bukkit.getPluginManager().registerEvents(
                new OptimizationListener(config, optMgr), ownerPlugin);
            System.out.println("[TitanMC] 7 event listeners registered.");
            Bukkit.getScheduler().runTaskTimer(ownerPlugin, () -> {
                try {
                    long tick = Bukkit.getCurrentTick();
                    com.titanmc.server.fixes.TntDuplicationFix.onTickStart(tick);
                    com.titanmc.server.fixes.RailDuplicationFix.onTickStart(tick);
                    com.titanmc.server.fixes.CarpetDuplicationFix.onTickStart(tick);
                    com.titanmc.server.fixes.GravityBlockDuplicationFix.onTickStart(tick);
                    com.titanmc.server.fixes.BookDuplicationFix.onTickStart(tick);
                    com.titanmc.server.fixes.InventoryDesyncDupeFix.onTickStart(tick);
                    com.titanmc.server.fixes.PistonDuplicationFix.onTickStart(tick);
                    com.titanmc.server.fixes.EntityDuplicationFix.onTickStart(tick);
                    com.titanmc.server.fixes.DeathDuplicationFix.onTickStart(tick);
                    com.titanmc.server.fixes.PortalRollbackDupeFix.onTickStart(tick);
                    com.titanmc.server.fixes.NetherPortalDupeFix.onTickStart(tick);
                    com.titanmc.server.fixes.EndPortalDuplicationFix.onTickStart(tick);
                    com.titanmc.server.fixes.CraftingDuplicationFix.onTickStart(tick);
                    com.titanmc.server.fixes.ExploitFixManager.onTickStart(tick);
                } catch (Exception ignored) {}
            }, 1L, 1L);

            Bukkit.getScheduler().runTaskTimer(ownerPlugin, () -> {
                com.titanmc.server.fixes.EntityDuplicationFix.periodicCleanup();
            }, 20L * 60, 20L * 60);
            System.out.println("[TitanMC] Tick tasks registered.");
            connectGUI(ownerPlugin, dupeMgr, exploitMgr, optMgr);

            System.out.println("[TitanMC] Fully initialized. Server protected.");

        } catch (Exception e) {
            System.err.println("[TitanMC] ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }

    
    private static void connectGUI(Plugin ownerPlugin,
                                     DuplicationFixManager dupeMgr,
                                     ExploitFixManager exploitMgr,
                                     OptimizationManager optMgr) {
        try {
            com.titanmc.server.gui.TitanGUI gui = com.titanmc.server.gui.TitanGUI.getInstance();
            if (gui == null) return;
            gui.setStats(dupeMgr.getFixCount(), exploitMgr.getFixCount(),
                          optMgr.getOptimizationCount());
            gui.setCommandHandler(cmd -> {
                try {
                    Bukkit.getScheduler().runTask(ownerPlugin, () -> {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
                    });
                } catch (Exception e) {
                    System.err.println("[TitanMC] Command error: " + e.getMessage());
                }
            });
            Bukkit.getScheduler().runTaskTimer(ownerPlugin, () -> {
                gui.onTick();
                int entities = 0;
                int chunks = 0;
                for (org.bukkit.World world : Bukkit.getWorlds()) {
                    entities += world.getEntities().size();
                    chunks += world.getLoadedChunks().length;
                }
                gui.setPlayerCount(Bukkit.getOnlinePlayers().size());
                gui.setEntityCount(entities);
                gui.setChunkCount(chunks);
            }, 1L, 1L);

            System.out.println("[TitanMC] GUI connected to server.");
        } catch (Exception e) {
        }
    }

    
    private static Plugin getOwnerPlugin() {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        if (plugins.length > 0) {
            return plugins[0];
        }
        final JavaPluginLoader pluginLoader = new JavaPluginLoader(Bukkit.getServer());
        final PluginDescriptionFile desc = new PluginDescriptionFile(
            "TitanMC", "1.16.5-R0.1", "com.titanmc.server.TitanBootstrap");

        return (Plugin) java.lang.reflect.Proxy.newProxyInstance(
            TitanBootstrap.class.getClassLoader(),
            new Class<?>[]{ Plugin.class },
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "isEnabled": return true;
                    case "getName": return "TitanMC";
                    case "getDescription": return desc;
                    case "getPluginLoader": return pluginLoader;
                    case "getLogger": return Logger.getLogger("TitanMC");
                    case "getServer": return Bukkit.getServer();
                    case "getDataFolder":
                        File f = new File("titanmc");
                        f.mkdirs();
                        return f;
                    case "hashCode": return System.identityHashCode(proxy);
                    case "equals": return proxy == args[0];
                    case "toString": return "TitanMC[built-in]";
                    default: return null;
                }
            }
        );
    }
}
