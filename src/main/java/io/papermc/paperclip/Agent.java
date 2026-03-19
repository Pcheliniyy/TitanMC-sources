package io.papermc.paperclip;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.jar.JarFile;

public class Agent {

    private static Instrumentation instrumentation;

    public static void premain(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        instrumentation = inst;
    }

    public static void addToClassPath(Path jarPath) {
        addToClassPath(jarPath.toFile());
    }

    public static void addToClassPath(File jarFile) {
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        Instrumentation inst = getInstrumentation();
        if (inst != null) {
            try {
                inst.appendToSystemClassLoaderSearch(new JarFile(jarFile));
                return;
            } catch (Exception e) {
                System.err.println("[TitanMC] Instrumentation classpath add failed: " + e.getMessage());
            }
        }
        if (classLoader instanceof URLClassLoader) {
            try {
                Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
                addURL.setAccessible(true);
                addURL.invoke(classLoader, jarFile.toURI().toURL());
                return;
            } catch (Exception e) {
                System.err.println("[TitanMC] URLClassLoader fallback failed: " + e.getMessage());
            }
        }
        try {
            Method addURL = classLoader.getClass().getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
            addURL.setAccessible(true);
            addURL.invoke(classLoader, jarFile.getAbsolutePath());
            return;
        } catch (Exception e) {
        }

        throw new RuntimeException(
            "[TitanMC] Cannot add to classpath. Use Java 11-16 for best compatibility, " +
            "or ensure TitanMC.jar is launched with -javaagent:TitanMC.jar"
        );
    }

    private static Instrumentation getInstrumentation() {
        if (instrumentation != null) return instrumentation;
        try {
            Class<?> titanAgent = Class.forName("com.titanmc.server.TitanAgent");
            Method getInst = titanAgent.getMethod("getInstrumentation");
            Object inst = getInst.invoke(null);
            if (inst instanceof Instrumentation) {
                return (Instrumentation) inst;
            }
        } catch (Exception ignored) {}

        return null;
    }
}
