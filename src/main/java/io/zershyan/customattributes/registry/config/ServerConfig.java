package io.zershyan.customattributes.registry.config;

import io.zershyan.customattributes.datagen.lang.CustomAttributesConfigLang;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/** 服务端权威配置：哪些属性可调、可调区间是什么。 */
public final class ServerConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();
    public static final ModConfigSpec SPEC;

    public static final ModConfigSpec.BooleanValue ENABLED;
    public static final ModConfigSpec.ConfigValue<String> DEFAULT_RANGE;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> BLACKLIST;
    public static final ModConfigSpec.IntValue SYNC_INTERVAL_TICKS;
    public static final ModConfigSpec.ConfigValue<List<? extends String>> RANGE_OVERRIDES;

    static {
        BUILDER.translation(CustomAttributesConfigLang.General.key()).comment("通用设置").push("general");
        ENABLED = BUILDER.translation(CustomAttributesConfigLang.Enabled.key())
                .comment("是否启用本模组的功能")
                .define("enabled", true);
        DEFAULT_RANGE = BUILDER.translation(CustomAttributesConfigLang.DefaultRange.key())
                .comment("没有单独配置的属性使用这个范围；留空表示这些属性不出现在界面里")
                .define("default_range", "[min, current]");
        BLACKLIST = BUILDER.translation(CustomAttributesConfigLang.Blacklist.key())
                .comment("完全不显示、也不可修改的属性ID")
                .defineListAllowEmpty("blacklist", List.<String>of(), () -> "", o -> o instanceof String);
        SYNC_INTERVAL_TICKS = BUILDER.translation(CustomAttributesConfigLang.SyncIntervalTicks.key())
                .comment("属性检查间隔（刻）：把越界的数值夹回区间，并把数值/区间的变化推送给打开界面的玩家")
                .defineInRange("sync_interval_ticks", 1, 1, 20);
        BUILDER.pop();

        BUILDER.translation(CustomAttributesConfigLang.Ranges.key()).comment("逐个属性覆盖范围").push("ranges");
        RANGE_OVERRIDES = BUILDER.translation(CustomAttributesConfigLang.Entries.key())
                .comment("每行一条，格式：属性ID=[下界, 上界]")
                .defineListAllowEmpty("entries", List.<String>of(), () -> "", o -> o instanceof String);
        BUILDER.pop();

        SPEC = BUILDER.build();
    }

    private ServerConfig() {
    }

    public static boolean isEnabled() {
        return SPEC.isLoaded() && ENABLED.get();
    }

    public static int syncInterval() {
        return SPEC.isLoaded() ? SYNC_INTERVAL_TICKS.get() : 1;
    }

    public static String defaultRange() {
        return SPEC.isLoaded() ? DEFAULT_RANGE.get() : "";
    }

    public static List<? extends String> blacklist() {
        return SPEC.isLoaded() ? BLACKLIST.get() : List.of();
    }

    public static List<? extends String> rangeOverrides() {
        return SPEC.isLoaded() ? RANGE_OVERRIDES.get() : List.of();
    }
}
