# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

ExtraMobs is a BentoBox addon (Bukkit/Spigot plugin) that re-skins certain natural mob spawns inside GameMode-managed worlds: Zombified Piglins → Blaze/Wither Skeleton in the Nether, Endermen → Shulkers in the End, and Fish → Guardians in deep-ocean overworld biomes. The addon does not alter Minecraft's spawn rules — it listens for natural spawns and conditionally cancels + re-spawns a different entity.

## Build & test

- `mvn clean package` — default goal; produces `target/ExtraMobs-<version>-LOCAL.jar`.
- `mvn test` — runs the test suite.
- `mvn test -Dtest=MobsSpawnListenerTest` — single test class.
- `mvn test -Dtest=MobsSpawnListenerTest#methodName` — single test method.
- Java 17 (`<java.version>17</java.version>` in pom.xml). Targets Spigot 1.21.3 and BentoBox 2.7.1-SNAPSHOT.
- Build versioning is driven by Maven profiles: `-LOCAL` by default, `-b<BUILD_NUMBER>` under Jenkins CI, and a clean release version when `GIT_BRANCH=origin/master`. Don't hand-edit version strings — change `build.version` in `pom.xml`.

## Architecture

Tiny codebase, three production classes:

- `ExtraMobsAddon` (`src/main/java/world/bentobox/extramobs/`) — extends `world.bentobox.bentobox.api.addons.Addon`. In `onLoad()` it loads `Settings` via BentoBox's `Config<>` (auto-creates `config.yml` from `src/main/resources/`). In `onEnable()` it iterates `getAddonsManager().getGameModeAddons()`, sets `hooked=true` if any GameMode is not in `disabledGameModes`, and registers `MobsSpawnListener`. If nothing hooks, the addon disables itself.
- `config.Settings` — `ConfigObject` with `@StoreAt(filename="config.yml", path="addons/ExtraMobs")`. Field annotations (`@ConfigEntry`, `@ConfigComment`) drive both YAML parsing and the on-disk comment block; getters/setters are mandatory for the BentoBox config framework to bind values.
- `listeners.MobsSpawnListener` — single `@EventHandler(priority=HIGHEST, ignoreCancelled=true)` on `CreatureSpawnEvent`. Only `SpawnReason.NATURAL` events are considered. Flow: resolve the GameMode via `plugin.getIWM().getAddon(world)`, bail if disabled, then branch by entity type + environment (`isIslandNether` / `isIslandEnd` / `World.Environment.NORMAL` + biome). Each branch checks a "suitable block" predicate (nether brick / purpur / prismarine, with slab+stairs variants) and rolls against the configured chance. On a successful roll the event is cancelled and `world.spawnEntity()` summons the replacement.

The "suitable location" helpers encode the design rule that drives the addon: replacement is gated on the player having built a themed structure. Changes to spawn rules almost always live in these predicates plus the dispatch branches in `onEntitySpawn`.

## Testing notes

- JUnit 4 + Mockito + PowerMock (`@RunWith(PowerMockRunner.class)`). Bukkit's static `Server`/`Registry`/`Tag` are stubbed via `listeners/mocks/ServerMocks.newServer()` — call this in `@Before` whenever a test touches Bukkit statics. The surefire plugin's long `--add-opens` argLine is required for PowerMock under Java 17; don't strip it.
- Jacoco excludes `**/*Names*` and `org/bukkit/Material*` to avoid synthetic-field / "Material too large to mock" failures. New tests that need to mock `Material` should rely on `ServerMocks` rather than reintroducing PowerMock static-mocking on `Material`.

## Resources & packaging

`src/main/resources/addon.yml` declares the addon to BentoBox (`main`, `softdepend` GameModes, icon). `config.yml` is filtered (`${version}` substitution), while `locales/*.yml` and `blueprints/*.{blu,json}` are copied unfiltered into the jar root under `./locales` and `./blueprints` — keep new resource directories consistent with this layout in `pom.xml` so BentoBox finds them at runtime.
