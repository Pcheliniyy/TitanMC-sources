package com.titanmc.server.command;

import com.titanmc.server.TitanMC;
import com.titanmc.server.optimization.TickOptimizer;

public class TitanCommand {

    
    public static boolean execute(Object sender, String[] args) {
        if (args.length == 0) {
            sendInfo(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload":
                return handleReload(sender);
            case "status":
                return handleStatus(sender);
            case "tps":
                return handleTPS(sender);
            case "fixes":
                return handleFixes(sender);
            case "debug":
                return handleDebug(sender);
            default:
                sendMessage(sender, "&cUnknown subcommand. Use /titan for help.");
                return true;
        }
    }

    private static void sendInfo(Object sender) {
        TitanMC titan = TitanMC.getInstance();
        sendMessage(sender, "&6&l========================================");
        sendMessage(sender, "&e&l  TitanMC &7v" + TitanMC.getVersion());
        sendMessage(sender, "&7  Optimized PaperMC 1.16.5 Fork");
        sendMessage(sender, "&6&l========================================");
        sendMessage(sender, "&a  Duplication fixes: &f" + titan.getDuplicationFixManager().getFixCount());
        sendMessage(sender, "&a  Exploit fixes: &f" + titan.getExploitFixManager().getFixCount());
        sendMessage(sender, "&a  Optimizations: &f" + titan.getOptimizationManager().getOptimizationCount());
        sendMessage(sender, "&7  TPS: &f" + String.format("%.1f", TickOptimizer.getCurrentTPS()));
        sendMessage(sender, "&6&l========================================");
        sendMessage(sender, "&7  /titan reload &8- &fReload config");
        sendMessage(sender, "&7  /titan status &8- &fShow status");
        sendMessage(sender, "&7  /titan tps &8- &fDetailed TPS info");
        sendMessage(sender, "&7  /titan fixes &8- &fList active fixes");
        sendMessage(sender, "&7  /titan debug &8- &fDebug info");
    }

    private static boolean handleReload(Object sender) {
        TitanMC.getInstance().reload();
        sendMessage(sender, "&aTitanMC configuration reloaded successfully.");
        return true;
    }

    private static boolean handleStatus(Object sender) {
        TitanMC titan = TitanMC.getInstance();
        sendMessage(sender, "&6&lTitanMC Status");
        sendMessage(sender, "&e  Protection Systems:");
        sendMessage(sender, "&a    Duplication fixes: &f" + titan.getDuplicationFixManager().getFixCount() + " active");
        sendMessage(sender, "&a    Exploit fixes: &f" + titan.getExploitFixManager().getFixCount() + " active");
        sendMessage(sender, "&e  Performance Systems:");
        sendMessage(sender, "&a    Optimizations: &f" + titan.getOptimizationManager().getOptimizationCount() + " active");
        sendMessage(sender, "&a    Current TPS: &f" + String.format("%.2f", TickOptimizer.getCurrentTPS()));
        return true;
    }

    private static boolean handleTPS(Object sender) {
        double tps = TickOptimizer.getCurrentTPS();
        String color = tps >= 19.5 ? "&a" : tps >= 17.0 ? "&e" : tps >= 14.0 ? "&6" : "&c";
        sendMessage(sender, "&6&lTitanMC TPS Monitor");
        sendMessage(sender, color + "  Current TPS: " + String.format("%.2f", tps));
        sendMessage(sender, "&7  Target: 20.00 TPS");
        if (tps < 19.5) {
            sendMessage(sender, "&7  Status: &eLagging - adaptive throttling active");
        } else {
            sendMessage(sender, "&7  Status: &aHealthy");
        }
        return true;
    }

    private static boolean handleFixes(Object sender) {
        TitanMC titan = TitanMC.getInstance();
        sendMessage(sender, "&6&lActive Duplication Fixes:");
        for (var fix : titan.getDuplicationFixManager().getFixes()) {
            String status = fix.isEnabled() ? "&a[ON]" : "&c[OFF]";
            sendMessage(sender, "  " + status + " &f" + fix.getName());
        }
        return true;
    }

    private static boolean handleDebug(Object sender) {
        sendMessage(sender, "&6&lTitanMC Debug Info");
        sendMessage(sender, "&7  Runtime: " + Runtime.getRuntime().availableProcessors() + " cores");
        sendMessage(sender, "&7  Memory: " +
            (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1048576 + "MB / " +
            Runtime.getRuntime().maxMemory() / 1048576 + "MB");
        sendMessage(sender, "&7  Java: " + System.getProperty("java.version"));
        sendMessage(sender, "&7  OS: " + System.getProperty("os.name"));
        return true;
    }

    private static void sendMessage(Object sender, String message) {
        System.out.println(message.replaceAll("&[0-9a-fk-or]", ""));
    }
}
