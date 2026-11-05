Custom Attributes lets you view and tune **your own** attributes in game, while the editable range is decided by the server through a range expression.

Typical use: you stacked a pile of buffs and now you are too fast to steer or jump too high for parkour — instead of tearing gear off, just lower the value inside the allowed range.

## Features

- Open the screen with the `K` key (rebindable) or the `/customattributes` command.
- One row per attribute: name, slider, exact value field, reset.
- Drag the slider for quick changes, release to submit; type a value and press Enter to submit; Esc discards the edit.
- **Reset** restores the base value of that attribute — equipment and potion modifiers stay untouched.
- Only attributes the server allows are listed; blacklisted ones and invalid ranges never show up.

## Server configuration

`config/customattributes-server.toml` (instance level, not per world)

- `list_mode`: `WHITELIST` (default) or `BLACKLIST`, with 20 common attributes shipped in the whitelist.
- `default_range`: the range applied to attributes without their own entry, default `[min, current]`.
- `[ranges] entries`: one line per attribute, e.g. `minecraft:generic.movement_speed=[0, current]`.
- `sync_interval_ticks`: how often out-of-range values are clamped back.

## Range expressions

Format is `[lower, upper]`; both sides are expressions supporting `+ - * /`, parentheses and unary minus. Wildcards (case-insensitive): `current`, `base`, `default`, `min`, `max`, `inf` — Chinese aliases are accepted too.

The lower bound must evaluate to no more than the upper bound, otherwise the attribute is disabled instead of being swapped automatically. That rule is what stops players from erasing their own debuffs. A collapsed range (lower equals upper) keeps the row on screen with only **Reset** usable.

## Behaviour

- What you set is a locked final value; the server keeps it there no matter what gear you put on later.
- `current` is the final value *without* this mod's own adjustment, so it never shrinks as you tune down.
- Adjustments use a dedicated modifier and are stored in player data (they survive death); base values are never touched.
- Values outside the range are clamped back on every sync tick, even with the screen closed.
- Server side or single player only; nothing is sent to other clients.

---

在游戏内查看并调整**自己的**属性，可调区间由服务端用范围表达式规定。

典型用途：身上叠了一堆增益，速度太快晕得难受、跳跃太高影响跑酷——不用拆装备，直接在区间内把数值调低。

## 功能

- 默认按键 `K` 打开界面（可在「控制 → 按键绑定」里改），也可以输入 `/customattributes`。
- 每一行是一个属性：名称、滑块、数值输入框、重置。
- 拖滑块快速调整，松手即提交；输入框里填精确值后回车提交；按 Esc 放弃这次编辑。
- **重置**把该属性的基础值恢复为默认值，装备与药水的修饰不受影响。
- 界面里只会出现服务端允许调整的属性，黑名单与无效区间不会显示。

## 服务端配置

路径 `config/customattributes-server.toml`（实例级，不随存档变化）

- `list_mode`：`WHITELIST`（默认）或 `BLACKLIST`，白名单预置了 20 个常见属性。
- `default_range`：未单独配置的属性用它，默认 `[min, current]`。
- `[ranges] entries`：每个属性一行，例如 `minecraft:generic.movement_speed=[0, current]`。
- `sync_interval_ticks`：越界数值被夹回区间的检查间隔。

## 范围表达式

格式固定为 `[下界, 上界]`，两端都是表达式，支持 `+ - * /`、括号与负号。通配符（大小写不敏感）：`current`、`base`、`default`、`min`、`max`、`inf`，也可以用中文写「当前值 / 基础值 / 默认值 / 最小值 / 最大值 / 无穷」。

下界求值后必须不大于上界，否则该属性直接禁用而不是自动交换两端——这条规则用来防止玩家把自身的减益抹掉。区间塌缩成一个点时，该行仍保留「重置」。

## 行为约定

- 玩家设定的是「最终值等于多少」，服务端会持续维持这个数，之后穿装备、喝药水都不会把它带走。
- `current` 取的是「没被本模组调整过」的最终值，所以往下调不会让它越缩越小。
- 调整通过本模组专属 modifier 表达，目标值记在玩家存档里（死亡重生后保留），基础值始终原样。
- 越界的数每个同步间隔都会被夹回区间，未打开界面也一样生效。
- 只发给本人，属性数值不会同步给其他客户端。
