# CurseForge listing copy

## Short summary (English, ≤ 300 chars)

> First Aid for Minecraft 1.21.1 (NeoForge). Damage is split across head, body, arms and legs; wounds need bandages and splints, broken legs slow you down, and a destroyed head or body is fatal. Now with switchable new/classic textures.

## Short summary (中文简介, ≤ 150 字)

> 医疗护理 1.21.1 NeoForge 版。伤害按头、身、双臂、双腿分部位计算，伤口需要绷带和石膏治疗，断腿会减速，头部或身体归零即死亡。新增新版/经典双套贴图可切换。

---

## Full description (English)

# First Aid Reborn

**Per-limb health for Minecraft 1.21.1 (NeoForge).**

First Aid replaces the single health bar with a body: head, body, two arms, two legs. Every hit lands
somewhere specific, that part bleeds, and you treat it separately. It is the mod that turns "I have
7 hearts" into "my left leg is broken, my head is at half, and the zombie is still coming".

This is a community port of [ichttt](https://github.com/ichttt)'s First Aid.

### What it does

**Damage per body part.** Head, body, arms and legs each have their own health. The part that takes
damage depends on where the attack came from, how a projectile flew, and what your armor covers. A
helmet protects the head, boots protect the legs — and nothing protects what is not covered.

**Limb-specific consequences.** A wounded head blurs your vision (blindness, nausea). A wounded leg
slows you down. A wounded arm weakens your attacks. The debuff follows the injury, so treating the
part also removes the penalty.

**Treatment takes time.** Bandages stop bleeding and restore a heart at a time. Splints fix broken
limbs. Both are interrupted if you get hit while applying them, so healing in combat is a real
decision.

**Death rules.** Destroying the head or the body kills you (configurable). Losing a limb does not.

**Wound screen.** Press `H` for a full-body view: heart bars per part, plus a 3D player model that
follows your cursor so you can inspect a specific limb.

**HUD.** Four display modes — player model, hearts, numbers, or hidden — with adjustable position,
offset, transparency and display duration.

**Absorption.** Golden hearts are distributed across body parts instead of floating above one bar.

### Ported and improved

Everything was rebuilt for 1.21.1's systems rather than patched in place:

- Damage handling moved into `Player#actuallyHurt` mixins, so **knockback and the invulnerability
  window keep working**.
- Absorption lives in the damage model again.
- Networking uses `CustomPacketPayload` + `StreamCodec`; the GUI uses `LayeredDraw.Layer`; hearts are
  drawn from the `hud/heart/*` sprite set.
- **Switchable textures**: the client option `textureMode` picks between the redrawn `NEW` art and the
  classic `OLD` art. It swaps the bandage, plaster and morphine icons, the morphine effect icon and
  the player-model health texture — live, no restart.

On top of the port:

- Fully localised config (including Chinese) with section headers and tooltips.
- **Custom healing items** — turn any item from any mod into a treatment item, with hearts restored,
  seconds per half heart, apply time and a per-item tooltip toggle.
- **Custom debuff suppressors** — morphine-like items with a configurable suppression duration.
- `maxHealthCap` (per-limb cap, 1–1000), `hideHealthInCreative`, configurable morphine duration.
- Optional [PlayerRevive](https://www.curseforge.com/minecraft/mc-mods/playerrevive) integration.

### Requirements

- Minecraft **1.21.1**
- NeoForge
- Java **21**
- Install on **both** client and server. `textureMode` is client-only.

### Compatibility notes

- Works with vanilla damage sources and with modded weapons: everything is routed through the
  standard damage pipeline instead of intercepting individual damage types.
- Optional PlayerRevive support is included in the released jar.
- The two knockback troubleshooting options (`forceKnockback`, `debugKnockback`) are **off by default**.

### Credits

- **ichttt** — original First Aid, and the entire per-limb health design.
- **TechnikForLife**, **rozarke** — original project contributors.

### License

**GPL-3.0 — the same license as the original First Aid.** The complete source is published in the
project repository.

---

## 完整介绍（中文正文）

# 医疗护理 Reborn（First Aid Reborn）

**Minecraft 1.21.1 / NeoForge 的分部位生命系统。**

原版把血量做成一根条，医疗护理把它做成一个身体：头、身体、两条手臂、两条腿。每一击都会落在具体
部位上，那个部位会受伤、会流血，需要单独处理。它把「我还有 7 颗心」变成「我左腿断了、头只剩一半、
僵尸还在过来」。

本作是 [ichttt](https://github.com/ichttt) 的 First Aid 的社区移植版。

### 它做了什么

**分部位伤害。** 头、身、双臂、双腿各有独立血量。伤害落在哪个部位，取决于攻击来的方向、弹射物的
飞行轨迹、以及护甲覆盖范围。头盔保护头，靴子保护腿，没盖住的地方就没人保护。

**部位专属后果。** 头部受伤会视野模糊（失明、反胃），腿部受伤会减速，手臂受伤会削弱攻击。减益跟着
伤情走，治好了伤，惩罚也一起消失。

**治疗需要时间。** 绷带止血并逐半颗心回复，石膏固定断肢。**治疗途中被攻击会中断**，所以战斗中治疗
是个真实的选择。

**死亡规则。** 头部或身体归零即死亡（可配置），断手断腿不会死。

**伤口界面。** 按 `H` 打开全身视图：每个部位的心形血条，加上一个可随鼠标转动的 3D 玩家模型，方便查看
某个具体部位。

**HUD。** 四种显示模式——玩家模型 / 心形 / 数字 / 关闭——位置、偏移、透明度、显示时长均可调。

**伤害吸收。** 金色心形按部位分配，而不是浮在血量条上方。

### 移植做了什么

整套逻辑按 1.21.1 的体系重写，而不是打补丁：

- 伤害处理改为 `Player#actuallyHurt` Mixin，**击退与无敌帧保持原版行为**
- 伤害吸收重新进入伤害模型
- 网络改 `CustomPacketPayload` + `StreamCodec`；GUI 改 `LayeredDraw.Layer`；心形改用 `hud/heart/*` 精灵
- **纹理切换**：客户端选项 `textureMode` 可在「新版贴图」与「经典贴图」之间切换，覆盖绷带、石膏、
  吗啡三个物品图标、吗啡效果图标与玩家模型血量贴图，**即时生效，无需重启**

移植之外还加上了：

- 配置全量本地化（含中文），带分节标题与悬停说明
- **自定义治疗物品**：把任意模组的任意物品配成治疗用品，可设回复心数、每半颗心所需秒数、举起耗时、
  是否显示说明
- **自定义减益抑制物品**：类吗啡物品，抑制时长可配
- `maxHealthCap`（部位血量上限 1~1000）、`hideHealthInCreative`、吗啡抑制时长可配
- 可选的 [PlayerRevive](https://www.curseforge.com/minecraft/mc-mods/playerrevive) 兼容

### 前置要求

- Minecraft **1.21.1**
- NeoForge
- Java **21**
- **客户端与服务端都要装**；`textureMode` 为客户端选项

### 兼容说明

- 兼容原版伤害来源与各类模组武器：整条链路走原版伤害管线，而不是逐类型拦截
- PlayerRevive 兼容已包含在发布版中
- 两个击退排查选项（`forceKnockback`、`debugKnockback`）**默认关闭**

### 致谢

- **ichttt** —— 原作 First Aid 与整套分部位生命设计
- **TechnikForLife**、**rozarke** —— 原作贡献者

### 许可

**GPL-3.0 —— 与原作 First Aid 完全相同的许可证**，完整源码已在项目仓库公开。
