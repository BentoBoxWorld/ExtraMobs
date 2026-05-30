# 👾 ExtraMobs Add-on for BentoBox
[![Discord](https://img.shields.io/discord/272499714048524288.svg?logo=discord)](https://discord.bentobox.world)
[![Build Status](https://ci.codemc.org/buildStatus/icon?job=BentoBoxWorld/ExtraMobs)](https://ci.codemc.org/job/BentoBoxWorld/job/ExtraMobs/)

## 🔍 What is ExtraMobs?

**ExtraMobs** is a BentoBox add-on that lets players spawn **Blazes**, **Wither Skeletons**, **Shulkers** and **Guardians** on their islands by building the right structures. It does **not** change Minecraft's spawning rules — it watches for natural spawns and replaces certain mobs by chance when the surrounding blocks match a themed pattern.

Works with every BentoBox game mode (AcidIsland, BSkyBlock, CaveBlock, SkyGrid, …).

---

## 🚀 Getting Started

1. Place the **ExtraMobs** `.jar` into your BentoBox `addons` folder.
2. Restart your server.
3. The addon creates `addons/ExtraMobs/config.yml`.
4. Edit `config.yml` to adjust the spawn chances or disable specific game modes.
5. Restart the server (or reload BentoBox) to apply your changes.

---

## ✨ How Replacements Work

ExtraMobs only acts on **natural** spawns inside a BentoBox-managed world. When a candidate mob spawns on a themed block, ExtraMobs rolls against the configured chance; on success the original spawn is cancelled and the replacement entity is summoned in its place.

### 🔥 Nether — Wither Skeleton & Blaze

A **Zombified Piglin** or **Piglin** is replaced when:
- the world is the BentoBox Nether,
- and the mob is standing on **nether brick**, **nether brick slab**, or **nether brick stairs**.

The wither skeleton roll is checked first, then the blaze roll.

### 🌌 End — Shulker

An **Enderman** is replaced with a **Shulker** when:
- the world is the BentoBox End,
- and the mob is standing on a **purpur block**, **purpur slab**, or **purpur stairs**.

### 🌊 Overworld — Guardian

A naturally-spawned **Cod**, **Salmon**, or **Tropical Fish** is replaced with a **Guardian** when:
- the world is the BentoBox Overworld,
- the biome is **Deep Ocean** (or Deep Cold / Deep Frozen / Deep Lukewarm),
- and the first non-water block above the fish is **prismarine**, **prismarine bricks**, or **dark prismarine** (block, slab, or stairs variant).

---

## ⚙️ Configuration

### Global Defaults

```yaml
# Game modes in which ExtraMobs should not run.
# Add the GameMode addon name (e.g. BSkyBlock, AcidIsland, CaveBlock).
disabled-gamemodes: []

nether-chances:
  # Chance (0.0–1.0) to spawn a Wither Skeleton instead of a Zombified Piglin.
  wither-skeleton: 0.01
  # Chance (0.0–1.0) to spawn a Blaze instead of a Zombified Piglin.
  blaze: 0.1

end-chances:
  # Chance (0.0–1.0) to spawn a Shulker instead of an Enderman.
  shulker: 0.1

overworld-chance:
  # Chance (0.0–1.0) to spawn a Guardian instead of a fish.
  guardian: 0.1
```

All chance values are decimals between `0.0` (never) and `1.0` (always). For the Nether the wither skeleton roll is evaluated before the blaze roll, so the blaze chance is effectively conditional on the wither skeleton roll failing.

### Per-Gamemode Overrides

You can define replacement rules that only apply to a specific game mode. Each gamemode may set rules for the `nether`, `end`, and/or `world` (overworld) environments. **When per-gamemode rules are present for an environment, the global defaults above are not applied for that gamemode.**

```yaml
gamemode-settings:
  BSkyBlock:
    nether:
      - old: ZOMBIFIED_PIGLIN
        new: WITHER_SKELETON
        chance: 0.05
      - old: ZOMBIFIED_PIGLIN
        new: BLAZE
        chance: 0.1
    end:
      - old: ENDERMAN
        new: SHULKER
        chance: 0.3
    world:
      - old: COD
        new: GUARDIAN
        chance: 0.15
  AcidIsland:
    end:
      - old: ENDERMAN
        new: SHULKER
        chance: 0.5
```

Each rule needs:
- `old` — the EntityType name of the mob to replace (e.g. `ZOMBIFIED_PIGLIN`, `ENDERMAN`, `COD`).
- `new` — the EntityType name of the replacement (e.g. `WITHER_SKELETON`, `SHULKER`, `GUARDIAN`).
- `chance` — probability in the range `0.0`–`1.0`.

The gamemode key must exactly match the GameMode addon name as registered in BentoBox (`BSkyBlock`, `AcidIsland`, `CaveBlock`, `SkyGrid`, …). Themed-block requirements (nether brick / purpur / prismarine) still apply to per-gamemode rules.

---

## 🐛 Bugs and Feature Requests

Please submit issues at [GitHub Issues](https://github.com/BentoBoxWorld/ExtraMobs/issues) or ask in the [BentoBox Discord](https://discord.bentobox.world).

More information is available on the [Wiki](https://github.com/BentoBoxWorld/ExtraMobs/wiki).
