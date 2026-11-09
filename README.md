# First Aid Reborn

**Per-limb health for Minecraft 1.21.1 (NeoForge)** — a community port of
[ichttt](https://github.com/ichttt)'s [First Aid](https://github.com/ichttt/FirstAid).

Instead of a single health bar, damage is tracked per body part: head, body, arms and legs each have
their own health, get wounded individually, and need bandages, splints and time to heal. A broken leg
slows you down, a wounded head blurs your vision, and a destroyed head or body is fatal.

分部位生命系统：伤害按头、身、双臂、双腿分别记录，每个部位独立受伤、独立治疗。腿部受伤会减速，
头部受伤会失明/反胃，头部或身体归零即死亡。

---

## Features / 特性

| English | 中文 |
|---|---|
| Per-limb health (head 4, body 12, limbs 8 by default, configurable) | 部位血量（默认头 4、身 12、四肢 8，可配置） |
| Damage is routed by hit direction, projectile trajectory and armor coverage | 伤害按命中方向、弹射物轨迹与护甲覆盖分配到具体部位 |
| Bandages and splints heal one heart at a time; taking damage interrupts treatment | 绷带/石膏逐半颗心治疗，治疗途中被攻击会中断 |
| Limb-specific debuffs: head → blindness/nausea, legs → slowness, arms → weakness | 减益绑定部位：头→失明/反胃，腿→减速，手臂→虚弱 |
| Wound screen (`H`) with heart bars and a 3D player model that follows the cursor | 伤口界面（`H`）：心形血条 + 3D 玩家模型（视线跟随鼠标） |
| HUD modes: player model / hearts / numbers / off | HUD 四种模式：玩家模型 / 心形 / 数字 / 关闭 |
| Absorption is distributed per limb and renders as golden hearts | 伤害吸收按部位分配，显示为金色心形 |
| Death when the head or body is destroyed (configurable) | 头部或身体归零即死亡（可配置） |
| Full Chinese localisation of every config entry | 配置项全量中文化（含分节标题与 tooltip） |
| **New:** switchable texture set (`textureMode`): new art or classic art | **新增**：纹理切换（`textureMode`）：新版贴图 / 旧版贴图 |

## Changes compared to upstream First Aid 1.20.x

- Ported the whole damage pipeline to `Player#actuallyHurt` mixins so knockback and the invulnerability
  window keep working.
- Absorption now lives in the damage model (`getAbsorptionAmount` / `internalSetAbsorptionAmount`).
- Networking migrated to `CustomPacketPayload` + `StreamCodec`; GUI to `LayeredDraw.Layer`; hearts to
  `hud/heart/*` sprites.
- Added custom healing items and custom debuff suppressors, a per-item lore toggle, `maxHealthCap`,
  `hideHealthInCreative`, configurable morphine duration and optional PlayerRevive compatibility.
- Added a **client-side texture switch** with two sets: `NEW` (redrawn art, default) and `OLD`
  (the classic First Aid art). It swaps the bandage / plaster / morphine item icons, the morphine
  effect icon and the player model health texture, live, without restarting the game.

## Requirements

- Minecraft **1.21.1**
- NeoForge
- Java **21**
- Optional: [PlayerRevive](https://www.curseforge.com/minecraft/mc-mods/playerrevive) (soft dependency)

Client and server both need the mod. The `textureMode` option is client-only.

## Installation

1. Install NeoForge for Minecraft 1.21.1.
2. Drop `FirstAid-Reborn-<version>.jar` into `mods/`.
3. Start the game. Configs are generated in `config/firstaid-{server,common,client}.toml`.

## Configuration

Most useful entries:

```toml
# firstaid-server.toml
maxHealthCap = 128                  # per-limb health cap (1 - 1000)
hideHealthInCreative = true         # hide limb health in creative / spectator
morphineDurationSeconds = 30        # morphine debuff suppression duration

# custom healing item: item,hearts,secondsPerHalfHeart,applyTimeTicks,lore
customHealingItems = ["minecraft:golden_apple,4,2.0,40,true"]

# custom debuff suppressor: item,seconds,lore
customDebuffSuppressors = ["minecraft:milk_bucket,60,true"]

# firstaid-client.toml
textureMode = "NEW"                 # NEW = redrawn art, OLD = classic art
overlayMode = "PLAYER_MODEL"        # PLAYER_MODEL / PLAYER_MODEL_4_COLORS / HEARTS / NUMBERS / OFF
overlayPosition = "TOP_LEFT"
visibleDurationTicks = -1           # -1 = always visible
```

Two troubleshooting switches in `firstaid-server.toml`, both **off by default**:

- `forceKnockback` — vanilla only knocks a player back on a fully applied hit; enable this to push the
  player back on every accepted hit as well.
- `debugKnockback` — writes `[FirstAid/KB]` lines for every hit taken, showing whether vanilla knocked
  the player back and what the resulting velocity was.

## Building

Requires JDK 21.

```bat
:: Windows
gradlew.bat build

:: Linux / macOS
./gradlew build
```

Output: `build/libs/FirstAid-Reborn-<version>.jar`. The first build downloads NeoForm and takes a while.

### Optional PlayerRevive integration

PlayerRevive is a soft dependency. Drop `PlayerRevive_NEOFORGE_*.jar` into `libs/` and rebuild;
`build.gradle` compiles the bridge automatically. Without that jar the bridge sources are excluded and
the mod still works, just without the revive compatibility.

## Credits

- **ichttt** — original *First Aid* mod, and the whole per-limb health design.
- **TechnikForLife**, **rozarke** — contributors to the original project.
- Everyone who reported bugs on the original issue tracker.

## License

Same split as the original First Aid:

- **GPL-3.0** for the mod itself — see [LICENSE](LICENSE)
- **LGPL-2.1** for the public API package (`ichttt.mods.firstaid.api`) — see [API_LICENSE](API_LICENSE)

The complete source is published in this repository.
