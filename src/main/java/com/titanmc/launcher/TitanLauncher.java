package com.titanmc.launcher;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class TitanLauncher {

    private static final String VERSION = "1.16.5-R0.1";
    private static final String AGENT_FLAG = "--titanmc-patched";

    public static void main(String[] args) throws Exception {
        boolean agentLoaded = false;
        List<String> cleanArgs = new ArrayList<>();
        for (String arg : args) {
            if (arg.equals(AGENT_FLAG)) {
                agentLoaded = true;
            } else {
                cleanArgs.add(arg);
            }
        }

        if (agentLoaded) {
            launchPaperDirect(cleanArgs.toArray(new String[0]));
        } else {
            printBanner();
            relaunchWithAgent(args);
        }
    }

    
    private static void relaunchWithAgent(String[] originalArgs) throws Exception {
        Path selfJar = getSelfJarPath();
        if (selfJar == null) {
            System.out.println("[TitanMC] WARNING: Could not determine JAR path, launching without agent");
            launchPaperDirect(originalArgs);
            return;
        }
        Path eulaPath = Paths.get("eula.txt");
        if (!Files.exists(eulaPath)) {
            Files.writeString(eulaPath, "eula=true\n");
            System.out.println("[TitanMC] EULA accepted.");
        }

        System.out.println("[TitanMC] Starting server with runtime patches...");
        System.out.println();

        List<String> command = new ArrayList<>();
        command.add(getJavaPath());
        command.add("-javaagent:" + selfJar.toAbsolutePath());
        command.add("-DPaper.IgnoreJavaVersion=true");
        command.add("-Dcom.destroystokyo.paper.ignoreJavaVersion=true");
        command.add("-Dio.papermc.paperclip.ignoreJavaVersion=true");
        int javaVersion = getJavaVersion();
        if (javaVersion >= 16) {
            command.add("--add-opens=java.base/java.lang=ALL-UNNAMED");
            command.add("--add-opens=java.base/java.lang.reflect=ALL-UNNAMED");
            command.add("--add-opens=java.base/java.io=ALL-UNNAMED");
            command.add("--add-opens=java.base/java.net=ALL-UNNAMED");
            command.add("--add-opens=java.base/java.nio=ALL-UNNAMED");
            command.add("--add-opens=java.base/java.util=ALL-UNNAMED");
            command.add("--add-opens=java.base/java.util.concurrent=ALL-UNNAMED");
            command.add("--add-opens=java.base/sun.nio.ch=ALL-UNNAMED");
            command.add("--add-opens=java.base/sun.security.ssl=ALL-UNNAMED");
            command.add("--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED");
            command.add("--add-modules=jdk.unsupported");
        }
        for (String prop : java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (prop.startsWith("-D") || prop.startsWith("-XX") || prop.startsWith("-Xm")) {
                command.add(prop);
            }
        }

        command.add("-jar");
        command.add(selfJar.toAbsolutePath().toString());
        command.add(AGENT_FLAG);

        for (String arg : originalArgs) {
            command.add(arg);
        }
        boolean hasNogui = false;
        for (String arg : originalArgs) {
            if (arg.equalsIgnoreCase("nogui")) {
                hasNogui = true;
                break;
            }
        }
        if (!hasNogui) {
            command.add("nogui");
            command.add("--titanmc-gui");
        }

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        pb.directory(Paths.get("").toAbsolutePath().toFile());

        Process process = pb.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (process.isAlive()) {
                process.destroy();
            }
        }));

        System.exit(process.waitFor());
    }

    
    private static void launchPaperDirect(String[] args) throws Exception {
        List<String> filteredArgs = new ArrayList<>();
        boolean showGui = false;
        for (String arg : args) {
            if (arg.equals("--titanmc-gui")) {
                showGui = true;
            } else {
                filteredArgs.add(arg);
            }
        }
        args = filteredArgs.toArray(new String[0]);

        if (showGui) {
            try {
                javax.swing.SwingUtilities.invokeLater(() -> {
                    com.titanmc.server.gui.TitanGUI.createAndShow();
                });
            } catch (Exception e) {
                System.err.println("[TitanMC] Could not open GUI: " + e.getMessage());
            }
        }
        String[] mainClasses = {
            "io.papermc.paperclip.Paperclip",
            "io.papermc.paperclip.Main",
            "org.bukkit.craftbukkit.Main"
        };

        for (String className : mainClasses) {
            try {
                Class<?> mainClass = Class.forName(className);
                Method mainMethod = mainClass.getMethod("main", String[].class);
                mainMethod.invoke(null, (Object) args);
                return;
            } catch (ClassNotFoundException ignored) {
            }
        }

        System.err.println("[TitanMC] ERROR: Could not find Paper main class!");
        System.err.println("[TitanMC] The JAR may be corrupted. Re-download or rebuild.");
        System.exit(1);
    }

    private static void printBanner() {
        System.out.println();
        System.out.println("  ████████╗██╗████████╗ █████╗ ███╗   ██╗███╗   ███╗ ██████╗");
        System.out.println("  ╚══██╔══╝██║╚══██╔══╝██╔══██╗████╗  ██║████╗ ████║██╔════╝");
        System.out.println("     ██║   ██║   ██║   ███████║██╔██╗ ██║██╔████╔██║██║     ");
        System.out.println("     ██║   ██║   ██║   ██╔══██║██║╚██╗██║██║╚██╔╝██║██║     ");
        System.out.println("     ██║   ██║   ██║   ██║  ██║██║ ╚████║██║ ╚═╝ ██║╚██████╗");
        System.out.println("     ╚═╝   ╚═╝   ╚═╝   ╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝     ╚═╝ ╚═════╝");
        System.out.println("  v" + VERSION + " | No Dupes. No Exploits. Max Performance.");
        System.out.println();
    }

    private static String getJavaPath() {
        String javaHome = System.getProperty("java.home");
        Path javaBin = Paths.get(javaHome, "bin", "java");
        if (Files.exists(javaBin)) return javaBin.toString();
        Path javaExe = Paths.get(javaHome, "bin", "java.exe");
        if (Files.exists(javaExe)) return javaExe.toString();
        return "java";
    }

    private static Path getSelfJarPath() {
        try {
            java.net.URL url = TitanLauncher.class.getProtectionDomain().getCodeSource().getLocation();
            Path path = Paths.get(url.toURI());
            if (Files.isRegularFile(path) && path.toString().endsWith(".jar")) {
                return path;
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static int getJavaVersion() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1.")) {
            return Integer.parseInt(version.substring(2, 3));
        }
        int dot = version.indexOf('.');
        if (dot > 0) {
            return Integer.parseInt(version.substring(0, dot));
        }
        try {
            return Integer.parseInt(version);
        } catch (NumberFormatException e) {
            return 11;
        }
    }
}
