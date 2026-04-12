# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Challenges is a BentoBox addon (Bukkit/Spigot plugin module) that adds player challenges to any BentoBox GameMode addon (BSkyBlock, AcidIsland, SkyGrid, CaveBlock). It is not a standalone plugin — it is loaded by BentoBox at runtime.

- Java 21, Maven, Spigot `1.21.3-R0.1-SNAPSHOT`
- Depends on BentoBox `3.4.0`, optionally Level `2.6.3` and Vault `1.7`
- Version is set via `<build.version>` in `pom.xml` (uses `${revision}` / CI `-bNNN` suffix)

## Commands

```bash
# Full build + tests + shaded jar (output in target/)
mvn clean verify

# Compile only
mvn compile

# Run all tests
mvn test

# Run a single test class
mvn test -Dtest=ChallengesManagerTest

# Run a single test method
mvn test -Dtest=ChallengesManagerTest#methodName

# Release build (strips -SNAPSHOT) — set via profile, normally CI only
mvn clean verify -Dbuild.number=-bLOCAL
```

Built jar lands in `target/` and must be dropped into `BentoBox/addons/` on a running BentoBox server to test manually.

## Architecture

Entry point: `ChallengesAddon` (extends BentoBox `Addon`). `ChallengesPladdon` is the Paper-plugin wrapper used when BentoBox is loaded as a Paper plugin. `onLoad` reads `config.yml` into `Settings`, `onEnable` registers commands against each hooked `GameModeAddon`, instantiates managers, registers flags, and wires listeners.

Key subsystems:

- **`managers/ChallengesManager`** — central service. Owns in-memory caches of `Challenge`, `ChallengeLevel`, and per-player `ChallengesPlayerData`, plus the BentoBox `Database` handles that persist them. All mutation of challenges/levels/player progress should go through this manager so caches and DB stay consistent. Challenges and levels are keyed by a unique ID that is prefixed by the gamemode (e.g. `bskyblock_mychallenge`) — see `getUniqueID` / `addChallengeToLevel` for the prefixing contract.
- **`managers/ChallengesImportManager`** — imports/exports challenges from `default.json`, template YAML, and library downloads via `web/WebManager` (the "Web Library" in the GUI).
- **`database/object/`** — Gson-serialized data objects (`Challenge`, `ChallengeLevel`, `ChallengesPlayerData`). `requirements/` holds the polymorphic `Requirement` hierarchy (Island, Inventory, Other, Statistic) used to gate completion; `adapters/` contains Gson type adapters that make the polymorphism work — new requirement types must be registered there.
- **`panel/`** — all GUI code, built on `bentobox-panelutils`. `CommonPanel`/`CommonPagedPanel` are the base classes; `admin/` contains the admin editor GUIs, `user/` the player-facing GUIs. Conversations (text input prompts) go through `ConversationUtils`.
- **`commands/`** — player commands (`ChallengesPlayerCommand`, `ChallengesGlobalPlayerCommand`, `CompleteChallengeCommand`) and `commands/admin/` admin commands. Each is registered per gamemode in `ChallengesAddon.onEnable`.
- **`handlers/`** — BentoBox API request handlers (`ChallengeDataRequestHandler`, `LevelListRequestHandler`, etc.) exposed over the BentoBox inter-addon request API so other addons can query challenge data.
- **`listeners/`** — `ResetListener` wipes player data on island reset; `SaveListener` flushes caches on save/quit.
- **`tasks/`** — `TryToComplete` is the core completion pipeline: validates requirements, consumes costs, applies rewards, fires events. New reward/requirement logic typically plugs in here.
- **`events/`** — custom Bukkit events (`ChallengeCompletedEvent`, `LevelCompletedEvent`, etc.) fired by `TryToComplete`/managers. Extend the existing event hierarchy rather than adding ad-hoc callbacks.
- **`web/WebManager`** — downloads the public challenges library via BentoBox's `WebManager`/GitHub content.

Resources in `src/main/resources/`: `addon.yml` (addon metadata read by BentoBox), `config.yml` (user settings → `config/Settings`), `default.json` (bundled default challenges), `template.yml`, `panels/*.yml` (GUI layouts loaded by panelutils), `locales/*.yml` (translations — managed via GitLocalize, do not hand-edit beyond `en-US.yml`).

## Conventions

- Target branch for PRs is `master`; active development happens on `develop`.
- When adding a new `Requirement` subclass, also register its Gson adapter in `database/object/adapters/` or loading will fail silently with missing fields.
- Challenge/level IDs are always gamemode-prefixed on disk; when comparing or looking up, use `ChallengesManager` helpers rather than raw string compares.
- User-visible strings belong in `locales/en-US.yml` under the appropriate namespace — never hardcode them in Java.

## Dependency Source Lookup

When you need to inspect source code for a dependency (e.g., BentoBox, addons):

1. **Check local Maven repo first**: `~/.m2/repository/` — sources jars are named `*-sources.jar`
2. **Check the workspace**: Look for sibling directories or Git submodules that may contain the dependency as a local project (e.g., `../bentoBox`, `../addon-*`)
3. **Check Maven local cache for already-extracted sources** before downloading anything
4. Only download a jar or fetch from the internet if the above steps yield nothing useful

Prefer reading `.java` source files directly from a local Git clone over decompiling or extracting a jar.

In general, the latest version of BentoBox should be targeted.

## Project Layout

Related projects are checked out as siblings under `~/git/`:

**Core:**
- `bentobox/` — core BentoBox framework

**Game modes:**
- `addon-acidisland/` — AcidIsland game mode
- `addon-bskyblock/` — BSkyBlock game mode
- `Boxed/` — Boxed game mode (expandable box area)
- `CaveBlock/` — CaveBlock game mode
- `OneBlock/` — AOneBlock game mode
- `SkyGrid/` — SkyGrid game mode
- `RaftMode/` — Raft survival game mode
- `StrangerRealms/` — StrangerRealms game mode
- `Brix/` — plot game mode
- `parkour/` — Parkour game mode
- `poseidon/` — Poseidon game mode
- `gg/` — gg game mode

**Addons:**
- `addon-level/` — island level calculation
- `addon-challenges/` — challenges system
- `addon-welcomewarpsigns/` — warp signs
- `addon-limits/` — block/entity limits
- `addon-invSwitcher/` / `invSwitcher/` — inventory switcher
- `addon-biomes/` / `Biomes/` — biomes management
- `Bank/` — island bank
- `Border/` — world border for islands
- `Chat/` — island chat
- `CheckMeOut/` — island submission/voting
- `ControlPanel/` — game mode control panel
- `Converter/` — ASkyBlock to BSkyBlock converter
- `DimensionalTrees/` — dimension-specific trees
- `discordwebhook/` — Discord integration
- `Downloads/` — BentoBox downloads site
- `DragonFights/` — per-island ender dragon fights
- `ExtraMobs/` — additional mob spawning rules
- `FarmersDance/` — twerking crop growth
- `GravityFlux/` — gravity addon
- `Greenhouses-addon/` — greenhouse biomes
- `IslandFly/` — island flight permission
- `IslandRankup/` — island rankup system
- `Likes/` — island likes/dislikes
- `Limits/` — block/entity limits
- `lost-sheep/` — lost sheep adventure
- `MagicCobblestoneGenerator/` — custom cobblestone generator
- `PortalStart/` — portal-based island start
- `pp/` — pp addon
- `Regionerator/` — region management
- `Residence/` — residence addon
- `TopBlock/` — top ten for OneBlock
- `TwerkingForTrees/` — twerking tree growth
- `Upgrades/` — island upgrades (Vault)
- `Visit/` — island visiting
- `weblink/` — web link addon
- `CrowdBound/` — CrowdBound addon

**Data packs:**
- `BoxedDataPack/` — advancement datapack for Boxed

**Documentation & tools:**
- `docs/` — main documentation site
- `docs-chinese/` — Chinese documentation
- `docs-french/` — French documentation
- `BentoBoxWorld.github.io/` — GitHub Pages site
- `website/` — website
- `translation-tool/` — translation tool

Check these for source before any network fetch.

## Testing

The project uses a mixed test stack:

- **JUnit 4 + PowerMock** — existing tests in `commands/`, `tasks/`, `utils/`, `database/`, and top-level addon/manager tests. These use `@RunWith(PowerMockRunner.class)` and `@PrepareForTest`.
- **JUnit 5 + Mockito** — newer tests in `panel/` package. These use `@ExtendWith(MockitoExtension.class)` with `@MockitoSettings(strictness = Strictness.LENIENT)`.
- Both frameworks coexist via `junit-vintage-engine` which runs JUnit 4 tests on the JUnit 5 platform.

### Writing new JUnit 5 tests alongside PowerMock

PowerMock's MockMaker conflicts with mockito-inline, so **`mockStatic()` and `mockConstruction()` are not available** in JUnit 5 tests. Use these workarounds:

- **Static singletons** (`Bukkit.getServer()`, `BentoBox.getInstance()`): Set via reflection on the private static field. See `PanelTestHelper.setServer()` and `setBentoBoxInstance()` for examples. Always save/restore the previous value in `@BeforeEach`/`@AfterEach`.
- **Final methods** (`JavaPlugin.getServer()` is final): Set the `server` field on the plugin object via reflection walking the class hierarchy. See `PanelTestHelper.setPluginServer()`.
- **Java records** (`ItemTemplateRecord`, `TemplatedPanel.ItemSlot`): Cannot be mocked. Create real instances using their public constructors. For `TemplatedPanel` (which has no public constructor), use `sun.misc.Unsafe.allocateInstance()` then set fields via reflection. See `PanelTestHelper.createItemSlot()`.
- **Varargs methods** (`User.getTranslation(String, String...)`): Use `Mockito.doAnswer().when()` with `Mockito.<String>any()` instead of `when().thenAnswer()`. The `doAnswer` pattern handles varargs correctly. See `PanelTestHelper.setupUserTranslations()`.

### PanelTestHelper

`src/test/java/world/bentobox/challenges/panel/PanelTestHelper.java` is a shared utility for all panel tests. It provides:
- Reflection helpers for Bukkit/BentoBox static fields
- `ItemTemplateRecord` factory methods (`createTemplate`, `createSimpleTemplate`, `createEmptyTemplate`)
- `createItemSlot()` for `TemplatedPanel.ItemSlot` records
- `createBasicChallenge()` for fully-mocked `Challenge` objects
- `setupUserTranslations()` for varargs-safe translation mocking

## Key Dependencies (source locations)

- `world.bentobox:bentobox` → `~/git/bentobox/src/`
