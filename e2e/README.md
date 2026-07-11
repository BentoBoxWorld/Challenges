# End-to-end (in-game) tests — plugwright spike

This is a spike using [plugwright](https://github.com/Drownek/plugwright) to verify Challenges
**in a real running server** — something the JUnit/MockBukkit unit tests can't do. Plugwright boots
a real Paper server, loads the plugins, and drives a headless [Mineflayer](https://github.com/PrismarineJS/mineflayer)
bot that runs commands and clicks GUIs, then asserts on what the bot sees.

It is a **separate Gradle sidecar** — it does not touch the Maven build. It deploys *prebuilt* jars:

- **BentoBox 3.14.0** and **BSkyBlock 1.20.0** are downloaded (cached in `.deps/`).
- The **Challenges** jar is taken from `../target/` (build it first with `mvn -DskipTests package`).

> **Gotcha that cost most of the spike:** BentoBox *addons* (BSkyBlock, Challenges) must be staged
> into `plugins/BentoBox/addons/`, **not** `plugins/`. In `plugins/` they load as bare Paper plugins
> and never register as gamemodes, so no `bskyblock_world` is created and every `/island`,
> `/bsbadmin`, `/challenges` command comes back "Unknown". See `build.gradle.kts`.

## Running locally

```bash
# 1. Build the Challenges jar (Maven)
mvn -DskipTests package

# 2. Run the E2E tests (needs Java 21, Node 18+)
cd e2e
JAVA_HOME=/path/to/jdk-21 ./gradlew plugwrightTest

# add PLUGWRIGHT_DEBUG=1 to see every message the bot receives
```

First run downloads Paper 1.21.11 (~50 MB) and the plugin jars; later runs reuse them
(`run/server.jar`, `.deps/`). A full run is ~30–40s (server boot + world gen dominate).

## What the tests cover (`src/test/e2e/challenges.spec.ts`)

- **`bot can interact with the server`** — smoke test: bot joins Paper 1.21.11, ops, runs a command,
  receives the reply. Proves the whole stack (BentoBox 3.14.0 + BSkyBlock + Challenges 1.8.0) loads
  and is drivable.
- **`confirmation prompts tell the player how to answer (#329)`** — opens the Challenges admin GUI,
  clicks "Challenge Wipe", and asserts the bot receives the confirmation instruction line
  ("Type 'confirm' ... or 'cancel' ...") added in #415. A real in-game validation of a 1.8.0 feature.

## Notes / next steps

- Tests requiring an **island** (block/biome/completion flows) still need island bootstrap — either
  a bot-driven `/island create` (blueprint GUI) or a pre-staged world via `writeFiles`.
- In CI this runs via `.github/workflows/e2e.yml` (manual `workflow_dispatch` for now — advisory,
  not a required check).
