package com.titanmc.server;

import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

public class TitanAgent {

    private static Instrumentation instrumentation;
    private static boolean loaded = false;

    public static void premain(String agentArgs, Instrumentation inst) {
        if (loaded) return;
        loaded = true;
        instrumentation = inst;

        System.out.println();
        System.out.println("  [TitanMC] Agent loaded. Runtime patches ready.");
        System.out.println("  [TitanMC] 25 dupe fixes | 20+ exploit fixes | 8 optimizers");
        System.out.println();
        Thread initThread = new Thread(() -> {
            int attempts = 0;
            while (attempts < 120) {
                try {
                    Thread.sleep(500);
                    attempts++;
                    Object server = Class.forName("org.bukkit.Bukkit")
                        .getMethod("getServer")
                        .invoke(null);
                    if (server == null) continue;
                    Object pm = server.getClass().getMethod("getPluginManager").invoke(server);
                    if (pm == null) continue;
                    Object sched = server.getClass().getMethod("getScheduler").invoke(server);
                    if (sched == null) continue;
                    Thread.sleep(5000);
                    Class<?> bootstrap = Class.forName("com.titanmc.server.TitanBootstrap");
                    bootstrap.getMethod("initialize").invoke(null);
                    break;

                } catch (ClassNotFoundException e) {
                } catch (Exception e) {
                    if (attempts > 20) {
                        System.err.println("[TitanMC] Init attempt " + attempts + ": " + e.getMessage());
                    }
                }
            }
        }, "TitanMC-Init");
        initThread.setDaemon(true);
        initThread.start();
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }

    public static Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static void addToClassPath(java.io.File jarFile) {
        if (instrumentation != null) {
            try {
                instrumentation.appendToSystemClassLoaderSearch(new JarFile(jarFile));
            } catch (Exception e) {
                System.err.println("[TitanMC] Failed to add to classpath: " + e.getMessage());
            }
        }
    }
}
