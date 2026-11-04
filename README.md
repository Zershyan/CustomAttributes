# Custom Attributes

Forge 1.20.1 模组：在游戏内查看并调整**自己的**属性，可调区间由服务端用范围表达式规定。

典型用途：身上叠了一堆增益，速度太快晕得难受、跳跃太高影响跑酷——不用拆装备，直接在区间内把数值调低。

## 环境

- Minecraft 1.20.1
- Forge 47.3.4+
- Java 17
- **必须同时安装 [forgeattachment](https://github.com/Zershyan/forgeattachment)**（本模组用它保存玩家自己的调整记录，未发布到 MC百科 / CurseForge）

## 打开界面

- 默认按键 `K`（可在「控制 → 按键绑定」里改）
- 或输入命令 `/customattributes`

打开后每一行是一个属性：名称、滑块、数值输入框、重置。

- 拖滑块快速调整，松手即提交
- 输入框里填精确值后回车提交；按 Esc 或点别处会放弃这次编辑
- 「重置」把该属性的**基础值**恢复为默认值，装备与药水的修饰不受影响

界面里只会出现服务端允许调整的属性，其余属性（黑名单、区间无效）不会显示。

## 服务端配置

路径：`<存档>/serverconfig/customattributes-server.toml`（每个世界一份）

```toml
[general]
	enabled = true                     # 总开关
	list_mode = "WHITELIST"            # WHITELIST：只有名单内的可调；BLACKLIST：只有名单内的不可调
	whitelist = [                      # 白名单模式下放行的属性ID，默认预置以下几项
		"minecraft:generic.movement_speed",
		"minecraft:generic.jump_strength",
		"minecraft:generic.step_height",
		"minecraft:generic.safe_fall_distance",
		"minecraft:player.sneaking_speed",
		"minecraft:generic.movement_efficiency",
		"minecraft:generic.water_movement_efficiency",
		"minecraft:generic.attack_damage",
		"minecraft:generic.attack_speed",
		"minecraft:generic.attack_knockback",
		"minecraft:player.sweeping_damage_ratio",
		"minecraft:generic.armor",
		"minecraft:generic.armor_toughness",
		"minecraft:generic.max_health",
		"minecraft:player.block_interaction_range",
		"minecraft:player.entity_interaction_range",
		"minecraft:player.block_break_speed",
		"minecraft:player.submerged_mining_speed",
		"minecraft:player.mining_efficiency"
	]
	blacklist = []                     # 黑名单模式下屏蔽的属性ID；白名单模式下忽略本项
	default_range = "[min, current]"   # 未单独配置的属性用它；留空则该属性不显示
	sync_interval_ticks = 5            # 夹取越界数值并推送变化的间隔（刻）

[ranges]
	entries = [
		"minecraft:generic.movement_speed=[0, current]",
		"minecraft:generic.jump_strength=[0.5, current]"
	]
```

筛选顺序：先按 `list_mode` 决定哪个属性允许出现在界面上 → 再用 `ranges.entries` 或 `default_range` 算出它的可调区间。写错的行会打日志并忽略，不影响其它行。

**默认是白名单模式**，并且预置了 19 个常见属性（移动与手感 7 个、战斗 7 个、交互与采集 5 个）——它们的共同点是"调低只会更难受，不会让人卡死"。想放开别的属性，把 ID 加进 `whitelist`；想先看有哪些 ID，可以临时把 `list_mode` 改成 `"BLACKLIST"` 打开界面，鼠标悬停属性名会显示 ID，抄完再改回 `"WHITELIST"`。把 `whitelist` 清空则等于全部不放行。

## 范围表达式

格式固定为 `[下界, 上界]`，两端都是表达式，支持 `+ - * /`、括号与负号。

| 通配符 | 含义 |
|---|---|
| `current` / `当前值` | 属性当前最终值 |
| `base` / `基础值` | 属性当前基础值 |
| `default` / `默认值` | 该玩家类型的默认基础值（全新玩家的值） |
| `min` / `最小值` | 属性硬下限 |
| `max` / `最大值` | 属性硬上限 |
| `inf` / `无穷` | 无穷，求值后仍会被属性自身的上下限夹住 |

大小写不敏感，中英文可混用。示例：

```
[0, current]              允许往下调到 0，不能超过当前值
[min, current]            同上，下界直接取属性下限（默认配置）
[default, current]        只能在不低于默认值的前提下往下调
[0, 100]                  固定区间
[当前值 * 0.5, 当前值]     只能调到当前值的一半以上
[min, current - 1]        上界比当前值低 1
```

### 方向必须由小到大

`[下界, 上界]` 求值后必须满足 `下界 <= 上界`，否则该属性直接禁用（不显示、也拒绝修改）。**不会自动交换两端。**

这条规则用来防止"把减益抹掉"：例如某玩家身上有速度减益，`current` 低于 `default`，此时 `[default, current]` 就是"大到小"，属性被禁用，玩家没法借这个界面把自己的减益调没。想要"越低收益越高"的属性，写成 `[current, default]` 即可。

若两端相等（区间塌缩成一个点），该行只保留「重置」，滑块与输入框不可用——这样把值调到下限之后仍然能靠重置回来。

## 行为约定

- **调过就是锁定值**：玩家设定的是"最终值等于多少"，服务端会持续维持这个数。之后穿装备、喝药水、换饰品，哪怕中间夹着乘法区，最终值仍然是玩家设的那个数；撤掉装备也不会把它带走。
- **`current` 不受你自己调整的影响**：它取的是"没被本模组调整过"的最终值。属性原本 5、配置 `[0, current]`，可调范围就一直是 0~5，你调到 3 之后仍然能调回 5。装备、药水等外部 modifier 变化会照常反映到这个数上。
- **调整不写基础值**：玩家设定通过本模组专属的 modifier 表达，目标值记在玩家存档里（死亡重生后保留）。基础值始终是原样的，重置就是把目标和这条 modifier 一起撤掉。
- **越界夹回**：服务端每隔 `sync_interval_ticks` 检查一次所有在线玩家，最终值超出区间的会被夹回边界；装备移除后区间恢复，值也会自己回到区间内。未打开界面也一样生效。
- **无法满足的配置会被停用**：如果无论怎么调都进不了区间（数值被其它 modifier 钉死，例如乘法系数为 0），该属性会停止限制、从界面移除并打印一条警告，改动配置后自动重新尝试。
- **名单变化会清理残留**：属性被移出白名单（或加进黑名单）后，它上面记录的玩家目标和 modifier 会被一起清掉，不会留下一个看不见的锁定值。
- **重置只撤掉本模组的调整**：装备、药水等外部 modifier 一律保留，所以重置不会移除真正的减益。
- **只发给本人**：属性数值只有自己能看到。

## 构建

```bash
./gradlew build        # 产出 build/libs/customattributes-<版本>-<MC版本>.jar
./gradlew runData      # 重新生成语言文件与 pack.mcmeta
./gradlew runClient    # 开发客户端
./gradlew runServer    # 开发服务端（需先接受 run/eula.txt）
```

## 许可

MIT，见 [LICENSE](LICENSE)。
