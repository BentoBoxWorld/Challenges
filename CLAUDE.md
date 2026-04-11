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
