# Changelog

## 1.0.0

首个公开发布版本，Minecraft 1.21.1 / NeoForge。

### 核心

- 从原作 First Aid 1.20.x 移植分部位生命系统到 1.21.1
- 伤害分配、护甲减免、部位减益、治疗物品、伤口界面、HUD、教程
- 伤害吸收按部位分配，金色心形正常显示
- 头部 / 身体致命判定

### 1.21.1 适配要点

- 伤害处理改到 `Player#actuallyHurt` Mixin，保证击退与无敌帧正常
- `entityData` 字段替换改为 `setHealth` / `getAbsorptionAmount` Mixin
- 网络层迁移到 `CustomPacketPayload` + `StreamCodec`
- GUI 迁移到 `LayeredDraw.Layer`，心形改用 `hud/heart/*` 精灵
- 注册迁移到 `DeferredHolder` / `AttachmentType` / `ModConfigSpec`

### 新增

- **纹理切换**（客户端配置 `textureMode`，默认 `NEW`）：
  - `NEW`：重绘的新版贴图（美术：enderdream）；`OLD`：First Aid 经典贴图（rozarke）
  - 覆盖绷带 / 石膏 / 吗啡三个物品图标、吗啡的药水效果图标、玩家模型血量贴图
  - 切换立即生效，无需重启游戏
- 配置全量中文化（含分节标题与 tooltip）
- 自定义治疗物品（`customHealingItems`）与自定义减益抑制物品（`customDebuffSuppressors`）
- 每个物品独立的 lore 开关（绷带 / 石膏 / 吗啡 / 每个自定义条目）
- `maxHealthCap`：部位血量上限可配（默认 128，范围 1~1000）
- `hideHealthInCreative`：创造 / 旁观者模式隐藏部位血量（默认开启）
- `morphineDurationSeconds`：吗啡抑制时长可配
- PlayerRevive 兼容（软依赖）
- 食物自然回血与生命恢复效果按部位结算
- 伤口界面玩家模型视线跟随鼠标

### 调整

- `forceKnockback` 默认关闭
- `debugKnockback` 默认关闭

### 修复

- 吗啡的药水效果图标显示为黑紫棋盘格（图集精灵名错误：应为 `firstaid:morphine_new`，
  而非 `firstaid:mob_effect/morphine_new`）
- 击退判定把「被取消」或「强度为 0」的 `LivingKnockBackEvent` 误判为已击退，导致兜底补推被跳过
- 取消 `actuallyHurt` 后未设置 `hurtMarked`，客户端收不到动量包，受击没有击退感
- 兜底击退改用直接施加冲量，绕过可能被其他模组取消的 `LivingKnockBackEvent`
- 受到攻击没有击退、原地连续掉血
- 创造模式查看狼甲等 `BODY` 槽位物品时崩溃
- 教程界面无法关闭、完成后反复弹出
- 界面文字因 ARGB alpha 为 0 不可见
- 伤口界面背景与模型被误加模糊
- 模型半透明（渲染状态泄漏）
- 玩家数据读取时发包导致「无效的玩家数据」/ 卡加载（`player.connection` 为空）
- 启动崩溃：初始化阶段读取配置项
- HUD 每帧尝试加载已删除的 `icons.png`
- 教程文字对比度过低
- 伤口界面 / 教程背景模糊，模型皮肤采样为线性过滤
