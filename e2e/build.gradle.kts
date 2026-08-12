import java.net.URI
import me.drownek.plugwright.PlugwrightTestTask

plugins {
    // Plugwright: boots a real Paper server, deploys plugins, drives Mineflayer bots.
    id("io.github.drownek.plugwright") version "2.0.2"
    id("java-base")
}

// --- Dependency jars -------------------------------------------------------------------------
// BentoBox is a Paper plugin (goes in plugins/). BSkyBlock and Challenges are BentoBox *addons*
// and MUST live in plugins/BentoBox/addons/ so BentoBox loads them as gamemode/addon (creating
// the gamemode world and registering commands) — dropping them straight into plugins/ makes them
// load as bare Paper plugins that never register with BentoBox.

val depsDir = layout.projectDirectory.dir(".deps").asFile.apply { mkdirs() }

fun cachedJar(name: String, url: String): File {
    val f = File(depsDir, name)
    if (!f.exists()) {
        logger.lifecycle("plugwright: downloading $name ...")
        URI(url).toURL().openStream().use { input -> f.outputStream().use { input.copyTo(it) } }
    }
    return f
}

val bentoboxJar = cachedJar(
    "BentoBox-3.14.0.jar",
    "https://github.com/BentoBoxWorld/BentoBox/releases/download/3.14.0/BentoBox-3.14.0.jar"
)
val bskyblockJar = cachedJar(
    "BSkyBlock-1.20.0.jar",
    "https://github.com/BentoBoxWorld/BSkyBlock/releases/download/1.20.0/BSkyBlock-1.20.0.jar"
)

// If Gradle is running on Java 17, try to find a Java 21+ toolchain to satisfy Maven/Paper.
// If Gradle is already running on Java 21+ (e.g., 22, 23), don't force a strict toolchain lock.
val currentJava = JavaVersion.current()
val javaLauncherProvider = if (currentJava < JavaVersion.VERSION_21) {
    extensions.getByType<JavaToolchainService>().launcherFor {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
} else {
    null
}

val buildChallenges = tasks.register<Exec>("buildChallenges") {
    workingDir = file("..")

    inputs.dir(file("../src"))
    inputs.file(file("../pom.xml"))

    val isWindows = System.getProperty("os.name").lowercase().contains("win")
    val executable = if (isWindows) listOf("cmd", "/c", "mvnw.cmd") else listOf("./mvnw")
    // Use 'clean' to avoid multiple jars causing singleFile to fail
    commandLine(executable + listOf("-q", "clean", "package", "-DskipTests"))

    // Pass the correct JAVA_HOME to Maven if we needed a custom toolchain
    if (javaLauncherProvider != null) {
        environment["JAVA_HOME"] = javaLauncherProvider.get().metadata.installationPath.asFile.absolutePath
    } else {
        environment["JAVA_HOME"] = System.getProperty("java.home")
    }

    // Explicitly define the expected output file
    val outputJar = layout.buildDirectory.file("Challenges.jar")
    outputs.file(outputJar)
    
    doFirst {
        logger.lifecycle("Building Challenges project using Maven Wrapper (mvnw package)...")
    }
    
    doLast {
        val builtJar = fileTree("../target") {
            include("Challenges-*.jar")
            exclude("*sources*", "*javadoc*")
        }.singleFile
        
        builtJar.copyTo(outputJar.get().asFile, overwrite = true)
    }
}

tasks.named<PlugwrightTestTask>("plugwrightTest") {
    dependsOn(buildChallenges)
    if (javaLauncherProvider != null) {
        javaLauncher.set(javaLauncherProvider)
    }
}

plugwright {
    minecraftVersion.set("1.21.11")
    acceptEula.set(true)
    // We deploy prebuilt jars (this is a Maven project, not a Gradle plugin build).
    useExternalPluginsOnly.set(true)
    testsDir.set(file("src/test/e2e"))
    jvmArgs.set(listOf("-Xmx3G"))

    writeFiles {
        file("plugins/BentoBox.jar", bentoboxJar)
        file("plugins/BentoBox/addons/BSkyBlock.jar", bskyblockJar)
        file("plugins/BentoBox/addons/Challenges.jar", file("build/Challenges.jar"))
    }
}
