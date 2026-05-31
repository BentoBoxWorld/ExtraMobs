# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

ExtraMobs is a BentoBox addon (Paper plugin) that re-skins certain natural mob spawns inside GameMode-managed worlds: Zombified Piglins / Piglins → Blaze/Wither Skeleton in the Nether, Endermen → Shulkers in the End, and Fish → Guardians in deep-ocean overworld biomes. The addon does not alter Minecraft's spawn rules — it listens for natural spawns and conditionally cancels + re-spawns a different entity. As of 1.15.0 it also ships a Pladdon entry point so it can be loaded directly by Paper as well as via BentoBox.

## Build & test

- `mvn clean package` — default goal; produces `target/ExtraMobs-<version>-LOCAL.jar`.
- `mvn test` — runs the test suite.
- `mvn test -Dtest=MobsSpawnListenerTest` — single test class.
- `mvn test -Dtest=MobsSpawnListenerTest#methodName` — single test method.
- Java 21 (`<java.version>21</java.version>` in pom.xml). Targets Paper 1.21.11 and BentoBox 3.14.0-SNAPSHOT.
- Build versioning is driven by Maven profiles: `-LOCAL` by default, `-b<BUILD_NUMBER>` under Jenkins CI, and a clean release version when `GIT_BRANCH=origin/master`. Don't hand-edit version strings — change `build.version` in `pom.xml`.

## Architecture

Tiny codebase, four production classes:

- `ExtraMobsAddon` (`src/main/java/world/bentobox/extramobs/`) — extends `world.bentobox.bentobox.api.addons.Addon`. In `onLoad()` it loads `Settings` via BentoBox's `Config<>` (auto-creates `config.yml` from `src/main/resources/`). In `onEnable()` it iterates `getAddonsManager().getGameModeAddons()`, sets `hooked=true` if any GameMode is not in `disabledGameModes`, and registers `MobsSpawnListener`. If nothing hooks, the addon disables itself.
- `ExtraMobsPladdon` — extends `Pladdon`. Lets Paper load the addon directly as a plugin (paired with `src/main/resources/plugin.yml`); returns a fresh `ExtraMobsAddon` from `getAddon()`.
- `config.Settings` — `ConfigObject` with `@StoreAt(filename="config.yml", path="addons/ExtraMobs")`. Field annotations (`@ConfigEntry`, `@ConfigComment`) drive both YAML parsing and the on-disk comment block; getters/setters are mandatory for the BentoBox config framework to bind values. The `gamemode-settings` field is stored as `Map<String, Object>` because BentoBox's config layer doesn't model the nested-list-of-maps shape; `getReplacements(gameMode, env)` parses the raw structure into `MobSpawnReplacement` records on read.
- `config.MobSpawnReplacement` — POJO for one per-gamemode rule (`old` mob, `new` mob, `chance`). `resolveOldEntityType()` / `resolveNewEntityType()` upper-case via `Locale.ROOT` then call `EntityType.valueOf` defensively.
- `listeners.MobsSpawnListener` — single `@EventHandler(priority=HIGHEST, ignoreCancelled=true)` on `CreatureSpawnEvent`. Only `SpawnReason.NATURAL` events are considered. Flow: `resolveActiveGameMode(world)` returns the GameMode name (or `null` if absent / disabled), then `onEntitySpawn` dispatches by entity type + environment to `handleNetherSpawn` / `handleEndSpawn` / `handleOverworldSpawn`. Each handler first checks a "suitable block" predicate (nether brick / purpur / prismarine, with slab+stairs variants), then calls `applyGameModeReplacements` for per-gamemode rules, and only falls back to the global `nether-chances` / `end-chances` / `overworld-chance` values if no per-gamemode rule fires for that event. The four block sets are pre-computed `Set<Material>` statics on the class.

The "suitable location" helpers encode the design rule that drives the addon: replacement is gated on the player having built a themed structure. Changes to spawn rules almost always live in these predicates plus the dispatch branches in `onEntitySpawn`.

Per-gamemode rules **supplement** the globals rather than suppress them — a per-gamemode rule with `chance: 0.05` for `ZOMBIFIED_PIGLIN→WITHER_SKELETON` falls through to the global wither/blaze chances on a miss, and `PIGLIN` (different entity) always falls through. The tests `testPerGameModeNetherChanceZeroFallsBackToGlobal` and `testPerGameModeNetherReplacementEntityMismatchFallsBackToGlobal` enshrine this behaviour — keep it in mind before changing the listener.

## Testing notes

- JUnit 5 + Mockito 5 + MockBukkit (`org.mockbukkit.mockbukkit:mockbukkit-v1.21`). Test classes extend `CommonTestSetup` which calls `MockBukkit.mock()` in `@BeforeEach` and tears down in `@AfterEach`; it also injects the `BentoBox` singleton via `WhiteBox.setInternalState(BentoBox.class, "instance", plugin)` and statically stubs `Bukkit` + `Util`. The surefire plugin's long `--add-opens` argLine plus the leading `@{argLine}` (which late-binds the Jacoco prepare-agent javaagent) are required; don't strip either.
- Jacoco excludes `**/*Names*` and `org/bukkit/Material*` to avoid synthetic-field / "Material too large to mock" failures. Coverage is reported to SonarCloud via the GitHub Actions workflow.

## Resources & packaging

`src/main/resources/addon.yml` declares the addon to BentoBox (`main`, `softdepend` GameModes, icon). `config.yml` is filtered (`${version}` substitution), while `locales/*.yml` and `blueprints/*.{blu,json}` are copied unfiltered into the jar root under `./locales` and `./blueprints` — keep new resource directories consistent with this layout in `pom.xml` so BentoBox finds them at runtime.
