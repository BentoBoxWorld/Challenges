import java.net.URI

plugins {
    // Plugwright: boots a real Paper server, deploys plugins, drives Mineflayer bots.
    id("io.github.drownek.plugwright") version "2.0.2"
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

// Maven-built Challenges jar (version/profile suffix varies: -SNAPSHOT-LOCAL locally, plain on CI).
val challengesJar: File = (file("../target").listFiles()?.toList() ?: emptyList())
    .firstOrNull {
        it.name.startsWith("Challenges-") && it.name.endsWith(".jar") &&
            !it.name.contains("sources") && !it.name.contains("javadoc")
    }
    ?: throw GradleException(
        "Challenges jar not found in ../target — run 'mvn -q -DskipTests package' first."
    )

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
        file("plugins/BentoBox/addons/Challenges.jar", challengesJar)
    }
}
