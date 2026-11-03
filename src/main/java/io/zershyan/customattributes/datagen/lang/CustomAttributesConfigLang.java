package io.zershyan.customattributes.datagen.lang;

import io.zershyan.customattributes.datagen.util.ConfigLangEntry;

import java.util.ArrayList;
import java.util.List;

/** 配置分类与配置项名称，{@code name} 必须与 {@code define(...)} 的键完全一致。 */
public class CustomAttributesConfigLang extends CustomAttributesLang {
    private static final List<FinalEntry<String>> TranslatableLang = new ArrayList<>();

    public static final ConfigLangEntry Title = entry("title", "Custom Attributes", "自定义属性");

    public static final ConfigLangEntry General = entry("general", "General", "通用");
    public static final ConfigLangEntry Enabled = entry("enabled", "Enabled", "启用本模组");
    public static final ConfigLangEntry ListMode = entry("list_mode", "List Mode", "名单模式");
    public static final ConfigLangEntry Whitelist = entry("whitelist", "Whitelist", "白名单属性");
    public static final ConfigLangEntry Blacklist = entry("blacklist", "Blacklist", "黑名单属性");
    public static final ConfigLangEntry DefaultRange = entry("default_range", "Default Range", "默认可调范围");
    public static final ConfigLangEntry SyncIntervalTicks = entry("sync_interval_ticks", "Sync Interval (Ticks)", "同步检查间隔（刻）");

    public static final ConfigLangEntry Ranges = entry("ranges", "Range Overrides", "属性范围覆盖");
    public static final ConfigLangEntry Entries = entry("entries", "Entries", "覆盖条目");

    public static final ConfigLangEntry ListModeTooltip = entry(ListMode.tooltipName(),
            "WHITELIST: only the attributes in the whitelist are shown and editable. BLACKLIST: the attributes in the blacklist are hidden and locked, everything else follows the range config.",
            "WHITELIST：只有白名单里的属性显示且可调，其余一律不可调。BLACKLIST：黑名单里的属性不显示也不可调，其余按范围配置处理。");
    public static final ConfigLangEntry WhitelistTooltip = entry(Whitelist.tooltipName(),
            "Attribute ids that are allowed while List Mode is WHITELIST. A few common ones ship by default; clear the list to allow nothing.",
            "名单模式为 WHITELIST 时放行的属性ID。默认预置了几个常见属性，清空列表则什么都不放开。");
    public static final ConfigLangEntry BlacklistTooltip = entry(Blacklist.tooltipName(),
            "Attribute ids that are hidden and locked while List Mode is BLACKLIST. Ignored in WHITELIST mode.",
            "名单模式为 BLACKLIST 时不显示且不可调的属性ID；WHITELIST 模式下本项被忽略。");
    public static final ConfigLangEntry DefaultRangeTooltip = entry(DefaultRange.tooltipName(),
            "Applied to attributes that have no override entry. Write [lower, upper] and the attribute is editable only while lower is smaller than upper; a reversed order disables it. Values outside the range are pushed back into it by the server. Wildcards: current (final value), base, default, min, max, inf; arithmetic and parentheses are supported, e.g. [min, current] or [current * 0.5, current].",
            "作用于没有单独配置的属性。写成 [下界, 上界]，只有下界小于上界时该属性才可调，反过来（大到小）则直接禁用；超出区间的数值会被服务端夹回区间。通配符：当前值、基础值、默认值、最小值、最大值、无穷，支持四则运算与括号，例如 [min, current] 或 [当前值 * 0.5, 当前值]。");
    public static final ConfigLangEntry EntriesTooltip = entry(Entries.tooltipName(),
            "One entry per line: \"attribute_id=[lower, upper]\". Example: minecraft:generic.movement_speed=[0, current]",
            "每行一条：\"属性ID=[下界, 上界]\"。例如 minecraft:generic.movement_speed=[0, current]");
    public static final ConfigLangEntry SyncIntervalTicksTooltip = entry(SyncIntervalTicks.tooltipName(),
            "How often the server clamps out-of-range values back into their range and pushes value/range changes to players who keep the screen open.",
            "服务端把越界数值夹回区间、并把数值与区间变化推送给打开界面的玩家的间隔。");

    private static ConfigLangEntry entry(String name, String enUs, String zhCn) {
        ConfigLangEntry langEntry = new ConfigLangEntry(name);
        TranslatableLang.add(new FinalEntry<>(langEntry.key(), enUs, zhCn));
        return langEntry;
    }

    @Override
    protected List<Entry> init(List<Entry> entries) {
        entries.addAll(TranslatableLang);
        return entries;
    }
}
