plugins {
    java
}

group = "com.titanmc"
version = "1.16.5-R0.1"

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.destroystokyo.com/repository/maven-public/")
}

dependencies {
    compileOnly("com.destroystokyo.paper:paper-api:1.16.5-R0.1-SNAPSHOT")
    compileOnly("org.jetbrains:annotations:23.0.0")
    // Paper JAR on classpath for jbsdiff and other bundled classes
    compileOnly(files("build/paper-1.16.5.jar"))
}

// Single JAR: merge Paper + ALL TitanMC classes (no plugin, everything built-in)
tasks.register<Jar>("buildTitan") {
    dependsOn("classes")
    archiveBaseName.set("TitanMC")
    archiveVersion.set("1.16.5")
    archiveClassifier.set("")

    // ALL TitanMC classes (launcher, agent, fixes, optimizations, listeners, config)
    from(sourceSets.main.get().output)

    // Override Multi-Release JAR entries too (Java 9+ reads from here)
    from(sourceSets.main.get().output) {
        include("io/papermc/paperclip/Agent.class")
        into("META-INF/versions/9")
    }

    // Paper JAR contents (base)
    from(zipTree("build/paper-1.16.5.jar")) {
        exclude("io/papermc/paperclip/Agent.class")
        exclude("io/papermc/paperclip/Paperclip.class")
        exclude("io/papermc/paperclip/PatchData.class")
        exclude("META-INF/versions/9/io/papermc/paperclip/Agent.class")
        exclude("META-INF/MANIFEST.MF")
    }

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes(
            "Main-Class" to "com.titanmc.launcher.TitanLauncher",
            "Premain-Class" to "com.titanmc.server.TitanAgent",
            "Agent-Class" to "com.titanmc.server.TitanAgent",
            "Can-Redefine-Classes" to "true",
            "Can-Retransform-Classes" to "true",
            "Implementation-Title" to "TitanMC",
            "Implementation-Version" to "1.16.5-R0.1"
        )
    }
}
