package io.papermc.paperclip;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.*;
import java.util.Properties;
import java.util.jar.*;

public class Paperclip {

    public static void main(String[] args) throws Exception {
        Path cacheDir = Paths.get("cache");
        Files.createDirectories(cacheDir);
        Path patchedJar = cacheDir.resolve("patched_1.16.5.jar");

        if (!Files.exists(patchedJar)) {
            Properties patchProps = new Properties();
            InputStream propsStream = Paperclip.class.getResourceAsStream("/patch.properties");
            if (propsStream == null) {
                System.err.println("[TitanMC] patch.properties not found");
                System.exit(1);
                return;
            }
            patchProps.load(propsStream);
            propsStream.close();

            String sourceUrl = patchProps.getProperty("sourceUrl");
            String patchFile = patchProps.getProperty("patch");
            Path vanillaJar = cacheDir.resolve("mojang_1.16.5.jar");

            if (!Files.exists(vanillaJar)) {
                System.out.println("[TitanMC] Downloading vanilla server...");
                downloadFile(sourceUrl, vanillaJar);
                System.out.println("[TitanMC] Vanilla server downloaded.");
            }

            System.out.println("[TitanMC] Applying Paper patches...");
            InputStream patchStream = Paperclip.class.getResourceAsStream("/" + patchFile);
            if (patchStream == null) {
                System.err.println("[TitanMC] " + patchFile + " not found");
                System.exit(1);
                return;
            }

            byte[] originalBytes = Files.readAllBytes(vanillaJar);
            byte[] patchBytes = patchStream.readAllBytes();
            patchStream.close();

            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try {
                Class<?> patchClass = Class.forName("org.jbsdiff.Patch");
                Method patchMethod = patchClass.getMethod("patch", byte[].class, byte[].class, OutputStream.class);
                patchMethod.invoke(null, originalBytes, patchBytes, output);
            } catch (ClassNotFoundException e) {
                ClassLoader jarLoader = new java.net.URLClassLoader(
                    new URL[]{ Paperclip.class.getProtectionDomain().getCodeSource().getLocation() },
                    Paperclip.class.getClassLoader()
                );
                Class<?> patchClass = jarLoader.loadClass("org.jbsdiff.Patch");
                Method patchMethod = patchClass.getMethod("patch", byte[].class, byte[].class, OutputStream.class);
                patchMethod.invoke(null, originalBytes, patchBytes, output);
            }

            Files.write(patchedJar, output.toByteArray());
            System.out.println("[TitanMC] Paper patches applied.");
        } else {
            System.out.println("[TitanMC] Using cached patched server.");
        }

        Agent.addToClassPath(patchedJar);

        String mainClassName = null;
        try (JarFile jar = new JarFile(patchedJar.toFile())) {
            Manifest manifest = jar.getManifest();
            if (manifest != null) {
                mainClassName = manifest.getMainAttributes().getValue("Main-Class");
            }
        }
        if (mainClassName == null) {
            mainClassName = "org.bukkit.craftbukkit.Main";
        }

        System.out.println("[TitanMC] Starting: " + mainClassName);
        Class<?> mainClass = Class.forName(mainClassName);
        Method mainMethod = mainClass.getMethod("main", String[].class);
        mainMethod.invoke(null, (Object) args);
    }

    public static Method getMainMethod(String[] args) throws Exception {
        main(args);
        return null;
    }

    private static void downloadFile(String urlStr, Path target) throws Exception {
        URL url = new URL(urlStr);
        try (InputStream in = url.openStream();
             OutputStream out = Files.newOutputStream(target)) {
            byte[] buf = new byte[8192];
            int len;
            long total = 0;
            while ((len = in.read(buf)) != -1) {
                out.write(buf, 0, len);
                total += len;
                if (total % (5 * 1024 * 1024) == 0) {
                    System.out.println("[TitanMC] Downloaded " + (total / 1024 / 1024) + " MB...");
                }
            }
            System.out.println("[TitanMC] Download complete: " + (total / 1024 / 1024) + " MB");
        }
    }
}
