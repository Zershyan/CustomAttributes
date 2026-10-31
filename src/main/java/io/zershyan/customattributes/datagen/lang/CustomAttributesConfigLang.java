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
    public static final ConfigLangEntry DefaultRange = entry("default_range", "Default Range", "默认可调范围");
    public static final ConfigLangEntry Blacklist = entry("blacklist", "Blacklist", "黑名单属性");
    public static final ConfigLangEntry SyncIntervalTicks = entry("sync_interval_ticks", "Sync Interval (Ticks)", "同步检查间隔（刻）");

    public static final ConfigLangEntry Ranges = entry("ranges", "Range Overrides", "属性范围覆盖");
    public static final ConfigLangEntry Entries = entry("entries", "Entries", "覆盖条目");

    public static final ConfigLangEntry DefaultRangeTooltip = entry("default_range.tooltip",
            "Applied to attributes that have no override entry. Write [lower, upper] and the attribute is editable only while lower is smaller than upper; a reversed order disables it. Values outside the range are pushed back into it by the server. Wildcards: current (final value), base, default, min, max, inf; arithmetic and parentheses are supported, e.g. [min, current] or [current * 0.5, current].",
            "作用于没有单独配置的属性。写成 [下界, 上界]，只有下界小于上界时该属性才可调，反过来（大到小）则直接禁用；超出区间的数值会被服务端夹回区间。通配符：当前值、基础值、默认值、最小值、最大值、无穷，支持四则运算与括号，例如 [min, current] 或 [当前值 * 0.5, 当前值]。");
    public static final ConfigLangEntry EntriesTooltip = entry("entries.tooltip",
            "One entry per line: \"attribute_id=[lower, upper]\". Example: minecraft:generic.movement_speed=[0, current]",
            "每行一条：\"属性ID=[下界, 上界]\"。例如 minecraft:generic.movement_speed=[0, current]");
    public static final ConfigLangEntry BlacklistTooltip = entry("blacklist.tooltip",
            "Listed attributes are neither shown nor editable.",
            "列出的属性既不会显示，也不能修改。");
    public static final ConfigLangEntry SyncIntervalTicksTooltip = entry("sync_interval_ticks.tooltip",
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
