package com.titanmc.server;

import com.titanmc.server.config.TitanConfig;
import com.titanmc.server.fixes.DuplicationFixManager;
import com.titanmc.server.fixes.ExploitFixManager;
import com.titanmc.server.optimization.OptimizationManager;

import java.util.logging.Logger;

public final class TitanMC {

    private static TitanMC instance;
    private static final String VERSION = "1.16.5-R0.1";
    private static final String NAME = "TitanMC";

    private final Logger logger;
    private final TitanConfig config;
    private final DuplicationFixManager duplicationFixManager;
    private final ExploitFixManager exploitFixManager;
    private final OptimizationManager optimizationManager;
    private boolean initialized = false;

    private TitanMC() {
        this.logger = Logger.getLogger(NAME);
        this.config = new TitanConfig();
        this.duplicationFixManager = new DuplicationFixManager(config, logger);
        this.exploitFixManager = new ExploitFixManager(config, logger);
        this.optimizationManager = new OptimizationManager(config, logger);
    }

    public static TitanMC getInstance() {
        if (instance == null) {
            synchronized (TitanMC.class) {
                if (instance == null) {
                    instance = new TitanMC();
                }
            }
        }
        return instance;
    }

    public void initialize() {
        if (initialized) return;

        logger.info("====================================");
        logger.info("  TitanMC " + VERSION);
        logger.info("  Optimized PaperMC 1.16.5 Fork");
        logger.info("====================================");
        config.load();
        logger.info("[TitanMC] Configuration loaded.");
        duplicationFixManager.initialize();
        logger.info("[TitanMC] Duplication fixes applied: " + duplicationFixManager.getFixCount() + " patches active.");

        exploitFixManager.initialize();
        logger.info("[TitanMC] Exploit fixes applied: " + exploitFixManager.getFixCount() + " patches active.");

        optimizationManager.initialize();
        logger.info("[TitanMC] Optimizations applied: " + optimizationManager.getOptimizationCount() + " optimizations active.");

        initialized = true;
        logger.info("[TitanMC] Server core fully initialized.");
    }

    public void shutdown() {
        logger.info("[TitanMC] Shutting down...");
        optimizationManager.shutdown();
        initialized = false;
    }

    public void reload() {
        logger.info("[TitanMC] Reloading configuration...");
        config.load();
        duplicationFixManager.reload();
        exploitFixManager.reload();
        optimizationManager.reload();
        logger.info("[TitanMC] Reload complete.");
    }
    public static String getVersion() { return VERSION; }
    public static String getServerName() { return NAME; }
    public TitanConfig getConfig() { return config; }
    public DuplicationFixManager getDuplicationFixManager() { return duplicationFixManager; }
    public ExploitFixManager getExploitFixManager() { return exploitFixManager; }
    public OptimizationManager getOptimizationManager() { return optimizationManager; }
    public Logger getLogger() { return logger; }
}
