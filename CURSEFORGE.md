# CurseForge listing copy

## Short summary (English)

> First Aid for Minecraft 1.21.1 NeoForge — damage split across head, body, arms and legs. Unofficial update of ichttt's First Aid, with redrawn textures you can swap back to the classic ones.

## Short summary (中文)

> 医疗护理 1.21.1 NeoForge 非官方更新版：伤害按头、身、双臂、双腿分部位计算，附带可切换的全新贴图。

---

## Full description (English)

# First Aid Reborn

An **unofficial** update of [ichttt](https://github.com/ichttt)'s First Aid for Minecraft **1.21.1 (NeoForge)**.
All credit for the original design goes to the original author.

Health is tracked per body part — head, body, arms, legs. Each hit lands somewhere specific; wounds
need bandages and splints, a broken leg slows you down, a wounded head blurs your vision, and
destroying the head or body is fatal.

### New in this version

- **Redrawn textures**, plus a client option `textureMode` to switch between the new art and the
  original art at any time — bandage, plaster, morphine icons, the morphine effect icon and the health
  overlay. No restart needed.
- **Custom healing items** — turn any item into a treatment item.
- **Custom debuff suppressors** — morphine-like items with a configurable duration.
- `maxHealthCap`, `hideHealthInCreative`, configurable morphine duration.
- Damage handling moved to `Player#actuallyHurt` mixins, so knockback and the invulnerability window
  keep working.
- Optional PlayerRevive integration.

### Please note

- **Do not report bugs to the original author.** Feedback and issues go to this project's tracker only.
- Released under **the same licenses as the original**: GPL-3.0 for the mod, LGPL-2.1 for the API
  package. The complete source is published, and the original copyright headers are kept.

### Credits

**ichttt** (original First Aid), **TechnikForLife**, **rozarke**.

---

## 完整介绍（中文）

# 医疗护理 Reborn（First Aid Reborn）

**ichttt** 的 First Aid 在 Minecraft **1.21.1（NeoForge）** 上的**非官方**更新版。原作设计与代码归原作者所有。

血量按部位计算——头、身体、双臂、双腿。每一击都落在具体部位：伤口需要绷带和石膏，断腿会减速，
头部受伤会视野模糊，头部或身体归零即死亡。

### 本版新增

- **重绘贴图**，并新增客户端选项 `textureMode`，可随时在新版贴图与原版贴图之间切换——绷带、石膏、
  吗啡图标、吗啡效果图标、血量覆盖层全部支持，**无需重启**
- **自定义治疗物品**：把任意物品配成治疗用品
- **自定义减益抑制物品**：类吗啡物品，抑制时长可配
- `maxHealthCap`、`hideHealthInCreative`、吗啡抑制时长可配
- 伤害处理改到 `Player#actuallyHurt` Mixin，击退与无敌帧保持原版行为
- 可选 PlayerRevive 兼容

### 请注意

- **本模组出现 bug 请勿在原作者处反馈**，请反馈到本项目的 issue 页面
- 以**与原作相同的许可证**发布：模组本体 GPL-3.0，API 包 LGPL-2.1；完整源码已公开，保留原有版权声明

### 致谢

**ichttt**（原作 First Aid）、**TechnikForLife**、**rozarke**
