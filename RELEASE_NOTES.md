# First Aid Reborn 1.0.0 — release notes

> Paste the section you need into the CurseForge changelog field / GitHub release body.

## English (CurseForge changelog)

**First Aid Reborn 1.0.0 — Minecraft 1.21.1 / NeoForge**

First release. First Aid ported to 1.21.1, since the original mod stops at 1.20.1.

**Ported**
- Per-limb health: head, body, arms and legs each have their own health.
- Damage is routed by hit direction, projectile trajectory and armor coverage.
- Bandages and splints heal one heart at a time; getting hit interrupts treatment.
- Limb debuffs: head → blindness/nausea, legs → slowness, arms → weakness.
- Wound screen (`H`) with per-part hearts and a 3D player model.
- HUD in four modes: player model, hearts, numbers, off.
- Absorption distributed per limb and drawn as golden hearts.
- Head or body at zero is fatal.

**New in this version**
- **Texture switch** — client option `textureMode`, `NEW` (redrawn, art by enderdream) or `OLD` (classic art by rozarke). Swaps the bandage, plaster and morphine icons, the morphine effect icon and the health overlay, live, no restart.
- Config fully localised, including Chinese.
- Custom healing items and custom debuff suppressors, with a per-item tooltip toggle.
- `maxHealthCap` (1–1000), `hideHealthInCreative`, configurable morphine duration.
- Optional PlayerRevive integration.

**Fixed**
- No knockback on hit (the damage model replaced `actuallyHurt` without setting `hurtMarked`, so clients never received the motion).
- Absorption not syncing to the client.
- Morphine effect icon showing as a missing texture.
- Crash when inspecting `BODY`-slot items in creative; tutorial screen not closing; invisible GUI text; blur leaking into the wound screen; startup crash when reading config too early.

**Notes**
- Requires Minecraft 1.21.1, NeoForge, Java 21. Install on client and server; `textureMode` is client-only.
- Unofficial continuation of ichttt's First Aid. Do not report bugs to the original author.
- GPL-3.0 for the mod, LGPL-2.1 for the API package — same as the original.

## 中文（CurseForge 更新日志）

**医疗护理 Reborn 1.0.0 —— Minecraft 1.21.1 / NeoForge**

首个版本。原作只更新到 1.20.1，本作把它移植到 1.21.1。

**移植内容**
- 分部位生命：头、身体、双臂、双腿各有独立血量。
- 伤害按命中方向、弹射物轨迹、护甲覆盖分配。
- 绷带与石膏逐半颗心治疗，治疗途中被攻击会中断。
- 部位减益：头 → 失明/反胃，腿 → 减速，手臂 → 虚弱。
- 伤口界面（`H`）：每个部位的心形血条 + 3D 玩家模型。
- HUD 四种模式：玩家模型 / 心形 / 数字 / 关闭。
- 伤害吸收按部位分配，显示为金色心形。
- 头部或身体归零即死亡。

**本版新增**
- **纹理切换** —— 客户端选项 `textureMode`：`NEW`（enderdream 重绘）或 `OLD`（rozarke 原作贴图）。覆盖绷带、石膏、吗啡图标、吗啡效果图标与血量覆盖层，即时生效无需重启。
- 配置全量中文化。
- 自定义治疗物品、自定义减益抑制物品，每项可单独关闭说明文字。
- `maxHealthCap`（1~1000）、`hideHealthInCreative`、吗啡抑制时长可配。
- 可选 PlayerRevive 兼容。

**修复**
- 受击没有击退（伤害模型替换了 `actuallyHurt` 却未设置 `hurtMarked`，客户端收不到动量包）。
- 伤害吸收不同步到客户端。
- 吗啡效果图标显示为缺失贴图。
- 创造模式查看 `BODY` 槽位物品崩溃；教程界面无法关闭；界面文字不可见；模糊泄漏到伤口界面；过早读取配置导致启动崩溃。

**说明**
- 需要 Minecraft 1.21.1、NeoForge、Java 21；客户端与服务端都要装，`textureMode` 为客户端选项。
- ichttt 的 First Aid 的非官方延续版本，**请勿在原作者处反馈 bug**。
- 模组本体 GPL-3.0，API 包 LGPL-2.1 —— 与原作一致。

## Short version (GitHub release body)

```
First Aid ported to Minecraft 1.21.1 (NeoForge). Per-limb health, limb debuffs, bandages and splints,
wound screen, four HUD modes — plus a new switchable texture set (NEW / OLD) and a fully localised
config.

Requires MC 1.21.1 / NeoForge / Java 21.
Unofficial continuation of ichttt's First Aid. GPL-3.0 (mod) + LGPL-2.1 (API).
```
